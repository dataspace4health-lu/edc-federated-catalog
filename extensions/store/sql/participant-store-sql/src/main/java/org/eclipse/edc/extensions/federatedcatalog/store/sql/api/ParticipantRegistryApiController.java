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

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.edc.extensions.federatedcatalog.store.sql.Participant;
import org.eclipse.edc.extensions.federatedcatalog.store.sql.ParticipantStore;
import org.eclipse.edc.spi.monitor.Monitor;

import java.util.List;

/**
 * REST API Controller for Participant Registry operations.
 * Provides CRUD endpoints for managing participants in the Federated Catalogue.
 */
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Path("/participants")
public class ParticipantRegistryApiController implements ParticipantRegistryApi {
    
    private final ParticipantStore store;
    private final Monitor monitor;
    
    public ParticipantRegistryApiController(ParticipantStore store, Monitor monitor) {
        this.store = store;
        this.monitor = monitor;
    }
    
    @Override
    @POST
    @Path("/")
    public Response createParticipant(ParticipantDto dto) {
        monitor.debug("Received request to create participant: " + dto.getId());
        
        // Validate input
        List<String> validationErrors = ParticipantValidator.validate(dto);
        if (!validationErrors.isEmpty()) {
            monitor.warning("Validation failed for participant creation: " + String.join(", ", validationErrors));
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(org.eclipse.edc.extensions.federatedcatalog.store.sql.api.ApiResponse.error(
                            "VALIDATION_ERROR",
                            String.join("; ", validationErrors)))
                    .build();
        }
        
        // Check if participant already exists
        var existing = store.findById(dto.getId());
        if (existing != null) {
            monitor.warning("Participant already exists: " + dto.getId());
            return Response.status(Response.Status.CONFLICT)
                    .entity(org.eclipse.edc.extensions.federatedcatalog.store.sql.api.ApiResponse.error(
                            "PARTICIPANT_EXISTS",
                            "Participant with ID '" + dto.getId() + "' already exists"))
                    .build();
        }
        
        // Convert DTO to entity
        long now = System.currentTimeMillis();
        var participant = Participant.builder()
                .id(dto.getId())
                .name(dto.getName())
                .endpoint(dto.getDspUrl())
                .supportedProtocols(dto.getSupportedProtocols())
                .createdAt(now)
                .updatedAt(now)
                .build();
        
        // Save to store
        var result = store.save(participant);
        if (result.succeeded()) {
            monitor.info("Successfully created participant: " + dto.getId());
            return Response.status(Response.Status.CREATED)
                    .entity(org.eclipse.edc.extensions.federatedcatalog.store.sql.api.ApiResponse.success(
                            toDto(participant),
                            "Participant created successfully"))
                    .build();
        } else {
            monitor.severe("Failed to create participant: " + result.getFailureDetail());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(org.eclipse.edc.extensions.federatedcatalog.store.sql.api.ApiResponse.error(
                            "CREATE_FAILED",
                            result.getFailureDetail()))
                    .build();
        }
    }
    
    @Override
    @GET
    @Path("/{id}")
    public Response getParticipant(@PathParam("id") String id) {
        monitor.debug("Received request to get participant: " + id);
        
        // Validate ID
        List<String> validationErrors = ParticipantValidator.validateId(id);
        if (!validationErrors.isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(org.eclipse.edc.extensions.federatedcatalog.store.sql.api.ApiResponse.error(
                            "INVALID_ID",
                            String.join("; ", validationErrors)))
                    .build();
        }
        
        var participant = store.findById(id);
        if (participant == null) {
            monitor.debug("Participant not found: " + id);
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(org.eclipse.edc.extensions.federatedcatalog.store.sql.api.ApiResponse.error(
                            "NOT_FOUND",
                            "Participant with ID '" + id + "' not found"))
                    .build();
        }
        
        monitor.debug("Successfully retrieved participant: " + id);
        return Response.ok(org.eclipse.edc.extensions.federatedcatalog.store.sql.api.ApiResponse.success(toDto(participant))).build();
    }
    
