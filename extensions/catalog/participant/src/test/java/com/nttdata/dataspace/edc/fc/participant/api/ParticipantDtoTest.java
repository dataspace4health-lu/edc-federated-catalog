package com.nttdata.dataspace.edc.fc.participant.api;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ParticipantDtoTest {

    @Test
    void builder_setsAllFieldsCorrectly() {
        List<String> protocols = List.of("protocol1", "protocol2");
        ParticipantDto dto = ParticipantDto.builder()
                .id("id123")
                .name("Test Name")
                .dspUrl("http://example.com")
                .supportedProtocols(protocols)
                .build();

        assertEquals("id123", dto.getId());
        assertEquals("Test Name", dto.getName());
        assertEquals("http://example.com", dto.getDspUrl());
        assertEquals(protocols, dto.getSupportedProtocols());
    }

    @Test
    void constructor_setsAllFieldsCorrectly() {
        List<String> protocols = List.of("p1");
        ParticipantDto dto = new ParticipantDto("id", "name", "url", protocols);

        assertEquals("id", dto.getId());
        assertEquals("name", dto.getName());
        assertEquals("url", dto.getDspUrl());
        assertEquals(protocols, dto.getSupportedProtocols());
    }
}
