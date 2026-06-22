# Architecture: edc-federated-catalog

## Overview

The `edc-federated-catalog` repository implements a **federator** node for the Dataspace4Health (DS4H) dataspace. It extends the Eclipse Dataspace Connector (EDC) Federated Catalog runtime with:

- A **database-backed participant registry** (PostgreSQL) replacing the static `participants.json` approach.
- A **REST CRUD API** for managing the participant registry at runtime.
- **Scope injection** for outgoing catalog requests to ensure correct Verifiable Credential presentation.
- **Dynamic CORS management** tied to the participant registry state.

The federator crawls all registered participant connectors on a schedule, aggregates their catalogs into a local store, and makes the combined view queryable by dataspace consumers.

---

## High-Level Component Map

```
┌──────────────────────────────────────────────────────────────────────┐
│                   edc-federated-catalog runtime                      │
│                                                                      │
│  ┌──────────────────────────┐  ┌──────────────────────────────────┐  │
│  │  CatalogNodeResolver     │  │   ParticipantApiExtension        │  │
│  │  Extension               │  │                                  │  │
│  │  ─────────────────────   │  │  ParticipantRegistryApi          │  │
│  │  @Provider               │  │  Controller                      │  │
│  │  TargetNodeDirectory     │  │  POST/GET/PUT/DELETE             │  │
│  │         │                │  │  /participants/                  │  │
│  │         ▼                │  │                                  │  │
│  │  DbCatalogNodeDirectory  │  │  SwaggerController               │  │
│  │  (TargetNodeDirectory)   │  │  /swagger/                       │  │
│  └──────────┬───────────────┘  └──────────────┬───────────────────┘  │
│             │ reads/writes                     │ reads/writes         │
│             ▼                                  ▼                     │
│  ┌──────────────────────────────────────────────────────────────┐    │
│  │                    ParticipantStore                          │    │
│  │         (extensions/store/sql/participant-sql)               │    │
│  │                                                              │    │
│  │   findAll()  findById()  save()  update()  delete()          │    │
│  └──────────────────────────┬───────────────────────────────────┘    │
│                             │ JDBC/SQL                               │
│                             ▼                                        │
│                    ┌──────────────────┐                              │
│                    │   PostgreSQL     │                              │
│                    │  edc_participant │                              │
│                    └──────────────────┘                              │
│                                                                      │
│  ┌─────────────────────────────┐  ┌───────────────────────────────┐  │
│  │      ScopeExtension         │  │  JerseyExtension + CorsFilter │  │
│  │  ScopeMappingFunction       │  │  JerseyConfiguration          │  │
│  │  post-validator on          │  │  (dynamic CORS origins)       │  │
│  │  RequestCatalogPolicyContext│  │                               │  │
│  └─────────────────────────────┘  └───────────────────────────────┘  │
└──────────────────────────────────────────────────────────────────────┘
        │ outgoing DSP catalog requests         │ inbound REST
        ▼                                        ▼
  Participant Connectors                  Operators / Dashboards
  (edc-controlplane instances)
```

---

## Extension Modules

### `extensions/catalog/directory`

**Purpose**: Wires the EDC crawler to the database-backed participant registry.

| Class | Role |
|---|---|
| `CatalogNodeResolverExtension` | `ServiceExtension` — injected with `ParticipantStore`, provides a `TargetNodeDirectory` via `@Provider` |
| `DbCatalogNodeDirectory` | `TargetNodeDirectory` implementation — adapts `Participant` entities to EDC `TargetNode` objects |

The `@Provider` annotation on `CatalogNodeResolverExtension.federatedCacheNodeDirectory()` registers the `TargetNodeDirectory` into the EDC service registry so the built-in crawler can discover it.

---

### `extensions/catalog/participant`

**Purpose**: REST CRUD API and Swagger UI for managing the participant registry.

| Class | Role |
|---|---|
| `ParticipantApiExtension` | `ServiceExtension` — registers API and Swagger controllers on the `catalog` web context |
| `ParticipantRegistryApiController` | JAX-RS controller (`@Path("/participants")`) — implements all CRUD operations |
| `ParticipantRegistryApi` | JAX-RS interface defining the API contract |
| `ParticipantDto` | Request/response DTO (`id`, `name`, `dspUrl`, `supportedProtocols`) |
| `ParticipantValidator` | Validates DTO fields before persistence |
| `ApiResponse<T>` | Standard response envelope (`success`, `data`, `message` / `error`) |
| `SwaggerController` | Serves the Swagger UI HTML page at `{catalog.path}/swagger/` |

---

### `extensions/common/http/jersey-core`

**Purpose**: Custom Jersey REST server with CORS support.

| Class | Role |
|---|---|
| `JerseyExtension` | Starts the embedded Jersey server |
| `JerseyRestService` | Registers JAX-RS resources with the server |
| `JerseyConfiguration` | Holds the CORS configuration, including dynamic participant origins |
| `CorsFilter` | JAX-RS `ContainerResponseFilter` that adds CORS headers based on `JerseyConfiguration` |

`JerseyConfiguration.setParticipantOrigins()` is called by `ParticipantRegistryApiController` after every successful create/update/delete to keep the CORS allowlist in sync with the registry.

