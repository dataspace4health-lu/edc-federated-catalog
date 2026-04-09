# Introduction 
This is the code base of Federated Catalog extensions which uses the Federated Catalog(an EDC component) as dependency.

# Getting Started

## Federator Features

This repo turns the EDC Federated Catalog runtime into a federator instance with a few concrete platform features on top of the base dependency.

### 1. Catalog crawling and aggregation

The federator crawls the participant connectors that are registered in the participant store and uses them as target nodes for the federated catalog crawler.

- Participant targets are loaded from the database through `ParticipantStore`
- `DbCatalogNodeDirectory` converts stored participants into crawler target nodes
- The crawler uses those target nodes to query remote participant catalog endpoints and aggregate the results

This means the federator does not depend on a static `participants.json` file anymore. The participant list is runtime-managed from the database.

### 2. Scope validation for catalog query requests

The federator also adds request-scope handling for catalog queries.

- `ScopeExtension` registers a post-validator for `RequestCatalogPolicyContext`
- The validator reads the configured catalog scope from `edc.policy.catalog.scope`
- If no value is configured, the default scope is `org.eclipse.edc.vc.type:MembershipCredential:read`
- `ScopeMappingFunction` merges the configured default scope with any scopes already present in the incoming request context

In short, catalog query processing is not only about crawling. The federator also ensures the request policy context contains the expected catalog access scope before policy evaluation proceeds.

### 3. CRUD management for the participant list

This repo provides a dedicated Participant Registry API for managing the participant list used by the crawler.

Supported operations:

- Create a participant
- Get a participant by id
- List all participants
- Update a participant
- Delete a participant

The implementation is provided by `ParticipantRegistryApiController`, backed by `ParticipantStore`.

### 4. Swagger and API discoverability

The participant management API is published together with Swagger metadata so the participant registry can be explored and tested without reading the code first.

- `ParticipantApiExtension` registers the Participant Registry API on the `catalog` web context
- It also registers a Swagger controller for the same base path
- The exact URL depends on `web.http.catalog.path`

### 5. Database-backed participant loading

The federator instance uses the database as the source of truth for participant discovery.

This provides:

- dynamic participant management without rebuilding config files
- persistent participant registry state
- a single source used by both participant CRUD operations and crawler target resolution

## Participant Management

The Federated Catalog loads participants (connectors) directly from a database using the `ParticipantStore`. This replaces the legacy file-based approach (`participants.json`) for better scalability and management.

### Database Schema

Participants are stored in a database table with the following structure:
- `id`: Unique identifier for the participant
- `name`: Display name of the participant
- `endpoint`: The catalog endpoint URL for crawling
- `supported_protocols`: Protocols supported by the connector
- `created_at`: Timestamp when the participant was created
- `updated_at`: Timestamp when the participant was last updated

### Managing Participants

Participants can be added, updated, or removed using SQL commands or the Participant Registry management API. The `DatabaseCatalogNodeDirectory` automatically loads all participants from the database on startup.

The same participant list is then used by the federator for catalog crawling.

### Participant Registry API

When the runtime is exposed with a catalog web path, the federator offers CRUD operations for the participant list through the Participant Registry API.

Available operations:

- `POST /participants/` to create a participant
- `GET /participants/` to list participants
- `GET /participants/{id}` to retrieve one participant
- `PUT /participants/{id}` to update one participant
- `DELETE /participants/{id}` to remove one participant

Swagger UI is also registered on the catalog path, which makes the participant management API easier to inspect.

Example SQL to add a participant:
```sql
INSERT INTO participants (id, name, endpoint, supported_protocols, created_at, updated_at)
VALUES ('participant-1', 'Example Connector', 'http://connector.example.com/api/v1/dsp', '["dataspace-protocol-http"]', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
```

# Build and Test
To build the image run the below command on top level of the repo
    docker build -t localhost:5432/edc-federated-catalog .
    docker push localhost:5432/edc-federated-catalog

# Contribute
Author :  Aastha Rai
To contribute in the project by writing extensions, use the folder extensions/src/main/java/org/eclipse/edc/extensions
The same extension needs to be registered as a service in the extensions/src/main/resources/META-INF/services - check the existing service for reference.

# Current Technical Design

## Overview

The federated catalog runtime in this repo uses a database-backed participant registry as the source of truth for crawling target connectors.

The main technical building blocks are:

- `CatalogNodeResolverExtension` for wiring the crawler to the participant registry
- `DbCatalogNodeDirectory` for exposing stored participants as crawler target nodes
- `ParticipantStore` for persistence access
- `ParticipantRegistryApiController` for participant CRUD operations
- `ScopeExtension` for catalog request scope enrichment before policy evaluation

