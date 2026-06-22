# memory-bank/productContext.md

# Product Context: edc-federated-catalog

## Why This Project Exists

In a dataspace, multiple participant connectors each expose their own catalog of available data offerings. Without a central aggregation point, a consumer would have to query every connector individually to discover what data is available across the whole dataspace. The Federated Catalog solves this by crawling all participant connectors on a schedule and maintaining a local, aggregated view of the entire dataspace catalog.

This repository customises the standard EDC Federated Catalog runtime to meet the operational requirements of the **Dataspace4Health (DS4H)** platform:

1. **Dynamic participant management** — The standard EDC Federated Catalog relies on a static `participants.json` file. Every time a new connector joins or leaves the dataspace, the file must be edited and the federator restarted. This is impractical at scale. This project replaces the file with a PostgreSQL-backed participant registry that can be managed at runtime via a REST API.

2. **Credential-based catalog access** — Catalog queries in a DCP (Dataspace Credential Protocol) dataspace require the requesting party to present the correct Verifiable Credential (VC) scopes. The `ScopeExtension` ensures that every outgoing catalog request carries the configured membership scope so the IdentityHub returns the right credentials.

3. **Operator-friendly participant CRUD** — Operators need a simple way to add, remove, or update participant connectors without writing SQL or restarting services. The Participant Registry API and its embedded Swagger UI provide this capability.

## Problems It Solves

| Problem | Solution |
|---|---|
| Static participant list requires restart on change | Database-backed `ParticipantStore` enables live add/remove/update |
| No UI for managing participants | REST API + Swagger UI on the `catalog` web context |
| Catalog queries fail without correct VC scope | `ScopeExtension` injects `MembershipCredential:read` scope automatically |
| CORS origins become stale when participants change | `refreshCorsOrigins()` rebuilds the allowlist after every registry mutation |
| Fat JAR running as root in Docker | Non-root `appuser` (UID 10100) in a slim `eclipse-temurin:17-jre` image |

## How It Should Work

### Catalog Crawling Flow

```
Startup
  → CatalogNodeResolverExtension initializes DbCatalogNodeDirectory
  → Federator crawler calls DbCatalogNodeDirectory.getAll()
  → ParticipantStore queries edc_participant table in PostgreSQL
  → Each row is converted to a TargetNode (id, name, endpoint, protocols)
  → Crawler contacts each participant's DSP catalog endpoint
  → Aggregated catalog is stored locally for query
```

### Participant Management Flow

```
Operator calls POST /participants/ with ParticipantDto payload
  → ParticipantRegistryApiController validates input via ParticipantValidator
  → Checks for duplicate id in ParticipantStore
  → Persists new Participant entity via ParticipantStore.save()
  → Calls refreshCorsOrigins() to update CORS allowlist
  → Returns 201 Created with the created participant DTO
```

### Scope Injection Flow

```
Federator sends catalog query to a participant connector
  → EDC creates RequestCatalogPolicyContext
  → PolicyEngine runs post-validators
  → ScopeMappingFunction.apply() merges configured scope with existing scopes
  → Updated scope is sent to IdentityHub STS
  → IdentityHub returns presentation token with MembershipCredential
  → Participant connector accepts the query
```

## User Experience Goals

- **Zero-downtime participant management**: operators can register or deregister connectors without restarting the federator.
- **Self-documenting API**: Swagger UI is co-located with the API — no separate documentation portal needed.
- **Transparent credential handling**: scope injection is automatic; operators do not need to configure per-request credential parameters.
- **Secure by default**: the container runs as a non-root user; security-patched dependencies are pinned explicitly in `build.gradle.kts`.
