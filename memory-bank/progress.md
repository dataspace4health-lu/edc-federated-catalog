# memory-bank/progress.md

# Progress: edc-federated-catalog

## What Works

### Core Functionality
- ✅ **Database-backed participant registry**: `ParticipantStore` + PostgreSQL via `edc_participant` table. Schema is bootstrapped automatically at startup via `edc:sql-bootstrapper`.
- ✅ **Dynamic TargetNodeDirectory**: `DbCatalogNodeDirectory` reads participants from the DB and exposes them as crawler `TargetNode` objects. Replaces the static `participants.json` approach.
- ✅ **Federated catalog crawling**: EDC crawler uses `DbCatalogNodeDirectory` to obtain crawl targets. Both DSP 2025/1 and DSP 08 protocol adapters are included.
- ✅ **Participant CRUD API**: Full REST API (`POST`, `GET`, `PUT`, `DELETE`) at `/participants/` on the `catalog` web context.
- ✅ **Input validation**: `ParticipantValidator` validates all required fields; duplicate-check on create returns `409 Conflict`.
- ✅ **Swagger UI**: `SwaggerController` registers the Swagger UI at `{web.http.catalog.path}/swagger/`.
- ✅ **Scope injection**: `ScopeExtension` registers a post-validator for `RequestCatalogPolicyContext` that merges the configured default catalog scope before policy evaluation.
- ✅ **Dynamic CORS origins**: `refreshCorsOrigins()` rebuilds the CORS allowlist from the participant store after every create/update/delete.
- ✅ **HashiCorp Vault integration**: Secrets (DB password, Vault token, private keys) are resolved from Vault at runtime.
- ✅ **Health endpoint**: `/api/check/health` via `edc:api-observability`.
- ✅ **Docker container**: Multi-stage build producing a slim `eclipse-temurin:17-jre` image running as non-root `appuser` (UID 10100).
- ✅ **Helm chart**: `helm-edc-federated-catalog` deploys the federator together with PostgreSQL, Vault, and IdentityHub.

### Security
- ✅ jackson-core pinned to 2.21.1 (GHSA-72hv-8253-57qq)
- ✅ Jetty pinned to 12.1.7 (CVE-2026-1605, CVE-2026-2332)
- ✅ `pebble` binary removed from Docker base image (8 Go stdlib CVEs)
- ✅ APT packages upgraded in Docker build

### Testing
- ✅ Unit tests present for all extension classes (`CatalogNodeResolverExtensionTest`, `DbCatalogNodeDirectoryTest`, `ParticipantApiExtensionTest`, `ParticipantRegistryApiControllerTest`, `ParticipantValidatorTest`, `ScopeExtensionTest`, `ScopeMappingFunctionTest`, `ApiResponseTest`, `ParticipantDtoTest`)
- ✅ JaCoCo coverage reports generated per subproject
- ✅ Aggregated test HTML report via `allTests` task

## What's Left to Build

### Missing Test Coverage
- ❌ **Integration tests**: No Testcontainers-based integration tests for the SQL store layer against a real PostgreSQL instance.
- ❌ **API integration tests**: No end-to-end REST API tests with RestAssured against a running federator container.

### Missing Features
- ❌ **OpenAPI spec export**: No static `openapi.json` / `openapi.yaml` generated as a build artifact.
- ❌ **Catalog query endpoint**: No dedicated endpoint to query the aggregated catalog content held by the federator (read-only federated catalog access for consumers).
- ❌ **Metrics / observability**: Health endpoint does not report participant count, last crawl timestamp, or per-participant crawl status.
- ❌ **Pagination on list endpoint**: `GET /participants/` returns all participants without pagination. May become a concern at scale.
- ❌ **Participant filtering/search**: No query parameters for filtering participants by name, endpoint, or protocol.

### Documentation
- ✅ Memory bank created (this session)
- ✅ Documentation files created: ARCHITECTURE.md, API.md, DEPLOYMENT.md, CONTRIBUTING.md, CHANGELOG.md, SECURITY.md, TROUBLESHOOTING.md

## Current Status

**Production-ready for the core use case** (crawling a known set of participant connectors with database-managed participant list). Integration test coverage and observability enhancements are the main gaps before a hardened production release.

## Known Issues

1. **No pagination on `GET /participants/`** — All participants are returned in a single response. This is acceptable for small dataspaces (< a few hundred participants) but will require pagination for larger deployments.

2. **CORS refresh on failure is silent** — If `refreshCorsOrigins()` throws an exception, the error is logged as a warning but the operation is not rolled back. The participant record change succeeds but CORS origins may be stale until the next successful mutation or restart.

3. **`jersey-core` module is custom, not upstream** — The `extensions/common/http/jersey-core` module embeds a custom CORS filter and Jersey configuration. This means EDC upgrades must be validated to ensure jersey API compatibility is maintained.

4. **SQL bootstrapper is startup-only** — Schema migrations beyond the initial creation must be handled manually. `edc:sql-bootstrapper` only runs `CREATE TABLE IF NOT EXISTS` — it does not manage schema evolution.

## Evolution of Project Decisions

| Decision | Original Approach | Current Approach | Reason |
|---|---|---|---|
| Participant discovery | Static `participants.json` file | `ParticipantStore` + PostgreSQL | Runtime management without restart |
| Participant API | None (file-only) | REST CRUD API + Swagger UI | Operator-friendly management |
| CORS management | Static config | Dynamic refresh from participant store | Keep CORS consistent with live participant list |
| Scope injection | All three policy contexts (catalog, negotiation, transfer) | Catalog only (`RequestCatalogPolicyContext`) | Federator only sends catalog requests |
| Container security | Default base image user | Non-root `appuser` + pebble removal | Eliminate privilege escalation risk and reduce CVE surface |
