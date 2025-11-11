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

package org.eclipse.edc.extensions.federatedcatalog.store.sql.api;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.ws.rs.core.Response;

/**
 * REST API for Participant Registry operations in the Federated Catalogue.
 * Provides CRUD operations for managing participants.
 */
@OpenAPIDefinition(
        info = @Info(
                description = "This is the Participant Registry API for managing participants in the Federated Catalogue",
                title = "Participant Registry API",
                version = "1.0"
        )
)
@Tag(name = "Participant Registry", description = "Operations for managing participants in the Federated Catalogue")
public interface ParticipantRegistryApi {

    @Operation(
            description = "Create a new participant in the Federated Catalogue.",
            operationId = "createParticipant",
            requestBody = @RequestBody(
                    description = "The participant data to create",
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = ParticipantDto.class),
                            mediaType = "application/json"
                    )
            ),
            responses = {
                    @ApiResponse(
                            responseCode = "201",
                            description = "Participant created successfully",
                            content = @Content(
                                    schema = @Schema(implementation = ApiResponse.class),
                                    mediaType = "application/json"
                            )
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Invalid request - validation error",
                            content = @Content(
                                    schema = @Schema(implementation = ApiResponse.class),
                                    mediaType = "application/json"
                            )
                    ),
                    @ApiResponse(
                            responseCode = "409",
                            description = "Participant with the given ID already exists",
                            content = @Content(
                                    schema = @Schema(implementation = ApiResponse.class),
                                    mediaType = "application/json"
                            )
                    ),
                    @ApiResponse(
                            responseCode = "500",
                            description = "Internal server error",
                            content = @Content(
                                    schema = @Schema(implementation = ApiResponse.class),
                                    mediaType = "application/json"
                            )
                    )
            }
    )
    Response createParticipant(ParticipantDto dto);

    @Operation(
            description = "Get a participant by ID.",
            operationId = "getParticipant",
            parameters = {
                    @Parameter(
                            name = "id",
                            description = "The unique identifier of the participant",
                            required = true
                    )
            },
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Participant retrieved successfully",
                            content = @Content(
                                    schema = @Schema(implementation = ApiResponse.class),
                                    mediaType = "application/json"
                            )
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Invalid participant ID",
                            content = @Content(
                                    schema = @Schema(implementation = ApiResponse.class),
                                    mediaType = "application/json"
                            )
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Participant not found",
                            content = @Content(
                                    schema = @Schema(implementation = ApiResponse.class),
                                    mediaType = "application/json"
                            )
                    )
            }
    )
    Response getParticipant(String id);

    @Operation(
            description = "List all participants in the Federated Catalogue.",
            operationId = "listParticipants",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "List of participants retrieved successfully",
                            content = @Content(
                                    array = @ArraySchema(schema = @Schema(implementation = ParticipantDto.class)),
                                    mediaType = "application/json"
                            )
                    ),
                    @ApiResponse(
                            responseCode = "500",
                            description = "Internal server error",
                            content = @Content(
                                    schema = @Schema(implementation = ApiResponse.class),
                                    mediaType = "application/json"
                            )
                    )
            }
    )
    Response listParticipants();

    @Operation(
            description = "Update an existing participant.",
            operationId = "updateParticipant",
            parameters = {
                    @Parameter(
                            name = "id",
                            description = "The unique identifier of the participant to update",
                            required = true
                    )
            },
            requestBody = @RequestBody(
                    description = "The updated participant data",
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = ParticipantDto.class),
                            mediaType = "application/json"
                    )
            ),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Participant updated successfully",
                            content = @Content(
                                    schema = @Schema(implementation = ApiResponse.class),
                                    mediaType = "application/json"
                            )
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Invalid request - validation error or ID mismatch",
                            content = @Content(
                                    schema = @Schema(implementation = ApiResponse.class),
                                    mediaType = "application/json"
                            )
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Participant not found",
                            content = @Content(
                                    schema = @Schema(implementation = ApiResponse.class),
                                    mediaType = "application/json"
                            )
                    ),
                    @ApiResponse(
                            responseCode = "500",
                            description = "Internal server error",
                            content = @Content(
                                    schema = @Schema(implementation = ApiResponse.class),
                                    mediaType = "application/json"
                            )
                    )
            }
    )
    Response updateParticipant(String id, ParticipantDto dto);

    @Operation(
            description = "Delete a participant from the Federated Catalogue.",
            operationId = "deleteParticipant",
            parameters = {
                    @Parameter(
                            name = "id",
                            description = "The unique identifier of the participant to delete",
                            required = true
                    )
            },
            responses = {
                    @ApiResponse(
                            responseCode = "204",
                            description = "Participant deleted successfully"
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Invalid participant ID",
                            content = @Content(
                                    schema = @Schema(implementation = ApiResponse.class),
                                    mediaType = "application/json"
                            )
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Participant not found",
                            content = @Content(
                                    schema = @Schema(implementation = ApiResponse.class),
                                    mediaType = "application/json"
                            )
                    ),
                    @ApiResponse(
                            responseCode = "500",
                            description = "Internal server error",
                            content = @Content(
                                    schema = @Schema(implementation = ApiResponse.class),
                                    mediaType = "application/json"
                            )
                    )
            }
    )
    Response deleteParticipant(String id);
}
