# SQL Participant Store Implementation

## Overview
This implementation provides a SQL-based participant store compatible with EDC 0.13.0, using direct JDBC connections for schema initialization while maintaining EDC's transaction management for operations.

## Architecture

### Key Components

1. **ParticipantStore (Interface)** - `/src/main/java/.../ParticipantStore.java`
   - Defines the contract for participant storage operations
   - Uses EDC's `StoreResult` pattern for error handling
   - Returns `Stream` for query results

2. **SqlParticipantStore** - `/src/main/java/.../SqlParticipantStore.java`
   - Implements CRUD operations with transaction support
   - Uses `DataSourceRegistry` for connection management
   - Includes proper error handling and logging

3. **ParticipantStatements (Interface)** - `/src/main/java/.../schema/ParticipantStatements.java`
   - Defines SQL statement templates
   - Allows for database-specific implementations

4. **PostgresDialectStatements** - `/src/main/java/.../schema/postgres/PostgresDialectStatements.java`
   - PostgreSQL-specific SQL implementations
   - Can be extended for other databases

5. **ParticipantStoreExtension** - `/src/main/java/.../ParticipantStoreExtension.java`
   - ServiceExtension that initializes the store
   - Uses direct JDBC for schema initialization
   - Handles configuration and monitoring

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

The store uses standard JDBC configuration via EDC properties:

```properties
# Database Connection
edc.datasource.default.url=jdbc:postgresql://localhost:5432/your_db
edc.datasource.default.user=your_user
edc.datasource.default.password=your_password
```

## Key Benefits

1. **Simple Schema Management**: Direct JDBC initialization ensures reliable table creation
2. **Transaction Safety**: Operations wrapped in EDC's TransactionContext
3. **Connection Management**: Uses EDC's DataSourceRegistry
4. **Error Handling**: Proper error handling with detailed logging
5. **Database Portability**: Easy to add support for other databases
6. **Monitoring**: Integrated with EDC's monitoring system

## Testing

### Manual Testing API

The extension provides a REST API for testing:

```bash
# Create participant
curl -X POST http://localhost:8181/api/participants \
  -H "Content-Type: application/json" \
  -d '{
    "id": "participant1",
    "name": "Test Participant",
    "endpoint": "http://test-participant:8181",
    "supportedProtocols": ["ids-multipart", "dsp"]
  }'

# Get participant
curl http://localhost:8181/api/participants/participant1

# Get all participants
curl http://localhost:8181/api/participants

# Update participant
curl -X PUT http://localhost:8181/api/participants/participant1 \
  -H "Content-Type: application/json" \
  -d '{
    "id": "participant1",
    "name": "Updated Participant",
    "endpoint": "http://updated-endpoint:8181",
    "supportedProtocols": ["dsp"]
  }'

# Delete participant
curl -X DELETE http://localhost:8181/api/participants/participant1
```

### Database Verification

Connect to PostgreSQL pod:
```bash
kubectl exec -it edc-fc-catalog-postgresql-0 -- bash
psql -U postgres
```

Query the table:
```sql
-- List all participants
SELECT * FROM edc_participant;

-- Check specific participant
SELECT * FROM edc_participant WHERE participant_id = 'participant1';
```

## Dependencies

```kotlin
dependencies {
    implementation(libs.edc.core.spi)
    implementation(libs.edc.transaction.spi)
    implementation(libs.edc.transactions.datasource.spi)
    implementation(libs.postgres)
}
```

## Files Structure

### Core Files:
```
participant-store-sql/
├── src/main/
│   ├── java/.../store/sql/
│   │   ├── Participant.java
│   │   ├── ParticipantStore.java
│   │   ├── SqlParticipantStore.java
│   │   ├── ParticipantStoreExtension.java
│   │   └── schema/
│   │       ├── ParticipantStatements.java
│   │       └── postgres/
│   │           └── PostgresDialectStatements.java
│   └── resources/
│       ├── participant-schema.sql
│       └── META-INF/services/
│           └── org.eclipse.edc.spi.system.ServiceExtension
```

## Next Steps

1. **Deploy the Extension**:
   - Include the extension in your EDC build
   - Configure database connection properties
   - Deploy to your Kubernetes cluster

2. **Verify Installation**:
   - Check logs for successful schema initialization
   - Use the test API to verify CRUD operations
   - Query PostgreSQL to confirm data persistence

3. **Integration**:
   - Inject `ParticipantStore` into your extensions
   - Replace file-based participant storage with database storage
   - Update existing code to use the new store

## Troubleshooting

1. **Table Not Created**:
   - Check logs for schema initialization messages
   - Verify database connection properties
   - Check PostgreSQL logs for errors

2. **Connection Issues**:
   - Verify database credentials
   - Check network connectivity
   - Ensure PostgreSQL pod is running

3. **Transaction Errors**:
   - Check TransactionContext registration
   - Verify DataSourceRegistry configuration
   - Monitor connection pool usage
