# memory-bank/techContext.md

# Tech Context: edc-federated-catalog

## Technologies Used

### Core Runtime
| Technology | Version | Purpose |
|---|---|---|
| Java | 17 (JDK for build, JRE for runtime) | Application language |
| Gradle | 8.8 | Build tool |
| Eclipse EDC | 0.15.1 | Federated Catalog core, crawler SPI, policy engine, DSP, DCP |
| Shadow Plugin | 8.3.8 | Fat JAR assembly (`com.gradleup.shadow`) |

### EDC Modules Used
| Module | Purpose |
|---|---|
| `edc:runtime-core` | BaseRuntime entry point |
| `edc:federated-catalog-core` | Core crawler and catalog aggregation |
| `edc:federated-catalog-core-2025` | DSP 2025/1 protocol adapter for crawling |
| `edc:federated-catalog-core-08` | DSP 08 protocol adapter for crawling |
| `edc:federated-catalog-api` | Federated catalog query API |
| `edc:connector-core` | Core EDC connector services |
| `edc:control-plane-core` | Control plane policy engine |
| `edc:dsp` | Dataspace Protocol (DSP) support |
| `edc:decentralized-claims-core` | DCP / Verifiable Credential support |
| `edc:identity-did-core` | DID resolution |
| `edc:oauth2-client` | OAuth2 client for identity |
| `edc:json-ld-lib` | JSON-LD processing |
| `edc:vault-hashicorp` | HashiCorp Vault secrets provider |
| `edc:sql-core`, `edc:sql-pool-apache-commons`, `edc:transaction-local` | SQL infrastructure |
| `edc:sql-bootstrapper` | Automatic schema bootstrapping at startup |
| `edc:configuration-filesystem` | `.properties` file configuration |
| `edc:api-observability` | `/api/check/health` health endpoint |

### Persistence
| Technology | Version | Purpose |
|---|---|---|
| PostgreSQL | 42.7.3 (JDBC driver) | Participant registry persistence |
| Apache Commons DBCP2 | (via `edc:sql-pool-apache-commons`) | JDBC connection pool |

### HTTP / REST
| Technology | Version | Purpose |
|---|---|---|
| Jersey | 3.1.11 | JAX-RS REST server (custom `jersey-core` extension) |
| Jakarta RS API | 4.0.0 | JAX-RS annotations |
| Swagger Annotations (Jakarta) | 2.2.20 | OpenAPI documentation annotations |

### Security / Identity
| Technology | Purpose |
|---|---|
| HashiCorp Vault | Secrets management (private keys, tokens) |
| DCP (Dataspace Credential Protocol) | VC-based identity for inter-connector communication |
| Verifiable Credentials (VC) | MembershipCredential scope validation |

### Container
| Technology | Version | Purpose |
|---|---|---|
| eclipse-temurin | 17-jre | Minimal JRE base image |
| Docker multi-stage build | — | gradle:8.8-jdk17 builder → temurin:17-jre runtime |

### Testing
| Technology | Version | Purpose |
|---|---|---|
| JUnit Jupiter | 5.10.1 | Unit test framework |
| Mockito | 5.8.0 | Mocking |
| AssertJ | 3.24.2 | Fluent assertions |
| Testcontainers | 1.21.3 | Integration tests with real PostgreSQL |
| OkHttp MockWebServer | 5.1.0 | HTTP mock for client tests |
| RestAssured | 5.5.5 | REST API integration tests |
| JaCoCo | 0.8.10 | Code coverage |

## Development Setup

### Prerequisites
- JDK 17
- Docker (for building the image and running integration tests)
- A running PostgreSQL instance (or use Docker Compose / Testcontainers)
- A running HashiCorp Vault instance (or use the Helm chart's embedded Vault)

### Build Commands
```bash
# Build the fat JAR (skips tests)
./gradlew shadowJar --no-daemon

# Run all unit tests with coverage
./gradlew test jacocoTestReport --no-daemon

# Run all tests across all subprojects with aggregated HTML report
./gradlew allTests --no-daemon

# Build the Docker image
docker build -t localhost:5432/edc-federated-catalog .

# Push to local registry
docker push localhost:5432/edc-federated-catalog
```

### Key Configuration Properties

The federator is configured via a `.properties` file loaded at startup by `edc:configuration-filesystem`.

| Property | Default | Description |
|---|---|---|
| `edc.policy.catalog.scope` | `org.eclipse.edc.vc.type:MembershipCredential:read` | Scope injected into outgoing catalog requests |
| `web.http.catalog.path` | `/api/catalog` | Base path for the participant registry API and Swagger UI |
| `edc.datasource.default.url` | — | JDBC URL for PostgreSQL |
| `edc.datasource.default.user` | — | PostgreSQL username |
| `edc.datasource.default.password` | — | PostgreSQL password (from Vault) |
| `edc.vault.hashicorp.url` | — | HashiCorp Vault URL |
| `edc.vault.hashicorp.token` | — | Vault token (from Kubernetes secret) |
| `edc.fc.crawler.execution.delay.seconds` | EDC default | Crawl interval in seconds |
| `edc.participant.id` | — | Federator's own participant ID in the dataspace |
| `edc.iam.sts.oauth.token.url` | — | Identity Hub STS token endpoint |

## Technical Constraints

1. **EDC 0.15.1 dependency pinning** — The entire EDC ecosystem is locked to version 0.15.1. Upgrading any single module requires coordinated upgrade of the entire dependency set.

2. **jackson-core pinned to 2.21.1** — Security fix for GHSA-72hv-8253-57qq. Only `jackson-core` received the 2.21.1 patch; `jackson-annotations` and `jackson-databind` have no 2.21.1 release and remain at BOM-resolved versions.

3. **Jetty pinned to 12.1.7** — Security fix for CVE-2026-1605 (GzipHandler resource leak) and CVE-2026-2332 (HTTP/1.1 request smuggling).

4. **PostgreSQL only** — The `participant-schema.sql` and SQL store implementation are tested with and designed for PostgreSQL. Other databases are not supported without schema and query adaptation.

5. **Single participant context** — `edc:participant-context-single-core` is used; multi-tenancy is not supported.

6. **jersey-core excluded from `edc:http`** — The standard EDC `jersey-core` module is excluded from the `edc:http` dependency because this repository provides its own `jersey-core` extension with custom CORS and configuration behaviour.

## Tool Usage Patterns

- **Secrets**: All sensitive values (DB passwords, Vault tokens, private keys) are stored in HashiCorp Vault. The `edc:vault-hashicorp` extension resolves vault-backed config properties at runtime.
- **Schema bootstrap**: `edc:sql-bootstrapper` automatically runs `participant-schema.sql` on startup if the `edc_participant` table does not already exist.
- **Logging**: EDC's built-in `Monitor` interface is used throughout (not SLF4J/Logback directly). Log level is controlled by the `LOG_LEVEL` environment variable passed to the container.
- **Helm deployment**: The federator is deployed via the `helm-edc-federated-catalog` Helm chart, which manages PostgreSQL, Vault integration, and federator configuration as a single deployable unit.
