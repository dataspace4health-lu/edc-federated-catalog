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

package com.nttdata.dataspace.edc.fc.store.sql.api;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for ParticipantValidator.
 */
class ParticipantValidatorTest {

    // ==================== VALIDATE DTO TESTS ====================

    private static final String PARTICIPANT_ID_1 = "participant-1";
    private static final String PARTICIPANT_ID_TEST = "Test Participant";
    private static final String DSP_URL = "https://example.com/api";
    private static final String SUPPORTED_PROTOCOLS_URL = "dataspace-protocol-http";
    private static final String ID_REQUIRED = "ID is required";
    private static final String MAX_CHARACTERS_EXCEEDED = "must not exceed 255 characters";


    @Test
    void validateValidDtoReturnsNoErrors() {
        // Arrange
        ParticipantDto dto = ParticipantDto.builder()
                .id(PARTICIPANT_ID_1)
                .name(PARTICIPANT_ID_TEST)
                .dspUrl(DSP_URL)
                .supportedProtocols(Arrays.asList(SUPPORTED_PROTOCOLS_URL))
                .build();

        // Act
        List<String> errors = ParticipantValidator.validate(dto);

        // Assert
        assertThat(errors).isEmpty();
    }

    @Test
    void validateNullIdReturnsError() {
        // Arrange
        ParticipantDto dto = ParticipantDto.builder()
                .name(PARTICIPANT_ID_TEST)
                .dspUrl(DSP_URL)
                .supportedProtocols(Arrays.asList(SUPPORTED_PROTOCOLS_URL))
                .build();

        // Act
        List<String> errors = ParticipantValidator.validate(dto);

        // Assert
        assertThat(errors).hasSize(1);
        assertThat(errors.get(0)).contains(ID_REQUIRED);
    }

    @Test
    void validateEmptyIdReturnsError() {
        // Arrange
        ParticipantDto dto = ParticipantDto.builder()
                .id("")
                .name(PARTICIPANT_ID_TEST)
                .dspUrl(DSP_URL)
                .supportedProtocols(Arrays.asList(SUPPORTED_PROTOCOLS_URL))
                .build();

        // Act
        List<String> errors = ParticipantValidator.validate(dto);

        // Assert
        assertThat(errors).hasSize(1);
        assertThat(errors.get(0)).contains(ID_REQUIRED);
    }

    @Test
    void validateWhitespaceIdReturnsError() {
        // Arrange
        ParticipantDto dto = ParticipantDto.builder()
                .id("   ")
                .name(PARTICIPANT_ID_TEST)
                .dspUrl(DSP_URL)
                .supportedProtocols(Arrays.asList(SUPPORTED_PROTOCOLS_URL))
                .build();

        // Act
        List<String> errors = ParticipantValidator.validate(dto);

        // Assert
        assertThat(errors).hasSize(1);
        assertThat(errors.get(0)).contains(ID_REQUIRED);
    }

    @Test
    void validateIdTooLongReturnsError() {
        // Arrange
        String longId = "a".repeat(256);
        ParticipantDto dto = ParticipantDto.builder()
                .id(longId)
                .name(PARTICIPANT_ID_TEST)
                .dspUrl(DSP_URL)
                .supportedProtocols(Arrays.asList(SUPPORTED_PROTOCOLS_URL))
                .build();

        // Act
        List<String> errors = ParticipantValidator.validate(dto);

        // Assert
        assertThat(errors).hasSize(1);
        assertThat(errors.get(0)).contains(MAX_CHARACTERS_EXCEEDED);
    }

    @Test
    void validateNullNameReturnsError() {
        // Arrange
        ParticipantDto dto = ParticipantDto.builder()
                .id(PARTICIPANT_ID_1)
                .dspUrl(DSP_URL)
                .supportedProtocols(Arrays.asList(SUPPORTED_PROTOCOLS_URL))
                .build();

        // Act
        List<String> errors = ParticipantValidator.validate(dto);

        // Assert
        assertThat(errors).hasSize(1);
        assertThat(errors.get(0)).contains("name is required");
    }

    @Test
    void validateEmptyNameReturnsError() {
        // Arrange
        ParticipantDto dto = ParticipantDto.builder()
                .id(PARTICIPANT_ID_1)
                .name("")
                .dspUrl(DSP_URL)
                .supportedProtocols(Arrays.asList(SUPPORTED_PROTOCOLS_URL))
                .build();

        // Act
        List<String> errors = ParticipantValidator.validate(dto);

        // Assert
        assertThat(errors).hasSize(1);
        assertThat(errors.get(0)).contains("name is required");
    }

