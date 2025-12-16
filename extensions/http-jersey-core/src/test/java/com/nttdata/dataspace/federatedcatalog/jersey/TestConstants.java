package com.nttdata.dataspace.federatedcatalog.jersey;

/**
 * Test constants shared across CORS extension test classes.
 * Centralizes configuration keys, default values, and common test data.
 */
public final class TestConstants {

    // Configuration Setting Keys
    public static final String SETTING_CORS_HEADERS = "edc.web.rest.cors.headers";
    public static final String SETTING_CORS_METHODS = "edc.web.rest.cors.methods";
    public static final String SETTING_CORS_ORIGINS = "edc.web.rest.cors.origins";
    public static final String SETTING_PARTICIPANT_FILE_PATH = "edc.federated.catalog.cors.participant.file.path";

    // Default Configuration Values
    public static final String DEFAULT_HEADERS = "origin, content-type, accept, authorization, x-api-key";
    public static final String DEFAULT_METHODS = "POST, OPTIONS";
    public static final String DEFAULT_PARTICIPANT_FILE = "/etc/config/participants.json";
    public static final String DEFAULT_CONTEXT = "catalog";

    // Test URLs and Origins
    public static final String TEST_ORIGIN_1 = "https://example1.com";
    public static final String TEST_ORIGIN_2 = "https://example2.com";
    public static final String TEST_ORIGIN_3 = "https://example3.com";
    public static final String TEST_ORIGIN_UNAUTHORIZED = "https://unauthorized.com";
    public static final String TEST_ORIGIN_LOCALHOST = "http://localhost";
    public static final String TEST_ORIGIN_LOCALHOST_3000 = "http://localhost:3000";
    public static final String TEST_ORIGIN_LOCALHOST_8080 = "http://localhost:8080";
    public static final String TEST_ORIGIN_WILDCARD = "*";

    // Test Participant URLs
    public static final String TEST_PARTICIPANT_URL_1 = "https://participant1.com/api";
    public static final String TEST_PARTICIPANT_URL_2 = "https://participant2.com/api";
    public static final String TEST_PARTICIPANT_ORIGIN_1 = "https://participant1.com";
    public static final String TEST_PARTICIPANT_ORIGIN_2 = "https://participant2.com";

    // HTTP Methods
    public static final String METHOD_POST = "POST";
    public static final String METHOD_OPTIONS = "OPTIONS";
    public static final String METHOD_GET = "GET";

    // HTTP Headers
    public static final String HEADER_CONTENT_TYPE = "content-type";
    public static final String HEADER_ACCEPT = "accept";
    public static final String HEADER_AUTHORIZATION = "authorization";
    public static final String HEADER_CREDENTIALS_VALUE = "true";

    // Test Messages
    public static final String MSG_EMPTY_HEADERS = "Empty allowed headers configuration";
    public static final String MSG_EMPTY_METHODS = "Empty allowed methods configuration";
    public static final String MSG_MISSING_OPTIONS = "do not include 'OPTIONS'";
    public static final String MSG_MISSING_POST = "do not include 'POST'";
    public static final String MSG_PREFLIGHT_FAIL = "Preflight requests will fail";
    public static final String MSG_CATALOG_QUERY_FAIL = "Catalog query requests will fail";

    // File Names
    public static final String FILE_PARTICIPANTS_JSON = "participants.json";
    public static final String FILE_PARTICIPANTS_EMPTY = "participants-empty.json";
    public static final String FILE_PARTICIPANTS_INVALID = "participants-invalid.json";
    public static final String FILE_PARTICIPANTS_VALID = "participants-valid.json";
    public static final String FILE_PARTICIPANTS_MIXED = "participants-mixed.json";

    // Numeric Constants
    public static final int MAX_FUNCTION_LINES = 50;
    public static final int MAX_CLASS_LINES = 300;
    public static final int PORT_HTTP_DEFAULT = 80;
    public static final int PORT_HTTPS_DEFAULT = 443;
    public static final int PORT_8080 = 8080;
    public static final int PORT_9090 = 9090;

    private TestConstants() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }
}
