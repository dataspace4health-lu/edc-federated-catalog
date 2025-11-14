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

import com.fasterxml.jackson.databind.ObjectMapper;
import org.eclipse.edc.extensions.federatedcatalog.store.sql.schema.ParticipantStatements;
import org.eclipse.edc.spi.monitor.Monitor;
import org.eclipse.edc.spi.persistence.EdcPersistenceException;
import org.eclipse.edc.spi.result.StoreResult;
import org.eclipse.edc.sql.QueryExecutor;
import org.eclipse.edc.sql.store.AbstractSqlStore;
import org.eclipse.edc.transaction.datasource.spi.DataSourceRegistry;
import org.eclipse.edc.transaction.spi.TransactionContext;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import static java.lang.String.format;

/**
 * SQL-based implementation of ParticipantStore following EDC patterns.
 */
public class SqlParticipantStore extends AbstractSqlStore implements ParticipantStore {
    
    private static final String PARTICIPANT_EXISTS = "Participant with ID '%s' already exists";
    private static final String PARTICIPANT_NOT_FOUND = "Participant with ID '%s' not found";
    
    private final ParticipantStatements statements;
    private final Monitor monitor;
    
    public SqlParticipantStore(DataSourceRegistry dataSourceRegistry,
                               String dataSourceName,
                               TransactionContext transactionContext,
                               ObjectMapper objectMapper,
                               ParticipantStatements statements,
                               QueryExecutor queryExecutor,
                               Monitor monitor) {
        super(dataSourceRegistry, dataSourceName, transactionContext, objectMapper, queryExecutor);
        this.statements = statements;
        this.monitor = monitor;
    }
    
    @Override
    public Participant findById(String id) {
        Objects.requireNonNull(id);
        return transactionContext.execute(() -> {
            try (var connection = getConnection()) {
                return queryExecutor.single(connection, false, this::mapResultSet, statements.getFindByIdTemplate(), id);
            } catch (SQLException e) {
                monitor.warning("Failed to find participant by id: " + id, e);
                throw new EdcPersistenceException(e.getMessage(), e);
            }
        });
    }
    
    @Override
    public Stream<Participant> findAll() {
        return transactionContext.execute(() -> {
            try (var connection = getConnection()) {
                return queryExecutor.query(connection, false, this::mapResultSet, statements.getFindAllTemplate()).toList().stream();
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
                var existing = queryExecutor.single(connection, false, this::mapResultSet, statements.getFindByIdTemplate(), participant.getId());
                if (existing != null) {
                    return StoreResult.alreadyExists(format(PARTICIPANT_EXISTS, participant.getId()));
                }
                insertInternal(connection, participant);
                monitor.debug("Inserted participant: " + participant.getId());
                return StoreResult.success();
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
                var existing = queryExecutor.single(connection, false, this::mapResultSet, statements.getFindByIdTemplate(), participant.getId());
                if (existing != null) {
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
                var participant = queryExecutor.single(connection, false, this::mapResultSet, statements.getFindByIdTemplate(), id);
                if (participant != null) {
                    queryExecutor.execute(connection, statements.getDeleteTemplate(), id);
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
        queryExecutor.execute(connection, statements.getInsertTemplate(),
                participant.getId(),
                participant.getName(),
                participant.getEndpoint(),
                String.join(",", participant.getSupportedProtocols()),
                now,
                now);
    }
    
    private void updateInternal(Connection connection, Participant participant) throws SQLException {
        Objects.requireNonNull(participant);
        queryExecutor.execute(connection, statements.getUpdateTemplate(),
                participant.getName(),
                participant.getEndpoint(),
                String.join(",", participant.getSupportedProtocols()),
                System.currentTimeMillis(),
                participant.getId());
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
}
