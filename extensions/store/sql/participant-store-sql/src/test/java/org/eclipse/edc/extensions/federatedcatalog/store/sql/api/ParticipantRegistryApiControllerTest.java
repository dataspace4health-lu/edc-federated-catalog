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

import jakarta.ws.rs.core.Response;
import org.eclipse.edc.extensions.federatedcatalog.store.sql.Participant;
import org.eclipse.edc.extensions.federatedcatalog.store.sql.ParticipantStore;
import org.eclipse.edc.spi.monitor.Monitor;
import org.eclipse.edc.spi.result.StoreResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for ParticipantRegistryApiController.
 * Tests all CRUD operations and validation logic.
 */
@ExtendWith(MockitoExtension.class)
class ParticipantRegistryApiControllerTest {


    private static final String PARTICIPANT_ID_1 = "participant-1";
    private static final String TEST_PARTICIPANT = "Test Participant";
    private static final String DSP_URL = "https://example.com/api";
    private static final String SUPPORTED_PROTOCOL = "dataspace-protocol-http";
    private static final String DATABASE_CONNECTION_FAILED = "Database connection failed";
    private static final String PARTICIPANT_ID_999 = "participant-999";
    private static final String UPDATED_PARTICIPANT = "Updated Participant";
    

    @Mock
    private ParticipantStore store;

    @Mock
    private Monitor monitor;

    private ParticipantRegistryApiController controller;

    @BeforeEach
    void setUp() {
        controller = new ParticipantRegistryApiController(store, monitor);
    }

    // ==================== CREATE TESTS ====================

    @Test
    void createParticipantValidRequestReturnsCreated() {
        // Arrange
        ParticipantDto dto = ParticipantDto.builder()
                .id(PARTICIPANT_ID_1)
                .name(TEST_PARTICIPANT)
                .dspUrl(DSP_URL)
                .supportedProtocols(Arrays.asList(SUPPORTED_PROTOCOL))
                .build();

        when(store.findById(PARTICIPANT_ID_1)).thenReturn(null);
        when(store.save(any(Participant.class))).thenReturn(StoreResult.success());

        // Act + Assert
        try (Response response = controller.createParticipant(dto)) {
            assertThat(response.getStatus()).isEqualTo(Response.Status.CREATED.getStatusCode());
        }

        verify(store).findById(PARTICIPANT_ID_1);
        verify(store).save(any(Participant.class));
    }

    @Test
    void createParticipantDuplicateIdReturnsConflict() {
        // Arrange
        ParticipantDto dto = ParticipantDto.builder()
                .id(PARTICIPANT_ID_1)
                .name(TEST_PARTICIPANT)
                .dspUrl(DSP_URL)
                .supportedProtocols(Arrays.asList(SUPPORTED_PROTOCOL))
                .build();

        Participant existing = Participant.builder()
                .id(PARTICIPANT_ID_1)
                .name("Existing Participant")
                .build();

        when(store.findById(PARTICIPANT_ID_1)).thenReturn(existing);

        // Act + Assert
        try (Response response = controller.createParticipant(dto)) {
            assertThat(response.getStatus()).isEqualTo(Response.Status.CONFLICT.getStatusCode());
        }

        verify(store).findById(PARTICIPANT_ID_1);
        verify(store, never()).save(any(Participant.class));
    }

    @Test
    void createParticipantMissingIdReturnsBadRequest() {
        // Arrange
        ParticipantDto dto = ParticipantDto.builder()
                .name(TEST_PARTICIPANT)
                .dspUrl(DSP_URL)
                .supportedProtocols(Arrays.asList(SUPPORTED_PROTOCOL))
                .build();
        


        // Act + Assert
        try (Response response = controller.createParticipant(dto)) {
            assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
        }
        verify(store, never()).findById(anyString());
        verify(store, never()).save(any(Participant.class));
    }

