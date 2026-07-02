package com.nttdata.dataspace.edc.fc.participant.api;

import com.nttdata.dataspace.edc.fc.store.sql.Participant;
import com.nttdata.dataspace.edc.fc.store.sql.ParticipantStore;
import com.nttdata.dataspace.edc.fc.web.jersey.JerseyConfiguration;
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
    private JerseyConfiguration jerseyConfiguration;
    private ParticipantRegistryApiController controller;

    @BeforeEach
    void setUp() {
        store = mock(ParticipantStore.class);
        monitor = mock(Monitor.class);
        jerseyConfiguration = mock(JerseyConfiguration.class);
        // Stub findAll() so the CORS refresh called after mutations returns an empty stream
        // instead of throwing a NullPointerException.
        when(store.findAll()).thenReturn(java.util.stream.Stream.empty());
        controller = new ParticipantRegistryApiController(store, monitor, jerseyConfiguration);
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
        when(store.save(any(Participant.class))).thenReturn(org.eclipse.edc.spi.result.StoreResult.success(null));

        Response response = controller.createParticipant(dto);

        assertEquals(Response.Status.CREATED.getStatusCode(), response.getStatus());
        assertNotNull(response.getEntity());
        verify(store).save(any(Participant.class));
        verify(monitor).info(contains("Successfully created participant"));
    }

    @Test
    void createParticipant_validationError_nullId() {
        ParticipantDto dto = ParticipantDto.builder()
                .id(null)
                .name("Test")
                .dspUrl("http://example.com")
                .supportedProtocols(List.of("protocol1"))
                .build();

        Response response = controller.createParticipant(dto);
        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
    }

    @Test
    void createParticipant_alreadyExists() {
        ParticipantDto dto = ParticipantDto.builder()
                .id("p1")
                .name("Test")
                .dspUrl("http://example.com")
                .supportedProtocols(List.of("protocol1"))
                .build();

        when(store.findById("p1")).thenReturn(Participant.builder().id("p1").name("Existing").endpoint("http://x.com").supportedProtocols(List.of()).createdAt(1L).updatedAt(1L).build());

        Response response = controller.createParticipant(dto);
        assertEquals(Response.Status.CONFLICT.getStatusCode(), response.getStatus());
    }

    @Test
    void createParticipant_storeFailure() {
        ParticipantDto dto = ParticipantDto.builder()
                .id("p1")
                .name("Test")
                .dspUrl("http://example.com")
                .supportedProtocols(List.of("protocol1"))
                .build();

        when(store.findById("p1")).thenReturn(null);
        when(store.save(any(Participant.class))).thenReturn(org.eclipse.edc.spi.result.StoreResult.generalError("DB error"));

        Response response = controller.createParticipant(dto);
        assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatus());
    }

    @Test
    void getParticipant_success() {
        Participant p = Participant.builder().id("p1").name("Test").endpoint("http://x.com").supportedProtocols(List.of()).createdAt(1L).updatedAt(1L).build();
        when(store.findById("p1")).thenReturn(p);

        Response response = controller.getParticipant("p1");
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
    }

    @Test
    void getParticipant_notFound() {
        when(store.findById("p1")).thenReturn(null);

        Response response = controller.getParticipant("p1");
        assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());
    }

    @Test
    void getParticipant_invalidId() {
        Response response = controller.getParticipant("");
        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
    }

    @Test
    void listParticipants_success() {
        Participant p = Participant.builder().id("p1").name("Test").endpoint("http://x.com").supportedProtocols(List.of()).createdAt(1L).updatedAt(1L).build();
        when(store.findAll()).thenReturn(java.util.stream.Stream.of(p));

        Response response = controller.listParticipants();
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
    }

    @Test
    void listParticipants_storeException() {
        when(store.findAll()).thenThrow(new RuntimeException("DB error"));

        Response response = controller.listParticipants();
        assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatus());
    }

    @Test
    void updateParticipant_success() {
        ParticipantDto dto = ParticipantDto.builder().id("p1").name("Updated").dspUrl("http://new.com").supportedProtocols(List.of("p1")).build();
        Participant existing = Participant.builder().id("p1").name("Old").endpoint("http://old.com").supportedProtocols(List.of()).createdAt(1L).updatedAt(1L).build();

        when(store.findById("p1")).thenReturn(existing);
        when(store.update(any(Participant.class))).thenReturn(org.eclipse.edc.spi.result.StoreResult.success(null));

        Response response = controller.updateParticipant("p1", dto);
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
    }

    @Test
    void updateParticipant_idMismatch() {
        ParticipantDto dto = ParticipantDto.builder().id("p2").name("Test").dspUrl("http://x.com").supportedProtocols(List.of()).build();

        Response response = controller.updateParticipant("p1", dto);
        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
    }

    @Test
    void updateParticipant_notFound() {
        ParticipantDto dto = ParticipantDto.builder().id("p1").name("Test").dspUrl("http://x.com").supportedProtocols(List.of("p1")).build();
        when(store.findById("p1")).thenReturn(null);

        Response response = controller.updateParticipant("p1", dto);
        assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());
    }

    @Test
    void updateParticipant_storeFailure() {
        ParticipantDto dto = ParticipantDto.builder().id("p1").name("Test").dspUrl("http://x.com").supportedProtocols(List.of("p1")).build();
        Participant existing = Participant.builder().id("p1").name("Old").endpoint("http://old.com").supportedProtocols(List.of()).createdAt(1L).updatedAt(1L).build();

        when(store.findById("p1")).thenReturn(existing);
        when(store.update(any(Participant.class))).thenReturn(org.eclipse.edc.spi.result.StoreResult.generalError("DB error"));

        Response response = controller.updateParticipant("p1", dto);
        assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatus());
    }

    @Test
    void deleteParticipant_success() {
        Participant existing = Participant.builder().id("p1").name("Test").endpoint("http://x.com").supportedProtocols(List.of()).createdAt(1L).updatedAt(1L).build();
        when(store.findById("p1")).thenReturn(existing);
        when(store.delete("p1")).thenReturn(org.eclipse.edc.spi.result.StoreResult.success(existing));

        Response response = controller.deleteParticipant("p1");
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
    }

    @Test
    void deleteParticipant_notFound() {
        when(store.findById("p1")).thenReturn(null);

        Response response = controller.deleteParticipant("p1");
        assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());
    }

    @Test
    void deleteParticipant_invalidId() {
        Response response = controller.deleteParticipant("");
        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
    }

    @Test
    void deleteParticipant_storeFailure() {
        Participant existing = Participant.builder().id("p1").name("Test").endpoint("http://x.com").supportedProtocols(List.of()).createdAt(1L).updatedAt(1L).build();
        when(store.findById("p1")).thenReturn(existing);
        when(store.delete("p1")).thenReturn(org.eclipse.edc.spi.result.StoreResult.generalError("DB error"));

        Response response = controller.deleteParticipant("p1");
        assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatus());
    }
}
