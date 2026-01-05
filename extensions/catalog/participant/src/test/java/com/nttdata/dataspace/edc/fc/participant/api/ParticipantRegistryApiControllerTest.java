package com.nttdata.dataspace.edc.fc.participant.api;

import com.nttdata.dataspace.edc.fc.store.sql.Participant;
import com.nttdata.dataspace.edc.fc.store.sql.ParticipantStore;
import org.eclipse.edc.spi.monitor.Monitor;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ParticipantRegistryApiControllerTest {

    private ParticipantStore store;
    private Monitor monitor;
    private ParticipantRegistryApiController controller;

    @BeforeEach
    void setUp() {
        store = mock(ParticipantStore.class);
        monitor = mock(Monitor.class);
        controller = new ParticipantRegistryApiController(store, monitor);
    }

    @Test
    void createParticipant_success() {
        ParticipantDto dto = ParticipantDto.builder()
                .id("p1")
                .name("Test Participant")
                .dspUrl("http://example.com")
                .supportedProtocols(List.of("protocol1"))
                .build();

        when(store.findById("p1")).thenReturn(null);
        Participant participant = Participant.builder()
                .id("p1")
                .name("Test Participant")
                .endpoint("http://example.com")
                .supportedProtocols(List.of("protocol1"))
                .createdAt(System.currentTimeMillis())
                .updatedAt(System.currentTimeMillis())
                .build();
        // Use ArgumentCaptor to capture the participant passed to save
        when(store.save(any(Participant.class))).thenReturn(org.eclipse.edc.spi.result.StoreResult.success(null));

        Response response = controller.createParticipant(dto);

        assertEquals(Response.Status.CREATED.getStatusCode(), response.getStatus());
        assertNotNull(response.getEntity());
        verify(store).save(any(Participant.class));
        verify(monitor).info(contains("Successfully created participant"));
    }

    // Additional tests for other scenarios should be implemented here:
    // - createParticipant: validation error, already exists, store failure
    // - getParticipant: success, not found, invalid id
    // - listParticipants: success, store failure
    // - updateParticipant: success, id mismatch, validation error, not found, store failure
    // - deleteParticipant: success, not found, invalid id, store failure
}