    @Test
    void validateNameTooLongReturnsError() {
        // Arrange
        String longName = "a".repeat(256);
        ParticipantDto dto = ParticipantDto.builder()
                .id(PARTICIPANT_ID_1)
                .name(longName)
                .dspUrl(DSP_URL)
                .supportedProtocols(Arrays.asList(SUPPORTED_PROTOCOLS_URL))
                .build();

        // Act
        List<String> errors = ParticipantValidator.validate(dto);

        // Assert
        assertThat(errors).hasSize(1);
        assertThat(errors.get(0)).contains(MAX_CHARACTERS_EXCEEDED);
    }

    @Test
    void validateNullDspUrlReturnsError() {
        // Arrange
        ParticipantDto dto = ParticipantDto.builder()
                .id(PARTICIPANT_ID_1)
                .name(PARTICIPANT_ID_TEST)
                .supportedProtocols(Arrays.asList(SUPPORTED_PROTOCOLS_URL))
                .build();

        // Act
        List<String> errors = ParticipantValidator.validate(dto);

        // Assert
        assertThat(errors).hasSize(1);
        assertThat(errors.get(0)).contains("DSP URL is required");
    }

    @Test
    void validateEmptyDspUrlReturnsError() {
        // Arrange
        ParticipantDto dto = ParticipantDto.builder()
                .id(PARTICIPANT_ID_1)
                .name(PARTICIPANT_ID_TEST)
                .dspUrl("")
                .supportedProtocols(Arrays.asList(SUPPORTED_PROTOCOLS_URL))
                .build();

        // Act
        List<String> errors = ParticipantValidator.validate(dto);

        // Assert
        assertThat(errors).hasSize(1);
        assertThat(errors.get(0)).contains("DSP URL is required");
    }

    @Test
    void validateInvalidDspUrlReturnsError() {
        // Arrange
        ParticipantDto dto = ParticipantDto.builder()
                .id(PARTICIPANT_ID_1)
                .name(PARTICIPANT_ID_TEST)
                .dspUrl("not-a-valid-url")
                .supportedProtocols(Arrays.asList(SUPPORTED_PROTOCOLS_URL))
                .build();

        // Act
        List<String> errors = ParticipantValidator.validate(dto);

        // Assert
        assertThat(errors).hasSize(1);
        assertThat(errors.get(0)).contains("not a valid URL");
    }

    @Test
    void validateDspUrlTooLongReturnsError() {
        // Arrange
        String longUrl = "https://example.com/" + "a".repeat(500);
        ParticipantDto dto = ParticipantDto.builder()
                .id(PARTICIPANT_ID_1)
                .name(PARTICIPANT_ID_TEST)
                .dspUrl(longUrl)
                .supportedProtocols(Arrays.asList(SUPPORTED_PROTOCOLS_URL))
                .build();

        // Act
        List<String> errors = ParticipantValidator.validate(dto);

        // Assert
        assertThat(errors).hasSizeGreaterThanOrEqualTo(1);
        assertThat(errors).anyMatch(error -> error.contains("must not exceed 500 characters"));
    }

    @Test
    void validateNullProtocolsReturnsError() {
        // Arrange
        ParticipantDto dto = ParticipantDto.builder()
                .id(PARTICIPANT_ID_1)
                .name(PARTICIPANT_ID_TEST)
                .dspUrl(DSP_URL)
                .build();

        // Act
        List<String> errors = ParticipantValidator.validate(dto);

        // Assert
        assertThat(errors).hasSize(1);
        assertThat(errors.get(0)).contains("At least one supported protocol is required");
    }

    @Test
    void validateEmptyProtocolsReturnsError() {
        // Arrange
        ParticipantDto dto = ParticipantDto.builder()
                .id(PARTICIPANT_ID_1)
                .name(PARTICIPANT_ID_TEST)
                .dspUrl(DSP_URL)
                .supportedProtocols(Collections.emptyList())
                .build();

        // Act
        List<String> errors = ParticipantValidator.validate(dto);

        // Assert
        assertThat(errors).hasSize(1);
        assertThat(errors.get(0)).contains("At least one supported protocol is required");
    }

