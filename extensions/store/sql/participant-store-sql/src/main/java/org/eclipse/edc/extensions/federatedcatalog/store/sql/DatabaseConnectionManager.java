package org.eclipse.edc.extensions.federatedcatalog.store.sql;

import org.eclipse.edc.spi.EdcException;
import org.eclipse.edc.spi.monitor.Monitor;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Manages database connections for the participant store.
 */
public class DatabaseConnectionManager {
    private final String jdbcUrl;
    private final Properties connectionProperties;
    private final Monitor monitor;

    public DatabaseConnectionManager(String jdbcUrl, String username, String password, Monitor monitor) {
        this.jdbcUrl = jdbcUrl;
        this.monitor = monitor;
        this.connectionProperties = new Properties();
        this.connectionProperties.setProperty("user", username);
        this.connectionProperties.setProperty("password", password);
        
        // Add connection optimizations
        this.connectionProperties.setProperty("ssl", "false");
        this.connectionProperties.setProperty("tcpKeepAlive", "true");
    }

    public Connection getConnection() {
        try {
            return DriverManager.getConnection(jdbcUrl, connectionProperties);
        } catch (SQLException e) {
            monitor.severe("Failed to get database connection: " + e.getMessage());
            throw new EdcException("Database connection failed", e);
        }
    }

    public void testConnection() {
        try (Connection conn = getConnection()) {
            monitor.debug("Database connection test successful");
        } catch (SQLException e) {
            monitor.warning("Database connection test failed: " + e.getMessage());
        }
    }
}