    @Test
    void createParticipantMissingNameReturnsBadRequest() {
        // Arrange
        ParticipantDto dto = ParticipantDto.builder()
                .id(PARTICIPANT_ID_1)
                .dspUrl(DSP_URL)
                .supportedProtocols(Arrays.asList(SUPPORTED_PROTOCOL))
                .build();

        // Act + Assert
        try (Response response = controller.createParticipant(dto)) {
            assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
        }
        verify(store, never()).save(any(Participant.class));
    }

    @Test
    void createParticipantInvalidDspUrlReturnsBadRequest() {
        // Arrange
        ParticipantDto dto = ParticipantDto.builder()
                .id(PARTICIPANT_ID_1)
                .name(TEST_PARTICIPANT)
                .dspUrl("not-a-valid-url")
                .supportedProtocols(Arrays.asList(SUPPORTED_PROTOCOL))
                .build();

        // Act + Assert
        try (Response response = controller.createParticipant(dto)) {
            assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
        }
        verify(store, never()).save(any(Participant.class));
    }

    @Test
    void createParticipantMissingProtocolsReturnsBadRequest() {
        // Arrange
        ParticipantDto dto = ParticipantDto.builder()
                .id(PARTICIPANT_ID_1)
                .name(TEST_PARTICIPANT)
                .dspUrl(DSP_URL)
                .build();

        // Act + Assert
        try (Response response = controller.createParticipant(dto)) {
            assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
        }
        verify(store, never()).save(any(Participant.class));
    }

