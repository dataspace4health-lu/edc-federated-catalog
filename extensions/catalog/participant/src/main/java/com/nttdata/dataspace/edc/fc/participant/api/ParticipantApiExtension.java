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

package com.nttdata.dataspace.edc.fc.participant.api;

import com.nttdata.dataspace.edc.fc.store.sql.ParticipantStore;
import com.nttdata.dataspace.edc.fc.web.jersey.JerseyConfiguration;
import org.eclipse.edc.runtime.metamodel.annotation.Extension;
import org.eclipse.edc.runtime.metamodel.annotation.Inject;
import org.eclipse.edc.spi.monitor.Monitor;
import org.eclipse.edc.spi.system.ServiceExtension;
import org.eclipse.edc.spi.system.ServiceExtensionContext;
import org.eclipse.edc.spi.types.TypeManager;
import org.eclipse.edc.web.spi.WebService;

/**
 * Participant Store API Extension following EDC patterns.
 */
@Extension(value = "Participant Store API")
public class ParticipantApiExtension implements ServiceExtension {
    
    @Inject
    private WebService webService;
    
    @Inject
    private TypeManager typeManager;

    @Inject
    private Monitor monitor;
    
    @Inject
    private ParticipantStore participantStore;

    @Inject
    private JerseyConfiguration jerseyConfiguration;
    
    @Override
    public String name() {
        return "Participant Store API";
    }
    
    @Override
    public void initialize(ServiceExtensionContext context) {
        monitor.info("Participant Store API: Initializing...");
        
        // Create and register the API controller
        var apiController = new ParticipantRegistryApiController(participantStore, monitor, jerseyConfiguration);
        webService.registerResource("catalog", apiController);
        
        // Get the catalog path from configuration
        String catalogPath = context.getConfig().getString("web.http.catalog.path", "/api/catalog");
        monitor.debug("Participant Store: Using catalog path: " + catalogPath);
        
        // Register Swagger UI controller with base path
        var swaggerController = new SwaggerController(catalogPath);
        webService.registerResource("catalog", swaggerController);
        
        monitor.debug("Participant Store: Participant Registry API registered successfully on 'catalog' context");
        monitor.debug("Participant Store: Swagger UI available at " + catalogPath + "/swagger/");
    }
}