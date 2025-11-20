# Participant Registry API Documentation

## Overview
The Participant Registry API provides RESTful endpoints for managing participants in the Federated Catalogue ecosystem. This API enables dynamic CRUD operations on participant entities, storing DSP (Dataspace Protocol) links for each participant.

## Base URL
```
https://edc-federator.dataspace4health.local/edc/fc/catalog/api
```

> **Note:** The API is exposed behind the path prefix `/edc/fc/catalog/api` via Traefik ingress. All requests must use this prefix in the URL.
> TLS is enabled; use `-k` with cURL to skip certificate verification if using self-signed certs.

## Internal Cluster Access
For internal Kubernetes access, use:
```
http://edc-fc-catalog.default.svc.cluster.local:9195/edc/fc/catalog/api
```

## Authentication
Currently, the API does not require authentication. In production, implement proper authentication mechanisms.

---

## Endpoints

### 1. Create Participant
Creates a new participant with DSP URL and metadata.

**Endpoint:** `POST /participants`

**Request Body:**
```json
{
  "id": "urn:connector:participant-001",
  "name": "Acme Corporation Connector",
  "dspUrl": "http://acme-connector.example.com:8080/api/v1/dsp",
  "supportedProtocols": ["dataspace-protocol-http"]
}
```

**Response:**
- **201 Created**
```json
{
  "success": true,
  "data": {
    "id": "urn:connector:participant-001",
    "name": "Acme Corporation Connector",
    "dspUrl": "http://acme-connector.example.com:8080/api/v1/dsp",
    "supportedProtocols": ["dataspace-protocol-http"]
  },
  "message": "Participant created successfully"
}
```

- **400 Bad Request** - Validation errors
- **409 Conflict** - Participant already exists
- **500 Internal Server Error** - Server error

**Example cURL:**
```bash
curl -k -X POST https://edc-federator.dataspace4health.local/edc/fc/catalog/api/participants \
  -H "Content-Type: application/json" \
  -d '{
    "id": "urn:connector:participant-001",
    "name": "Acme Corporation Connector",
    "dspUrl": "http://acme-connector.example.com:8080/api/v1/dsp",
    "supportedProtocols": ["dataspace-protocol-http"]
  }'
```

---

### 2. Get Participant by ID
Retrieves a specific participant by its unique identifier.

**Endpoint:** `GET /participants/{id}`

**Path Parameters:**
- `id` (required): Participant unique identifier

**Response:**
- **200 OK**
```json
{
  "success": true,
  "data": {
    "id": "urn:connector:participant-001",
    "name": "Acme Corporation Connector",
    "dspUrl": "http://acme-connector.example.com:8080/api/v1/dsp",
    "supportedProtocols": ["dataspace-protocol-http"]
  }
}
```

- **404 Not Found** - Participant does not exist
- **500 Internal Server Error** - Server error

**Example cURL:**
```bash
curl -k https://edc-federator.dataspace4health.local/edc/fc/catalog/api/participants/urn:connector:participant-001
```

---

### 3. List All Participants
Retrieves all registered participants.

**Endpoint:** `GET /participants`

**Response:**
- **200 OK**
```json
{
  "success": true,
  "data": [
    {
      "id": "urn:connector:participant-001",
      "name": "Acme Corporation Connector",
      "dspUrl": "http://acme-connector.example.com:8080/api/v1/dsp",
      "supportedProtocols": ["dataspace-protocol-http"]
    },
    {
      "id": "urn:connector:participant-002",
      "name": "TechCorp Connector",
      "dspUrl": "http://techcorp-connector.example.com:8080/api/v1/dsp",
      "supportedProtocols": ["dataspace-protocol-http"]
    }
  ]
}
```

- **500 Internal Server Error** - Server error

**Example cURL:**
```bash
curl -k https://edc-federator.dataspace4health.local/edc/fc/catalog/api/participants
```

---

### 4. Update Participant
Updates an existing participant's information.

**Endpoint:** `PUT /participants/{id}`

**Path Parameters:**
- `id` (required): Participant unique identifier

**Request Body:**
```json
{
  "id": "urn:connector:participant-001",
  "name": "Acme Corporation Connector Updated",
  "dspUrl": "http://new-acme-connector.example.com:8080/api/v1/dsp",
  "supportedProtocols": ["dataspace-protocol-http", "ids-multipart"]
}
```

**Response:**
- **200 OK**
```json
{
  "success": true,
  "data": {
    "id": "urn:connector:participant-001",
    "name": "Acme Corporation Connector Updated",
    "dspUrl": "http://new-acme-connector.example.com:8080/api/v1/dsp",
    "supportedProtocols": ["dataspace-protocol-http", "ids-multipart"]
  },
  "message": "Participant updated successfully"
}
```

