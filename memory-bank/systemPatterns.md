# memory-bank/systemPatterns.md

# System Patterns: edc-federated-catalog

## Architecture Overview

```
┌──────────────────────────────────────────────────────────────────────┐
│                   edc-federated-catalog runtime                      │
│  (org.eclipse.edc.boot.system.runtime.BaseRuntime)                   │
│                                                                      │
│  ┌──────────────────────┐   ┌──────────────────────────────────────┐ │
│  │ CatalogNodeResolver  │   │      ParticipantApiExtension         │ │
│  │     Extension        │   │  (extensions/catalog/participant)    │ │
│  │ (extensions/catalog/ │   │                                      │ │
│  │      directory)      │   │  ParticipantRegistryApiController    │ │
│  │                      │   │  POST/GET/PUT/DELETE /participants/  │ │
│  │  DbCatalogNode       │   │                                      │ │
│  │  Directory           │   │  SwaggerController                   │ │
│  │  TargetNodeDirectory │   │  /swagger/                           │ │
│  └──────────┬───────────┘   └──────────────┬───────────────────────┘ │
│             │                              │                         │
│             ▼                              ▼                         │
│  ┌──────────────────────────────────────────────────────────────┐   │
│  │                     ParticipantStore                         │   │
│  │          (extensions/store/sql/participant-sql)               │   │
│  │                      PostgreSQL                              │   │
│  │                   edc_participant table                      │   │
│  └──────────────────────────────────────────────────────────────┘   │
│                                                                      │
│  ┌──────────────────┐   ┌──────────────────────────────────────────┐ │
│  │  ScopeExtension  │   │    JerseyExtension / CorsFilter          │ │
│  │  (extensions/    │   │  (extensions/common/http/jersey-core)    │ │
│  │   scope)         │   │                                          │ │
│  │  ScopeMapping    │   │  JerseyConfiguration                     │ │
│  │  Function        │   │  (dynamic CORS origins)                  │ │
│  └──────────────────┘   └──────────────────────────────────────────┘ │
└──────────────────────────────────────────────────────────────────────┘
         │ DSP crawl (outgoing)          │ Catalog Management API
         ▼                               ▼
  Participant Connectors          Operators / Dashboard
  (edc-controlplane instances)    POST/GET/PUT/DELETE /participants/
```

## Key Technical Decisions

### 1. EDC Service Extension Pattern
Every custom behavior is packaged as a `ServiceExtension` discovered via `META-INF/services/org.eclipse.edc.spi.system.ServiceExtension`. This is the canonical EDC extension mechanism — no reflection or custom loaders are needed.

### 2. Database-Backed TargetNodeDirectory
The EDC Federated Catalog crawler requires a `TargetNodeDirectory` to obtain the list of connectors to crawl. `CatalogNodeResolverExtension` provides this via the `@Provider` mechanism — it creates a `DbCatalogNodeDirectory` that reads participants from PostgreSQL rather than a static file. This makes the crawl target list live and manageable at runtime.

### 3. Participant ↔ TargetNode Conversion
`DbCatalogNodeDirectory` acts as a bidirectional adapter between two domain models:
- `Participant` — the internal persistence entity (id, name, endpoint, supportedProtocols, createdAt, updatedAt)
- `TargetNode` — the EDC crawler SPI interface (id, name, targetUrl, supportedProtocols)

The `toTargetNode()` and `toParticipant()` private methods handle the conversion in both directions.

### 4. Scope Injection via Post-Validators
`ScopeMappingFunction` is registered as a `PolicyValidatorRule<RequestCatalogPolicyContext>` post-validator on the `PolicyEngine`. This hook runs after the request policy context is created but before policy evaluation, giving the extension the opportunity to add default scopes without changing the caller's request payload. The default scope (`org.eclipse.edc.vc.type:MembershipCredential:read`) can be overridden via `edc.policy.catalog.scope`.

### 5. Dynamic CORS Origin Refresh
After every successful participant create/update/delete operation, `ParticipantRegistryApiController.refreshCorsOrigins()` re-reads all participants from the store and passes their endpoint origins to `JerseyConfiguration.setParticipantOrigins()`. The `CorsFilter` uses this updated list for subsequent requests. This eliminates the need to restart the federator when participant CORS origins change.

### 6. Flat Source Set Aggregation
`build.gradle.kts` uses a custom `sourceSets` block that walks `extensions/**/src/main/java` directories and registers them all as source roots of the root project. This allows Gradle to compile all extension classes as one unit while maintaining logical module separation in the filesystem.

