# Troubleshooting: edc-federated-catalog

## Overview

This guide covers the most common issues encountered when running the `edc-federated-catalog` federator, organized by symptom. Check the federator logs first — most issues produce a descriptive log message.

### Viewing Logs

```bash
# Kubernetes
kubectl logs -n ds4h deployment/edc-federator --follow

# Docker (local)
docker logs -f <container-id>

# Increase verbosity
# Set LOG_LEVEL=debug in the container environment
```

---

## Startup Issues

### Federator fails to start — `Connection refused` to PostgreSQL

**Symptom**: The container exits immediately with a JDBC connection error in the logs.

**Cause**: PostgreSQL is not reachable at the configured `edc.datasource.default.url`.

**Resolution**:
1. Verify PostgreSQL is running: `kubectl get pods -n ds4h | grep postgresql`
2. Verify the JDBC URL: `jdbc:postgresql://<service-name>:<port>/<database>`
3. For Helm deployments, the PostgreSQL service name is `catalog-postgresql` by default.
4. Check that the database exists and the configured user has access:
   ```bash
   psql -h <host> -U <user> -d <database> -c "SELECT 1;"
   ```

---

### Federator fails to start — `Vault connection error` or `401 Unauthorized`

**Symptom**: Startup fails with a Vault-related error. Log message contains `vault` or `403`/`401`.

**Cause**: Either Vault is not reachable or the Vault token is invalid/expired.

**Resolution**:
1. Verify the Vault token Kubernetes secret exists:
   ```bash
   kubectl get secret edc-federator-vault -n ds4h
   ```
2. Check the token value:
   ```bash
   kubectl get secret edc-federator-vault -n ds4h -o jsonpath='{.data.token}' | base64 -d
   ```
3. Verify the token is valid against the Vault server:
   ```bash
   vault token lookup <token>
   ```
4. Verify `edc.vault.hashicorp.url` points to the correct Vault address.

---

### Federator fails to start — `ServiceExtension` initialization failure

**Symptom**: Log contains `Failed to initialize extension` with a class name.

**Cause**: A required service (e.g., `ParticipantStore`, `PolicyEngine`) could not be injected, usually due to a missing dependency or misconfigured module.

**Resolution**:
1. Check that all required extensions are registered in the relevant `META-INF/services/org.eclipse.edc.spi.system.ServiceExtension` files.
2. Rebuild the fat JAR: `./gradlew shadowJar --no-daemon`. Stale JARs without service file merging can cause missing extensions.
3. Look for `NoSuchServiceException` or `InjectionFailure` earlier in the logs.

---

### Federator starts but `edc_participant` table is missing

**Symptom**: Startup succeeds but queries to the participant API return `500` with a SQL error about a missing table.

**Cause**: `edc:sql-bootstrapper` did not run the schema, or it ran against a different database.

**Resolution**:
1. Verify the database URL points to the intended database.
2. Run the schema manually:
   ```sql
   CREATE TABLE IF NOT EXISTS edc_participant (
     participant_id       VARCHAR(255) PRIMARY KEY,
     name                 VARCHAR(255) NOT NULL,
     endpoint             VARCHAR(500) NOT NULL,
     supported_protocols  TEXT,
     created_at           BIGINT NOT NULL,
     updated_at           BIGINT NOT NULL
   );
   CREATE INDEX IF NOT EXISTS idx_participant_id ON edc_participant(participant_id);
   ```
3. Check that the database user has `CREATE TABLE` permission.

---

## Catalog Crawling Issues

### Federator crawls no participants — catalog is always empty

**Symptom**: No data is returned from the federated catalog query endpoint. Logs show `Loaded 0 participants from database`.

**Cause**: The `edc_participant` table is empty.

**Resolution**:
1. Verify there are participants in the database:
   ```bash
   curl http://federator.ds4h.example.com/api/catalog/participants/
   ```
2. If empty, seed participants via the REST API or SQL (see [API.md](API.md) and [DEPLOYMENT.md](DEPLOYMENT.md)).
3. Verify the crawler is running by looking for `Loaded N participants from database` in the logs.

---

### Crawling fails for some participants — `Connection refused` or `SSL handshake failed`

**Symptom**: Some participants appear in the registry but their catalogs are not aggregated. Logs show connection errors for specific endpoints.

**Cause 1**: The participant's DSP endpoint is incorrect or unreachable.

**Resolution**:
1. Verify the endpoint is reachable from within the cluster:
   ```bash
   kubectl exec -n ds4h deployment/edc-federator -- curl -v <participant-dsp-endpoint>
   ```
2. Update the participant endpoint via the API:
   ```bash
   curl -X PUT http://federator.ds4h.example.com/api/catalog/participants/<id> \
     -H "Content-Type: application/json" \
     -d '{"id": "<id>", "name": "<name>", "dspUrl": "<corrected-url>", "supportedProtocols": ["dataspace-protocol-http"]}'
   ```

**Cause 2**: TLS certificate validation fails for the participant endpoint.

**Resolution** (non-production only):
- Set `edc.http.disable.tls=true` in the configuration file to bypass TLS verification.
- **Do not use this setting in production.**

---

### Catalog is stale — participants added recently are not crawled yet

**Symptom**: A participant was added to the registry but its catalog data does not appear in federated catalog queries.

