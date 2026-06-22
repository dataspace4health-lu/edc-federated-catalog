# Deployment Guide: edc-federated-catalog

## Overview

The `edc-federated-catalog` is packaged as a Docker container and deployed via the `helm-edc-federated-catalog` Helm chart. This guide covers:

1. Building the Docker image
2. Helm chart deployment
3. Environment variables and configuration properties
4. Database setup
5. HashiCorp Vault setup
6. Rollback procedures

---

## Prerequisites

| Requirement | Version | Notes |
|---|---|---|
| JDK | 17 | Build only (not needed if using Docker build) |
| Docker | 20+ | For building and pushing the image |
| Helm | 3.x | For Kubernetes deployment |
| Kubernetes | 1.24+ | Target cluster |
| PostgreSQL | 14+ | Participant registry database |
| HashiCorp Vault | 1.13+ | Secrets management |

---

## 1. Building the Docker Image

The Dockerfile uses a multi-stage build. The Gradle build runs inside the `gradle:8.8-jdk17` stage and the resulting fat JAR is copied into `eclipse-temurin:17-jre`.

```bash
# Build the image
docker build -t localhost:5432/edc-federated-catalog:latest .

# Push to local registry
docker push localhost:5432/edc-federated-catalog:latest

# Tag and push to a remote registry
docker tag localhost:5432/edc-federated-catalog:latest registry.example.com/ds4h/edc-federated-catalog:1.0.0
docker push registry.example.com/ds4h/edc-federated-catalog:1.0.0
```

> Tests are skipped during Docker builds (`-x test`). Run tests locally with `./gradlew test` before building the production image.

---

## 2. Helm Chart Deployment

The Helm chart is located at `helm-edc-federated-catalog/`.

### Install

```bash
# Default install (without embedded Vault)
helm install edc-federator ./helm-edc-federated-catalog \
  --namespace ds4h \
  --create-namespace \
  -f helm-edc-federated-catalog/values.yaml

# Install with embedded Vault (development only)
helm install edc-federator ./helm-edc-federated-catalog \
  --namespace ds4h \
  --create-namespace \
  -f helm-edc-federated-catalog/values.yaml \
  -f helm-edc-federated-catalog/vault-values.yaml
```

### Upgrade

```bash
helm upgrade edc-federator ./helm-edc-federated-catalog \
  --namespace ds4h \
  -f helm-edc-federated-catalog/values.yaml
```

### Uninstall

```bash
helm uninstall edc-federator --namespace ds4h
```

### Key Helm Values

| Value | Default | Description |
|---|---|---|
| `edc-federated-catalog.config.vault.url` | `http://edc-fc-vault:8200` | HashiCorp Vault URL |
| `edc-federated-catalog.config.vault.token` | `root` | Vault token (only used when `createSecret: true`) |
| `edc-federated-catalog.config.vault.tokenSecretName` | `edc-federator-vault` | Kubernetes secret holding the Vault token |
| `edc-federated-catalog.config.vault.tokenSecretKey` | `token` | Key in the Kubernetes secret |
| `edc-federated-catalog.config.vault.createSecret` | `false` | Set to `true` to create the Vault token secret |
| `edc-federated-catalog.postgresql.nameOverride` | `catalog-postgresql` | PostgreSQL subchart name |
| `vault.enabled` | `false` | Set to `true` to deploy embedded Vault (development only) |
| `federator-identityhub.config.vault.url` | `http://edc-fc-vault:8200` | IdentityHub Vault URL |

---

## 3. Configuration Properties

The federator is configured via a `.properties` file, loaded by the `edc:configuration-filesystem` module at startup. In the Helm deployment, this file is mounted as a ConfigMap.

### Required Properties

| Property | Example | Description |
|---|---|---|
| `edc.participant.id` | `did:web:federator.ds4h.example.com` | Federator's own participant identifier |
| `edc.datasource.default.url` | `jdbc:postgresql://catalog-postgresql:5432/catalogdb` | PostgreSQL JDBC URL |
| `edc.datasource.default.user` | `cataloguser` | PostgreSQL username |
| `edc.datasource.default.password` | `vault:secret/catalog#db-password` | PostgreSQL password (Vault reference) |
| `edc.vault.hashicorp.url` | `http://edc-fc-vault:8200` | HashiCorp Vault URL |
| `edc.vault.hashicorp.token` | `vault-token` | Vault authentication token |
| `edc.iam.sts.oauth.token.url` | `https://identityhub.ds4h.example.com/api/sts/token` | IdentityHub STS token endpoint |

### Optional Properties

