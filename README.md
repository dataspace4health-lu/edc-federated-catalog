# EDC Federated Catalog

Custom Eclipse Dataspace Connector (EDC) Federated Catalog runtime for the **Dataspace4Health (DS4H)** project, developed and maintained by NTT Data.

This repo extends the EDC Federated Catalog component with a database-backed participant registry, a participant CRUD API, and catalog request scope handling — turning the base EDC crawler into a fully managed DS4H federator instance.

## Documentation

| Document | Description |
|---|---|
| [ARCHITECTURE.md](ARCHITECTURE.md) | System architecture, component design, and data flows |
| [API.md](API.md) | REST API reference for participant registry and catalog query endpoints |
| [DEPLOYMENT.md](DEPLOYMENT.md) | Deployment instructions, environment variables, and Helm configuration |
| [CONTRIBUTING.md](CONTRIBUTING.md) | Development setup, coding standards, and contribution guidelines |
| [CHANGELOG.md](CHANGELOG.md) | Version history and release notes |
| [SECURITY.md](SECURITY.md) | Security policies and vulnerability reporting |
| [TROUBLESHOOTING.md](TROUBLESHOOTING.md) | Common issues and debugging guidance |
| [INFRASTRUCTURE.md](INFRASTRUCTURE.md) | Helm and Kubernetes infrastructure reference |

## Overview

The EDC Federated Catalog runtime acts as a **federator** in the DS4H dataspace. It:

- Crawls participant connectors on a schedule and aggregates their catalogs
- Manages the participant list via a database-backed registry (no static config files)
- Exposes a participant CRUD API for runtime management of the crawl target list
- Enriches catalog request policy contexts with the configured credential scope

## Custom Extensions

| Extension | Description |
|---|---|
| `CatalogNodeResolverExtension` | Wires the crawler to the database-backed `ParticipantStore` via `DbCatalogNodeDirectory` |
| `DbCatalogNodeDirectory` | Reads participants from the database and converts them to crawler target nodes |
| `ParticipantApiExtension` | Exposes CRUD operations for participant management and registers Swagger UI |
| `ParticipantRegistryApiController` | REST controller for participant create, read, update, delete, and list |
| `ScopeExtension` | Injects the configured catalog scope into incoming catalog request policy contexts |

## Quick Start

### Prerequisites

- Java 17
- Gradle 8.x (or use the included `./gradlew` wrapper)
- Docker
- PostgreSQL 14+
- HashiCorp Vault

### Build

```bash
# Build fat JAR
./gradlew shadowJar

# Build and push Docker image
docker build -t localhost:5432/edc-federated-catalog . && docker push localhost:5432/edc-federated-catalog

# Run tests
./gradlew test
```

### Minimum required configuration

```properties
edc.participant.id=did:web:federator.dataspace4health.local
edc.datasource.default.url=jdbc:postgresql://localhost:5432/catalogdb
edc.datasource.default.user=cataloguser
edc.datasource.default.password=<password>
edc.vault.hashicorp.url=http://vault:8200
edc.vault.hashicorp.token=<vault-token>
edc.iam.sts.oauth.token.url=http://edc-identityhub:8185/edc/ih/sts/api/token
```

See [DEPLOYMENT.md](DEPLOYMENT.md) for the full configuration reference.

## API Ports

| Port | Purpose |
|---|---|
| 8181 | Health check (`/api/check/health`) |
| 8082 | Catalog API — participant registry CRUD and catalog query |

## Participant Registry API

The federator exposes a REST API for managing the participant list used by the crawler:

| Method | Path | Description |
|---|---|---|
| `POST` | `/participants/` | Register a new participant connector |
| `GET` | `/participants/` | List all registered participants |
| `GET` | `/participants/{id}` | Get one participant by ID |
| `PUT` | `/participants/{id}` | Update a participant |
| `DELETE` | `/participants/{id}` | Remove a participant |

Swagger UI is available at the catalog path for interactive API exploration.

### Add a participant via SQL

```sql
INSERT INTO edc_participant (participant_id, name, endpoint, supported_protocols, created_at, updated_at)
VALUES ('hospital-01', 'Hospital Connector',
        'https://hospital.ds4h.example.com/api/v1/dsp',
        '["dataspace-protocol-http"]',
        EXTRACT(EPOCH FROM NOW()) * 1000,
        EXTRACT(EPOCH FROM NOW()) * 1000);
```

## Kubernetes Deployment

Use the Helm chart at `helm-edc-federated-catalog`. See [DEPLOYMENT.md](DEPLOYMENT.md) and [INFRASTRUCTURE.md](INFRASTRUCTURE.md) for details.

## Contributing

Author: Aastha Rai

To contribute new extensions, place them under `extensions/src/main/java/org/eclipse/edc/extensions` and register the service in `extensions/src/main/resources/META-INF/services`.
