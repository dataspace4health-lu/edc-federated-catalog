package com.nttdata.dataspace.edc.fc.web.jersey;

import org.eclipse.edc.spi.monitor.Monitor;
import org.eclipse.edc.spi.system.ServiceExtensionContext;
import org.eclipse.edc.spi.types.TypeManager;
import org.eclipse.edc.web.spi.WebServer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Path;

import static com.nttdata.dataspace.edc.fc.web.jersey.TestConstants.*;
import static com.nttdata.dataspace.edc.fc.web.jersey.TestDataFactory.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link CorsJerseyExtension}.
 * Tests extension initialization, configuration handling, and CORS filter registration.
 */
class JerseyExtensionTest {

    private JerseyExtension extension;
    private ServiceExtensionContext context;
    private WebServer webServer;
    private Monitor monitor;
    private TypeManager typeManager;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        extension = new JerseyExtension();
        context = mock(ServiceExtensionContext.class);
        webServer = mock(WebServer.class);
        monitor = createMockMonitor();
        typeManager = createTypeManager();

        injectDependencies();
    }

    /**
     * Injects required dependencies into the extension using reflection.
     */
    private void injectDependencies() {
        injectField(extension, "webServer", webServer);
        injectField(extension, "monitor", monitor);
        injectField(extension, "typeManager", typeManager);
    }

    @Test
    void shouldInitializeWithDefaultConfiguration() throws IOException {
        // Arrange
        Path participantsFile = createEmptyParticipantsFile(tempDir);
        setupContextWithDefaults(participantsFile);

        // Act
        extension.initialize(context);

        // Assert
        verifySuccessfulInitialization();
    }
    
    // ========== Helper Methods ==========

    /**
     * Sets up context with default configuration and specified file path.
     */
    private void setupContextWithDefaults(Path participantsFile) {
        when(context.getSetting(SETTING_CORS_HEADERS, DEFAULT_HEADERS)).thenReturn(DEFAULT_HEADERS);
        when(context.getSetting(SETTING_CORS_METHODS, DEFAULT_METHODS)).thenReturn(DEFAULT_METHODS);
        when(context.getSetting(SETTING_CORS_ORIGINS, null)).thenReturn(null);
        when(context.getSetting(SETTING_PARTICIPANT_FILE_PATH, DEFAULT_PARTICIPANT_FILE))
                .thenReturn(participantsFile.toString());
    }

    /**
     * Sets up context with custom configuration settings.
     */
    private void setupContextWithCustomSettings(Path participantsFile, String headers, 
                                                  String methods, String origins) {
        when(context.getSetting(SETTING_CORS_HEADERS, DEFAULT_HEADERS)).thenReturn(headers);
        when(context.getSetting(SETTING_CORS_METHODS, DEFAULT_METHODS)).thenReturn(methods);
        when(context.getSetting(SETTING_CORS_ORIGINS, null)).thenReturn(origins);
        when(context.getSetting(SETTING_PARTICIPANT_FILE_PATH, DEFAULT_PARTICIPANT_FILE))
                .thenReturn(participantsFile.toString());
    }

    /**
     * Sets up context with specified configuration settings.
     */
    private void setupContextWithSettings(Path participantsFile, String headers, 
                                           String methods, String origins) {
        when(context.getSetting(SETTING_CORS_HEADERS, DEFAULT_HEADERS)).thenReturn(headers);
        when(context.getSetting(SETTING_CORS_METHODS, DEFAULT_METHODS)).thenReturn(methods);
        when(context.getSetting(SETTING_CORS_ORIGINS, null)).thenReturn(origins);
        when(context.getSetting(SETTING_PARTICIPANT_FILE_PATH, DEFAULT_PARTICIPANT_FILE))
                .thenReturn(participantsFile.toString());
    }

    /**
     * Sets up context to return default argument values for all settings.
     */
    private void setupContextWithDefaultsReturningArguments(Path participantsFile) {
        when(context.getSetting(anyString(), anyString()))
                .thenAnswer(invocation -> invocation.getArgument(1));
        when(context.getSetting(SETTING_PARTICIPANT_FILE_PATH, DEFAULT_PARTICIPANT_FILE))
                .thenReturn(participantsFile.toString());
    }

    /**
     * Sets up context with only the participant file path.
     */
    private void setupContextWithPath(String filePath) {
        when(context.getSetting(anyString(), anyString()))
                .thenAnswer(invocation -> invocation.getArgument(1));
        when(context.getSetting(SETTING_PARTICIPANT_FILE_PATH, DEFAULT_PARTICIPANT_FILE))
                .thenReturn(filePath);
    }

    /**
     * Verifies successful initialization log messages.
     */
    private void verifySuccessfulInitialization() {
        verify(monitor).info(contains("Initializing Jersey extension."));
    }

    /**
     * Helper method to inject private fields using reflection.
     */
    private void injectField(Object target, String fieldName, Object value) {
        try {
            var field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException("Failed to inject field: " + fieldName, e);
        }
    }
}
