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
import org.eclipse.edc.spi.monitor.Monitor;
import org.eclipse.edc.spi.persistence.EdcPersistenceException;
import org.eclipse.edc.spi.result.StoreResult;
import org.eclipse.edc.transaction.datasource.spi.DataSourceRegistry;
import org.eclipse.edc.transaction.spi.TransactionContext;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import static java.lang.String.format;

/**
 * SQL-based implementation of ParticipantStore for EDC 0.13.0.
 */
public class SqlParticipantStore implements ParticipantStore {
    
    private static final String PARTICIPANT_EXISTS = "Participant with ID '%s' already exists";
    private static final String PARTICIPANT_NOT_FOUND = "Participant with ID '%s' not found";
    
    private final DataSourceRegistry dataSourceRegistry;
    private final String dataSourceName;
    private final TransactionContext transactionContext;
    private final ParticipantStatements statements;
    private final Monitor monitor;
    
    public SqlParticipantStore(DataSourceRegistry dataSourceRegistry,
                               String dataSourceName,
                               TransactionContext transactionContext,
                               ParticipantStatements statements,
                               Monitor monitor) {
        this.dataSourceRegistry = dataSourceRegistry;
        this.dataSourceName = dataSourceName;
        this.transactionContext = transactionContext;
        this.statements = statements;
        this.monitor = monitor;
    }
    
    @Override
    public Participant findById(String id) {
        Objects.requireNonNull(id);
        return transactionContext.execute(() -> {
            try (var connection = getConnection();
                 var statement = connection.prepareStatement(statements.getFindByIdTemplate())) {
                statement.setString(1, id);
                try (var rs = statement.executeQuery()) {
                    if (rs.next()) {
                        return mapResultSet(rs);
                    }
                    return null;
                }
            } catch (SQLException e) {
                monitor.warning("Failed to find participant by id: " + id, e);
                throw new EdcPersistenceException(e.getMessage(), e);
            }
        });
    }
    
    @Override
    public Stream<Participant> findAll() {
        return transactionContext.execute(() -> {
            try (var connection = getConnection();
                 var statement = connection.createStatement();
                 var rs = statement.executeQuery(statements.getFindAllTemplate())) {
                
                var participants = new java.util.ArrayList<Participant>();
                while (rs.next()) {
                    participants.add(mapResultSet(rs));
                }
                return participants.stream();
            } catch (SQLException e) {
                monitor.warning("Failed to find all participants", e);
                throw new EdcPersistenceException(e);
            }
        });
    }
    
    @Override
    public StoreResult<Void> save(Participant participant) {
        return transactionContext.execute(() -> {
            try (var connection = getConnection()) {
                if (existsById(connection, participant.getId())) {
                    return StoreResult.alreadyExists(format(PARTICIPANT_EXISTS, participant.getId()));
                } else {
                    insertInternal(connection, participant);
                    monitor.debug("Inserted participant: " + participant.getId());
                    return StoreResult.success();
                }
            } catch (Exception e) {
                monitor.warning("Failed to save participant: " + participant.getId(), e);
                throw new EdcPersistenceException(e.getMessage(), e);
            }
        });
    }
    
    @Override
    public StoreResult<Void> update(Participant participant) {
        return transactionContext.execute(() -> {
            try (var connection = getConnection()) {
                if (existsById(connection, participant.getId())) {
                    updateInternal(connection, participant);
                    monitor.debug("Updated participant: " + participant.getId());
                    return StoreResult.success();
                } else {
                    return StoreResult.notFound(format(PARTICIPANT_NOT_FOUND, participant.getId()));
                }
            } catch (Exception e) {
                monitor.warning("Failed to update participant: " + participant.getId(), e);
                throw new EdcPersistenceException(e.getMessage(), e);
            }
        });
    }
    
    @Override
    public StoreResult<Participant> delete(String id) {
        Objects.requireNonNull(id);
        return transactionContext.execute(() -> {
            try (var connection = getConnection()) {
                var participant = findById(id);
                if (participant != null) {
                    try (var statement = connection.prepareStatement(statements.getDeleteTemplate())) {
                        statement.setString(1, id);
                        statement.executeUpdate();
                    }
                    monitor.debug("Deleted participant: " + id);
                    return StoreResult.success(participant);
                } else {
                    return StoreResult.notFound(format(PARTICIPANT_NOT_FOUND, id));
                }
            } catch (Exception e) {
                monitor.warning("Failed to delete participant: " + id, e);
                throw new EdcPersistenceException(e.getMessage(), e);
            }
        });
    }
    
    private void insertInternal(Connection connection, Participant participant) throws SQLException {
        long now = System.currentTimeMillis();
        try (var statement = connection.prepareStatement(statements.getInsertTemplate())) {
            statement.setString(1, participant.getId());
            statement.setString(2, participant.getName());
            statement.setString(3, participant.getEndpoint());
            statement.setString(4, String.join(",", participant.getSupportedProtocols()));
            statement.setLong(5, now);
            statement.setLong(6, now);
            statement.executeUpdate();
        }
    }
    
    private void updateInternal(Connection connection, Participant participant) throws SQLException {
        Objects.requireNonNull(participant);
        try (var statement = connection.prepareStatement(statements.getUpdateTemplate())) {
            statement.setString(1, participant.getName());
            statement.setString(2, participant.getEndpoint());
            statement.setString(3, String.join(",", participant.getSupportedProtocols()));
            statement.setLong(4, System.currentTimeMillis());
            statement.setString(5, participant.getId());
            statement.executeUpdate();
        }
    }
    
    private boolean existsById(Connection connection, String id) throws SQLException {
        try (var statement = connection.prepareStatement(statements.getCountTemplate())) {
            statement.setString(1, id);
            try (var rs = statement.executeQuery()) {
                if (rs.next()) {
                    return rs.getLong(1) > 0;
                }
                return false;
            }
        }
    }
    
    private Participant mapResultSet(ResultSet rs) throws SQLException {
        String protocolsString = rs.getString(statements.getSupportedProtocolsColumn());
        List<String> protocols = protocolsString != null && !protocolsString.isEmpty() 
                ? Arrays.asList(protocolsString.split(",")) 
                : List.of();
        
        return Participant.builder()
                .id(rs.getString(statements.getIdColumn()))
                .name(rs.getString(statements.getNameColumn()))
                .endpoint(rs.getString(statements.getEndpointColumn()))
                .supportedProtocols(protocols)
                .createdAt(rs.getLong(statements.getCreatedAtColumn()))
                .updatedAt(rs.getLong(statements.getUpdatedAtColumn()))
                .build();
    }
    
    private Connection getConnection() {
        DataSource dataSource = dataSourceRegistry.resolve(dataSourceName);
        if (dataSource == null) {
            throw new EdcPersistenceException("DataSource not found: " + dataSourceName);
        }
        try {
            return dataSource.getConnection();
        } catch (SQLException e) {
            throw new EdcPersistenceException(e);
        }
    }
}
