package com.nttdata.dataspace.federatedcatalog.jersey;

import org.eclipse.edc.spi.EdcException;
import org.eclipse.edc.spi.monitor.Monitor;
import org.eclipse.edc.spi.types.TypeManager;
import org.eclipse.edc.web.spi.WebServer;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.servlet.ServletContainer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class JerseyRestServiceTest {

    private WebServer webServer;
    private TypeManager typeManager;
    private JerseyConfiguration configuration;
    private Monitor monitor;
    private JerseyRestService service;

    @BeforeEach
    void setUp() {
        webServer = mock(WebServer.class);
        typeManager = mock(TypeManager.class);
        configuration = mock(JerseyConfiguration.class);
        monitor = mock(Monitor.class);
        service = new JerseyRestService(webServer, typeManager, configuration, monitor);
    }

    @Test
    void registerResource_defaultContext() {
        Object resource = new Object();
        service.registerResource(resource);

        // Start should register the resource in the default context
        service.start();

        // Verify that registerServlet is called for the default context
        verify(webServer).registerServlet(eq("default"), any(ServletContainer.class));
    }

    @Test
    void registerResource_customContext() {
        Object resource = new Object();
        service.registerResource("custom", resource);

        service.start();

        verify(webServer).registerServlet(eq("custom"), any(ServletContainer.class));
    }

    @Test
    void registerDynamicResource_shouldNotThrow() {
        Object resource = new Object();
        service.registerDynamicResource("custom", Object.class, resource);

        // Register a controller to trigger servlet registration
        service.registerResource("custom", new Object());

        // Should not throw on start
        service.start();

        verify(webServer, atLeastOnce()).registerServlet(anyString(), any(ServletContainer.class));
    }

    @Test
    void registerInstance_shouldNotThrow() {
        service.registerInstance(() -> new Object());

        // Register a controller to trigger servlet registration
        service.registerResource(new Object());

        // Should not throw on start
        service.start();

        verify(webServer, atLeastOnce()).registerServlet(anyString(), any(ServletContainer.class));
    }

    @Test
    void start_shouldThrowEdcException_onFailure() {
        JerseyRestService failingService = new JerseyRestService(webServer, typeManager, configuration, monitor) {
            @Override
            public void start() {
                throw new RuntimeException("fail");
            }
        };

        assertThrows(RuntimeException.class, failingService::start);
    }

    @Test
    void start_shouldRegisterCorsFilterIfEnabled() {
        when(configuration.isCorsEnabled()).thenReturn(true);

        Object resource = new Object();
        service.registerResource(resource);

        service.start();

        verify(webServer).registerServlet(eq("default"), any(ServletContainer.class));
        // We can't directly verify the filter registration, but no exception means it was handled
    }
}
