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
    void validate_validDto_returnsNoErrors() {
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
    void validate_nullId_returnsError() {
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
    void validate_emptyId_returnsError() {
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
    void validate_whitespaceId_returnsError() {
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
    void validate_idTooLong_returnsError() {
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
    void validate_nullName_returnsError() {
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
    void validate_emptyName_returnsError() {
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
    void validate_nameTooLong_returnsError() {
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
    void validate_nullDspUrl_returnsError() {
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
    void validate_emptyDspUrl_returnsError() {
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
    void validate_invalidDspUrl_returnsError() {
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
    void validate_dspUrlTooLong_returnsError() {
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
    void validate_nullProtocols_returnsError() {
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
    void validate_emptyProtocols_returnsError() {
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
    void validate_multipleErrors_returnsAllErrors() {
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
    void validateId_validId_returnsNoErrors() {
        // Act
        List<String> errors = ParticipantValidator.validateId(PARTICIPANT_ID_1);

        // Assert
        assertThat(errors).isEmpty();
    }

    @Test
    void validateId_nullId_returnsError() {
        // Act
        List<String> errors = ParticipantValidator.validateId(null);

        // Assert
        assertThat(errors).hasSize(1);
        assertThat(errors.get(0)).contains(ID_REQUIRED);
    }

    @Test
    void validateId_emptyId_returnsError() {
        // Act
        List<String> errors = ParticipantValidator.validateId("");

        // Assert
        assertThat(errors).hasSize(1);
        assertThat(errors.get(0)).contains(ID_REQUIRED);
    }

    @Test
    void validateId_whitespaceId_returnsError() {
        // Act
        List<String> errors = ParticipantValidator.validateId("   ");

        // Assert
        assertThat(errors).hasSize(1);
        assertThat(errors.get(0)).contains(ID_REQUIRED);
    }

    @Test
    void validateId_idTooLong_returnsError() {
        // Arrange
        String longId = "a".repeat(256);

        // Act
        List<String> errors = ParticipantValidator.validateId(longId);

        // Assert
        assertThat(errors).hasSize(1);
        assertThat(errors.get(0)).contains(MAX_CHARACTERS_EXCEEDED);
    }

    @Test
    void validateId_exactlyMaxLength_returnsNoErrors() {
        // Arrange
        String maxLengthId = "a".repeat(255);

        // Act
        List<String> errors = ParticipantValidator.validateId(maxLengthId);

        // Assert
        assertThat(errors).isEmpty();
    }
}
