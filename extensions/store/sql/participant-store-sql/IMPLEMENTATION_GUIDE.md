# SQL Participant Store Implementation

## Overview
This implementation follows EDC (Eclipse Dataspace Connector) best practices for SQL-based stores, using the same patterns as the Connector's store implementations.

## Architecture

### Key Components

1. **ParticipantStore (Interface)** - `/src/main/java/.../ParticipantStore.java`
   - Defines the contract for participant storage operations
   - Uses EDC's `StoreResult` pattern for error handling
   - Returns `Stream` for query results (following EDC patterns)

2. **SqlParticipantStore** - `/src/main/java/.../SqlParticipantStore.java`
   - Extends `AbstractSqlStore` from EDC
   - Implements CRUD operations with proper transaction management
   - Uses `QueryExecutor` for safe SQL execution
   - Handles connection management via `DataSourceRegistry`

3. **ParticipantStatements (Interface)** - `/src/main/java/.../schema/ParticipantStatements.java`
   - Defines SQL statement templates
   - Allows for database-specific implementations

4. **PostgresDialectStatements** - `/src/main/java/.../schema/postgres/PostgresDialectStatements.java`
   - PostgreSQL-specific SQL implementations
   - Can be extended for other databases (MySQL, H2, etc.)

5. **ParticipantStoreExtension** - `/src/main/java/.../ParticipantStoreExtension.java`
   - ServiceExtension that initializes the store
   - Registers the schema bootstrapper
   - Injects required EDC services

## Database Schema

Table: `edc_participant`

| Column | Type | Description |
|--------|------|-------------|
| participant_id | VARCHAR(255) | Primary key |
| name | VARCHAR(255) | Participant name |
| endpoint | VARCHAR(500) | Participant endpoint URL |
| supported_protocols | TEXT | Comma-separated protocols |
| created_at | BIGINT | Creation timestamp |
| updated_at | BIGINT | Last update timestamp |

Indexes:
- `idx_participant_name` on `name`
- `idx_participant_endpoint` on `endpoint`

## Configuration

The store uses EDC's standard datasource configuration:

```properties
# Default datasource (automatically used)
edc.datasource.default.url=${EDC_DATASOURCE_DEFAULT_URL}
edc.datasource.default.user=${EDC_DATASOURCE_DEFAULT_USER}
edc.datasource.default.password=${EDC_DATASOURCE_DEFAULT_PASSWORD}

# Optional: Use a different datasource for participants
edc.sql.store.participant.datasource=participant
```

## Key Benefits

1. **Automatic Schema Management**: Schema is created/updated on startup via `SqlSchemaBootstrapper`
2. **Transaction Safety**: All operations wrapped in transactions via `TransactionContext`
3. **Connection Pooling**: Handled by `DataSourceRegistry`
4. **Consistent Error Handling**: Uses EDC's `StoreResult` pattern
5. **Database Portability**: Easy to add support for other databases
6. **Testability**: Can inject test implementations easily

## Usage Example

```java
@Inject
private ParticipantStore participantStore;

// Save a participant
var participant = Participant.builder()
    .id("participant-1")
    .name("Test Participant")
    .endpoint("https://example.com/dsp")
    .supportedProtocols(List.of("dataspace-protocol-http"))
    .build();

StoreResult<Void> result = participantStore.save(participant);
if (result.succeeded()) {
    // Success
} else {
    // Handle error: result.getFailureDetail()
}

// Find all participants
Stream<Participant> participants = participantStore.findAll();

// Find by ID
Participant found = participantStore.findById("participant-1");

// Update
participant.setName("Updated Name");
StoreResult<Void> updateResult = participantStore.update(participant);

// Delete
StoreResult<Participant> deleteResult = participantStore.delete("participant-1");
```

## Dependencies

```kotlin
dependencies {
    implementation(libs.edc.core.spi)
    implementation(libs.edc.sql.core)
    implementation(libs.edc.sql.bootstrapper)
    implementation(libs.edc.spi.transaction.datasource)
    implementation(libs.postgres)
}
```

## Files Created/Modified

### New Files:
- `src/main/resources/participant-schema.sql` - Database schema
- `src/main/java/.../SqlParticipantStore.java` - Store implementation
- `src/main/java/.../schema/ParticipantStatements.java` - SQL statements interface
- `src/main/java/.../schema/postgres/PostgresDialectStatements.java` - PostgreSQL implementation

### Modified Files:
- `ParticipantStore.java` - Updated interface with StoreResult pattern
- `ParticipantStoreExtension.java` - Rewritten to follow EDC patterns
- `build.gradle.kts` - Updated dependencies

### Deleted Files:
- `DatabaseConnectionManager.java` - No longer needed (replaced by DataSourceRegistry)
- `SimpleParticipantStore.java` - Replaced by SqlParticipantStore

## Next Steps

To use this in your federated catalog:

1. **Ensure datasource is configured** in your launcher's `config.properties`:
   ```properties
   edc.datasource.default.url=jdbc:postgresql://localhost:5432/your_db
   edc.datasource.default.user=your_user
   edc.datasource.default.password=your_password
   ```

2. **Include required EDC modules** in your launcher's `build.gradle.kts`:
   ```kotlin
   implementation(libs.edc.sql.pool)  // Connection pooling
   implementation(libs.edc.sql.transactionlocal)  // Transaction management
   ```

3. **The table will be created automatically** when the extension initializes

4. **Inject and use** the ParticipantStore in your extensions
