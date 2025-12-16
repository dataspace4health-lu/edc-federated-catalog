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

package com.nttdata.dataspace.federatedcatalog.jersey;

import jakarta.ws.rs.container.ContainerRequestContext;

import java.util.regex.Pattern;

/**
 * Jersey extension configuration class
 */
public class JerseyConfiguration {

    private String allowedOrigins;
    private String allowedHeaders;
    private String allowedMethods;
    private boolean corsEnabled;

    private JerseyConfiguration() {
    }

    public String getAllowedOrigins() {
        return allowedOrigins;
    }

    public String getAllowedOrigin(ContainerRequestContext requestContext) {
        String origin = requestContext.getHeaderString("Origin");
        if (origin == null || origin.isEmpty() || allowedOrigins == null) {
            return null;
        }
        if (allowedOrigins.length() == 1 && allowedOrigins.contains("*")) {
            return "*";
        }
        
        var pattern = Pattern.quote(origin.trim());
        // (^|,\s*) <item> (\s*,|$)  — match item boundaries in CSV
        Pattern p = Pattern.compile("(^\\s*|,\\s*)" + pattern + "(\\s*,|\\s*$)");
        if (p.matcher(allowedOrigins).find()) {
            return origin;
        }
        
        return null;
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