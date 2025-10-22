package org.eclipse.edc.extensions.federatedcatalog.noderesolver;

import org.eclipse.edc.spi.monitor.Monitor;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class PostgresConnector {
    private final String url;
    private final String user;
    private final String password;
    private final Monitor monitor;

    public PostgresConnector(String url, String user, String password, Monitor monitor) {
        this.url = url;
        this.user = user;
        this.password = password;
        this.monitor = monitor;
    }

    public void testConnection() {
        try (Connection conn = DriverManager.getConnection(url, user, password)) {
            monitor.info(String.format("Connecting to PostgreSQL: url=%s, user=%s, password=%s", url, user, password));
            if (conn != null && !conn.isClosed()) {
                monitor.info("✅ Successfully connected to PostgreSQL database!");
            } else {
                monitor.warning("⚠️ Connection to PostgreSQL failed (connection is null or closed).");
            }
        } catch (SQLException e) {
            monitor.severe("❌ Failed to connect to PostgreSQL: " + e.getMessage());
        }
    }
}
