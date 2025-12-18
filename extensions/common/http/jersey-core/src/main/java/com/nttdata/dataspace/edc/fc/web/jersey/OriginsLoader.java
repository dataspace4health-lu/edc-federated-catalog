package com.nttdata.dataspace.edc.fc.web.jersey;

import org.eclipse.edc.spi.monitor.Monitor;
import org.eclipse.edc.spi.types.TypeManager;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Service responsible for loading and managing participant origins from a JSON file.
 * 
 * This loader reads a JSON file containing participant information (participant ID to DSP URL mappings)
 * and extracts HTTP origins from the URLs for CORS validation purposes.
 * 
 * Expected File Format:
 * {
 *   "participant1": "https://example.com:8080/api/v1/dsp",
 *   "participant2": "http://localhost:9090/dsp"
 * }
 * 
 * Origin Extraction:
 * Origins are extracted as scheme + host + port (if non-default):
 * - https://example.com:8080/api/v1/dsp → https://example.com:8080
 * - https://example.com/api → https://example.com (default port 443 omitted)
 * - http://localhost:9090/dsp → http://localhost:9090
 */
class OriginsLoader {

    private final TypeManager typeManager;
    private final Monitor monitor;
    private final String filePath;

    /**
     * Creates a new origin loader.
     *
     * @param typeManager the type manager for JSON deserialization
     * @param monitor the monitor for logging
     * @param filePath the absolute path to the participants JSON file
     */
    OriginsLoader(@NotNull TypeManager typeManager, @NotNull Monitor monitor, @NotNull String filePath) {
        this.typeManager = typeManager;
        this.monitor = monitor;
        this.filePath = filePath;
    }

    /**
     * Loads participant origins from the configured file path.
     * 
     * This method is designed to be fault-tolerant and will return an empty set if the file
     * doesn't exist, cannot be read, or contains invalid data. All errors are logged but do not
     * throw exceptions to prevent disrupting the CORS extension initialization.
     *
     * @return Set of participant origins extracted from the file, or empty set if loading fails
     */
    @NotNull
    Set<String> loadOrigins() {
        File participantFile = new File(filePath);

        if (!participantFile.exists()) {
            monitor.warning(String.format(
                    "%s: Participants file not found at '%s'. Participant-based CORS validation disabled.",
                    this.getClass().getSimpleName(),
                    filePath
            ));
            return Collections.emptySet();
        }

        if (!participantFile.canRead()) {
            monitor.warning(String.format(
                    "%s: Participants file at '%s' is not readable. Check file permissions. " +
                    "Participant-based CORS validation disabled.",
                    this.getClass().getSimpleName(),
                    filePath
            ));
            return Collections.emptySet();
        }

        try {
            String content = Files.readString(participantFile.toPath());
            @SuppressWarnings("unchecked")
            Map<String, String> participants = typeManager.readValue(content, Map.class);

            if (participants == null || participants.isEmpty()) {
                monitor.warning(String.format(
                        "%s: Participants file at '%s' is empty. No origins loaded.",
                        this.getClass().getSimpleName(),
                        filePath
                ));
                return Collections.emptySet();
            }

            var origins = extractOriginsFromParticipants(participants);
            
            monitor.info(String.format(
                    "%s: Successfully loaded %d participant origin(s) from '%s'",
                    this.getClass().getSimpleName(),
                    origins.size(),
                    filePath
            ));
            
            return origins;

        } catch (IOException | RuntimeException e) {
            monitor.severe(String.format(
                    "%s: Failed to read participants file at '%s': %s. " +
                    "Participant-based CORS validation disabled.",
                    this.getClass().getSimpleName(),
                    filePath, e.getMessage()
            ), e);
            return Collections.emptySet();
        }
    }

    /**
     * Extracts HTTP origins from a map of participant URLs.
     *
     * @param participants map of participant ID to URL
     * @return set of extracted origins
     */
    @NotNull
    private Set<String> extractOriginsFromParticipants(@NotNull Map<String, String> participants) {
        Set<String> origins = new HashSet<>();

        for (Map.Entry<String, String> entry : participants.entrySet()) {
            String participantId = entry.getKey();
            String url = entry.getValue();

            if (url == null || url.isBlank()) {
                monitor.warning(String.format(
                        "%s: Empty URL for participant '%s' - Skipping",
                        this.getClass().getSimpleName(),
                        participantId
                ));
                continue;
            }

            try {
                URI uri = new URI(url);
                String origin = buildOrigin(uri);
                origins.add(origin);
                
                monitor.debug(String.format(
                        "%s: Extracted origin '%s' from participant '%s' URL '%s'",
                        this.getClass().getSimpleName(),
                        origin, participantId, url
                ));
                
            } catch (URISyntaxException e) {
                monitor.warning(String.format(
                        "%s: Invalid URL for participant '%s': '%s' - Skipping. Reason: %s",
                        this.getClass().getSimpleName(),
                        participantId, url, e.getMessage()
                ));
            }
        }

        return origins;
    }

    /**
     * Builds an origin string from a URI (scheme + host + port if non-default).
     * 
     * The origin format matches the browser's Origin header format, which does not
     * include a trailing slash. This ensures proper CORS validation when browsers
     * send preflight OPTIONS requests.
     *
     * @param uri the URI to extract origin from
     * @return the origin string without trailing slash (e.g., "https://example.com:8080")
     */
    @NotNull
    private String buildOrigin(@NotNull URI uri) {
        StringBuilder origin = new StringBuilder()
                .append(uri.getScheme())
                .append("://")
                .append(uri.getHost());

        int port = uri.getPort();
        if (port != -1 && !isDefaultPort(uri.getScheme(), port)) {
            origin.append(":").append(port);
        }
        
        // Do NOT add trailing slash - browsers send Origin headers without trailing slash
        
        monitor.debug(String.format(
                "%s: origin generated '%s'",
                this.getClass().getSimpleName(),
                origin.toString()
        ));
        
        return origin.toString();
    }

    /**
     * Checks if the port is the default port for the given scheme.
     *
     * @param scheme the URI scheme (http or https)
     * @param port the port number
     * @return true if the port is the default for the scheme
     */
    private boolean isDefaultPort(String scheme, int port) {
        return (port == 80 && "http".equalsIgnoreCase(scheme)) ||
               (port == 443 && "https".equalsIgnoreCase(scheme));
    }
}