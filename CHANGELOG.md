# Changelog: edc-federated-catalog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/), and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

---

## [Unreleased]

### Added
- Memory bank (`memory-bank/`) with six core files: `projectBrief.md`, `productContext.md`, `systemPatterns.md`, `techContext.md`, `activeContext.md`, `progress.md`
- Documentation suite: `ARCHITECTURE.md`, `API.md`, `DEPLOYMENT.md`, `CONTRIBUTING.md`, `CHANGELOG.md`, `SECURITY.md`, `TROUBLESHOOTING.md`

---

## [1.1.0] — Security Hardening & Dynamic CORS

### Added
- **Dynamic CORS origin refresh**: `ParticipantRegistryApiController.refreshCorsOrigins()` rebuilds the CORS allowlist from the participant store after every successful create, update, or delete operation. Eliminates the need to restart the federator when participant origins change.
- `OriginsLoader` helper class to extract HTTP origins from participant endpoint URLs.
- `JerseyConfiguration.setParticipantOrigins()` to allow runtime updates to the CORS allowlist.

### Changed
- **Jetty pinned to 12.1.7** across all modules: fixes CVE-2026-1605 (GzipHandler resource leak in range 12.1.0–12.1.5) and CVE-2026-2332 (HTTP/1.1 request smuggling via chunk extensions, affects 12.1.6).
- **jackson-core pinned to 2.21.1**: fixes GHSA-72hv-8253-57qq (number length constraint bypass in async parser, affects 2.19.0–2.21.0). `jackson-annotations` and `jackson-databind` remain at BOM-resolved versions (no 2.21.1 release for those modules).

### Fixed
- **Dockerfile — `pebble` binary removed**: The Canonical Ubuntu `pebble` process manager binary shipped in the `eclipse-temurin:17-jre` base image was removed (`rm -f /usr/bin/pebble`). This eliminates 8 HIGH-severity Go stdlib CVEs: CVE-2026-33811, CVE-2026-33814, CVE-2026-39820, CVE-2026-39823, CVE-2026-39825, CVE-2026-39836, CVE-2026-42499, CVE-2026-42504.
- **Dockerfile — APT packages upgraded**: `apt-get upgrade -y` now explicitly upgrades `dirmngr`, `gnupg`, `gnupg-utils`, `gpg`, `gpg-agent`, `gpgconf`, `gpgsm`, `gpgv`, `keyboxd` to pull in all available security patches during image build.

---

## [1.0.0] — Initial Release

### Added
- **Database-backed participant registry** (`extensions/store/sql/participant-sql`):
  - `Participant` entity with builder pattern
  - `ParticipantStore` interface: `findAll()`, `findById()`, `save()`, `update()`, `delete()`
  - PostgreSQL SQL store implementation
  - `participant-schema.sql` bootstrapped at startup via `edc:sql-bootstrapper`

- **`CatalogNodeResolverExtension`** (`extensions/catalog/directory`):
  - Provides `TargetNodeDirectory` via `@Provider` mechanism
  - Creates `DbCatalogNodeDirectory` backed by `ParticipantStore`

- **`DbCatalogNodeDirectory`** (`extensions/catalog/directory`):
  - Implements EDC `TargetNodeDirectory`
  - Converts `Participant` ↔ `TargetNode` (bidirectional adapter)
  - `getAll()`: loads all participants from DB for crawler consumption
  - `insert()`: saves a new participant via `ParticipantStore`
  - `remove()`: deletes a participant and returns the removed `TargetNode`

- **Participant Registry REST API** (`extensions/catalog/participant`):
  - `POST /participants/` — create a participant
  - `GET /participants/` — list all participants
  - `GET /participants/{id}` — get a participant by ID
  - `PUT /participants/{id}` — update a participant
  - `DELETE /participants/{id}` — remove a participant
  - `ParticipantDto` request/response DTO with builder
  - `ParticipantValidator` for input validation
  - `ApiResponse<T>` standard response envelope

- **Swagger UI** (`extensions/catalog/participant`):
  - `SwaggerController` serves the Swagger UI at `{catalog.path}/swagger/`
  - Co-registered on the `catalog` web context alongside the participant API

- **`ScopeExtension`** (`extensions/scope`):
  - Reads `edc.policy.catalog.scope` configuration property
  - Registers a post-validator for `RequestCatalogPolicyContext`
  - Default scope: `org.eclipse.edc.vc.type:MembershipCredential:read`

- **`ScopeMappingFunction`** (`extensions/scope`):
  - Merges configured default scopes with any scopes already present in the request policy context
  - Ensures every outgoing catalog request carries the correct VC presentation scope

- **Custom Jersey server** (`extensions/common/http/jersey-core`):
  - `JerseyExtension`, `JerseyRestService`, `JerseyConfiguration`
  - `CorsFilter` JAX-RS response filter

- **HTTP client** (`extensions/common/http/client`):
  - Configurable shared HTTP client for EDC extensions

- **Docker container**:
  - Multi-stage build: `gradle:8.8-jdk17` builder → `eclipse-temurin:17-jre` runtime
  - Non-root `appuser` (UID 10100)
  - `LOG_LEVEL` environment variable for runtime log level control

- **EDC DSP protocol adapters**:
  - `edc:federated-catalog-core-2025` (DSP 2025/1)
  - `edc:federated-catalog-core-08` (DSP 08)

- **Helm chart** (`helm-edc-federated-catalog`):
  - Deploys federator, PostgreSQL, optional Vault, and IdentityHub as a single release

- **Test infrastructure**:
  - JUnit Jupiter + Mockito + AssertJ
  - JaCoCo per-subproject coverage reports
  - Aggregated HTML test report via `allTests` Gradle task

---

## Migration Notes

### From static `participants.json` to database-backed registry

The original EDC Federated Catalog uses a `participants.json` file for participant discovery. This release replaces that mechanism entirely with the PostgreSQL-backed `ParticipantStore`.

**Migration steps**:

1. Deploy the updated federator with a PostgreSQL database configured.
2. Migrate participant entries from `participants.json` to the `edc_participant` table using the Participant Registry API or direct SQL.
3. Remove the `participants.json` file from the deployment configuration.
4. Verify participants are loaded at startup via the logs: `DbCatalogNodeDirectory: Loaded N participants from database`.
