# memory-bank/projectBrief.md

# Project Brief: edc-federated-catalog

## Overview

Custom Eclipse Dataspace Connector (EDC) Federated Catalog implementation for the **Dataspace4Health (DS4H)** project, developed and maintained by NTT Data.

The repository turns the standard EDC Federated Catalog runtime into a fully operational **federator** instance. It replaces static file-based participant discovery with a database-backed participant registry, adds a REST API for managing that registry at runtime, and enriches outgoing catalog request policy contexts with the configured access scopes.

## Repository

- **Group**: `com.nttdata.dataspace`
- **Package root**: `com.nttdata.dataspace.edc.fc` (catalog/participant modules), `com.nttdata.dataspace.fc` (scope module)
- **Build tool**: Gradle 8.8 with Shadow plugin
- **EDC version**: 0.15.1

## Core Goals

1. Replace the static `participants.json` file with a database-backed `ParticipantStore` so participant connectors can be managed at runtime without rebuilding or redeploying configuration.
2. Expose a REST CRUD API (`/participants/`) on the `catalog` web context so operators can add, update, and remove connector participants without direct database access.
3. Publish Swagger UI alongside the participant API so the registry can be explored and tested without reading the source code.
4. Inject configured catalog scope (`edc.policy.catalog.scope`) into outgoing catalog request policy contexts before policy evaluation so the IdentityHub returns the correct credentials.
5. Provide dynamic CORS origin management — origins are refreshed from the participant store on every create/update/delete operation.
6. Package the runtime and all extensions as a single deployable fat JAR inside a secure, non-root Docker container.

## Modules

| Module | Path | Description |
|---|---|---|
| `catalog:directory` | `extensions/catalog/directory` | Wires the EDC crawler to the database-backed participant registry via `DbCatalogNodeDirectory` |
| `catalog:participant` | `extensions/catalog/participant` | REST CRUD API and Swagger controller for participant management |
| `common:http:client` | `extensions/common/http/client` | Configurable HTTP client provider |
| `common:http:jersey-core` | `extensions/common/http/jersey-core` | Jersey REST server with CORS filter and participant-origin management |
| `scope` | `extensions/scope` | Post-validator that merges the configured default scope into catalog request policy contexts |
| `store:sql:participant-sql` | `extensions/store/sql/participant-sql` | SQL/PostgreSQL persistence for the `ParticipantStore` |

## Scope Boundaries

- This repository is **federator / catalog aggregator only**. It does not act as a provider or consumer connector.
- No custom data transfer or contract negotiation logic — those are delegated to `edc-controlplane` and `edc-dataplane`.
- Credential issuance and DID resolution are delegated to `edc-identityhub`.
- Participant connector endpoints are discovered by crawling the DSP catalog API of each registered participant.
