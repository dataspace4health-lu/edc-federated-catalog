package org.eclipse.edc.extensions.federatedcatalog.store.sql;

import org.eclipse.edc.spi.EdcException;
import org.eclipse.edc.spi.monitor.Monitor;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Simple JDBC-based participant store.
 */
public class SimpleParticipantStore implements ParticipantStore {
    
    private final DatabaseConnectionManager connectionManager;
    
    public SimpleParticipantStore(DatabaseConnectionManager connectionManager) {
        this.connectionManager = connectionManager;
        initializeTable();
    }
    
    private void initializeTable() {
        String createTableSql = """
            CREATE TABLE IF NOT EXISTS edc_participants (
                id VARCHAR(255) PRIMARY KEY,
                name VARCHAR(255) NOT NULL,
                endpoint VARCHAR(500) NOT NULL,
                supported_protocols TEXT,
                created_at BIGINT NOT NULL,
                updated_at BIGINT NOT NULL
            )
            """;
            
        String createIndex1 = "CREATE INDEX IF NOT EXISTS idx_participants_name ON edc_participants(name)";
        String createIndex2 = "CREATE INDEX IF NOT EXISTS idx_participants_endpoint ON edc_participants(endpoint)";
        
        try (Connection connection = connectionManager.getConnection();
             Statement statement = connection.createStatement()) {
            
            // Create table
            statement.execute(createTableSql);
            
            // Create indexes
            try {
                statement.execute(createIndex1);
                statement.execute(createIndex2);
            } catch (SQLException e) {
                // Indexes might already exist, which is fine
            }
            
        } catch (SQLException e) {
            throw new EdcException("Failed to create participant table", e);
        }
    }
    
    @Override
    public Optional<Participant> findById(String id) {
        String sql = "SELECT * FROM edc_participants WHERE id = ?";
        
        try (Connection connection = connectionManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            
            statement.setString(1, id);
            ResultSet resultSet = statement.executeQuery();
            
            if (resultSet.next()) {
                return Optional.of(mapResultSet(resultSet));
            }
            return Optional.empty();
            
        } catch (SQLException e) {
            throw new EdcException("Database error finding participant by id: " + id, e);
        }
    }
    
    @Override
    public List<Participant> findAll() {
        String sql = "SELECT * FROM edc_participants ORDER BY name";
        List<Participant> participants = new ArrayList<>();
        
        try (Connection connection = connectionManager.getConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {
            
            while (resultSet.next()) {
                participants.add(mapResultSet(resultSet));
            }
            return participants;
            
        } catch (SQLException e) {
            throw new EdcException("Database error finding all participants", e);
        }
    }
    
    @Override
    public void save(Participant participant) {
        Optional<Participant> existing = findById(participant.getId());
        
        if (existing.isPresent()) {
            update(participant);
        } else {
            insert(participant);
        }
    }
    
    private void insert(Participant participant) {
        String sql = "INSERT INTO edc_participants (id, name, endpoint, supported_protocols, created_at, updated_at) VALUES (?, ?, ?, ?, ?, ?)";
        
        try (Connection connection = connectionManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            
            long now = System.currentTimeMillis();
            statement.setString(1, participant.getId());
            statement.setString(2, participant.getName());
            statement.setString(3, participant.getEndpoint());
            statement.setString(4, String.join(",", participant.getSupportedProtocols()));
            statement.setLong(5, now);
            statement.setLong(6, now);
            
            statement.executeUpdate();
            
        } catch (SQLException e) {
            throw new EdcException("Database error inserting participant: " + participant.getId(), e);
        }
    }
    
    private void update(Participant participant) {
        String sql = "UPDATE edc_participants SET name = ?, endpoint = ?, supported_protocols = ?, updated_at = ? WHERE id = ?";
        
        try (Connection connection = connectionManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            
            statement.setString(1, participant.getName());
            statement.setString(2, participant.getEndpoint());
            statement.setString(3, String.join(",", participant.getSupportedProtocols()));
            statement.setLong(4, System.currentTimeMillis());
            statement.setString(5, participant.getId());
            
            statement.executeUpdate();
            
        } catch (SQLException e) {
            throw new EdcException("Database error updating participant: " + participant.getId(), e);
        }
    }
    
    @Override
    public void delete(String id) {
        String sql = "DELETE FROM edc_participants WHERE id = ?";
        
        try (Connection connection = connectionManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            
            statement.setString(1, id);
            statement.executeUpdate();
            
        } catch (SQLException e) {
            throw new EdcException("Database error deleting participant: " + id, e);
        }
    }
    
    private Participant mapResultSet(ResultSet resultSet) throws SQLException {
        String protocolsString = resultSet.getString("supported_protocols");
        List<String> protocols = protocolsString != null ? List.of(protocolsString.split(",")) : List.of();
        
        return Participant.builder()
            .id(resultSet.getString("id"))
            .name(resultSet.getString("name"))
            .endpoint(resultSet.getString("endpoint"))
            .supportedProtocols(protocols)
            .createdAt(resultSet.getLong("created_at"))
            .updatedAt(resultSet.getLong("updated_at"))
            .build();
    }
}