## Runtime Components

### 1. CatalogNodeResolverExtension

**File:** `extensions/catalog/directory/src/main/java/com/nttdata/dataspace/edc/fc/catalog/directory/CatalogNodeResolverExtension.java`

Purpose:

- Initializes the catalog node resolution layer
- Creates the `TargetNodeDirectory` used by the crawler
- Connects the crawler to `ParticipantStore` through `DbCatalogNodeDirectory`

### 2. DbCatalogNodeDirectory

**File:** `extensions/catalog/directory/src/main/java/com/nttdata/dataspace/edc/fc/catalog/directory/DbCatalogNodeDirectory.java`

Purpose:

- Implements `TargetNodeDirectory`
- Reads participants from the database
- Converts stored participant records into crawler target nodes

Key behavior:

- `getAll()` returns all stored participants for crawling
- conversion logic maps database entities to the target node model used by the federated catalog crawler

### 3. Participant Store SQL modules

**Files:**

- `extensions/store/sql/participant-sql/...`
- `extensions/store/sql/participant-store-sql/...`

Purpose:

- define SQL statements and persistence behavior for participant records
- provide the `ParticipantStore` abstraction used by the runtime and API layers

### 4. Participant Registry API

**Files:**

- `extensions/catalog/participant/src/main/java/com/nttdata/dataspace/edc/fc/participant/api/ParticipantApiExtension.java`
- `extensions/catalog/participant/src/main/java/com/nttdata/dataspace/edc/fc/participant/api/ParticipantRegistryApiController.java`

Purpose:

- expose CRUD operations for participant management
- validate incoming participant payloads
- refresh runtime-origin related data after create, update, and delete operations
- publish Swagger UI for easier API discovery

### 5. Scope handling

**Files:**

- `extensions/scope/src/main/java/com/nttdata/dataspace/fc/ScopeExtension.java`
- `extensions/scope/src/main/java/com/nttdata/dataspace/fc/ScopeMappingFunction.java`

Purpose:

- register catalog-scope handling for `RequestCatalogPolicyContext`
- enrich incoming catalog requests with the configured default scope
- support catalog policy evaluation with the expected access scope set

## How It Works

1. **Initialization**: On startup, `FederatedCatalogNodeResolverExtension` initializes with database-backed loading
2. **Provider Method**: Creates `DatabaseCatalogNodeDirectory` with injected `ParticipantStore`
3. **Participant Loading**: `DatabaseCatalogNodeDirectory.getAll()` queries the database for all participants
4. **Crawling**: The federated catalog crawler uses the loaded participants to crawl connectors
5. **Scope Handling**: `ScopeExtension` enriches catalog request policy contexts with the configured catalog scope
6. **Participant Management**: `ParticipantRegistryApiController` exposes CRUD operations for the same participant dataset used by the crawler

## Database Schema

Participants are stored with the following structure:
```sql
- id: Unique identifier (VARCHAR)
- name: Display name (VARCHAR)
- endpoint: Catalog endpoint URL (VARCHAR)
- supported_protocols: JSON array of supported protocols (TEXT)
- created_at: Timestamp (BIGINT)
- updated_at: Timestamp (BIGINT)
```

## Managing Participants

### Adding a Participant
```sql
INSERT INTO participants (id, name, endpoint, supported_protocols, created_at, updated_at)
VALUES ('participant-1', 'Example Connector', 'http://connector.example.com/api/v1/dsp', 
        '["dataspace-protocol-http"]', EXTRACT(EPOCH FROM NOW()) * 1000, EXTRACT(EPOCH FROM NOW()) * 1000);
```

### Updating a Participant
```sql
UPDATE participants 
SET endpoint = 'http://new-endpoint.example.com/api/v1/dsp', 
    updated_at = EXTRACT(EPOCH FROM NOW()) * 1000
WHERE id = 'participant-1';
```

### Removing a Participant
```sql
DELETE FROM participants WHERE id = 'participant-1';
```

## Testing

Build and runtime verification typically include:

- compilation of the runtime and extensions
- container image build
- deployment validation in the target environment
- participant loading verification through logs or the participant API
- catalog query verification against configured participant connectors

## Benefits

1. **Scalability**: Database can handle many more participants than a JSON file
2. **Manageability**: Easy to add/remove/update participants via SQL or API
3. **Consistency**: Centralized storage with transaction support
4. **Performance**: Database queries optimized for fast retrieval
5. **Simplicity**: No more configuration file management

## Next Steps

1. Populate the database with initial participants
2. Configure database connection in the federated catalog runtime
3. Deploy the updated federated catalog
4. Monitor logs to ensure participants are loaded correctly
