# memory-bank/activeContext.md

# Active Context: edc-federated-catalog

## Current Work Focus

The edc-federated-catalog repository is at a stable, functional state. All core features are implemented and operational:

1. Database-backed participant registry replacing the static `participants.json` approach.
2. REST CRUD API for participant management with Swagger UI.
3. Scope injection for catalog request policy contexts.
4. Dynamic CORS origin refresh tied to participant registry mutations.
5. Security-patched dependency versions (jackson-core 2.21.1, Jetty 12.1.7).
6. Non-root Docker container with pebble binary removed.

The current focus is on **documentation** — generating the memory bank and standard documentation files to establish persistent knowledge for future development sessions.

## Recent Changes

- **Dockerfile hardened**: `pebble` binary removed from base image to eliminate 8 Go stdlib CVEs (CVE-2026-33811, CVE-2026-33814, CVE-2026-39820, CVE-2026-39823, CVE-2026-39825, CVE-2026-39836, CVE-2026-42499, CVE-2026-42504). APT packages upgraded to pull in latest security patches.
- **Jetty pinned to 12.1.7**: Fixes CVE-2026-1605 (GzipHandler resource leak) and CVE-2026-2332 (HTTP/1.1 request smuggling via chunk extensions).
- **jackson-core pinned to 2.21.1**: Fixes GHSA-72hv-8253-57qq (number length constraint bypass in async parser).
- **`ScopeExtension` registered only for `RequestCatalogPolicyContext`**: Earlier versions also registered for contract negotiation and transfer; this was narrowed to catalog-only scope injection.
- **Dynamic CORS**: `refreshCorsOrigins()` introduced in `ParticipantRegistryApiController` to keep the CORS allowlist consistent with the participant registry without requiring a restart.

## Active Decisions and Considerations

### Crawl interval
The EDC federated catalog crawler runs on a configurable schedule. For production, the crawl interval should be tuned via `edc.fc.crawler.execution.delay.seconds` to balance freshness against load on participant connectors.

### Participant ID uniqueness
Participant IDs must be unique across the registry. The `createParticipant` endpoint returns `409 Conflict` if an attempt is made to register an already-existing ID. Operators should use stable, meaningful IDs (e.g., the participant's BPN or DID).

### Supported protocols
When registering participants, the `supportedProtocols` field should include `"dataspace-protocol-http"` for DSP-compliant connectors. The federator supports both DSP 2025/1 and DSP 08 protocol adapters.

### Vault vs environment variables
Sensitive configuration (DB password, Vault token, private keys) must be stored in HashiCorp Vault. Plain-text environment variables should not be used for secrets in production.

### Jersey-core exclusion
The `edc:http` dependency is included with `jersey-core` explicitly excluded (`exclude group: "org.eclipse.edc", module: "jersey-core"`). This is intentional — the custom `jersey-core` extension provides CORS handling and participant-origin awareness that the standard EDC module does not.

## Next Steps

1. **Integration test coverage**: Add Testcontainers-based integration tests for `ParticipantRegistryApiController` against a real PostgreSQL instance.
2. **OpenAPI spec generation**: Configure Swagger/OpenAPI spec generation in the Gradle build to produce a static `openapi.json` alongside the jar.
3. **Crawl result API**: Consider exposing an endpoint to query the aggregated catalog held by the federator (not just participant management).
4. **Metrics / observability**: Extend the health endpoint to report participant count, last crawl time, and crawl success/failure per participant.
5. **EDC upgrade path**: EDC 0.15.1 is the current baseline. Monitor EDC release notes for the next stable version; plan coordinated upgrade across all repos (edc-controlplane, edc-dataplane, edc-identityhub, edc-federated-catalog).

## Important Patterns and Preferences

- **Monitor over SLF4J**: Use `Monitor.debug()`, `Monitor.info()`, `Monitor.warning()`, `Monitor.severe()` — never import `java.util.logging` or SLF4J directly in extension code.
- **`@Inject` over constructors for EDC services**: EDC-managed services (Monitor, WebService, PolicyEngine, etc.) are injected via `@Inject`; user-created collaborators are wired in `initialize()` or `@Provider` methods.
- **`ApiResponse<T>` envelope**: All participant API responses must use the `ApiResponse.success()` / `ApiResponse.error()` factory methods for consistency.
- **`Result<T>` return types from store**: `ParticipantStore` returns `ServiceResult<T>`; always check `.succeeded()` / `.failed()` before using `.getContent()`.
