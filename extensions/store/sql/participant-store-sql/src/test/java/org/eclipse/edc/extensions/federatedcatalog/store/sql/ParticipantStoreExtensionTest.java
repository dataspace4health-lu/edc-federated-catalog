package org.eclipse.edc.extensions.federatedcatalog.store.sql;

import org.eclipse.edc.spi.system.ServiceExtensionContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ParticipantStoreExtensionTest {

    private ParticipantStoreExtension extension;
    private ServiceExtensionContext context;

    @BeforeEach
    void setUp() throws Exception {
        extension = new ParticipantStoreExtension();
        context = mock(ServiceExtensionContext.class, RETURNS_DEEP_STUBS);

        // Create mocks for all injected fields
        var dataSourceRegistry = mock(org.eclipse.edc.transaction.datasource.spi.DataSourceRegistry.class);
        var transactionContext = mock(org.eclipse.edc.transaction.spi.TransactionContext.class);
        var webService = mock(org.eclipse.edc.web.spi.WebService.class);
        var typeManager = mock(org.eclipse.edc.spi.types.TypeManager.class);
        var queryExecutor = mock(org.eclipse.edc.sql.QueryExecutor.class);
        var sqlSchemaBootstrapper = mock(org.eclipse.edc.sql.bootstrapper.SqlSchemaBootstrapper.class);
        var statements = mock(org.eclipse.edc.extensions.federatedcatalog.store.sql.schema.ParticipantStatements.class);
        var monitor = mock(org.eclipse.edc.spi.monitor.Monitor.class);

        // Set all injected fields via reflection
        java.lang.reflect.Field f;
        f = extension.getClass().getDeclaredField("dataSourceRegistry");
        f.setAccessible(true); f.set(extension, dataSourceRegistry);
        f = extension.getClass().getDeclaredField("transactionContext");
        f.setAccessible(true); f.set(extension, transactionContext);
        f = extension.getClass().getDeclaredField("webService");
        f.setAccessible(true); f.set(extension, webService);
        f = extension.getClass().getDeclaredField("typeManager");
        f.setAccessible(true); f.set(extension, typeManager);
        f = extension.getClass().getDeclaredField("queryExecutor");
        f.setAccessible(true); f.set(extension, queryExecutor);
        f = extension.getClass().getDeclaredField("sqlSchemaBootstrapper");
        f.setAccessible(true); f.set(extension, sqlSchemaBootstrapper);
        f = extension.getClass().getDeclaredField("statements");
        f.setAccessible(true); f.set(extension, statements);
        f = extension.getClass().getDeclaredField("dataSourceName");
        f.setAccessible(true); f.set(extension, "testDataSource");

        // Mock context.getMonitor() and getService for TypeManager
        when(context.getMonitor()).thenReturn(monitor);
        when(typeManager.getMapper()).thenReturn(mock(com.fasterxml.jackson.databind.ObjectMapper.class));
        when(context.getService(eq(org.eclipse.edc.spi.types.TypeManager.class))).thenReturn(typeManager);

        // Stub methods that are called in initialize
        doNothing().when(sqlSchemaBootstrapper).addStatementFromResource(anyString(), anyString());
        doNothing().when(webService).registerResource(anyString(), any());

        // Stub context.getConfig().getString(anyString(), anyString()) to return "/api/catalog"
        when(context.getConfig().getString(anyString(), anyString())).thenReturn("/api/catalog");

        // Call initialize if required by the extension
        extension.initialize(context);
    }

    @Test
    void name_shouldReturnExtensionName() {
        String name = extension.name();
        assertNotNull(name);
        assertTrue(name.toLowerCase().contains("participant"));
    }

    @Test
    void createSqlParticipantStore_shouldReturnParticipantStore() {
        Object store = extension.createSqlParticipantStore(context);
        assertNotNull(store);
        assertTrue(store instanceof ParticipantStore);
    }
}
