# Introduction 
This is the code base of Federated Catalog extensions which uses the Federated Catalog(an EDC component) as dependency.

# Getting Started

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

Participants can be added, updated, or removed using SQL commands or a management API. The `DatabaseCatalogNodeDirectory` automatically loads all participants from the database on startup.

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

# Federated Catalogue Database Integration - Implementation Summary

## Overview
Successfully updated the Federated Catalogue to load participants (connectors) directly from a database instead of using a static `participants.json` configuration file.

## Changes Made

### 1. FederatedCatalogNodeResolverExtension
**File:** `extensions/catalog-node-resolver/src/main/java/org/eclipse/edc/extensions/federatedcatalog/noderesolver/FederatedCatalogNodeResolverExtension.java`

- **Removed:** All file-based configuration settings and logic
- **Removed:** Migration on startup functionality
- **Removed:** Backward compatibility code for `participants.json`
- **Simplified:** Extension now only supports database-backed participant loading
- **Implementation:** Uses `ParticipantStore` to retrieve participants from the database

### 2. DatabaseCatalogNodeDirectory
**File:** `extensions/catalog-node-resolver/src/main/java/org/eclipse/edc/extensions/federatedcatalog/noderesolver/DatabaseCatalogNodeDirectory.java`

- **Purpose:** Implements `TargetNodeDirectory` interface for database-backed participant loading
- **Features:**
  - `getAll()`: Retrieves all participants from the database
  - `insert()`: Adds a new participant to the database
  - `remove()`: Deletes a participant from the database
  - Conversion methods between `Participant` (database entity) and `TargetNode` (crawler entity)

### 3. Build Configuration
**File:** `extensions/catalog-node-resolver/build.gradle.kts`

- **Added:** Dependency on `participant-store-sql` module
- **Purpose:** Provides access to `ParticipantStore` and `Participant` classes

**File:** `extensions/store/sql/participant-store-sql/build.gradle.kts`

- **Removed:** Circular dependency on `catalog-node-resolver`
- **Purpose:** Clean separation of concerns

### 4. Documentation
**File:** `README.md`

- **Added:** Section on participant management
- **Included:** Database schema information
- **Provided:** SQL example for adding participants

### 5. Files Removed
- `ParticipantMigrationService.java` - No longer needed

## How It Works

1. **Initialization**: On startup, `FederatedCatalogNodeResolverExtension` initializes with database-backed loading
2. **Provider Method**: Creates `DatabaseCatalogNodeDirectory` with injected `ParticipantStore`
3. **Participant Loading**: `DatabaseCatalogNodeDirectory.getAll()` queries the database for all participants
4. **Crawling**: The federated catalog crawler uses the loaded participants to crawl connectors

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

The implementation was successfully built and tested:
- ✅ Compilation successful
- ✅ No circular dependencies
- ✅ Docker image built successfully
- ✅ Docker image pushed to registry

## Benefits

1. **Scalability**: Database can handle many more participants than a JSON file
2. **Manageability**: Easy to add/remove/update participants via SQL or API
3. **Consistency**: Centralized storage with transaction support
4. **Performance**: Database queries optimized for fast retrieval
5. **Simplicity**: No more configuration file management

## Migration from participants.json

If you have an existing `participants.json` file, you can migrate participants manually using SQL INSERT statements or through the Participant Registry API (if implemented).

## Next Steps

1. Populate the database with initial participants
2. Configure database connection in the federated catalog runtime
3. Deploy the updated federated catalog
4. Monitor logs to ensure participants are loaded correctly
