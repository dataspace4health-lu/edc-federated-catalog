package com.nttdata.dataspace.edc.fc.store.sql;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nttdata.dataspace.edc.fc.store.sql.schema.ParticipantStatements;
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
                monitor.debug("Executing transaction block: " + block.getClass() + " loader: " + block.getClass().getClassLoader());
                monitor.debug("TransactionBlock interface: " + TransactionContext.TransactionBlock.class + " loader: " + TransactionContext.TransactionBlock.class.getClassLoader());
                block.execute();
            }

            @Override
            public Object execute(TransactionContext.ResultTransactionBlock block) {
                monitor.debug("Executing result transaction block: " + block.getClass() + " loader: " + block.getClass().getClassLoader());
                Object result = block.execute();
                monitor.debug("Result transaction block result: " + result);
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

        // Setup common statement mocks
        when(statements.getInsertTemplate()).thenReturn("INSERT INTO participant ...");
        when(statements.getUpdateTemplate()).thenReturn("UPDATE participant SET ...");
        when(statements.getFindByIdTemplate()).thenReturn("SELECT * FROM participant WHERE id = ?");
        when(statements.getFindAllTemplate()).thenReturn("SELECT * FROM participant");
        when(statements.getDeleteTemplate()).thenReturn("DELETE FROM participant WHERE id = ?");
        when(statements.getIdColumn()).thenReturn("id");
        when(statements.getNameColumn()).thenReturn("name");
        when(statements.getEndpointColumn()).thenReturn("endpoint");
        when(statements.getSupportedProtocolsColumn()).thenReturn("protocols");
        when(statements.getCreatedAtColumn()).thenReturn("createdAt");
        when(statements.getUpdatedAtColumn()).thenReturn("updatedAt");
    }

    @Test
    void save_shouldInsertParticipant_whenNotExists() {
        Participant participant = createParticipant("id1");
        when(queryExecutor.single(any(), anyBoolean(), any(), any(), any())).thenReturn(null);
        when(queryExecutor.execute(any(), any(), any(), any(), any(), any(), any(), any())).thenReturn(1);

        StoreResult<Void> result = store.save(participant);

        assertTrue(result.succeeded());
        verify(monitor).debug(contains("Inserted participant"));
    }

    @Test
    void save_shouldReturnAlreadyExists_whenParticipantExists() {
        Participant participant = createParticipant("id1");
        when(queryExecutor.single(any(), anyBoolean(), any(), any(), any())).thenReturn(participant);

        StoreResult<Void> result = store.save(participant);

        assertTrue(result.failed());
        assertTrue(result.getFailureDetail().contains("already exists"));
    }

    @Test
    void findById_shouldReturnParticipant_whenExists() {
        Participant expected = createParticipant("id1");
        when(queryExecutor.single(any(), anyBoolean(), any(), any(), any())).thenReturn(expected);

        Participant result = store.findById("id1");

        assertNotNull(result);
        assertEquals("id1", result.getId());
    }

    @Test
    void findById_shouldReturnNull_whenNotExists() {
        when(queryExecutor.single(any(), anyBoolean(), any(), any(), any())).thenReturn(null);

        Participant result = store.findById("nonexistent");

        assertNull(result);
    }

    @Test
    void findAll_shouldReturnAllParticipants() {
        List<Participant> participants = List.of(createParticipant("id1"), createParticipant("id2"));
        doReturn(participants.stream()).when(queryExecutor).query(any(), anyBoolean(), any(), any());

        List<Participant> result = store.findAll().toList();

        assertEquals(2, result.size());
    }

    @Test
    void update_shouldUpdateParticipant_whenExists() {
        Participant participant = createParticipant("id1");
        when(queryExecutor.single(any(), anyBoolean(), any(), any(), any())).thenReturn(participant);
        when(queryExecutor.execute(any(), any(), any(), any(), any(), any(), any())).thenReturn(1);

        StoreResult<Void> result = store.update(participant);

        assertTrue(result.succeeded());
        verify(monitor).debug(contains("Updated participant"));
    }

    @Test
    void update_shouldReturnNotFound_whenParticipantNotExists() {
        Participant participant = createParticipant("id1");
        when(queryExecutor.single(any(), anyBoolean(), any(), any(), any())).thenReturn(null);

        StoreResult<Void> result = store.update(participant);

        assertTrue(result.failed());
        assertTrue(result.getFailureDetail().contains("not found"));
    }

    @Test
    void delete_shouldDeleteParticipant_whenExists() {
        Participant participant = createParticipant("id1");
        when(queryExecutor.single(any(), anyBoolean(), any(), any(), any())).thenReturn(participant);
        when(queryExecutor.execute(any(), any(), any())).thenReturn(1);

        StoreResult<Participant> result = store.delete("id1");

        assertTrue(result.succeeded());
        assertEquals("id1", result.getContent().getId());
        verify(monitor).debug(contains("Deleted participant"));
    }

    @Test
    void delete_shouldReturnNotFound_whenParticipantNotExists() {
        when(queryExecutor.single(any(), anyBoolean(), any(), any(), any())).thenReturn(null);

        StoreResult<Participant> result = store.delete("nonexistent");

        assertTrue(result.failed());
        assertTrue(result.getFailureDetail().contains("not found"));
    }

    private Participant createParticipant(String id) {
        return Participant.builder()
                .id(id)
                .name("Test " + id)
                .endpoint("http://example.com/" + id)
                .supportedProtocols(List.of("http"))
                .createdAt(System.currentTimeMillis())
                .updatedAt(System.currentTimeMillis())
                .build();
    }
}
