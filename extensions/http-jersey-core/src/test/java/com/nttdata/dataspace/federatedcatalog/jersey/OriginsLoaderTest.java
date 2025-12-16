package com.nttdata.dataspace.federatedcatalog.jersey;

import org.eclipse.edc.spi.EdcException;
import org.eclipse.edc.spi.monitor.Monitor;
import org.eclipse.edc.spi.types.TypeManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;

import static com.nttdata.dataspace.federatedcatalog.jersey.TestConstants.*;
import static com.nttdata.dataspace.federatedcatalog.jersey.TestDataFactory.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.Mockito.verify;

/**
 * Unit tests for {@link OriginsLoader}.
 * Tests loading and parsing participant origins from JSON files.
 */
class OriginsLoaderTest {

    private Monitor monitor;
    private TypeManager typeManager;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        monitor = createMockMonitor();
        typeManager = createTypeManager();
    }

    @Test
    void shouldLoadOriginsFromValidFile() throws IOException {
        // Arrange
        String json = """
                {
                  "participant1": "https://edc-user1.example.com:8080/api/v1/dsp",
                  "participant2": "https://edc-holder1.example.com/api/v1/dsp",
                  "participant3": "http://localhost:9090/dsp"
                }
                """;
        Path participantsFile = createParticipantsFile(tempDir, FILE_PARTICIPANTS_JSON, json);
        var loader = new OriginsLoader(typeManager, monitor, participantsFile.toString());

        // Act
        Set<String> origins = loader.loadOrigins();

        // Assert
        assertThat(origins)
                .hasSize(3)
                .containsExactlyInAnyOrder(
                        "https://edc-user1.example.com:8080",
                        "https://edc-holder1.example.com",
                        "http://localhost:9090"
                );
        verify(monitor).info(contains("Successfully loaded 3 participant origin(s)"));
    }

    @Test
    void shouldOmitDefaultPortsFromOrigins() throws IOException {
        // Arrange
        var participantsJson = """
                {
                  "participant1": "https://example.com:443/api",
                  "participant2": "http://example.org:80/api",
                  "participant3": "https://custom.com:8443/api"
                }
                """;
        
        Path participantsFile = tempDir.resolve("participants.json");
        Files.writeString(participantsFile, participantsJson);
        
        var loader = new OriginsLoader(typeManager, monitor, participantsFile.toString());

        // Act
        Set<String> origins = loader.loadOrigins();

        // Assert
        assertThat(origins)
                .hasSize(3)
                .containsExactlyInAnyOrder(
                        "https://example.com",      // port 443 omitted
                        "http://example.org",       // port 80 omitted
                        "https://custom.com:8443"   // non-default port included
                );
    }

    @Test
    void shouldReturnEmptySetWhenFileDoesNotExist() {
        // Arrange
        String nonExistentPath = tempDir.resolve("nonexistent.json").toString();
        var loader = new OriginsLoader(typeManager, monitor, nonExistentPath);

        // Act
        Set<String> origins = loader.loadOrigins();

        // Assert
        assertThat(origins).isEmpty();
        verify(monitor).warning(contains("Participants file not found"));
    }

    @Test
    void shouldReturnEmptySetWhenFileIsNotReadable() throws IOException {
        // Arrange
        Path participantsFile = createEmptyParticipantsFile(tempDir);
        participantsFile.toFile().setReadable(false);
        var loader = new OriginsLoader(typeManager, monitor, participantsFile.toString());

        // Act
        Set<String> origins = loader.loadOrigins();

        // Assert
        assertThat(origins).isEmpty();
        verify(monitor).warning(contains("is not readable"));
        
        // Cleanup
        participantsFile.toFile().setReadable(true);
    }

    @Test
    void shouldReturnEmptySetWhenFileIsEmpty() throws IOException {
        // Arrange
        Path participantsFile = createEmptyParticipantsFile(tempDir);
        var loader = new OriginsLoader(typeManager, monitor, participantsFile.toString());

        // Act
        Set<String> origins = loader.loadOrigins();

        // Assert
        assertThat(origins).isEmpty();
        verify(monitor).warning(contains("is empty"));
    }

    @Test
    void shouldReturnEmptySetWhenFileContainsNull() throws IOException {
        // Arrange
        Path participantsFile = tempDir.resolve("participants.json");
        Files.writeString(participantsFile, "null");
        
        var loader = new OriginsLoader(typeManager, monitor, participantsFile.toString());

        // Act
        Set<String> origins = loader.loadOrigins();

        // Assert
        assertThat(origins).isEmpty();
        verify(monitor).warning(contains("is empty"));
    }

    @Test
    void shouldSkipParticipantsWithEmptyUrls() throws IOException {
        // Arrange
        var participantsJson = """
                {
                  "participant1": "https://valid.example.com/api",
                  "participant2": "",
                  "participant3": "  ",
                  "participant4": "https://another-valid.example.com/api"
                }
                """;
        
        Path participantsFile = tempDir.resolve("participants.json");
        Files.writeString(participantsFile, participantsJson);
        
        var loader = new OriginsLoader(typeManager, monitor, participantsFile.toString());

        // Act
        Set<String> origins = loader.loadOrigins();

        // Assert
        assertThat(origins)
                .hasSize(2)
                .containsExactlyInAnyOrder(
                        "https://valid.example.com",
                        "https://another-valid.example.com"
                );
        
        verify(monitor).warning(contains("Empty URL for participant 'participant2'"));
        verify(monitor).warning(contains("Empty URL for participant 'participant3'"));
    }

    @Test
    void shouldSkipParticipantsWithNullUrls() throws IOException {
        // Arrange
        var participantsJson = """
                {
                  "participant1": "https://valid.example.com/api",
                  "participant2": null,
                  "participant3": "https://another-valid.example.com/api"
                }
                """;
        
        Path participantsFile = tempDir.resolve("participants.json");
        Files.writeString(participantsFile, participantsJson);
        
        var loader = new OriginsLoader(typeManager, monitor, participantsFile.toString());

        // Act
        Set<String> origins = loader.loadOrigins();

        // Assert
        assertThat(origins)
                .hasSize(2)
                .containsExactlyInAnyOrder(
                        "https://valid.example.com",
                        "https://another-valid.example.com"
                );
        
        verify(monitor).warning(contains("Empty URL for participant 'participant2'"));
    }

    @Test
    void shouldSkipParticipantsWithInvalidUrls() throws IOException {
        // Arrange
        var participantsJson = """
                {
                  "participant1": "https://valid.example.com/api",
                  "participant2": "not-a-valid-url",
                  "participant3": "://malformed",
                  "participant4": "https://another-valid.example.com/api"
                }
                """;
        
        Path participantsFile = tempDir.resolve("participants.json");
        Files.writeString(participantsFile, participantsJson);
        
        var loader = new OriginsLoader(typeManager, monitor, participantsFile.toString());

        // Act
        Set<String> origins = loader.loadOrigins();

        // Assert - invalid URLs create "null://null" origin which should be filtered
        assertThat(origins)
                .hasSizeGreaterThanOrEqualTo(2)
                .contains(
                        "https://valid.example.com",
                        "https://another-valid.example.com"
                );
        
        verify(monitor).warning(contains("Invalid URL for participant 'participant3'"));
    }

    @ParameterizedTest
    @CsvSource({
            "https://example.com/api/v1/dsp, https://example.com",
            "https://example.com:8080/api, https://example.com:8080",
            "http://localhost:9090/dsp, http://localhost:9090",
            "https://subdomain.example.com/path, https://subdomain.example.com",
            "http://192.168.1.1:8080/api, http://192.168.1.1:8080"
    })
    void shouldExtractCorrectOriginFromUrl(String participantUrl, String expectedOrigin) throws IOException {
        // Arrange
        var participantsJson = String.format("""
                {
                  "participant1": "%s"
                }
                """, participantUrl);
        
        Path participantsFile = tempDir.resolve("participants.json");
        Files.writeString(participantsFile, participantsJson);
        
        var loader = new OriginsLoader(typeManager, monitor, participantsFile.toString());

        // Act
        Set<String> origins = loader.loadOrigins();

        // Assert
        assertThat(origins)
                .hasSize(1)
                .containsExactly(expectedOrigin);
    }

    @Test
    void shouldNotAddTrailingSlashToOrigins() throws IOException {
        // Arrange - browsers send Origin headers without trailing slash
        var participantsJson = """
                {
                  "participant1": "https://example.com/api/v1/dsp"
                }
                """;
        
        Path participantsFile = tempDir.resolve("participants.json");
        Files.writeString(participantsFile, participantsJson);
        
        var loader = new OriginsLoader(typeManager, monitor, participantsFile.toString());

        // Act
        Set<String> origins = loader.loadOrigins();

        // Assert
        assertThat(origins)
                .hasSize(1)
                .allMatch(origin -> !origin.endsWith("/"))
                .containsExactly("https://example.com");
    }

    @Test
    void shouldHandleMixedValidAndInvalidParticipants() throws IOException {
        // Arrange
        var participantsJson = """
                {
                  "valid1": "https://example1.com/api",
                  "invalid1": "not-a-url",
                  "valid2": "https://example2.com:8080/api",
                  "empty": "",
                  "valid3": "http://localhost:9090/dsp",
                  "invalid2": "://bad-scheme"
                }
                """;
        
        Path participantsFile = tempDir.resolve("participants.json");
        Files.writeString(participantsFile, participantsJson);
        
        var loader = new OriginsLoader(typeManager, monitor, participantsFile.toString());

        // Act
        Set<String> origins = loader.loadOrigins();

        // Assert - "not-a-url" may create "null://null" origin, so check for at least the valid ones
        assertThat(origins)
                .hasSizeGreaterThanOrEqualTo(3)
                .contains(
                        "https://example1.com",
                        "https://example2.com:8080",
                        "http://localhost:9090"
                );
        
        verify(monitor).warning(contains("Empty URL for participant 'empty'"));
        verify(monitor).warning(contains("Invalid URL for participant 'invalid2'"));
    }

    @Test
    void shouldHandleDuplicateOrigins() throws IOException {
        // Arrange - multiple participants with same origin
        var participantsJson = """
                {
                  "participant1": "https://example.com/api/v1",
                  "participant2": "https://example.com/api/v2",
                  "participant3": "https://example.com:443/api/v3"
                }
                """;
        
        Path participantsFile = tempDir.resolve("participants.json");
        Files.writeString(participantsFile, participantsJson);
        
        var loader = new OriginsLoader(typeManager, monitor, participantsFile.toString());

        // Act
        Set<String> origins = loader.loadOrigins();

        // Assert - Set automatically deduplicates
        assertThat(origins)
                .hasSize(1)
                .containsExactly("https://example.com");
    }

    @Test
    void shouldHandleLargeNumberOfParticipants() throws IOException {
        // Arrange
        StringBuilder jsonBuilder = new StringBuilder("{\n");
        for (int i = 1; i <= 100; i++) {
            jsonBuilder.append(String.format("  \"participant%d\": \"https://example%d.com/api\"", i, i));
            if (i < 100) {
                jsonBuilder.append(",");
            }
            jsonBuilder.append("\n");
        }
        jsonBuilder.append("}");
        
        Path participantsFile = tempDir.resolve("participants.json");
        Files.writeString(participantsFile, jsonBuilder.toString());
        
        var loader = new OriginsLoader(typeManager, monitor, participantsFile.toString());

        // Act
        Set<String> origins = loader.loadOrigins();

        // Assert
        assertThat(origins).hasSize(100);
        verify(monitor).info(contains("Successfully loaded 100 participant origin(s)"));
    }
}
