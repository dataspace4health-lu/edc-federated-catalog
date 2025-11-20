# Participant Registry API - Swagger Documentation

## Overview

The Participant Registry API provides comprehensive OpenAPI/Swagger documentation for all CRUD operations on participants in the Federated Catalogue.

## Accessing Swagger UI

### ✅ **RECOMMENDED: Local Swagger UI Server**

The easiest way to view and interact with the API documentation:

```bash
# From the edc-federated-catalog directory
./serve-swagger.sh
```

Then open your browser to: **http://localhost:8080**

This will show you a fully interactive Swagger UI where you can:
- View all endpoints and their documentation
- Test API calls directly from the browser
- See request/response examples
- Try out different parameters

**Note:** Make sure your EDC Federated Catalog application is running on port 8181 for the "Try it out" feature to work.

---

### Option 1: Generate OpenAPI Specification File

Generate the OpenAPI spec file that can be viewed with any Swagger tool:

```bash
# From the edc-federated-catalog directory
./generate-openapi.sh
```

The spec will be generated at: `build/openapi/participant-registry-api.json`

---

### Option 2: Use Online Swagger Editor

1. Generate the OpenAPI spec using the script above
2. Visit [Swagger Editor](https://editor.swagger.io/)
3. Click **File → Import File**
4. Select `build/openapi/participant-registry-api.json`
5. The Swagger UI will render the interactive documentation

---

### Option 3: View the Generated Files

After running `./generate-openapi.sh`, you'll have:
- `build/openapi/participant-registry-api.json` - The OpenAPI 3.0 specification
- `build/openapi/index.html` - A standalone HTML page with embedded Swagger UI

You can open the `index.html` file directly in your browser (though you'll need to serve it via HTTP for the "Try it out" feature to work due to CORS).

---

The Participant Registry API is registered and accessible at:

**Base URL:** `http://localhost:<catalog-port>/api/catalog/participants`

Where `<catalog-port>` is configured by the EDC Management API settings (typically port 8181 or check your configuration).

**Example endpoints:**
- `GET http://localhost:8181/api/catalog/participants` - List all participants
- `POST http://localhost:8181/api/catalog/participants/` - Create a participant
- `GET http://localhost:8181/api/catalog/participants/{id}` - Get a participant
- `PUT http://localhost:8181/api/catalog/participants/{id}` - Update a participant
- `DELETE http://localhost:8181/api/catalog/participants/{id}` - Delete a participant

### Testing with curl

```bash
# List all participants
curl -X GET http://localhost:8181/api/catalog/participants

# Create a participant
curl -X POST http://localhost:8181/api/catalog/participants/ \
  -H "Content-Type: application/json" \
  -d '{
    "id": "participant-001",
    "name": "ACME Corporation",
    "dspUrl": "https://participant.example.com/api/dsp",
    "supportedProtocols": ["dataspace-protocol-http"]
  }'

# Get a specific participant
curl -X GET http://localhost:8181/api/catalog/participants/participant-001

# Update a participant
curl -X PUT http://localhost:8181/api/catalog/participants/participant-001 \
  -H "Content-Type: application/json" \
  -d '{
    "id": "participant-001",
    "name": "ACME Corp Updated",
    "dspUrl": "https://participant.example.com/api/dsp",
    "supportedProtocols": ["dataspace-protocol-http"]
  }'

# Delete a participant
curl -X DELETE http://localhost:8181/api/catalog/participants/participant-001
```

## API Operations

### Create Participant
- **Endpoint**: `POST /participants/`
- **Description**: Create a new participant in the Federated Catalogue
- **Request Body**: ParticipantDto
- **Response Codes**:
  - 201: Participant created successfully
  - 400: Validation error
  - 409: Participant already exists
  - 500: Internal server error

### Get Participant
- **Endpoint**: `GET /participants/{id}`
- **Description**: Retrieve a participant by ID
- **Response Codes**:
  - 200: Success
  - 400: Invalid ID
  - 404: Participant not found

### List Participants
- **Endpoint**: `GET /participants/`
- **Description**: List all participants in the Federated Catalogue
- **Response Codes**:
  - 200: Success
  - 500: Internal server error

### Update Participant
- **Endpoint**: `PUT /participants/{id}`
- **Description**: Update an existing participant
- **Request Body**: ParticipantDto
- **Response Codes**:
  - 200: Participant updated successfully
  - 400: Validation error or ID mismatch
  - 404: Participant not found
  - 500: Internal server error

### Delete Participant
- **Endpoint**: `DELETE /participants/{id}`
- **Description**: Delete a participant from the Federated Catalogue
- **Response Codes**:
  - 204: Participant deleted successfully
  - 400: Invalid ID
  - 404: Participant not found
  - 500: Internal server error

## Data Models

### ParticipantDto
```json
{
  "id": "participant-001",
  "name": "ACME Corporation",
  "dspUrl": "https://participant.example.com/api/dsp",
  "supportedProtocols": ["dataspace-protocol-http"]
}
```

### ApiResponse
```json
{
  "success": true,
  "data": {...},
  "message": "Operation completed successfully"
}
```

or in case of error:
```json
{
  "success": false,
  "error": "VALIDATION_ERROR",
  "message": "Detailed error message"
}
```

## Generating Swagger Documentation

The Swagger documentation can be generated using the Swagger Gradle plugin configured in `build.gradle.kts`.

To generate the OpenAPI spec:
```bash
./gradlew resolve
```

The generated OpenAPI specification will be available in the build output directory and can be viewed using Swagger UI or other OpenAPI documentation tools.

## Swagger Annotations

All API endpoints are documented using:
- `@OpenAPIDefinition` - API-level metadata
- `@Operation` - Endpoint operation details
- `@Parameter` - Path/query parameter documentation
- `@RequestBody` - Request body schema
- `@ApiResponse` - Response codes and schemas
- `@Schema` - Data model documentation
- `@Tag` - API grouping

## Integration

The API documentation is automatically generated from the Java annotations and can be:
1. Exported as OpenAPI 3.0 JSON/YAML
2. Integrated with Swagger UI for interactive documentation
3. Used for API client generation
4. Published to API documentation portals