### 7. Shadow JAR Deployment
The `com.gradleup.shadow` plugin merges all runtime dependencies and extension service files (`mergeServiceFiles()`) into a single fat JAR. The Dockerfile's build stage produces `/workspace/build/libs/*.jar`, which is copied to `/app/federated-catalog.jar` in the runtime image.

## Component Relationships

```
CatalogNodeResolverExtension
    └─ @Inject ─▶ ParticipantStore
    └─ @Inject ─▶ Monitor
    └─ @Provider ─▶ TargetNodeDirectory (= DbCatalogNodeDirectory)

DbCatalogNodeDirectory (implements TargetNodeDirectory)
    └─ getAll() ─▶ ParticipantStore.findAll() → List<Participant> → List<TargetNode>
    └─ insert() ─▶ ParticipantStore.save(Participant)
    └─ remove() ─▶ ParticipantStore.delete(id)

ParticipantApiExtension
    └─ @Inject ─▶ WebService, ParticipantStore, JerseyConfiguration, Monitor
    └─ registers ─▶ ParticipantRegistryApiController on "catalog" context
    └─ registers ─▶ SwaggerController on "catalog" context

ParticipantRegistryApiController (implements ParticipantRegistryApi)
    └─ POST   /participants/    ─▶ ParticipantStore.save()
    └─ GET    /participants/    ─▶ ParticipantStore.findAll()
    └─ GET    /participants/{id}─▶ ParticipantStore.findById()
    └─ PUT    /participants/{id}─▶ ParticipantStore.update()
    └─ DELETE /participants/{id}─▶ ParticipantStore.delete()
    └─ after mutate ─▶ refreshCorsOrigins() → JerseyConfiguration.setParticipantOrigins()

ScopeExtension
    └─ @Inject ─▶ PolicyEngine, TrustedIssuerRegistry
    └─ reads ─▶ Config("edc.policy.catalog.scope")
    └─ creates ─▶ ScopeMappingFunction(Set<String> defaultScopes)
    └─ registers ─▶ PolicyEngine.registerPostValidator(RequestCatalogPolicyContext)

ScopeMappingFunction
    └─ apply() ─▶ merges defaultScopes ∪ existingScopes in RequestCatalogPolicyContext
```

## Database Schema

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

`created_at` and `updated_at` are stored as Unix millisecond timestamps (BIGINT).

## Critical Implementation Paths

### Crawler target resolution path
```
EDC Federated Catalog crawler scheduled execution
  → crawler calls TargetNodeDirectory.getAll()
  → DbCatalogNodeDirectory.getAll() called
  → ParticipantStore.findAll() queries SELECT * FROM edc_participant
  → Each Participant row converted to TargetNode(name, id, endpoint, protocols)
  → Crawler iterates TargetNodes, contacts each endpoint/DSP catalog API
  → Responses aggregated into the local federated catalog
```

### Participant creation path
```
POST /participants/ with { id, name, dspUrl, supportedProtocols }
  → ParticipantValidator.validate(dto) checks required fields
  → ParticipantStore.findById(id) checks for duplicate
  → Participant entity built with createdAt = updatedAt = now()
  → ParticipantStore.save(participant) → INSERT INTO edc_participant
  → refreshCorsOrigins() → OriginsLoader reads all participants → extracts origins
  → JerseyConfiguration.setParticipantOrigins(origins)
  → 201 Created returned with ParticipantDto
```

### Scope injection path
```
Federator sends outgoing catalog query to a participant connector
  → EDC creates RequestCatalogPolicyContext
  → PolicyEngine runs registered post-validators
  → ScopeMappingFunction.apply(policy, context)
  → Merges {"org.eclipse.edc.vc.type:MembershipCredential:read"} ∪ existing scopes
  → Updated RequestScope sent to IdentityHub STS token endpoint
  → IdentityHub returns VP token containing MembershipCredential
  → Participant connector validates the VP and accepts the catalog request
```

## API Response Envelope

All participant API responses are wrapped in an `ApiResponse<T>` envelope:

```json
{
  "success": true,
  "data": { ... },
  "message": "Participant created successfully"
}
```

Error responses follow the same envelope:

```json
{
  "success": false,
  "error": {
    "code": "VALIDATION_ERROR",
    "message": "id must not be blank; endpoint must be a valid URL"
  }
}
