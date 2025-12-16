package org.eclipse.edc.extensions.federatedcatalog.store.sql;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.eclipse.edc.extensions.federatedcatalog.store.sql.schema.ParticipantStatements;
import org.eclipse.edc.spi.monitor.Monitor;
import org.eclipse.edc.spi.result.StoreResult;
import org.eclipse.edc.sql.QueryExecutor;
import org.eclipse.edc.transaction.datasource.spi.DataSourceRegistry;
import org.eclipse.edc.transaction.spi.TransactionContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;

import java.sql.Connection;
import java.sql.ResultSet;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SqlParticipantStoreTest {

    private DataSourceRegistry dataSourceRegistry;
    private TransactionContext transactionContext;
    private ObjectMapper objectMapper;
    private ParticipantStatements statements;
    private QueryExecutor queryExecutor;
    private Monitor monitor;
    private SqlParticipantStore store;

    @BeforeEach
    void setUp() throws Exception {
        dataSourceRegistry = mock(DataSourceRegistry.class);
        transactionContext = new TransactionContext() {
            @Override
            public void execute(TransactionContext.TransactionBlock block) {
                if (block == null) {
                    throw new AssertionError("TransactionBlock is null");
                }
                System.out.println("Executing transaction block: " + block.getClass() + " loader: " + block.getClass().getClassLoader());
                System.out.println("TransactionBlock interface: " + TransactionContext.TransactionBlock.class + " loader: " + TransactionContext.TransactionBlock.class.getClassLoader());
                block.execute();
            }

            @Override
            public Object execute(TransactionContext.ResultTransactionBlock block) {
                System.out.println("Executing result transaction block: " + block.getClass() + " loader: " + block.getClass().getClassLoader());
                Object result = block.execute();
                System.out.println("Result transaction block result: " + result);
                return result;
            }

            @Override
            public void registerSynchronization(TransactionContext.TransactionSynchronization synchronization) {
                // No-op for test
            }
        };
        var dataSource = mock(javax.sql.DataSource.class);
        var connection = mock(java.sql.Connection.class);
        when(dataSource.getConnection()).thenReturn(connection);
        when(dataSourceRegistry.resolve("testDataSource")).thenReturn(dataSource);

        objectMapper = mock(ObjectMapper.class);
        statements = mock(ParticipantStatements.class);
        queryExecutor = mock(QueryExecutor.class);
        monitor = mock(Monitor.class);

        store = new SqlParticipantStore(
                dataSourceRegistry,
                "testDataSource",
                transactionContext,
                objectMapper,
                statements,
                queryExecutor,
                monitor
        );
    }

    @Test
    void save_shouldInsertParticipant_whenNotExists() {
        Participant participant = Participant.builder()
                .id("id1")
                .name("name1")
                .endpoint("endpoint1")
                .supportedProtocols(List.of("http"))
                .build();


        // Mock queryExecutor.single to return null (participant does not exist)
        when(queryExecutor.single(any(), anyBoolean(), any(), any(), any())).thenReturn(null);

        // Mock queryExecutor.execute for insertParticipant (returns int)
        when(queryExecutor.execute(any(), any(), any(), any(), any(), any(), any())).thenReturn(1);

        // Mock statements.getInsertTemplate() to return a dummy SQL
        when(statements.getInsertTemplate()).thenReturn("INSERT INTO participant ...");
        when(statements.getIdColumn()).thenReturn("id");
        when(statements.getNameColumn()).thenReturn("name");
        when(statements.getEndpointColumn()).thenReturn("endpoint");
        when(statements.getSupportedProtocolsColumn()).thenReturn("protocols");
        when(statements.getCreatedAtColumn()).thenReturn("createdAt");
        when(statements.getUpdatedAtColumn()).thenReturn("updatedAt");

        // Assert that findById returns null (participant does not exist)
        assertNull(store.findById(participant.getId()), "findById should return null for a new participant");

        store.save(participant);

        // Capture and print all debug calls for diagnosis
        var captor = org.mockito.ArgumentCaptor.forClass(String.class);
        verify(monitor, atLeastOnce()).debug(captor.capture());
        System.out.println("Monitor debug calls: " + captor.getAllValues());
    }

    // Additional tests for findById, findAll, update, delete, edge cases should be added here.
}