**Cause**: The crawler runs on a schedule; newly added participants are only included on the next crawl cycle.

**Resolution**:
- Wait for the next crawl cycle. The interval is controlled by `edc.fc.crawler.execution.delay.seconds`.
- Check logs for the next `Loaded N participants from database` message to confirm the new participant was picked up.

---

## Participant Registry API Issues

### `POST /participants/` returns `409 Conflict`

**Symptom**: Attempting to create a participant returns `409 Conflict` with `PARTICIPANT_EXISTS`.

**Cause**: A participant with the same `id` already exists in the registry.

**Resolution**:
1. List existing participants: `GET /participants/`
2. Either use a different `id` for the new participant, or delete the existing entry first: `DELETE /participants/<id>`

---

### `PUT /participants/{id}` returns `400 Bad Request` with `ID_MISMATCH`

**Symptom**: Update request returns `ID_MISMATCH` error.

**Cause**: The `id` field in the JSON request body does not match the `{id}` path parameter.

**Resolution**: Ensure the `id` in the URL path and the `id` field in the JSON body are identical.

---

### `POST /participants/` returns `400 Bad Request` with `VALIDATION_ERROR`

**Symptom**: Create request returns validation errors.

**Cause**: One or more required fields are missing or invalid.

**Resolution**: Ensure the request body includes:
- `id`: non-blank string
- `name`: non-blank string
- `dspUrl`: non-blank, valid URL (must start with `http://` or `https://`)

---

### Participant API returns `500 Internal Server Error`

**Symptom**: Any participant API call returns `500` with a store-level error code (`CREATE_FAILED`, `UPDATE_FAILED`, etc.).

**Cause**: The database is unreachable or the query failed due to a constraint violation.

**Resolution**:
1. Check PostgreSQL connectivity from the federator pod.
2. Check PostgreSQL logs for constraint violations or disk full errors.
3. Verify the database user has SELECT, INSERT, UPDATE, DELETE permissions on `edc_participant`.

---

## Scope / Identity Issues

### Catalog queries to participant connectors fail with `401 Unauthorized`

**Symptom**: The federator crawls participants but receives `401` responses. Logs may mention token validation failure.

**Cause**: The federator's Verifiable Presentation (VP) token is missing the required `MembershipCredential` scope, or the token is expired/invalid.

**Resolution**:
1. Verify `edc.policy.catalog.scope` is set to `org.eclipse.edc.vc.type:MembershipCredential:read` (this is the default, but check your configuration file).
2. Verify the IdentityHub (`edc-identityhub`) is running and reachable at `edc.iam.sts.oauth.token.url`.
3. Check IdentityHub logs for token issuance errors.
4. Verify the federator's DID is registered with the IdentityHub and has a valid `MembershipCredential`.

---

### `ScopeExtension` logs warn about missing configuration

**Symptom**: Log message: `ScopeExtension initializing with default scope`.

**Cause**: `edc.policy.catalog.scope` is not set in the configuration file.

**Resolution**: This is informational, not an error. The default scope `org.eclipse.edc.vc.type:MembershipCredential:read` is used. If a different scope is required, add `edc.policy.catalog.scope=<your-scope>` to the configuration file.

---

## CORS Issues

### Browser requests to the participant API are blocked by CORS

**Symptom**: Browser console shows `CORS policy: No 'Access-Control-Allow-Origin' header`.

**Cause**: The requesting origin is not in the CORS allowlist. The allowlist is derived from registered participant endpoints.

**Resolution**:
1. Verify the participant connector's origin matches the endpoint registered in the database (e.g., if the UI is at `https://dashboard.ds4h.example.com`, a participant with `dspUrl: https://dashboard.ds4h.example.com/...` must be registered).
2. After adding or updating a participant via the REST API, CORS origins are refreshed automatically. If you updated the database directly via SQL, the CORS allowlist will not be updated until the next restart or REST API mutation.

---

## Health Check

### Health endpoint returns `503` or unhealthy components

**Symptom**: `GET /api/check/health` returns non-healthy status.

**Cause**: One or more EDC subsystems failed initialization.

**Resolution**:
1. Review startup logs for `ERROR` or `SEVERE` messages.
2. Check database connectivity.
3. Check Vault connectivity.
4. Restart the pod after resolving the underlying issue:
   ```bash
   kubectl rollout restart deployment/edc-federator -n ds4h
   ```

---

## Build Issues

### `./gradlew shadowJar` fails with `Checkstyle` violations

**Symptom**: Build fails with `Checkstyle rule violations found`.

**Cause**: Code style violations in modified Java files.

**Resolution**:
1. Check the HTML report: `build/reports/checkstyle/lint.html`
2. Fix the reported violations.
3. Re-run: `./gradlew shadowJar --no-daemon`

---

### Docker build fails — `permission denied` during `./gradlew`

**Symptom**: Docker build fails on the `RUN ./gradlew dependencies` step.

**Cause**: `gradlew` is not marked executable in the Docker build context.

**Resolution**: The Dockerfile includes `RUN chmod +x gradlew`. If this still fails, ensure the file is committed with execute permissions:
```bash
git update-index --chmod=+x gradlew
git commit -m "Fix gradlew execute permission"
