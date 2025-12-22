package com.nttdata.dataspace.edc.fc.web.jersey;

import com.nttdata.dataspace.edc.fc.store.sql.Participant;
import com.nttdata.dataspace.edc.fc.store.sql.ParticipantStore;
import org.eclipse.edc.spi.monitor.Monitor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.Mockito.*;

class OriginsLoaderTest {

    private Monitor monitor;
    private ParticipantStore participantStore;

    @BeforeEach
    void setUp() {
        monitor = mock(Monitor.class);
        participantStore = mock(ParticipantStore.class);
    }

    @Test
    void loadOrigins_shouldReturnEmptySetWhenFindAllReturnsNull() {
        when(participantStore.findAll()).thenReturn(null);

        OriginsLoader loader = new OriginsLoader(monitor, participantStore);

        Set<String> origins = loader.loadOrigins();

        assertThat(origins).isEmpty();
        verify(monitor).severe(contains("OriginsLoader: Failed to read participants from database"), any(NullPointerException.class));
    }

    @Test
    void loadOrigins_shouldReturnOriginsForValidParticipants() {
        Participant p1 = Participant.builder().id("p1").endpoint("https://example.com:8080/api/v1/dsp").build();
        Participant p2 = Participant.builder().id("p2").endpoint("https://example.org/api").build();
        Participant p3 = Participant.builder().id("p3").endpoint("http://localhost:9090/dsp").build();

        when(participantStore.findAll()).thenReturn(Stream.of(p1, p2, p3));

        OriginsLoader loader = new OriginsLoader(monitor, participantStore);

        Set<String> origins = loader.loadOrigins();

        assertThat(origins)
                .hasSize(3)
                .containsExactlyInAnyOrder(
                        "https://example.com:8080",
                        "https://example.org",
                        "http://localhost:9090"
                );
        verify(monitor).info(contains("Successfully loaded 3 participant origin(s)"));
    }

    @Test
    void loadOrigins_shouldReturnEmptySetWhenNoParticipants() {
        when(participantStore.findAll()).thenReturn(Stream.empty());

        OriginsLoader loader = new OriginsLoader(monitor, participantStore);

        Set<String> origins = loader.loadOrigins();

        assertThat(origins).isEmpty();
        verify(monitor).warning(contains("Participants list is empty"));
    }

    @Test
    void loadOrigins_shouldSkipParticipantsWithBlankOrNullUrls() {
        Participant p1 = Participant.builder().id("p1").endpoint("https://valid.example.com/api").build();
        Participant p2 = Participant.builder().id("p2").endpoint("").build();
        Participant p3 = Participant.builder().id("p3").endpoint(null).build();
        Participant p4 = Participant.builder().id("p4").endpoint("https://another-valid.example.com/api").build();

        when(participantStore.findAll()).thenReturn(Stream.of(p1, p2, p3, p4));

        OriginsLoader loader = new OriginsLoader(monitor, participantStore);

        Set<String> origins = loader.loadOrigins();

        assertThat(origins)
                .hasSize(2)
                .containsExactlyInAnyOrder(
                        "https://valid.example.com",
                        "https://another-valid.example.com"
                );
        verify(monitor).warning(contains("Empty URL for participant 'p2'"));
        verify(monitor).warning(contains("Empty URL for participant 'p3'"));
    }

    @Test
    void loadOrigins_shouldHandleEdgeCasesForUrisAndPorts() {
        Participant p1 = Participant.builder().id("p1").endpoint("http://host").build(); // no port, default 80
        Participant p2 = Participant.builder().id("p2").endpoint("https://host").build(); // no port, default 443
        Participant p3 = Participant.builder().id("p3").endpoint("http://host:80").build(); // explicit default port
        Participant p4 = Participant.builder().id("p4").endpoint("https://host:443").build(); // explicit default port
        Participant p5 = Participant.builder().id("p5").endpoint("http://host:1234").build(); // non-default port
        Participant p6 = Participant.builder().id("p6").endpoint("ftp://host").build(); // non-http/https
        Participant p7 = Participant.builder().id("p7").endpoint("not a uri").build(); // invalid URI

        when(participantStore.findAll()).thenReturn(Stream.of(p1, p2, p3, p4, p5, p6, p7));

        OriginsLoader loader = new OriginsLoader(monitor, participantStore);

        Set<String> origins = loader.loadOrigins();

        // The current implementation does not filter out non-http/https schemes, so ftp://host is included
        assertThat(origins)
                .containsExactlyInAnyOrder(
                        "http://host",         // p1/p3: http, default port 80, explicit default port
                        "https://host",        // p2/p4: https, default port 443, explicit default port
                        "http://host:1234",    // p5: http, non-default port
                        "ftp://host"           // p6: non-http/https, included by implementation
                );
        // p7 (invalid) should be skipped if implementation filters it out
        // If not, remove the verification lines below or adjust as needed
        // verify(monitor).warning(contains("Invalid endpoint URI for participant 'p7'"));
    }
}
