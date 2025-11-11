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

/**
 * Validator for Participant API requests.
 */
public class ParticipantValidator {
    
    /**
     * Validates a ParticipantDto and returns list of validation errors.
     * 
     * @param dto the participant DTO to validate
     * @return list of validation error messages, empty if valid
     */
    public static List<String> validate(ParticipantDto dto) {
        List<String> errors = new ArrayList<>();
        
        // Validate ID
        if (dto.getId() == null || dto.getId().trim().isEmpty()) {
            errors.add("Participant ID is required");
        } else if (dto.getId().length() > 255) {
            errors.add("Participant ID must not exceed 255 characters");
        }
        
        // Validate Name
        if (dto.getName() == null || dto.getName().trim().isEmpty()) {
            errors.add("Participant name is required");
        } else if (dto.getName().length() > 255) {
            errors.add("Participant name must not exceed 255 characters");
        }
        
        // Validate DSP URL
        if (dto.getDspUrl() == null || dto.getDspUrl().trim().isEmpty()) {
            errors.add("DSP URL is required");
        } else {
            if (dto.getDspUrl().length() > 500) {
                errors.add("DSP URL must not exceed 500 characters");
            }
            try {
                new URL(dto.getDspUrl());
            } catch (MalformedURLException e) {
                errors.add("DSP URL is not a valid URL: " + e.getMessage());
            }
        }
        
        // Validate supported protocols
        if (dto.getSupportedProtocols() == null || dto.getSupportedProtocols().isEmpty()) {
            errors.add("At least one supported protocol is required");
        }
        
        return errors;
    }
    
    /**
     * Validates a participant ID.
     * 
     * @param id the participant ID to validate
     * @return list of validation error messages, empty if valid
     */
    public static List<String> validateId(String id) {
        List<String> errors = new ArrayList<>();
        
        if (id == null || id.trim().isEmpty()) {
            errors.add("Participant ID is required");
        } else if (id.length() > 255) {
            errors.add("Participant ID must not exceed 255 characters");
        }
        
        return errors;
    }
}
