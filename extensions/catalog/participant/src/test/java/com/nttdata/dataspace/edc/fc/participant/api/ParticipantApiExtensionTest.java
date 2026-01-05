package com.nttdata.dataspace.edc.fc.participant.api;

import com.nttdata.dataspace.edc.fc.store.sql.ParticipantStore;
import org.eclipse.edc.spi.monitor.Monitor;
import org.eclipse.edc.spi.system.ServiceExtensionContext;
import org.eclipse.edc.spi.types.TypeManager;
import org.eclipse.edc.web.spi.WebService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.*;

class ParticipantApiExtensionTest {

    private WebService webService;
    private TypeManager typeManager;
    private Monitor monitor;
    private ParticipantStore participantStore;
    private ServiceExtensionContext context;
    private ParticipantApiExtension extension;

    @BeforeEach
    void setUp() {
        webService = mock(WebService.class);
        typeManager = mock(TypeManager.class);
        monitor = mock(Monitor.class);
        participantStore = mock(ParticipantStore.class);
        context = mock(ServiceExtensionContext.class);

        extension = new ParticipantApiExtension();

        // Use reflection to inject mocks into private fields
        injectField(extension, "webService", webService);
        injectField(extension, "typeManager", typeManager);
        injectField(extension, "monitor", monitor);
        injectField(extension, "participantStore", participantStore);
    }

    @Test
    void name_returnsCorrectName() {
        assert(extension.name().equals("Participant Store API"));
    }

    @Test
    void initialize_registersResourcesAndLogs() {
        var config = mock(org.eclipse.edc.spi.system.configuration.Config.class);
        when(context.getConfig()).thenReturn(config);
        when(config.getString(eq("web.http.catalog.path"), anyString())).thenReturn("/api/catalog");

        extension.initialize(context);

        verify(monitor).info(contains("Initializing"));
        verify(webService, times(2)).registerResource(eq("catalog"), any());
        verify(monitor, atLeastOnce()).debug(contains("catalog path"));
        verify(monitor, atLeastOnce()).debug(contains("registered successfully"));
        verify(monitor, atLeastOnce()).debug(contains("Swagger UI available"));
    }

    // Helper to inject mocks into private fields
    private static void injectField(Object target, String fieldName, Object value) {
        try {
            var field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