    @Test
    void createParticipantStoreFailsReturnsInternalServerError() {
        // Arrange
        ParticipantDto dto = ParticipantDto.builder()
                .id(PARTICIPANT_ID_1)
                .name(TEST_PARTICIPANT)
                .dspUrl(DSP_URL)
                .supportedProtocols(Arrays.asList(SUPPORTED_PROTOCOL))
                .build();

        when(store.findById(PARTICIPANT_ID_1)).thenReturn(null);
        when(store.save(any(Participant.class)))
                .thenReturn(StoreResult.generalError(DATABASE_CONNECTION_FAILED));

        // Act + Assert
        try (Response response = controller.createParticipant(dto)) {
            assertThat(response.getStatus()).isEqualTo(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
        }
        verify(store).save(any(Participant.class));
    }

    // ==================== GET BY ID TESTS ====================

    @Test
    void getParticipantExistingIdReturnsOk() {
        // Arrange
        Participant participant = Participant.builder()
                .id(PARTICIPANT_ID_1)
                .name(TEST_PARTICIPANT)
                .endpoint(DSP_URL)
                .supportedProtocols(Arrays.asList(SUPPORTED_PROTOCOL))
                .build();

        when(store.findById(PARTICIPANT_ID_1)).thenReturn(participant);

        // Act
        Response response = controller.getParticipant(PARTICIPANT_ID_1);

        // Assert
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        verify(store).findById(PARTICIPANT_ID_1);
    }

    @Test
    void getParticipantNonExistingIdReturnsNotFound() {
        // Arrange
        when(store.findById(PARTICIPANT_ID_999)).thenReturn(null);

        // Act
        Response response = controller.getParticipant(PARTICIPANT_ID_999);

        // Assert
        assertThat(response.getStatus()).isEqualTo(Response.Status.NOT_FOUND.getStatusCode());
        verify(store).findById(PARTICIPANT_ID_999);
    }

    @Test
    void getParticipantNullIdReturnsBadRequest() {
        // Act
        Response response = controller.getParticipant(null);

        // Assert
        assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
        verify(store, never()).findById(anyString());
    }

    @Test
    void getParticipantEmptyIdReturnsBadRequest() {
        // Act
        Response response = controller.getParticipant("");

        // Assert
        assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
        verify(store, never()).findById(anyString());
    }

    // ==================== LIST ALL TESTS ====================

    @Test
    void listParticipantsReturnsAllParticipants() {
        // Arrange
        List<Participant> participants = Arrays.asList(
                Participant.builder()
                        .id(PARTICIPANT_ID_1)
                        .name("Participant One")
                        .endpoint("https://one.com/api")
                        .supportedProtocols(Arrays.asList("dsp"))
                        .build(),
                Participant.builder()
                        .id("participant-2")
                        .name("Participant Two")
                        .endpoint("https://two.com/api")
                        .supportedProtocols(Arrays.asList("dsp"))
                        .build()
        );

        when(store.findAll()).thenReturn(participants.stream());

        // Act
        Response response = controller.listParticipants();

        // Assert
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        verify(store).findAll();
    }

    @Test
    void listParticipantsEmptyDatabaseReturnsEmptyList() {
        // Arrange
        when(store.findAll()).thenReturn(Stream.empty());

        // Act
        Response response = controller.listParticipants();

        // Assert
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        verify(store).findAll();
    }

    @Test
    void listParticipantsStoreThrowsExceptionReturnsInternalServerError() {
        // Arrange
        when(store.findAll()).thenThrow(new RuntimeException("Database error"));

        // Act
        Response response = controller.listParticipants();

        // Assert
        assertThat(response.getStatus()).isEqualTo(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
        verify(store).findAll();
    }

    // ==================== UPDATE TESTS ====================

    @Test
    void updateParticipantValidRequestReturnsOk() {
        // Arrange
        String id = PARTICIPANT_ID_1;
        ParticipantDto dto = ParticipantDto.builder()
                .id(id)
                .name(UPDATED_PARTICIPANT)
                .dspUrl(DSP_URL)
                .supportedProtocols(Arrays.asList(SUPPORTED_PROTOCOL))
                .build();

        Participant existing = Participant.builder()
                .id(id)
                .name("Old Participant")
                .endpoint("https://old.com/api")
                .createdAt(1000L)
                .updatedAt(1000L)
                .build();

        when(store.findById(id)).thenReturn(existing);
        when(store.update(any(Participant.class))).thenReturn(StoreResult.success());

        // Act
        Response response = controller.updateParticipant(id, dto);

        // Assert
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        verify(store).findById(id);
        verify(store).update(any(Participant.class));
    }

    @Test
    void updateParticipantNonExistingIdReturnsNotFound() {
        // Arrange
        String id = PARTICIPANT_ID_999;
        ParticipantDto dto = ParticipantDto.builder()
                .id(id)
                .name(UPDATED_PARTICIPANT)
                .dspUrl(DSP_URL)
                .supportedProtocols(Arrays.asList(SUPPORTED_PROTOCOL))
                .build();

        when(store.findById(id)).thenReturn(null);

        // Act
        Response response = controller.updateParticipant(id, dto);

        // Assert
        assertThat(response.getStatus()).isEqualTo(Response.Status.NOT_FOUND.getStatusCode());
        verify(store).findById(id);
        verify(store, never()).update(any(Participant.class));
    }

    @Test
    void updateParticipantIdMismatchReturnsBadRequest() {
        // Arrange
        String pathId = PARTICIPANT_ID_1;
        ParticipantDto dto = ParticipantDto.builder()
                .id("participant-2")
                .name(UPDATED_PARTICIPANT)
                .dspUrl(DSP_URL)
                .supportedProtocols(Arrays.asList(SUPPORTED_PROTOCOL))
                .build();

        // Act
        Response response = controller.updateParticipant(pathId, dto);

        // Assert
        assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
        verify(store, never()).findById(anyString());
        verify(store, never()).update(any(Participant.class));
    }

    @Test
    void updateParticipantInvalidDataReturnsBadRequest() {
        // Arrange
        String id = PARTICIPANT_ID_1;
        ParticipantDto dto = ParticipantDto.builder()
                .id(id)
                .name("") // Invalid: empty name
                .dspUrl(DSP_URL)
                .supportedProtocols(Arrays.asList(SUPPORTED_PROTOCOL))
                .build();

        // Act
        Response response = controller.updateParticipant(id, dto);

        // Assert
        assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
        verify(store, never()).update(any(Participant.class));
    }

    @Test
    void updateParticipantInvalidDspUrlReturnsBadRequest() {
        // Arrange
        String id = PARTICIPANT_ID_1;
        ParticipantDto dto = ParticipantDto.builder()
                .id(id)
                .name(UPDATED_PARTICIPANT)
                .dspUrl("not-a-valid-url") // Invalid DSP URL
                .supportedProtocols(Arrays.asList(SUPPORTED_PROTOCOL))
                .build();

        // Act
        Response response = controller.updateParticipant(id, dto);

        // Assert
        assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
        verify(store, never()).update(any(Participant.class));
    }

    @Test
    void updateParticipantStoreFailsReturnsInternalServerError() {
        // Arrange
        String id = PARTICIPANT_ID_1;
        ParticipantDto dto = ParticipantDto.builder()
                .id(id)
                .name(UPDATED_PARTICIPANT)
                .dspUrl(DSP_URL)
                .supportedProtocols(Arrays.asList(SUPPORTED_PROTOCOL))
                .build();

        Participant existing = Participant.builder()
                .id(id)
                .name("Old Participant")
                .createdAt(1000L)
                .build();

        when(store.findById(id)).thenReturn(existing);
        when(store.update(any(Participant.class)))
                .thenReturn(StoreResult.generalError(DATABASE_CONNECTION_FAILED));

        // Act
        Response response = controller.updateParticipant(id, dto);

        // Assert
        assertThat(response.getStatus()).isEqualTo(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
        verify(store).update(any(Participant.class));
    }

    // ==================== DELETE TESTS ====================

    @Test
    void deleteParticipantExistingIdReturnsNoContent() {
        // Arrange
        String id = PARTICIPANT_ID_1;
        Participant existing = Participant.builder()
                .id(id)
                .name(TEST_PARTICIPANT)
                .build();

        when(store.findById(id)).thenReturn(existing);
        when(store.delete(id)).thenReturn(StoreResult.success());

        // Act
        Response response = controller.deleteParticipant(id);

        // Assert
        assertThat(response.getStatus()).isEqualTo(Response.Status.NO_CONTENT.getStatusCode());
        verify(store).findById(id);
        verify(store).delete(id);
    }

    @Test
    void deleteParticipantNonExistingIdReturnsNotFound() {
        // Arrange
        String id = PARTICIPANT_ID_999;
        when(store.findById(id)).thenReturn(null);

        // Act
        Response response = controller.deleteParticipant(id);

        // Assert
        assertThat(response.getStatus()).isEqualTo(Response.Status.NOT_FOUND.getStatusCode());
        verify(store).findById(id);
        verify(store, never()).delete(anyString());
    }

    @Test
    void deleteParticipantNullIdReturnsBadRequest() {
        // Act
        Response response = controller.deleteParticipant(null);

        // Assert
        assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
        verify(store, never()).findById(anyString());
        verify(store, never()).delete(anyString());
    }

    @Test
    void deleteParticipantEmptyIdReturnsBadRequest() {
        // Act
        Response response = controller.deleteParticipant("");

        // Assert
        assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
        verify(store, never()).findById(anyString());
        verify(store, never()).delete(anyString());
    }

    @Test
    void deleteParticipantStoreFailsReturnsInternalServerError() {
        // Arrange
        String id = PARTICIPANT_ID_1;
        Participant existing = Participant.builder()
                .id(id)
                .name(TEST_PARTICIPANT)
                .build();

        when(store.findById(id)).thenReturn(existing);
        when(store.delete(id)).thenReturn(StoreResult.generalError(DATABASE_CONNECTION_FAILED));

        // Act
        Response response = controller.deleteParticipant(id);

        // Assert
        assertThat(response.getStatus()).isEqualTo(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
        verify(store).delete(id);
    }
}
