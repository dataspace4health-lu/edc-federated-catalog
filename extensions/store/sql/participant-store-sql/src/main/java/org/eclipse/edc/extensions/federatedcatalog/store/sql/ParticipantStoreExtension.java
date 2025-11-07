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

import org.eclipse.edc.extensions.federatedcatalog.store.sql.schema.ParticipantStatements;
import org.eclipse.edc.extensions.federatedcatalog.store.sql.schema.postgres.PostgresDialectStatements;
import org.eclipse.edc.runtime.metamodel.annotation.Extension;
import org.eclipse.edc.runtime.metamodel.annotation.Inject;
import org.eclipse.edc.runtime.metamodel.annotation.Provider;
import org.eclipse.edc.runtime.metamodel.annotation.Setting;
import org.eclipse.edc.spi.EdcException;
import org.eclipse.edc.spi.system.ServiceExtension;
import org.eclipse.edc.spi.system.ServiceExtensionContext;
import org.eclipse.edc.transaction.datasource.spi.DataSourceRegistry;
import org.eclipse.edc.transaction.spi.TransactionContext;

import javax.sql.DataSource;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.stream.Collectors;

/**
 * SQL Participant Store Extension for EDC 0.13.0.
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
    
    @Override
    public String name() {
        return "SQL Participant Store";
    }
    
    @Override
    public void initialize(ServiceExtensionContext context) {
        var monitor = context.getMonitor();
        monitor.info("SQL Participant Store: Initializing...");
        monitor.info("SQL Participant Store: Using datasource: " + dataSourceName);
        
        // Initialize schema during extension initialization (not in provider)
        try {
            initializeSchema(context);
            monitor.info("SQL Participant Store: Schema initialized successfully during startup");
        } catch (Exception e) {
            monitor.severe("SQL Participant Store: Failed to initialize schema during startup", e);
            throw new EdcException("Failed to initialize participant store schema", e);
        }
    }
    
    @Provider
    public ParticipantStore createSqlParticipantStore(ServiceExtensionContext context) {
        var monitor = context.getMonitor();
        
        monitor.info("SQL Participant Store: Creating store instance...");
        
        var store = new SqlParticipantStore(
                dataSourceRegistry, 
                dataSourceName, 
                transactionContext,
                getStatementImpl(),
                monitor
        );
        
        monitor.info("SQL Participant Store: Store created successfully");
        
        return store;
    }
    
    private void initializeSchema(ServiceExtensionContext context) {
        var monitor = context.getMonitor();
        monitor.info("SQL Participant Store: Starting schema initialization...");
        
        // Get database connection parameters from configuration
        String jdbcUrl = context.getConfig().getString("edc.datasource.default.url", null);
        String username = context.getConfig().getString("edc.datasource.default.user", null);
        String password = context.getConfig().getString("edc.datasource.default.password", null);
        
        if (jdbcUrl == null || username == null || password == null) {
            monitor.warning("SQL Participant Store: Database configuration not found, skipping schema initialization");
            return;
        }
        
        monitor.info("SQL Participant Store: Using direct JDBC connection to: " + jdbcUrl);
        
        // Read SQL schema file
        String schemaSql;
        try (var inputStream = getClass().getClassLoader().getResourceAsStream("participant-schema.sql")) {
            if (inputStream == null) {
                throw new EdcException("Could not find participant-schema.sql in classpath");
            }
            schemaSql = new BufferedReader(new InputStreamReader(inputStream))
                    .lines()
                    .collect(Collectors.joining("\n"));
            
            monitor.info("SQL Participant Store: Schema SQL loaded from file");
        } catch (IOException e) {
            throw new EdcException("Failed to read participant-schema.sql", e);
        }
        
        // Create direct JDBC connection bypassing EDC's DataSource wrapper
        try (Connection connection = DriverManager.getConnection(jdbcUrl, username, password)) {
            connection.setAutoCommit(true); // Enable auto-commit
            
            try (var statement = connection.createStatement()) {
                monitor.info("SQL Participant Store: Executing schema SQL with direct JDBC connection...");
                
                // Split SQL into individual statements and execute them
                String[] statements = schemaSql.split(";");
                for (String sql : statements) {
                    sql = sql.trim();
                    if (!sql.isEmpty()) {
                        monitor.debug("SQL Participant Store: Executing: " + sql);
                        statement.execute(sql);
                    }
                }
                
                monitor.info("SQL Participant Store: Schema created successfully with auto-commit");
            }
            
        } catch (SQLException e) {
            monitor.severe("SQL Participant Store: Failed to execute schema SQL", e);
            throw new EdcException("Failed to create participant table", e);
        }
    }
    
    private ParticipantStatements getStatementImpl() {
        return statements != null ? statements : new PostgresDialectStatements();
    }
}