---

### `extensions/common/http/client`

**Purpose**: Provides a shared, configurable HTTP client for EDC extensions.

---

### `extensions/scope`

**Purpose**: Injects the configured catalog scope into outgoing catalog request policy contexts.

| Class | Role |
|---|---|
| `ScopeExtension` | `ServiceExtension` — reads `edc.policy.catalog.scope` and registers a post-validator |
| `ScopeMappingFunction` | Post-validator — merges `defaultScopes` ∪ existing scopes in `RequestCatalogPolicyContext` |
| `ScopeConstants` | Constants for config property keys and default scope values |

Default scope: `org.eclipse.edc.vc.type:MembershipCredential:read`

---

### `extensions/store/sql/participant-sql`

**Purpose**: SQL/PostgreSQL persistence implementation for `ParticipantStore`.

| Artifact | Role |
|---|---|
| `participant-schema.sql` | `CREATE TABLE IF NOT EXISTS edc_participant` — bootstrapped at startup |
| `ParticipantStore` | Store interface (`findAll`, `findById`, `save`, `update`, `delete`) |
| `Participant` | Persistence entity with builder |

---

## Data Model

### `edc_participant` table

```sql
participant_id       VARCHAR(255) PRIMARY KEY   -- unique connector identifier
name                 VARCHAR(255) NOT NULL        -- display name
endpoint             VARCHAR(500) NOT NULL        -- DSP catalog endpoint URL
supported_protocols  TEXT                         -- JSON array, e.g. ["dataspace-protocol-http"]
created_at           BIGINT NOT NULL              -- Unix ms timestamp
updated_at           BIGINT NOT NULL              -- Unix ms timestamp
```

---

## Request Flows

### Catalog Crawl

```
1. EDC Federated Catalog crawler scheduled execution
2. Calls TargetNodeDirectory.getAll()
3. DbCatalogNodeDirectory queries ParticipantStore.findAll()
4. SQL: SELECT * FROM edc_participant
5. Each row → TargetNode(name, id, endpoint, supportedProtocols)
6. For each TargetNode: crawler sends DSP catalog request to endpoint
7. Responses aggregated into the local federated catalog store
```

### Outgoing Catalog Request Scope

```
1. Crawler prepares outgoing DSP catalog request to a TargetNode
2. EDC creates RequestCatalogPolicyContext
3. PolicyEngine runs all registered post-validators
4. ScopeMappingFunction.apply() → merges {"MembershipCredential:read"} ∪ existing scopes
5. Updated scope sent to IdentityHub STS token endpoint
6. IdentityHub returns VP token containing MembershipCredential
7. Catalog request sent with VP token to participant connector
8. Participant connector validates VP and returns catalog
```

### Participant Creation

```
1. POST /participants/ { id, name, dspUrl, supportedProtocols }
2. ParticipantValidator.validate(dto) → check required fields, valid URL
3. ParticipantStore.findById(id) → 409 Conflict if duplicate
4. Participant entity built with createdAt = updatedAt = System.currentTimeMillis()
5. ParticipantStore.save(participant) → INSERT INTO edc_participant
6. refreshCorsOrigins() → OriginsLoader.loadOrigins() → JerseyConfiguration.setParticipantOrigins()
7. 201 Created with ApiResponse.success(participantDto)
```

---

## Security Architecture

- **Non-root container**: `appuser` (UID 10100) — no root privileges inside the container.
- **`pebble` removed**: Canonical Ubuntu pebble process manager binary removed from base image, eliminating 8 Go stdlib CVEs.
- **Secrets in Vault**: DB passwords, Vault tokens, private keys stored in HashiCorp Vault; resolved at runtime by `edc:vault-hashicorp`.
- **Dependency pinning**: `jackson-core` → 2.21.1 (GHSA-72hv-8253-57qq), Jetty → 12.1.7 (CVE-2026-1605, CVE-2026-2332).
- **CORS**: Dynamic CORS filter; only participant connector origins are allowed.

---

## Deployment Architecture

The federator is deployed via the `helm-edc-federated-catalog` Helm chart, which bundles:

- `edc-federated-catalog` container (this repo)
- PostgreSQL (subchart, `catalog-postgresql`)
- HashiCorp Vault (optional subchart, disabled by default; use external Vault in production)
- `federator-identityhub` (`edc-identityhub`, subchart)

The federator requires network access to:
- All registered participant connector DSP endpoints (outbound crawling)
- The IdentityHub STS token endpoint (outbound, for VP token acquisition)
- PostgreSQL (internal to the Helm release)
- HashiCorp Vault (internal or external)

---

## Related Repositories

| Repository | Role |
|---|---|
| `edc-controlplane` | Participant connector control plane — crawled by the federator |
| `edc-dataplane` | Participant connector data plane — not directly interacted with by the federator |
| `edc-identityhub` | Identity Hub — provides VP tokens for outgoing catalog requests |
| `helm-edc-federated-catalog` | Helm chart for deploying the full federator stack |
| `integration-edc` | Integration test setup for the full DS4H dataspace |
