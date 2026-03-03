/*
 *  Copyright (c) 2022 Bayerische Motoren Werke Aktiengesellschaft (BMW AG)
 *
 *  This program and the accompanying materials are made available under the
 *  terms of the Apache License, Version 2.0 which is available at
 *  https://www.apache.org/licenses/LICENSE-2.0
 *
 *  SPDX-License-Identifier: Apache-2.0
 *
 *  Contributors:
 *       Bayerische Motoren Werke Aktiengesellschaft (BMW AG) - Initial implementation
 *
 */

package com.nttdata.dataspace.edc.fc.web.jersey;

import jakarta.ws.rs.container.ContainerRequestContext;

import java.util.Collections;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Jersey extension configuration class
 */
public class JerseyConfiguration {

    private String allowedOrigins;
    private String allowedHeaders;
    private String allowedMethods;
    private boolean corsEnabled;

    /**
     * Participant-derived origins, refreshed on every participant create/update/delete.
     * Volatile so that visibility across threads is guaranteed without synchronization.
     */
    private volatile Set<String> participantOrigins = Collections.emptySet();

    private JerseyConfiguration() {
    }

    public String getAllowedOrigins() {
        return allowedOrigins;
    }

    public String getAllowedOrigin(ContainerRequestContext requestContext) {
        String origin = requestContext.getHeaderString("Origin");
        if (origin == null || origin.isEmpty()) {
            return null;
        }
        // Wildcard — allow every origin.
        if (allowedOrigins != null && allowedOrigins.trim().equals("*")) {
            return "*";
        }
        // Check static origins from configuration.
        if (allowedOrigins != null) {
            var pattern = Pattern.quote(origin.trim());
            // (^|,\s*) <item> (\s*,|$)  — match item boundaries in CSV
            Pattern p = Pattern.compile("(^\\s*|,\\s*)" + pattern + "(\\s*,|\\s*$)");
            if (p.matcher(allowedOrigins).find()) {
                return origin;
            }
        }
        // Check participant-derived origins (kept up-to-date by ParticipantRegistryApiController).
        if (participantOrigins.contains(origin.trim().toLowerCase())) {
            return origin;
        }
        return null;
    }

    /**
     * Replaces the current set of participant-derived CORS origins.
     * Called by the participant API controller after every create/update/delete so
     * that CORS allowlist stays consistent with the participant registry.
     *
     * @param origins the freshly loaded set of participant origins
     */
    public void setParticipantOrigins(Set<String> origins) {
        this.participantOrigins = (origins != null) ? Set.copyOf(origins) : Collections.emptySet();
    }

    void setAllowedOrigin(String allowedOrigins) {
        this.allowedOrigins = allowedOrigins;
    }

    public String getAllowedHeaders() {
        return allowedHeaders;
    }

    public String getAllowedMethods() {
        return allowedMethods;
    }

    public boolean isCorsEnabled() {
        return corsEnabled;
    }

    public static class Builder {

        private final JerseyConfiguration instance = new JerseyConfiguration();

        public static Builder newInstance() {
            return new Builder();
        }

        private Builder() {
        }

        public Builder allowedOrigins(String allowedOrigins) {
            instance.allowedOrigins = allowedOrigins;
            return this;
        }

        public Builder allowedHeaders(String allowedHeaders) {
            instance.allowedHeaders = allowedHeaders;
            return this;
        }

        public Builder allowedMethods(String allowedMethods) {
            instance.allowedMethods = allowedMethods;
            return this;
        }

        public Builder corsEnabled(boolean corsEnabled) {
            instance.corsEnabled = corsEnabled;
            return this;
        }

        public JerseyConfiguration build() {
            return instance;
        }

    }

}