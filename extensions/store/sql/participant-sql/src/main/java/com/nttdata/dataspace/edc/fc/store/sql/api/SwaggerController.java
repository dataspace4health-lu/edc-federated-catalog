/*
 *  Copyright (c) 2025
 *
 *  This program and the accompanying materials are made available under the
 *  terms of the Apache License, Version 2.0 which is available at
 *  https://www.apache.org/licenses/LICENSE-2.0
 *
 *  SPDX-License-Identifier: Apache-2.0
 *
 *  Contributors:
 *       Initial API and implementation
 *
 */

package com.nttdata.dataspace.edc.fc.store.sql.api;

import io.swagger.v3.oas.annotations.Hidden;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

/**
 * Swagger UI and OpenAPI specification endpoint.
 */
@Path("/swagger")
@Hidden
public class SwaggerController {

    private final String basePath;

    public SwaggerController(String basePath) {
        this.basePath = basePath != null ? basePath : "";
    }

    @GET
    @Path("/openapi.json")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getOpenApiSpec() {
        String spec = generateOpenApiSpec();
        return Response.ok(spec).build();
    }

    @GET
    @Path("/")
    @Produces(MediaType.TEXT_HTML)
    public Response getSwaggerUI() {
        String html = generateSwaggerUI();
        return Response.ok(html).build();
    }

    private String generateOpenApiSpec() {
        // The servers section contains the base path
        // Paths should be relative to the server URL
        return """
{
  "openapi": "3.0.1",
  "info": {
    "title": "Participant Registry API",
    "description": "This is the Participant Registry API for managing participants in the Federated Catalogue",
    "version": "1.0"
  },
  "servers": [
    {
      "url": "%s",
      "description": "Participant Registry API Server"
    }
  ],
  "tags": [
    {
      "name": "Participant Registry",
      "description": "Operations for managing participants in the Federated Catalogue"
    }
  ],
  "paths": {
    "/participants": {
      "get": {
        "tags": ["Participant Registry"],
        "summary": "List all participants",
        "description": "List all participants in the Federated Catalogue.",
        "operationId": "listParticipants",
        "responses": {
          "200": {
            "description": "List of participants retrieved successfully",
            "content": {
              "application/json": {
                "schema": {
                  "type": "object",
                  "properties": {
                    "success": { "type": "boolean" },
                    "data": {
                      "type": "array",
                      "items": { "$ref": "#/components/schemas/ParticipantDto" }
                    }
                  }
                }
              }
            }
          },
          "500": {
            "description": "Internal server error",
            "content": {
              "application/json": {
                "schema": { "$ref": "#/components/schemas/ApiResponse" }
              }
            }
          }
        }
      },
      "post": {
        "tags": ["Participant Registry"],
        "summary": "Create a new participant",
        "description": "Create a new participant in the Federated Catalogue.",
        "operationId": "createParticipant",
        "requestBody": {
          "description": "The participant data to create",
          "required": true,
          "content": {
            "application/json": {
              "schema": { "$ref": "#/components/schemas/ParticipantDto" }
            }
          }
        },
        "responses": {
          "201": {
            "description": "Participant created successfully",
            "content": {
              "application/json": {
                "schema": { "$ref": "#/components/schemas/ApiResponse" }
              }
            }
          },
          "400": { "description": "Invalid request - validation error" },
          "409": { "description": "Participant with the given ID already exists" },
          "500": { "description": "Internal server error" }
        }
      }
    },
    "/participants/{id}": {
      "get": {
        "tags": ["Participant Registry"],
        "summary": "Get a participant by ID",
        "description": "Get a participant by ID.",
        "operationId": "getParticipant",
        "parameters": [
          {
            "name": "id",
            "in": "path",
            "description": "The unique identifier of the participant",
            "required": true,
            "schema": { "type": "string" }
          }
        ],
        "responses": {
          "200": {
            "description": "Participant retrieved successfully",
            "content": {
              "application/json": {
                "schema": { "$ref": "#/components/schemas/ApiResponse" }
              }
            }
          },
          "400": { "description": "Invalid participant ID" },
          "404": { "description": "Participant not found" }
        }
      },
      "put": {
        "tags": ["Participant Registry"],
        "summary": "Update an existing participant",
        "description": "Update an existing participant.",
        "operationId": "updateParticipant",
        "parameters": [
          {
            "name": "id",
            "in": "path",
            "description": "The unique identifier of the participant to update",
            "required": true,
            "schema": { "type": "string" }
          }
        ],
        "requestBody": {
          "description": "The updated participant data",
          "required": true,
          "content": {
            "application/json": {
              "schema": { "$ref": "#/components/schemas/ParticipantDto" }
            }
          }
        },
        "responses": {
          "200": { "description": "Participant updated successfully" },
          "400": { "description": "Invalid request - validation error or ID mismatch" },
          "404": { "description": "Participant not found" },
          "500": { "description": "Internal server error" }
        }
      },
      "delete": {
        "tags": ["Participant Registry"],
        "summary": "Delete a participant",
        "description": "Delete a participant from the Federated Catalogue.",
        "operationId": "deleteParticipant",
        "parameters": [
          {
            "name": "id",
            "in": "path",
            "description": "The unique identifier of the participant to delete",
            "required": true,
            "schema": { "type": "string" }
          }
        ],
        "responses": {
          "204": { "description": "Participant deleted successfully" },
          "400": { "description": "Invalid participant ID" },
          "404": { "description": "Participant not found" },
          "500": { "description": "Internal server error" }
        }
      }
    }
  },
  "components": {
    "schemas": {
      "ParticipantDto": {
        "type": "object",
        "required": ["id", "name", "dspUrl"],
        "properties": {
          "id": {
            "type": "string",
            "description": "Unique identifier of the participant",
            "example": "participant-001"
          },
          "name": {
            "type": "string",
            "description": "Human-readable name of the participant",
            "example": "ACME Corporation"
          },
          "dspUrl": {
            "type": "string",
            "description": "DSP (Dataspace Protocol) endpoint URL of the participant",
            "example": "https://participant.example.com/api/dsp"
          },
          "supportedProtocols": {
            "type": "array",
            "description": "List of supported protocols",
            "items": { "type": "string" },
            "example": ["dataspace-protocol-http"]
          }
        }
      },
      "ApiResponse": {
        "type": "object",
        "properties": {
          "success": {
            "type": "boolean",
            "description": "Indicates whether the operation was successful",
            "example": true
          },
          "data": {
            "type": "object",
            "description": "Response data payload"
          },
          "error": {
            "type": "string",
            "description": "Error code if operation failed",
            "example": "VALIDATION_ERROR"
          },
          "message": {
            "type": "string",
            "description": "Human-readable message about the operation result",
            "example": "Participant created successfully"
          }
        }
      }
    }
  }
}
""".formatted(basePath);
    }

