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

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

/**
 * Data Transfer Object for Participant API operations.
 * Contains only the DSP link as per requirements.
 */
@Schema(description = "Represents a participant in the Federated Catalogue")
public class ParticipantDto {
    
    @Schema(description = "Unique identifier of the participant", example = "participant-001", required = true)
    private String id;
    
    @Schema(description = "Human-readable name of the participant", example = "ACME Corporation", required = true)
    private String name;
    
    @Schema(description = "DSP (Dataspace Protocol) endpoint URL of the participant", 
            example = "https://participant.example.com/api/dsp", required = true)
    private String dspUrl;  // DSP link endpoint
    
    @Schema(description = "List of supported protocols", 
            example = "[\"dataspace-protocol-http\"]")
    private List<String> supportedProtocols;
    
    @JsonCreator
    public ParticipantDto(
            @JsonProperty("id") String id,
            @JsonProperty("name") String name,
            @JsonProperty("dspUrl") String dspUrl,
            @JsonProperty("supportedProtocols") List<String> supportedProtocols) {
        this.id = id;
        this.name = name;
        this.dspUrl = dspUrl;
        this.supportedProtocols = supportedProtocols;
    }
    
    public String getId() {
        return id;
    }
    
    public String getName() {
        return name;
    }
    
    public String getDspUrl() {
        return dspUrl;
    }
    
    public List<String> getSupportedProtocols() {
        return supportedProtocols;
    }
    
    public static Builder builder() {
        return new Builder();
    }
    
    public static class Builder {
        private String id;
        private String name;
        private String dspUrl;
        private List<String> supportedProtocols;
        
        public Builder id(String id) {
            this.id = id;
            return this;
        }
        
        public Builder name(String name) {
            this.name = name;
            return this;
        }
        
        public Builder dspUrl(String dspUrl) {
            this.dspUrl = dspUrl;
            return this;
        }
        
        public Builder supportedProtocols(List<String> supportedProtocols) {
            this.supportedProtocols = supportedProtocols;
            return this;
        }
        
        public ParticipantDto build() {
            return new ParticipantDto(id, name, dspUrl, supportedProtocols);
        }
    }
}
