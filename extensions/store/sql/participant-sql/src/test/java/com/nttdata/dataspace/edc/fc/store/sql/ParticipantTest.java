package com.nttdata.dataspace.edc.fc.store.sql;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for the Participant entity class.
 */
class ParticipantTest {

    @Test
    void testBuilderSetsAllFields() {
        // Arrange
        String id = "participant-123";
        String name = "Test Participant";
        String endpoint = "https://example.com/api";
        List<String> protocols = Arrays.asList("dataspace-protocol-http", "ids-rest");
        long createdAt = System.currentTimeMillis();
        long updatedAt = createdAt + 1000;

        // Act
        Participant participant = Participant.builder()
                .id(id)
                .name(name)
                .endpoint(endpoint)
                .supportedProtocols(protocols)
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .build();

        // Assert
        assertThat(participant.getId()).isEqualTo(id);
        assertThat(participant.getName()).isEqualTo(name);
        assertThat(participant.getEndpoint()).isEqualTo(endpoint);
        assertThat(participant.getSupportedProtocols()).isEqualTo(protocols);
        assertThat(participant.getCreatedAt()).isEqualTo(createdAt);
        assertThat(participant.getUpdatedAt()).isEqualTo(updatedAt);
    }

    @Test
    void testBuilderBuildsEmptyParticipant() {
        // Act
        Participant participant = Participant.builder().build();

        // Assert
        assertThat(participant.getId()).isNull();
        assertThat(participant.getName()).isNull();
        assertThat(participant.getEndpoint()).isNull();
        assertThat(participant.getSupportedProtocols()).isNull();
        assertThat(participant.getCreatedAt()).isZero();
        assertThat(participant.getUpdatedAt()).isZero();
    }

    @Test
    void testSettersAndGetters() {
        // Arrange
        Participant participant = new Participant();
        String id = "participant-456";
        String name = "Another Participant";
        String endpoint = "https://another.com/api";
        List<String> protocols = Arrays.asList("dsp");
        long createdAt = System.currentTimeMillis();
        long updatedAt = createdAt + 2000;

        // Act
        participant.setId(id);
        participant.setName(name);
        participant.setEndpoint(endpoint);
        participant.setSupportedProtocols(protocols);
        participant.setCreatedAt(createdAt);
        participant.setUpdatedAt(updatedAt);

        // Assert
        assertThat(participant.getId()).isEqualTo(id);
        assertThat(participant.getName()).isEqualTo(name);
        assertThat(participant.getEndpoint()).isEqualTo(endpoint);
        assertThat(participant.getSupportedProtocols()).isEqualTo(protocols);
        assertThat(participant.getCreatedAt()).isEqualTo(createdAt);
        assertThat(participant.getUpdatedAt()).isEqualTo(updatedAt);
    }

    private static final String PARTICIPANT_ONE = "Participant One";

    @Test
    void testEqualsSameIdReturnsTrue() {
        // Arrange
        Participant participant1 = Participant.builder()
                .id("participant-789")
                .name(PARTICIPANT_ONE)
                .build();

        Participant participant2 = Participant.builder()
                .id("participant-789")
                .name("Participant Two")
                .build();

        // Act & Assert
        assertThat(participant1).isEqualTo(participant2);
    }

    @Test
    void testEqualsDifferentIdReturnsFalse() {
        // Arrange
        Participant participant1 = Participant.builder()
                .id("participant-111")
                .name(PARTICIPANT_ONE)
                .build();

        Participant participant2 = Participant.builder()
                .id("participant-222")
                .name(PARTICIPANT_ONE)
                .build();

        // Act & Assert
        assertThat(participant1).isNotEqualTo(participant2);
    }

    @Test
    void testEqualsSameInstanceReturnsTrue() {
        // Arrange
        Participant participant = Participant.builder()
                .id("participant-333")
                .build();

        // Act & Assert
        assertThat(participant).isEqualTo(participant);
    }

    @Test
    void testEqualsWithNullReturnsFalse() {
        // Arrange
        Participant participant = Participant.builder()
                .id("participant-444")
                .build();

        // Act & Assert
        assertThat(participant).isNotEqualTo(null);
    }

    @Test
    void testEqualsWithDifferentClassReturnsFalse() {
        // Arrange
        Participant participant = Participant.builder()
                .id("participant-555")
                .build();

        String differentClassObject = "not a participant";

        // Act & Assert
        assertThat(participant).isNotEqualTo(differentClassObject);
    }

    @Test
    void testHashCodeSameIdReturnsSameHashCode() {
        // Arrange
        Participant participant1 = Participant.builder()
                .id("participant-666")
                .name(PARTICIPANT_ONE)
                .build();

        Participant participant2 = Participant.builder()
                .id("participant-666")
                .name("Participant Two")
                .build();

        // Act & Assert
        assertThat(participant1.hashCode()).isEqualTo(participant2.hashCode());
    }

    @Test
    void testHashCodeDifferentIdReturnsDifferentHashCode() {
        // Arrange
        Participant participant1 = Participant.builder()
                .id("participant-777")
                .build();

        Participant participant2 = Participant.builder()
                .id("participant-888")
                .build();

        // Act & Assert
        assertThat(participant1.hashCode()).isNotEqualTo(participant2.hashCode());
    }

    @Test
    void testHashCodeNullIdHandlesGracefully() {
        // Arrange
        Participant participant1 = Participant.builder().build();
        Participant participant2 = Participant.builder().build();

        // Act & Assert - both should have the same hash code since both have null IDs
        assertThat(participant1.hashCode()).isEqualTo(participant2.hashCode());
    }

    @Test
    void testBuilderCanChainMethods() {
        // Arrange & Act
        Participant participant = Participant.builder()
                .id("participant-999")
                .name("Chained Participant")
                .endpoint("https://chain.com")
                .supportedProtocols(Arrays.asList("protocol1", "protocol2"))
                .createdAt(1000L)
                .updatedAt(2000L)
                .build();

        // Assert
        assertThat(participant).isNotNull();
        assertThat(participant.getId()).isEqualTo("participant-999");
        assertThat(participant.getName()).isEqualTo("Chained Participant");
        assertThat(participant.getEndpoint()).isEqualTo("https://chain.com");
        assertThat(participant.getSupportedProtocols()).containsExactly("protocol1", "protocol2");
        assertThat(participant.getCreatedAt()).isEqualTo(1000L);
        assertThat(participant.getUpdatedAt()).isEqualTo(2000L);
    }
}