    @Test
    void validateProtocolsWithEmptyStringReturnsError() {
        // Arrange
        ParticipantDto dto = ParticipantDto.builder()
                .id(PARTICIPANT_ID_1)
                .name(PARTICIPANT_ID_TEST)
                .dspUrl(DSP_URL)
                .supportedProtocols(List.of(""))
                .build();

        // Act
        List<String> errors = ParticipantValidator.validate(dto);

        // Assert
        assertThat(errors).hasSize(1);
        assertThat(errors.get(0)).contains("Protocol entries must not be blank");
    }

    @Test
    void validateProtocolsWithMixedValidAndEmptyReturnsError() {
        // Arrange
        ParticipantDto dto = ParticipantDto.builder()
                .id(PARTICIPANT_ID_1)
                .name(PARTICIPANT_ID_TEST)
                .dspUrl(DSP_URL)
                .supportedProtocols(List.of(SUPPORTED_PROTOCOLS_URL, ""))
                .build();

        // Act
        List<String> errors = ParticipantValidator.validate(dto);

        // Assert
        assertThat(errors).hasSize(1);
        assertThat(errors.get(0)).contains("Protocol entries must not be blank");
    }

    @Test
    void validateProtocolsWithWhitespaceReturnsError() {
        // Arrange
        ParticipantDto dto = ParticipantDto.builder()
                .id(PARTICIPANT_ID_1)
                .name(PARTICIPANT_ID_TEST)
                .dspUrl(DSP_URL)
                .supportedProtocols(List.of("   "))
                .build();

        // Act
        List<String> errors = ParticipantValidator.validate(dto);

        // Assert
        assertThat(errors).hasSize(1);
        assertThat(errors.get(0)).contains("Protocol entries must not be blank");
    }

    @Test
    void validateProtocolsWithDuplicatesReturnsError() {
        // Arrange
        ParticipantDto dto = ParticipantDto.builder()
                .id(PARTICIPANT_ID_1)
                .name(PARTICIPANT_ID_TEST)
                .dspUrl(DSP_URL)
                .supportedProtocols(List.of(SUPPORTED_PROTOCOLS_URL, SUPPORTED_PROTOCOLS_URL))
                .build();

        // Act
        List<String> errors = ParticipantValidator.validate(dto);

        // Assert
        assertThat(errors).hasSize(1);
        assertThat(errors.get(0)).contains("Duplicate protocols are not allowed");
    }

    @Test
    void validateMultipleErrorsReturnsAllErrors() {
        // Arrange
        ParticipantDto dto = ParticipantDto.builder()
                .dspUrl("not-a-url")
                .build();

        // Act
        List<String> errors = ParticipantValidator.validate(dto);

        // Assert
        assertThat(errors).hasSizeGreaterThanOrEqualTo(4); // id, name, url, protocols
    }

    // ==================== VALIDATE ID TESTS ====================

    @Test
    void validateIdValidIdReturnsNoErrors() {
        // Act
        List<String> errors = ParticipantValidator.validateId(PARTICIPANT_ID_1);

        // Assert
        assertThat(errors).isEmpty();
    }

    @Test
    void validateIdNullIdReturnsError() {
        // Act
        List<String> errors = ParticipantValidator.validateId(null);

        // Assert
        assertThat(errors).hasSize(1);
        assertThat(errors.get(0)).contains(ID_REQUIRED);
    }

    @Test
    void validateIdEmptyIdReturnsError() {
        // Act
        List<String> errors = ParticipantValidator.validateId("");

        // Assert
        assertThat(errors).hasSize(1);
        assertThat(errors.get(0)).contains(ID_REQUIRED);
    }

    @Test
    void validateIdWhitespaceIdReturnsError() {
        // Act
        List<String> errors = ParticipantValidator.validateId("   ");

        // Assert
        assertThat(errors).hasSize(1);
        assertThat(errors.get(0)).contains(ID_REQUIRED);
    }

    @Test
    void validateIdIdTooLongReturnsError() {
        // Arrange
        String longId = "a".repeat(256);

        // Act
        List<String> errors = ParticipantValidator.validateId(longId);

        // Assert
        assertThat(errors).hasSize(1);
        assertThat(errors.get(0)).contains(MAX_CHARACTERS_EXCEEDED);
    }

    @Test
    void validateIdExactlyMaxLengthReturnsNoErrors() {
        // Arrange
        String maxLengthId = "a".repeat(255);

        // Act
        List<String> errors = ParticipantValidator.validateId(maxLengthId);

        // Assert
        assertThat(errors).isEmpty();
    }
}