    private String generateSwaggerUI() {
        return """
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Participant Registry API - Swagger UI</title>
    <link rel="stylesheet" type="text/css" href="https://cdnjs.cloudflare.com/ajax/libs/swagger-ui/5.10.3/swagger-ui.min.css">
    <style>
        body {
            margin: 0;
            padding: 0;
        }
        #swagger-ui {
            max-width: 1460px;
            margin: 0 auto;
        }
        .topbar {
            display: none;
        }
    </style>
</head>
<body>
    <div id="swagger-ui"></div>
    
    <script src="https://cdnjs.cloudflare.com/ajax/libs/swagger-ui/5.10.3/swagger-ui-bundle.min.js"></script>
    <script src="https://cdnjs.cloudflare.com/ajax/libs/swagger-ui/5.10.3/swagger-ui-standalone-preset.min.js"></script>
    <script>
        window.onload = function() {
            const ui = SwaggerUIBundle({
                url: window.location.origin + window.location.pathname.replace('/swagger/', '/swagger/openapi.json'),
                dom_id: '#swagger-ui',
                deepLinking: true,
                presets: [
                    SwaggerUIBundle.presets.apis,
                    SwaggerUIStandalonePreset
                ],
                plugins: [
                    SwaggerUIBundle.plugins.DownloadUrl
                ],
                layout: "StandaloneLayout"
            });
            window.ui = ui;
        };
    </script>
</body>
</html>
""";
    }
}
