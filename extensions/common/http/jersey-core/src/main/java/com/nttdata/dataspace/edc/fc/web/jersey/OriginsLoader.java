package com.nttdata.dataspace.edc.fc.web.jersey;

import com.nttdata.dataspace.edc.fc.store.sql.Participant;
import com.nttdata.dataspace.edc.fc.store.sql.ParticipantStore;

import org.eclipse.edc.spi.monitor.Monitor;
import org.jetbrains.annotations.NotNull;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Service responsible for loading and managing participant origins from DB.
 * Origin Extraction:
 * Origins are extracted as scheme + host + port (if non-default):
 * - https://example.com:8080/api/v1/dsp → https://example.com:8080
 * - https://example.com/api → https://example.com (default port 443 omitted)
 * - http://localhost:9090/dsp → http://localhost:9090
 */
class OriginsLoader {

    private final Monitor monitor;
    private final ParticipantStore participantStore;

    /**
     * Creates a new origin loader.
     *
     * @param monitor the monitor for logging
     * @param participantStore participant db store
     */
    OriginsLoader(@NotNull Monitor monitor, @NotNull ParticipantStore participantStore) {
        this.monitor = monitor;
        this.participantStore = participantStore;
    }

    /**
     * Loads participant origins from database.
     * 
     * @return Set of participant origins extracted from the database, or empty set if loading fails
     */
    @NotNull
    Set<String> loadOrigins() {
        try {
            Set<Participant> participants =
                participantStore.findAll().collect(Collectors.toSet());

            if (participants == null || participants.isEmpty()) {
                monitor.warning(String.format(
                        "%s: Participants list is empty. No origins loaded.",
                        this.getClass().getSimpleName()
                ));
                return Collections.emptySet();
            }

            var origins = extractOriginsFromParticipants(participants);
            
            monitor.info(String.format(
                    "%s: Successfully loaded %d participant origin(s) from database",
                    this.getClass().getSimpleName(),
                    origins.size()
            ));
            
            return origins;

        } catch (RuntimeException e) {
            monitor.severe(String.format(
                    "%s: Failed to read participants from database: %s. " +
                    "Participant-based CORS validation disabled.",
                    this.getClass().getSimpleName(),
                    e.getMessage()
            ), e);
            return Collections.emptySet();
        }
    }

    /**
     * Extracts HTTP origins from a map of participant URLs.
     *
     * @param participants map of participant
     * @return set of extracted origins
     */
    @NotNull
    private Set<String> extractOriginsFromParticipants(@NotNull Set<Participant> participants) {
        Set<String> origins = new HashSet<>();

        for (Participant entry : participants) {
            String participantId = entry.getId();
            String url = entry.getEndpoint();

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