package com.nttdata.dataspace.edc.fc.participant.api;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ParticipantValidatorTest {

    @Test
    void validate_validDto_returnsNoErrors() {
        ParticipantDto dto = ParticipantDto.builder()
                .id("p1")
                .name("Valid Name")
                .dspUrl("http://example.com")
                .supportedProtocols(List.of("protocol1", "protocol2"))
                .build();

        List<String> errors = ParticipantValidator.validate(dto);
        assertTrue(errors.isEmpty());
    }

    @Test
    void validate_missingFields_returnsErrors() {
        ParticipantDto dto = ParticipantDto.builder()
                .id("")
                .name(null)
                .dspUrl(" ")
                .supportedProtocols(null)
                .build();

        List<String> errors = ParticipantValidator.validate(dto);
        assertTrue(errors.stream().anyMatch(e -> e.contains("Participant ID is required")));
        assertTrue(errors.stream().anyMatch(e -> e.contains("Participant name is required")));
        assertTrue(errors.stream().anyMatch(e -> e.contains("DSP URL is required")));
        assertTrue(errors.stream().anyMatch(e -> e.contains("At least one supported protocol is required")));
    }

    @Test
    void validate_invalidUrl_returnsError() {
        ParticipantDto dto = ParticipantDto.builder()
                .id("p1")
                .name("Name")
                .dspUrl("not-a-url")
                .supportedProtocols(List.of("protocol1"))
                .build();

        List<String> errors = ParticipantValidator.validate(dto);
        assertTrue(errors.stream().anyMatch(e -> e.contains("DSP URL is not a valid URL")));
    }

    @Test
    void validate_duplicateAndBlankProtocols_returnsErrors() {
        ParticipantDto dto = ParticipantDto.builder()
                .id("p1")
                .name("Name")
                .dspUrl("http://example.com")
                .supportedProtocols(Arrays.asList("protocol1", " ", "protocol1", null))
                .build();

        List<String> errors = ParticipantValidator.validate(dto);
        assertTrue(errors.stream().anyMatch(e -> e.contains("Protocol entries must not be blank")));
        assertTrue(errors.stream().anyMatch(e -> e.contains("Duplicate protocols are not allowed")));
    }

    @Test
    void validateId_tooLong_returnsError() {
        String longId = "a".repeat(256);
        List<String> errors = ParticipantValidator.validateId(longId);
        assertTrue(errors.stream().anyMatch(e -> e.contains("must not exceed")));
    }
}
