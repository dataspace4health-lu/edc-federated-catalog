package org.eclipse.edc.extensions.federatedcatalog.store.sql;

import org.eclipse.edc.runtime.metamodel.annotation.Setting;
import org.eclipse.edc.spi.system.ServiceExtension;
import org.eclipse.edc.spi.system.ServiceExtensionContext;
import org.eclipse.edc.spi.monitor.Monitor;

/**
 * Simple SQL Participant Store Extension without complex dependencies.
 */
public class ParticipantStoreExtension implements ServiceExtension {
    
    @Override
    public String name() {
        return "SQL Participant Store";
    }
    
    @Override
    public void initialize(ServiceExtensionContext context) {
        Monitor monitor = context.getMonitor();
        
        try {
            // Get database configuration from settings
            String jdbcUrl = System.getenv("POSTGRES_URL");
            String username = System.getenv("POSTGRES_USER");
            String password = System.getenv("POSTGRES_PASSWORD");
            
            monitor.info("Initializing SQL Participant Store with URL: " + jdbcUrl);
            
            // Create connection manager
            DatabaseConnectionManager connectionManager = new DatabaseConnectionManager(jdbcUrl, username, password, monitor);
            
            // Test connection
            connectionManager.testConnection();
            
            // Create and initialize participant store
            SimpleParticipantStore participantStore = new SimpleParticipantStore(connectionManager);
            
            // Register the service
            context.registerService(ParticipantStore.class, participantStore);
            
            monitor.info("SQL Participant Store initialized successfully");
            
        } catch (Exception e) {
            monitor.severe("Failed to initialize SQL Participant Store: " + e.getMessage(), e);
        }
    }
}