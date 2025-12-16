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
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;

public class CorsFilter implements ContainerResponseFilter {

    private final JerseyConfiguration configuration;

    public CorsFilter(JerseyConfiguration configuration) {
        this.configuration = configuration;
    }

    @Override
    public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) {
        var allowedOrigin = configuration.getAllowedOrigin(requestContext);
        if (allowedOrigin == null) {
            return;
        }

        responseContext.getHeaders().add("Access-Control-Allow-Origin", allowedOrigin);
        responseContext.getHeaders().add("Access-Control-Allow-Headers", configuration.getAllowedHeaders());
        responseContext.getHeaders().add("Access-Control-Allow-Methods", configuration.getAllowedMethods());
    }
}