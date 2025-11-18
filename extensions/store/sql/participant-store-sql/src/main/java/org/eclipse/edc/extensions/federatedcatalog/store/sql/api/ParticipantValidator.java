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

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Validator for Participant API requests.
 */
public class ParticipantValidator {
    
    private static final int MAX_ID_LENGTH = 255;
    private static final int MAX_NAME_LENGTH = 255;
    private static final int MAX_URL_LENGTH = 500;
    
    /**
     * Validates a ParticipantDto and returns list of validation errors.
     * 
     * @param dto the participant DTO to validate
     * @return list of validation error messages, empty if valid
     */
    public static List<String> validate(ParticipantDto dto) {
        List<String> errors = new ArrayList<>();
        
        errors.addAll(validateId(dto.getId()));
        errors.addAll(validateName(dto.getName()));
        errors.addAll(validateDspUrl(dto.getDspUrl()));
        errors.addAll(validateSupportedProtocols(dto.getSupportedProtocols()));
        
        return errors;
    }
    
    /**
     * Validates a participant ID.
     * 
     * @param id the participant ID to validate
     * @return list of validation error messages, empty if valid
     */
    public static List<String> validateId(String id) {
        return validateStringField(id, "Participant ID", MAX_ID_LENGTH);
    }
    
    /**
     * Validates a participant name.
     * 
     * @param name the participant name to validate
     * @return list of validation error messages, empty if valid
     */
    private static List<String> validateName(String name) {
        return validateStringField(name, "Participant name", MAX_NAME_LENGTH);
    }
    
    /**
     * Validates a DSP URL.
     * 
     * @param dspUrl the DSP URL to validate
     * @return list of validation error messages, empty if valid
     */
    private static List<String> validateDspUrl(String dspUrl) {
        List<String> errors = new ArrayList<>();
        
        // Check if URL is null or blank
        if (!isValidString(dspUrl)) {
            errors.add("DSP URL is required");
            return errors;
        }
        
        // Check URL length
        if (dspUrl.length() > MAX_URL_LENGTH) {
            errors.add("DSP URL must not exceed " + MAX_URL_LENGTH + " characters");
        }
        
        // Validate URL format
        try {
            new URL(dspUrl);
        } catch (MalformedURLException e) {
            errors.add("DSP URL is not a valid URL: " + e.getMessage());
        }
        
        return errors;
    }
    
    /**
     * Validates supported protocols list.
     * 
     * @param supportedProtocols the list of supported protocols
     * @return list of validation error messages, empty if valid
     */
    private static List<String> validateSupportedProtocols(List<String> supportedProtocols) {
        List<String> errors = new ArrayList<>();
        
        if (supportedProtocols == null || supportedProtocols.isEmpty()) {
            errors.add("At least one supported protocol is required");
            return errors;
        }
        
        // Check for blank entries
        boolean hasBlankEntry = supportedProtocols.stream()
                .anyMatch(protocol -> protocol == null || protocol.trim().isEmpty());
        if (hasBlankEntry) {
            errors.add("Protocol entries must not be blank");
        }
        
        // Check for duplicates
        long distinctCount = supportedProtocols.stream()
                .filter(protocol -> protocol != null && !protocol.trim().isEmpty())
                .distinct()
                .count();
        long nonBlankCount = supportedProtocols.stream()
                .filter(protocol -> protocol != null && !protocol.trim().isEmpty())
                .count();
        if (distinctCount < nonBlankCount) {
            errors.add("Duplicate protocols are not allowed");
        }
        
        return errors;
    }
    
    /**
     * Validates a generic string field with required and max length constraints.
     * 
     * @param value the string value to validate
     * @param fieldName the name of the field (for error messages)
     * @param maxLength the maximum allowed length
     * @return list of validation error messages, empty if valid
     */
    private static List<String> validateStringField(String value, String fieldName, int maxLength) {
        List<String> errors = new ArrayList<>();
        
        if (!isValidString(value)) {
            errors.add(fieldName + " is required");
        } else if (value.length() > maxLength) {
            errors.add(fieldName + " must not exceed " + maxLength + " characters");
        }
        
        return errors;
    }
    
    /**
     * Checks if a string is valid (not null and not blank).
     * 
     * @param value the string to check
     * @return true if valid, false otherwise
     */
    private static boolean isValidString(String value) {
        return Optional.ofNullable(value)
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .isPresent();
    }
}