    @Override
    @GET
    @Path("/")
    public Response listParticipants() {
        monitor.debug("Received request to list all participants");
        
        try {
            var participants = store.findAll()
                    .map(this::toDto)
                    .toList();
            
            monitor.debug("Successfully retrieved " + participants.size() + " participants");
            return Response.ok(org.eclipse.edc.extensions.federatedcatalog.store.sql.api.ApiResponse.success(participants)).build();
        } catch (Exception e) {
            monitor.severe("Failed to list participants", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(org.eclipse.edc.extensions.federatedcatalog.store.sql.api.ApiResponse.error(
                            "LIST_FAILED",
                            "Failed to retrieve participants: " + e.getMessage()))
                    .build();
        }
    }
    
    @Override
    @PUT
    @Path("/{id}")
    public Response updateParticipant(@PathParam("id") String id, ParticipantDto dto) {
        monitor.debug("Received request to update participant: " + id);
        
        // Validate that IDs match
        if (!id.equals(dto.getId())) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(org.eclipse.edc.extensions.federatedcatalog.store.sql.api.ApiResponse.error(
                            "ID_MISMATCH",
                            "Path ID does not match request body ID"))
                    .build();
        }
        
        // Validate input
        List<String> validationErrors = ParticipantValidator.validate(dto);
        if (!validationErrors.isEmpty()) {
            monitor.warning("Validation failed for participant update: " + String.join(", ", validationErrors));
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(org.eclipse.edc.extensions.federatedcatalog.store.sql.api.ApiResponse.error(
                            "VALIDATION_ERROR",
                            String.join("; ", validationErrors)))
                    .build();
        }
        
        // Check if participant exists
        var existing = store.findById(id);
        if (existing == null) {
            monitor.warning("Participant not found for update: " + id);
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(org.eclipse.edc.extensions.federatedcatalog.store.sql.api.ApiResponse.error(
                            "NOT_FOUND",
                            "Participant with ID '" + id + "' not found"))
                    .build();
        }
        
        // Update participant - preserve createdAt, update updatedAt
        var updated = Participant.builder()
                .id(dto.getId())
                .name(dto.getName())
                .endpoint(dto.getDspUrl())
                .supportedProtocols(dto.getSupportedProtocols())
                .createdAt(existing.getCreatedAt())
                .updatedAt(System.currentTimeMillis())
                .build();
        
        var result = store.update(updated);
        if (result.succeeded()) {
            monitor.info("Successfully updated participant: " + id);
            return Response.ok(org.eclipse.edc.extensions.federatedcatalog.store.sql.api.ApiResponse.success(
                    toDto(updated),
                    "Participant updated successfully"))
                    .build();
        } else {
            monitor.severe("Failed to update participant: " + result.getFailureDetail());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(org.eclipse.edc.extensions.federatedcatalog.store.sql.api.ApiResponse.error(
                            "UPDATE_FAILED",
                            result.getFailureDetail()))
                    .build();
        }
    }
    
    @Override
    @DELETE
    @Path("/{id}")
    public Response deleteParticipant(@PathParam("id") String id) {
        monitor.debug("Received request to delete participant: " + id);
        
        // Validate ID
        List<String> validationErrors = ParticipantValidator.validateId(id);
        if (!validationErrors.isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(org.eclipse.edc.extensions.federatedcatalog.store.sql.api.ApiResponse.error(
                            "INVALID_ID",
                            String.join("; ", validationErrors)))
                    .build();
        }
        
        // Check if participant exists
        var existing = store.findById(id);
        if (existing == null) {
            monitor.warning("Participant not found for deletion: " + id);
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(org.eclipse.edc.extensions.federatedcatalog.store.sql.api.ApiResponse.error(
                            "NOT_FOUND",
                            "Participant with ID '" + id + "' not found"))
                    .build();
        }
        
        var result = store.delete(id);
        if (result.succeeded()) {
            monitor.info("Successfully deleted participant: " + id);
            return Response.noContent().build();
        } else {
            monitor.severe("Failed to delete participant: " + result.getFailureDetail());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(org.eclipse.edc.extensions.federatedcatalog.store.sql.api.ApiResponse.error(
                            "DELETE_FAILED",
                            result.getFailureDetail()))
                    .build();
        }
    }
    
    /**
     * Converts a Participant entity to a ParticipantDto.
     */
    private ParticipantDto toDto(Participant participant) {
        return ParticipantDto.builder()
                .id(participant.getId())
                .name(participant.getName())
                .dspUrl(participant.getEndpoint())
                .supportedProtocols(participant.getSupportedProtocols())
                .build();
    }
}