| Property | Default | Description |
|---|---|---|
| `edc.policy.catalog.scope` | `org.eclipse.edc.vc.type:MembershipCredential:read` | VC scope injected into catalog requests |
| `web.http.catalog.path` | `/api/catalog` | Base path for Participant Registry API |
| `web.http.catalog.port` | `8082` | Port for the catalog web context |
| `web.http.default.port` | `8181` | Port for the default web context (health check) |
| `edc.fc.crawler.execution.delay.seconds` | EDC default | Interval between crawler executions |
| `edc.http.disable.tls` | `false` | Set to `true` to disable TLS verification (non-production only) |

---

## 4. Database Setup

### Schema Bootstrap

The `edc:sql-bootstrapper` module runs `participant-schema.sql` automatically at startup if the `edc_participant` table does not exist. No manual schema creation is needed for fresh deployments.

### Manual Schema Creation

If you need to create the schema manually:

```sql
CREATE TABLE IF NOT EXISTS edc_participant
(
    participant_id       VARCHAR(255) PRIMARY KEY,
    name                 VARCHAR(255) NOT NULL,
    endpoint             VARCHAR(500) NOT NULL,
    supported_protocols  TEXT,
    created_at           BIGINT       NOT NULL,
    updated_at           BIGINT       NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_participant_id ON edc_participant(participant_id);
```

### Seeding Initial Participants

After deployment, seed the initial participant list via the REST API or SQL:

```bash
# Via REST API
curl -X POST http://federator.ds4h.example.com/api/catalog/participants/ \
  -H "Content-Type: application/json" \
  -d '{
    "id": "connector-hospital-01",
    "name": "Hospital Connector",
    "dspUrl": "https://hospital.ds4h.example.com/api/v1/dsp",
    "supportedProtocols": ["dataspace-protocol-http"]
  }'
```

```sql
-- Via direct SQL
INSERT INTO edc_participant (participant_id, name, endpoint, supported_protocols, created_at, updated_at)
VALUES ('connector-hospital-01', 'Hospital Connector',
        'https://hospital.ds4h.example.com/api/v1/dsp',
        '["dataspace-protocol-http"]',
        EXTRACT(EPOCH FROM NOW()) * 1000,
        EXTRACT(EPOCH FROM NOW()) * 1000);
```

---

## 5. HashiCorp Vault Setup

### Required Vault Secrets

The federator expects the following secrets to be present in Vault before startup:

| Vault Path | Key | Description |
|---|---|---|
| `secret/catalog` | `db-password` | PostgreSQL password |
| `secret/catalog` | `private-key` | Federator's private key for DID/signing |
| `secret/catalog` | `public-key` | Federator's public key |

### Creating Secrets

```bash
# Enable the KV secrets engine (if not already enabled)
vault secrets enable -path=secret kv-v2

# Set database password
vault kv put secret/catalog db-password="your-secure-password"

# Set key material
vault kv put secret/catalog private-key="$(cat private.pem)" public-key="$(cat public.pem)"
```

### Vault Token Kubernetes Secret

```bash
kubectl create secret generic edc-federator-vault \
  --from-literal=token="your-vault-token" \
  --namespace ds4h
```

---

## 6. Verifying the Deployment

### Health Check

```bash
curl http://federator.ds4h.example.com/api/check/health
```

Expected response:
```json
[{"isHealthy": true, "componentName": "..."}]
```

### Participant Registry

```bash
# List participants (should return empty list on fresh deployment before seeding)
curl http://federator.ds4h.example.com/api/catalog/participants/
```

### Crawler Verification

Check the federator logs for crawl activity:

```bash
kubectl logs -n ds4h deployment/edc-federator --follow | grep -i "participant\|crawl\|target"
```

Expected log lines after a crawl cycle:
```
DbCatalogNodeDirectory: Loaded N participants from database
```

---

## 7. Rollback

### Helm Rollback

```bash
# List helm release history
helm history edc-federator --namespace ds4h

# Rollback to the previous revision
helm rollback edc-federator --namespace ds4h

# Rollback to a specific revision
helm rollback edc-federator 2 --namespace ds4h
```

### Database Rollback

The `edc_participant` table schema is additive — the only migration performed is the initial `CREATE TABLE IF NOT EXISTS`. There is no automated schema migration tool. If a schema change was applied manually, revert it manually before rolling back the application.

---

## 8. Environment Variables

The Docker container accepts one environment variable at runtime:

| Variable | Default | Description |
|---|---|---|
| `LOG_LEVEL` | `info` | EDC log level (`debug`, `info`, `warning`, `severe`) |

```bash
# Override log level at container startup
docker run -e LOG_LEVEL=debug edc-federated-catalog:latest
```

---

## 9. Docker Run (Local Testing)

For local testing without Kubernetes:

```bash
docker run \
  -p 8082:8082 \
  -p 8181:8181 \
  -v /path/to/config.properties:/app/config.properties \
  -e LOG_LEVEL=debug \
  localhost:5432/edc-federated-catalog:latest
```

The properties file path is configured via the `edc.fs.config` system property, which defaults to `/app/config.properties` in the standard EDC filesystem configuration setup.
