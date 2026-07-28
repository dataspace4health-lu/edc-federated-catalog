# API Documentation: edc-federated-catalog

## Overview

The `edc-federated-catalog` exposes two categories of HTTP API:

1. **Participant Registry API** — CRUD management of the connector participant list used by the catalog crawler.
2. **Federated Catalog API** — Standard EDC endpoint for querying the aggregated catalog (provided by the upstream `edc:federated-catalog-api` module).

Additionally, a **Swagger UI** is available for interactive API exploration.

---

## Base URLs

All URLs are relative to the configured `web.http.catalog.path`. The default is `/api/catalog`.

| Context | Default Base Path | Description |
|---|---|---|
| Catalog (management) | `http://host:{catalog.port}/api/catalog` | Participant Registry API + Swagger |
| Default (observability) | `http://host:{default.port}/api` | Health check |

---

## Authentication

> The Participant Registry API does not currently enforce authentication on its own. It is expected to be deployed behind a network boundary (Kubernetes ingress with mTLS, or VPN) that restricts access to operators only. Do **not** expose the participant registry port to the public internet.

---

## Participant Registry API

Base path: `{web.http.catalog.path}/participants`

### Data Model

#### `ParticipantDto`

```json
{
  "id": "string",               // required — unique identifier for the participant connector
  "name": "string",             // required — human-readable display name
  "dspUrl": "string",           // required — DSP catalog endpoint URL (e.g. https://connector.example.com/api/v1/dsp)
  "supportedProtocols": [       // optional — defaults to ["dataspace-protocol-http"]
    "dataspace-protocol-http"
  ]
}
```

#### `ApiResponse<T>`

All responses are wrapped in this envelope:

```json
{
  "success": true,
  "data": { ... },       // present on success
  "message": "string"    // present on success with a description
}
```

```json
{
  "success": false,
  "error": {
    "code": "string",    // error code (see Error Codes)
    "message": "string"  // human-readable error detail
  }
}
```

---

### Endpoints

---

#### `POST /participants/`

Creates a new participant in the registry.

**Request Body**: `ParticipantDto` (JSON)

**Responses**:

| Status | Condition | Body |
|---|---|---|
| `201 Created` | Participant created successfully | `ApiResponse<ParticipantDto>` |
| `400 Bad Request` | Validation error (missing/invalid fields) | `ApiResponse` with `VALIDATION_ERROR` |
| `409 Conflict` | Participant with the given `id` already exists | `ApiResponse` with `PARTICIPANT_EXISTS` |
| `500 Internal Server Error` | Persistence failure | `ApiResponse` with `CREATE_FAILED` |

**Example request**:
```bash
curl -X POST http://localhost:8082/api/catalog/participants/ \
  -H "Content-Type: application/json" \
  -d '{
    "id": "connector-ds4h-01",
    "name": "DS4H Hospital Connector",
    "dspUrl": "https://hospital.ds4h.example.com/api/v1/dsp",
    "supportedProtocols": ["dataspace-protocol-http"]
  }'
```

**Example response** (`201`):
```json
{
  "success": true,
  "data": {
    "id": "connector-ds4h-01",
    "name": "DS4H Hospital Connector",
    "dspUrl": "https://hospital.ds4h.example.com/api/v1/dsp",
    "supportedProtocols": ["dataspace-protocol-http"]
  },
  "message": "Participant created successfully"
}
```

---

#### `GET /participants/`

Returns all registered participants.

**Responses**:

| Status | Condition | Body |
|---|---|---|
| `200 OK` | Success | `ApiResponse<List<ParticipantDto>>` |
| `500 Internal Server Error` | Query failure | `ApiResponse` with `LIST_FAILED` |

**Example request**:
```bash
curl http://localhost:8082/api/catalog/participants/
```

**Example response** (`200`):
```json
{
  "success": true,
  "data": [
    {
      "id": "connector-ds4h-01",
      "name": "DS4H Hospital Connector",
      "dspUrl": "https://hospital.ds4h.example.com/api/v1/dsp",
      "supportedProtocols": ["dataspace-protocol-http"]
    }
  ]
}
```

---

#### `GET /participants/{id}`

Returns a single participant by ID.

**Path Parameters**:

| Parameter | Type | Description |
|---|---|---|
| `id` | `string` | The unique participant identifier |

**Responses**:

| Status | Condition | Body |
|---|---|---|
| `200 OK` | Participant found | `ApiResponse<ParticipantDto>` |
| `400 Bad Request` | Invalid (blank) ID | `ApiResponse` with `INVALID_ID` |
| `404 Not Found` | No participant with this ID | `ApiResponse` with `NOT_FOUND` |

**Example request**:
```bash
curl http://localhost:8082/api/catalog/participants/connector-ds4h-01
```

---

#### `PUT /participants/{id}`

Updates an existing participant. The `id` in the path must match the `id` in the request body.

**Path Parameters**:

| Parameter | Type | Description |
|---|---|---|
| `id` | `string` | The unique participant identifier |

**Request Body**: `ParticipantDto` (JSON) — `id` field must match the path parameter.

**Responses**:

| Status | Condition | Body |
|---|---|---|
| `200 OK` | Participant updated | `ApiResponse<ParticipantDto>` |
| `400 Bad Request` | Validation error or path/body ID mismatch | `ApiResponse` with `VALIDATION_ERROR` or `ID_MISMATCH` |
| `404 Not Found` | No participant with this ID | `ApiResponse` with `NOT_FOUND` |
| `500 Internal Server Error` | Persistence failure | `ApiResponse` with `UPDATE_FAILED` |

**Example request**:
```bash
curl -X PUT http://localhost:8082/api/catalog/participants/connector-ds4h-01 \
  -H "Content-Type: application/json" \
  -d '{
    "id": "connector-ds4h-01",
    "name": "DS4H Hospital Connector (updated)",
    "dspUrl": "https://hospital-v2.ds4h.example.com/api/v1/dsp",
    "supportedProtocols": ["dataspace-protocol-http"]
  }'
```

---

#### `DELETE /participants/{id}`

Removes a participant from the registry.

**Path Parameters**:

| Parameter | Type | Description |
|---|---|---|
| `id` | `string` | The unique participant identifier |

**Responses**:

| Status | Condition | Body |
|---|---|---|
| `200 OK` | Participant deleted | — |
| `400 Bad Request` | Invalid (blank) ID | `ApiResponse` with `INVALID_ID` |
| `404 Not Found` | No participant with this ID | `ApiResponse` with `NOT_FOUND` |
| `500 Internal Server Error` | Persistence failure | `ApiResponse` with `DELETE_FAILED` |

**Example request**:
```bash
curl -X DELETE http://localhost:8082/api/catalog/participants/connector-ds4h-01
```

---

## Error Codes

| Code | HTTP Status | Description |
|---|---|---|
| `VALIDATION_ERROR` | 400 | One or more required fields are missing or invalid |
| `INVALID_ID` | 400 | The provided participant ID is blank or malformed |
| `ID_MISMATCH` | 400 | Path `id` does not match request body `id` on PUT |
| `PARTICIPANT_EXISTS` | 409 | A participant with the same `id` already exists (POST only) |
| `NOT_FOUND` | 404 | No participant found for the given ID |
| `CREATE_FAILED` | 500 | Database error during participant creation |
| `UPDATE_FAILED` | 500 | Database error during participant update |
| `DELETE_FAILED` | 500 | Database error during participant deletion |
| `LIST_FAILED` | 500 | Database error during participant list retrieval |

---

## Swagger UI

The Swagger UI is available at:

```
http://host:{catalog.port}{web.http.catalog.path}/swagger/
```

Default: `http://localhost:8082/api/catalog/swagger/`

The Swagger UI provides interactive documentation and a try-it-out interface for all Participant Registry API endpoints.

---

## Federated Catalog Query API

The standard EDC Federated Catalog query endpoint is provided by the upstream `edc:federated-catalog-api` module. It allows consumers to query the aggregated catalog of all crawled participant connectors.

**Endpoint**: `POST /federatedcatalog`

This endpoint follows the standard EDC Management API conventions. Refer to the [EDC Federated Catalog documentation](https://github.com/eclipse-edc/FederatedCatalog) for the request/response schema.

---

## Health Check API

Provided by the `edc:api-observability` module.

### `GET /api/check/health`

Returns the runtime health status.

**Response** (`200 OK`):
```json
[
  {
    "isHealthy": true,
    "componentName": "...",
    "failure": null
  }
]
```

**Example**:
```bash
curl http://localhost:8181/api/check/health
```

---

## SQL Participant Management (Direct Database Access)

As an alternative to the REST API, participants can be managed directly via SQL. The table name is `edc_participant`.

### Insert a participant
```sql
INSERT INTO edc_participant (participant_id, name, endpoint, supported_protocols, created_at, updated_at)
VALUES (
  'connector-ds4h-01',
  'DS4H Hospital Connector',
  'https://hospital.ds4h.example.com/api/v1/dsp',
  '["dataspace-protocol-http"]',
  EXTRACT(EPOCH FROM NOW()) * 1000,
  EXTRACT(EPOCH FROM NOW()) * 1000
);
```

### Update a participant endpoint
```sql
UPDATE edc_participant
SET endpoint = 'https://hospital-v2.ds4h.example.com/api/v1/dsp',
    updated_at = EXTRACT(EPOCH FROM NOW()) * 1000
WHERE participant_id = 'connector-ds4h-01';
```

### Remove a participant
```sql
DELETE FROM edc_participant WHERE participant_id = 'connector-ds4h-01';
```

> **Note**: Direct SQL changes take effect on the next crawler execution cycle. The CORS allowlist is **not** refreshed by direct SQL changes — use the REST API to trigger a CORS refresh, or restart the federator.