- **400 Bad Request** - Validation errors or ID mismatch
- **404 Not Found** - Participant does not exist
- **500 Internal Server Error** - Server error

**Example cURL:**
```bash
curl -k -X PUT https://edc-federator.dataspace4health.local/edc/fc/catalog/api/participants/urn:connector:participant-001 \
  -H "Content-Type: application/json" \
  -d '{
    "id": "urn:connector:participant-001",
    "name": "Acme Corporation Connector Updated",
    "dspUrl": "http://new-acme-connector.example.com:8080/api/v1/dsp",
    "supportedProtocols": ["dataspace-protocol-http", "ids-multipart"]
  }'
```

---

### 5. Delete Participant
Removes a participant from the registry.

**Endpoint:** `DELETE /participants/{id}`

**Path Parameters:**
- `id` (required): Participant unique identifier

**Response:**
- **204 No Content** - Participant deleted successfully
- **404 Not Found** - Participant does not exist
- **500 Internal Server Error** - Server error

**Example cURL:**
```bash
curl -k -X DELETE https://edc-federator.dataspace4health.local/edc/fc/catalog/api/participants/urn:connector:participant-001
```

---

## Data Models

### ParticipantDto
```json
{
  "id": "string (required, max 255 characters)",
  "name": "string (required, max 255 characters)",
  "dspUrl": "string (required, valid URL, max 500 characters)",
  "supportedProtocols": ["string (required, at least one protocol)"]
}
```

### ApiResponse<T>
```json
{
  "success": "boolean",
  "data": "T (nullable)",
  "error": "string (nullable)",
  "message": "string (nullable)"
}
```

---

## Validation Rules

1. **ID**:
   - Required
   - Maximum 255 characters
   - Must be unique

2. **Name**:
   - Required
   - Maximum 255 characters

3. **DSP URL**:
   - Required
   - Must be a valid URL
   - Maximum 500 characters

4. **Supported Protocols**:
   - Required
   - Must contain at least one protocol

---

## Error Codes

| Error Code | Description |
|------------|-------------|
| VALIDATION_ERROR | Input validation failed |
| PARTICIPANT_EXISTS | Participant with this ID already exists |
| NOT_FOUND | Participant not found |
| ID_MISMATCH | Path ID does not match request body ID |
| CREATE_FAILED | Failed to create participant |
| UPDATE_FAILED | Failed to update participant |
| DELETE_FAILED | Failed to delete participant |
| LIST_FAILED | Failed to retrieve participants |
| INVALID_ID | Invalid participant ID |

---

## Testing

### Full CRUD Test Scenario

```bash
# 1. Create a participant
curl -X POST http://localhost:9195/api/participants \
  -H "Content-Type: application/json" \
  -d '{
    "id": "test-participant-1",
    "name": "Test Participant",
    "dspUrl": "http://test-connector:8080/api/v1/dsp",
    "supportedProtocols": ["dataspace-protocol-http"]
  }'

# 2. Get the participant
curl http://localhost:9195/api/participants/test-participant-1

# 3. List all participants
curl http://localhost:9195/api/participants

# 4. Update the participant
curl -X PUT http://localhost:9195/api/participants/test-participant-1 \
  -H "Content-Type: application/json" \
  -d '{
    "id": "test-participant-1",
    "name": "Updated Test Participant",
    "dspUrl": "http://updated-test-connector:8080/api/v1/dsp",
    "supportedProtocols": ["dataspace-protocol-http", "ids-multipart"]
  }'

# 5. Delete the participant
curl -X DELETE http://localhost:9195/api/participants/test-participant-1
```

---

## Database Schema

The API interacts with the `edc_participant` table:

```sql
CREATE TABLE edc_participant (
    participant_id VARCHAR(255) PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    endpoint VARCHAR(500) NOT NULL,  -- Stores the DSP URL
    supported_protocols TEXT,
    created_at BIGINT NOT NULL,
    updated_at BIGINT NOT NULL
);

CREATE INDEX idx_participant_name ON edc_participant(name);
CREATE INDEX idx_participant_endpoint ON edc_participant(endpoint);
```

---

## Integration

To integrate with the Participant Registry API:

1. **Service Discovery**: Use the List Participants endpoint to discover all participants
2. **Dynamic Registration**: Use Create Participant to register new connectors
3. **Health Monitoring**: Periodically check participant availability and update accordingly
4. **Decommissioning**: Use Delete Participant when connectors are removed

---

## Future Enhancements

- [ ] Pagination support for List Participants
- [ ] Search and filtering capabilities
- [ ] Participant health status tracking
- [ ] Authentication and authorization
- [ ] Rate limiting
- [ ] API versioning
- [ ] Webhook notifications for participant changes
