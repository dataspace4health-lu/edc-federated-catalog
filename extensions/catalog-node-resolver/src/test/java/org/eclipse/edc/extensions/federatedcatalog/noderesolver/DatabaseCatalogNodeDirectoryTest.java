package org.eclipse.edc.extensions.federatedcatalog.noderesolver;

import org.eclipse.edc.crawler.spi.TargetNode;
import org.eclipse.edc.extensions.federatedcatalog.store.sql.Participant;
import org.eclipse.edc.extensions.federatedcatalog.store.sql.ParticipantStore;
import org.eclipse.edc.spi.monitor.Monitor;
import org.eclipse.edc.spi.result.StoreResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

class DatabaseCatalogNodeDirectoryTest {

    private ParticipantStore participantStore;
    private Monitor monitor;
    private DatabaseCatalogNodeDirectory directory;

    @BeforeEach
    void setUp() {
        participantStore = mock(ParticipantStore.class);
        monitor = mock(Monitor.class);
        directory = new DatabaseCatalogNodeDirectory(participantStore, monitor);
    }

    @Test
    void getAll_shouldReturnTargetNodes_whenParticipantsExist() {
        var participant1 = Participant.builder()
                .id("id1")
                .name("name1")
                .endpoint("url1")
                .supportedProtocols(List.of("protocol1"))
                .createdAt(1L)
                .updatedAt(1L)
                .build();
        var participant2 = Participant.builder()
                .id("id2")
                .name("name2")
                .endpoint("url2")
                .supportedProtocols(List.of("protocol2"))
                .createdAt(2L)
                .updatedAt(2L)
                .build();

        when(participantStore.findAll()).thenReturn(Stream.of(participant1, participant2));

        List<TargetNode> result = directory.getAll();

        assertThat(result).hasSize(2);
        assertThat(result.get(0).id()).isEqualTo("id1");
        assertThat(result.get(0).name()).isEqualTo("name1");
        assertThat(result.get(0).targetUrl()).isEqualTo("url1");
        assertThat(result.get(0).supportedProtocols()).containsExactly("protocol1");

        assertThat(result.get(1).id()).isEqualTo("id2");
        assertThat(result.get(1).name()).isEqualTo("name2");
        assertThat(result.get(1).targetUrl()).isEqualTo("url2");
        assertThat(result.get(1).supportedProtocols()).containsExactly("protocol2");

        verify(monitor).debug(contains("Loading all participants from database"));
        verify(monitor).info(contains("Loaded 2 participants from database"));
    }

    @Test
    void getAll_shouldReturnEmptyList_whenNoParticipantsExist() {
        when(participantStore.findAll()).thenReturn(Stream.empty());

        List<TargetNode> result = directory.getAll();

        assertThat(result).isEmpty();

        verify(monitor).debug(contains("Loading all participants from database"));
        verify(monitor).info(contains("Loaded 0 participants from database"));
    }

    @Test
    void insert_shouldSaveParticipant_whenSuccessful() {
        var targetNode = new TargetNode("name1", "id1", "url1", List.of("protocol1"));
        when(participantStore.save(any(Participant.class))).thenReturn(StoreResult.success());

        directory.insert(targetNode);

        ArgumentCaptor<Participant> captor = ArgumentCaptor.forClass(Participant.class);
        verify(participantStore).save(captor.capture());
        
        Participant savedParticipant = captor.getValue();
        assertThat(savedParticipant.getId()).isEqualTo("id1");
        assertThat(savedParticipant.getName()).isEqualTo("name1");
        assertThat(savedParticipant.getEndpoint()).isEqualTo("url1");
        assertThat(savedParticipant.getSupportedProtocols()).containsExactly("protocol1");

        verify(monitor).debug(contains("Inserting target node: id1"));
        verify(monitor).info(contains("Successfully inserted participant: id1"));
    }

    @Test
    void insert_shouldThrowException_whenSaveFails() {
        var targetNode = new TargetNode("name1", "id1", "url1", List.of("protocol1"));
        when(participantStore.save(any(Participant.class)))
                .thenReturn(StoreResult.generalError("Database error"));

        assertThatThrownBy(() -> directory.insert(targetNode))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Failed to insert participant: Database error");

        verify(monitor).debug(contains("Inserting target node: id1"));
        verify(monitor).warning(contains("Failed to insert participant: Database error"));
        verify(monitor, never()).info(anyString());
    }

    @Test
    void remove_shouldReturnTargetNode_whenSuccessful() {
        var participant = Participant.builder()
                .id("id1")
                .name("name1")
                .endpoint("url1")
                .supportedProtocols(List.of("protocol1"))
                .createdAt(1L)
                .updatedAt(1L)
                .build();
        when(participantStore.delete("id1")).thenReturn(StoreResult.success(participant));

        TargetNode result = directory.remove("id1");

        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo("id1");
        assertThat(result.name()).isEqualTo("name1");
        assertThat(result.targetUrl()).isEqualTo("url1");
        assertThat(result.supportedProtocols()).containsExactly("protocol1");

        verify(monitor).debug(contains("Removing target node: id1"));
        verify(monitor).info(contains("Successfully removed participant: id1"));
    }

    @Test
    void remove_shouldThrowException_whenDeleteFails() {
        when(participantStore.delete("id1")).thenReturn(StoreResult.notFound("Not found"));

        assertThatThrownBy(() -> directory.remove("id1"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Failed to remove participant: Not found");

        verify(monitor).debug(contains("Removing target node: id1"));
        verify(monitor).warning(contains("Failed to remove participant: Not found"));
        verify(monitor, never()).info(anyString());
    }
}
