package com.nttdata.dataspace.edc.fc.web.jersey;

import org.eclipse.edc.json.JacksonTypeManager;
import org.eclipse.edc.spi.monitor.Monitor;
import org.eclipse.edc.spi.types.TypeManager;

import jakarta.ws.rs.container.ContainerRequestContext;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.mockito.Mockito.*;

import jakarta.ws.rs.core.MultivaluedHashMap;
import jakarta.ws.rs.core.MultivaluedMap;

/**
 * Factory class for creating test data and mock objects.
 * Provides reusable methods for test setup across different test classes.
 */
public final class TestDataFactory {

    private TestDataFactory() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    /**
     * Creates a mock Monitor instance.
     *
     * @return mocked Monitor
     */
    public static Monitor createMockMonitor() {
        return mock(Monitor.class);
    }

    public static ContainerRequestContext createMockContainerRequestContext() {
        var ctx = mock(ContainerRequestContext.class);

        // Provide a REAL, MUTABLE headers map
        MultivaluedMap<String, String> headers = new MultivaluedHashMap<>();
        when(ctx.getHeaders()).thenReturn(headers);
        
        when(ctx.getHeaderString(anyString())).thenAnswer(inv -> {
            String name = inv.getArgument(0);
            var values = headers.get(name);
            return (values == null || values.isEmpty()) ? null : String.join(",", values);
        });

        return ctx;
    }

    /**
     * Creates a TypeManager instance for testing.
     *
     * @return JacksonTypeManager instance
     */
    public static TypeManager createTypeManager() {
        return new JacksonTypeManager();
    }

    /**
     * Creates a participants JSON file with the specified content.
     *
     * @param tempDir temporary directory
     * @param fileName file name
     * @param content JSON content
     * @return Path to the created file
     * @throws IOException if file creation fails
     */
    public static Path createParticipantsFile(Path tempDir, String fileName, String content) throws IOException {
        Path file = tempDir.resolve(fileName);
        Files.writeString(file, content);
        return file;
    }

    /**
     * Creates an empty participants JSON file.
     *
     * @param tempDir temporary directory
     * @return Path to the created file
     * @throws IOException if file creation fails
     */
    public static Path createEmptyParticipantsFile(Path tempDir) throws IOException {
        return createParticipantsFile(tempDir, TestConstants.FILE_PARTICIPANTS_JSON, "{}");
    }

    /**
     * Creates a participants JSON file with one participant.
     *
     * @param tempDir temporary directory
     * @param participantId participant identifier
     * @param participantUrl participant URL
     * @return Path to the created file
     * @throws IOException if file creation fails
     */
    public static Path createSingleParticipantFile(Path tempDir, String participantId, String participantUrl)
            throws IOException {
        String json = String.format("""
                {
                  "%s": "%s"
                }
                """, participantId, participantUrl);
        return createParticipantsFile(tempDir, TestConstants.FILE_PARTICIPANTS_JSON, json);
    }

    /**
     * Creates a participants JSON file with two participants.
     *
     * @param tempDir temporary directory
     * @return Path to the created file
     * @throws IOException if file creation fails
     */
    public static Path createTwoParticipantsFile(Path tempDir) throws IOException {
        String json = String.format("""
                {
                  "participant1": "%s",
                  "participant2": "%s"
                }
                """, TestConstants.TEST_PARTICIPANT_URL_1, TestConstants.TEST_PARTICIPANT_URL_2);
        return createParticipantsFile(tempDir, TestConstants.FILE_PARTICIPANTS_JSON, json);
    }

    /**
     * Creates a JerseyConfiguration builder with default test settings.
     *
     * @param typeManager type manager
     * @param monitor monitor
     * @return JerseyConfiguration.Builder
     */
    public static JerseyConfiguration.Builder createDefaultConfigBuilder(TypeManager typeManager, Monitor monitor) {
        return JerseyConfiguration.Builder
                .newInstance()
                .allowedOrigins(TestConstants.TEST_ORIGIN_1)
                .allowedHeaders(TestConstants.HEADER_CONTENT_TYPE)
                .allowedMethods(TestConstants.METHOD_POST)
                .corsEnabled(true);
    }

    /**
     * Creates a JerseyConfiguration with wildcard origin.
     *
     * @param typeManager type manager
     * @param monitor monitor
     * @return JerseyConfiguration
     */
    public static JerseyConfiguration createWildcardConfig(TypeManager typeManager, Monitor monitor) {
        return JerseyConfiguration.Builder
                .newInstance()
                .allowedOrigins(TestConstants.TEST_ORIGIN_WILDCARD)
                .allowedHeaders(TestConstants.HEADER_CONTENT_TYPE)
                .allowedMethods(TestConstants.DEFAULT_METHODS)
                .corsEnabled(true)
                .build();
    }

    /**
     * Creates a JerseyConfiguration with multiple origins.
     *
     * @param typeManager type manager
     * @param monitor monitor
     * @param origins comma-separated origins
     * @return JerseyConfiguration
     */
    public static JerseyConfiguration createMultiOriginConfig(TypeManager typeManager, Monitor monitor, String origins) {
        return JerseyConfiguration.Builder
                .newInstance()
                .allowedOrigins(origins)
                .allowedHeaders(TestConstants.HEADER_CONTENT_TYPE)
                .allowedMethods(TestConstants.DEFAULT_METHODS)
                .corsEnabled(true)
                .build();
    }

    /**
     * Creates a JerseyConfiguration with participant file.
     *
     * @param typeManager type manager
     * @param monitor monitor
     * @param participantFilePath path to participant file
     * @return JerseyConfiguration
     */
    public static JerseyConfiguration createConfigWithParticipantFile(
            TypeManager typeManager,
            Monitor monitor,
            String participantFilePath) {
        return JerseyConfiguration.Builder
                .newInstance()
                .allowedOrigins(null)
                .allowedHeaders(TestConstants.HEADER_CONTENT_TYPE)
                .allowedMethods(TestConstants.METHOD_POST)
                .corsEnabled(true)
                .build();
    }
}
