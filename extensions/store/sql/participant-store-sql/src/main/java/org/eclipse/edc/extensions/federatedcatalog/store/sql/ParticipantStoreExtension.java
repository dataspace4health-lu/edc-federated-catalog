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

package org.eclipse.edc.extensions.federatedcatalog.store.sql;

import org.eclipse.edc.extensions.federatedcatalog.store.sql.api.ParticipantRegistryApiController;
import org.eclipse.edc.extensions.federatedcatalog.store.sql.api.SwaggerController;
import org.eclipse.edc.extensions.federatedcatalog.store.sql.schema.ParticipantStatements;
import org.eclipse.edc.extensions.federatedcatalog.store.sql.schema.postgres.PostgresDialectStatements;
import org.eclipse.edc.runtime.metamodel.annotation.Extension;
import org.eclipse.edc.runtime.metamodel.annotation.Inject;
import org.eclipse.edc.runtime.metamodel.annotation.Provider;
import org.eclipse.edc.runtime.metamodel.annotation.Setting;
import org.eclipse.edc.spi.system.ServiceExtension;
import org.eclipse.edc.spi.system.ServiceExtensionContext;
import org.eclipse.edc.spi.types.TypeManager;
import org.eclipse.edc.sql.QueryExecutor;
import org.eclipse.edc.sql.bootstrapper.SqlSchemaBootstrapper;
import org.eclipse.edc.transaction.datasource.spi.DataSourceRegistry;
import org.eclipse.edc.transaction.spi.TransactionContext;
import org.eclipse.edc.web.spi.WebService;

/**
 * SQL Participant Store Extension following EDC patterns.
 */
@Extension(value = "SQL Participant Store")
public class ParticipantStoreExtension implements ServiceExtension {
    
    @Setting(description = "The datasource to be used", 
             defaultValue = DataSourceRegistry.DEFAULT_DATASOURCE, 
             key = "edc.sql.store.participant.datasource")
    private String dataSourceName;
    
    @Inject
    private DataSourceRegistry dataSourceRegistry;
    
    @Inject
    private TransactionContext transactionContext;
    
    @Inject(required = false)
    private ParticipantStatements statements;
    
    @Inject
    private WebService webService;
    
    @Inject
    private TypeManager typeManager;
    
    @Inject
    private QueryExecutor queryExecutor;
    
    @Inject
    private SqlSchemaBootstrapper sqlSchemaBootstrapper;
    
    private ParticipantStore participantStore;
    
    @Override
    public String name() {
        return "SQL Participant Store";
    }
    
    @Override
    public void initialize(ServiceExtensionContext context) {
        var monitor = context.getMonitor();
        monitor.info("SQL Participant Store: Initializing...");
        monitor.info("SQL Participant Store: Using datasource: " + dataSourceName);
        
        // Create and register the participant store
        participantStore = createSqlParticipantStore(context);
        
        // Register schema for bootstrap
        sqlSchemaBootstrapper.addStatementFromResource(dataSourceName, "participant-schema.sql");
        
        // Create and register the API controller
        var apiController = new ParticipantRegistryApiController(participantStore, monitor);
        webService.registerResource("catalog", apiController);
        
        // Get the catalog path from configuration
        String catalogPath = context.getConfig().getString("web.http.catalog.path", "/api/catalog");
        monitor.info("SQL Participant Store: Using catalog path: " + catalogPath);
        
        // Register Swagger UI controller with base path
        var swaggerController = new SwaggerController(catalogPath);
        webService.registerResource("catalog", swaggerController);
        
        monitor.info("SQL Participant Store: Participant Registry API registered successfully on 'catalog' context");
        monitor.info("SQL Participant Store: Swagger UI available at " + catalogPath + "/swagger/");
    }
    
    @Provider
    public ParticipantStore createSqlParticipantStore(ServiceExtensionContext context) {
        if (participantStore != null) {
            return participantStore;
        }
        
        var monitor = context.getMonitor();
        monitor.info("SQL Participant Store: Creating store instance...");
        
        participantStore = new SqlParticipantStore(
                dataSourceRegistry, 
                dataSourceName, 
                transactionContext,
                typeManager.getMapper(),
                getStatementImpl(),
                queryExecutor,
                monitor
        );
        
        monitor.info("SQL Participant Store: Store created successfully");
        return participantStore;
    }
    
    private ParticipantStatements getStatementImpl() {
        return statements != null ? statements : new PostgresDialectStatements();
    }
}
