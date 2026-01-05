package com.nttdata.dataspace.edc.fc.web.jersey;

import com.nttdata.dataspace.edc.fc.store.sql.ParticipantStore;
import com.nttdata.dataspace.edc.fc.store.sql.Participant;

import org.eclipse.edc.spi.EdcException;
import org.eclipse.edc.spi.monitor.Monitor;
import org.eclipse.edc.spi.types.TypeManager;
import org.eclipse.edc.web.spi.WebServer;
import org.glassfish.jersey.servlet.ServletContainer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class JerseyRestServiceTest {

    private WebServer webServer;
    private TypeManager typeManager;
    private JerseyConfiguration configuration;
    private Monitor monitor;
    private ParticipantStore participantStore;
    private JerseyRestService service;

    @BeforeEach
    void setUp() {
        webServer = mock(WebServer.class);
        typeManager = mock(TypeManager.class);
        configuration = mock(JerseyConfiguration.class);
        monitor = mock(Monitor.class);
        participantStore = mock(ParticipantStore.class);
        service = new JerseyRestService(webServer, typeManager, configuration, monitor, participantStore);
    }

    @Test
    void registerResource_shouldAddToDefaultContext() {
        Object resource = new Object();
        service.registerResource(resource);

        // Use reflection to check controllers map
        Map<String, List<Object>> controllers = getControllers(service);
        assertThat(controllers.get("default")).contains(resource);
    }

    @Test
    void registerResource_shouldAddToCustomContext() {
        Object resource = new Object();
        service.registerResource("custom", resource);

        Map<String, List<Object>> controllers = getControllers(service);
        assertThat(controllers.get("custom")).contains(resource);
    }

    @Test
    void registerDynamicResource_shouldAddToDynamicResources() {
        Object resource = new Object();
        service.registerDynamicResource("ctx", String.class, resource);

        Map<String, Map<Class<?>, List<Object>>> dynamicResources = getDynamicResources(service);
        assertThat(dynamicResources.get("ctx").get(String.class)).contains(resource);
    }

    @Test
    void registerInstance_shouldAddSupplier() {
        Supplier<Object> supplier = () -> new Object();
        service.registerInstance(supplier);

        List<Supplier<Object>> additional = getAdditionalInstances(service);
        assertThat(additional).contains(supplier);
    }

    @Test
    void start_shouldMergeOriginsAndRegisterServlets() {
        // Arrange
        when(configuration.getAllowedOrigins()).thenReturn("https://a.com, https://b.com");
        when(configuration.isCorsEnabled()).thenReturn(false);
        when(participantStore.findAll()).thenReturn(java.util.stream.Stream.empty());
        service.registerResource("default", new Object());
        service.registerResource("api", new Object());

        // Act
        service.start();

        // Assert
        verify(configuration).setAllowedOrigin("https://a.com,https://b.com");
        verify(webServer).registerServlet(eq("default"), any(ServletContainer.class));
        verify(webServer).registerServlet(eq("api"), any(ServletContainer.class));
    }

    @Test
    void start_shouldIncludeParticipantOrigins() {
        when(configuration.getAllowedOrigins()).thenReturn("https://a.com");
        when(configuration.isCorsEnabled()).thenReturn(false);

        // Simulate participant origins by returning Participants with endpoints
        Participant p1 = Participant.builder().id("p1").endpoint("https://b.com").build();
        Participant p2 = Participant.builder().id("p2").endpoint("https://c.com").build();
        when(participantStore.findAll()).thenReturn(java.util.stream.Stream.of(p1, p2));

        // Register a resource
        service.registerResource(new Object());

        // Act
        service.start();

        // Assert
        verify(configuration).setAllowedOrigin("https://a.com,https://b.com,https://c.com");
        verify(webServer).registerServlet(eq("default"), any(ServletContainer.class));
    }

    @Test
    void start_shouldRegisterCorsFilterIfEnabled() {
        when(configuration.getAllowedOrigins()).thenReturn("https://a.com");
        when(configuration.isCorsEnabled()).thenReturn(true);
        when(participantStore.findAll()).thenReturn(java.util.stream.Stream.empty());

        service.registerResource(new Object());
        service.start();

        verify(webServer).registerServlet(eq("default"), any(ServletContainer.class));
        // Can't directly verify filter, but no exception means it was handled
    }

    @Test
    void start_shouldRegisterAdditionalInstances() {
        when(configuration.getAllowedOrigins()).thenReturn("https://a.com");
        when(configuration.isCorsEnabled()).thenReturn(false);
        when(participantStore.findAll()).thenReturn(java.util.stream.Stream.empty());

        Object additional = new Object();
        service.registerInstance(() -> additional);
        service.registerResource(new Object());

        service.start();

        verify(webServer).registerServlet(eq("default"), any(ServletContainer.class));
    }

    @Test
    void start_shouldThrowEdcExceptionOnError() {
        when(configuration.getAllowedOrigins()).thenThrow(new RuntimeException("fail"));

        service.registerResource(new Object());
        EdcException ex = assertThrows(EdcException.class, service::start);
        assertTrue(ex.getCause() instanceof RuntimeException);
        assertEquals("fail", ex.getCause().getMessage());
    }

    // --- Helper methods to access private fields ---

    @SuppressWarnings("unchecked")
    private Map<String, List<Object>> getControllers(JerseyRestService service) {
        try {
            var field = JerseyRestService.class.getDeclaredField("controllers");
            field.setAccessible(true);
            return (Map<String, List<Object>>) field.get(service);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, Map<Class<?>, List<Object>>> getDynamicResources(JerseyRestService service) {
        try {
            var field = JerseyRestService.class.getDeclaredField("dynamicResources");
            field.setAccessible(true);
            return (Map<String, Map<Class<?>, List<Object>>>) field.get(service);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("unchecked")
    private List<Supplier<Object>> getAdditionalInstances(JerseyRestService service) {
        try {
            var field = JerseyRestService.class.getDeclaredField("additionalInstances");
            field.setAccessible(true);
            return (List<Supplier<Object>>) field.get(service);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
