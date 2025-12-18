package com.nttdata.dataspace.edc.fc.web.jersey;

import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.core.MultivaluedHashMap;
import jakarta.ws.rs.core.MultivaluedMap;
import org.eclipse.edc.spi.monitor.Monitor;
import org.eclipse.edc.spi.types.TypeManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;

import com.nttdata.dataspace.edc.fc.web.jersey.JerseyConfiguration;

import static com.nttdata.dataspace.edc.fc.web.jersey.TestConstants.*;
import static com.nttdata.dataspace.edc.fc.web.jersey.TestDataFactory.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link CorsFilter}.
 * Tests CORS header handling, origin validation, and filter behavior.
 */
class CorsFilterTest {

    private Monitor monitor;
    private TypeManager typeManager;
    private ContainerRequestContext requestContext;
    private ContainerResponseContext responseContext;
    private MultivaluedMap<String, Object> responseHeaders;

    @BeforeEach
    void setUp() {
        monitor = createMockMonitor();
        typeManager = createTypeManager();
        requestContext = mock(ContainerRequestContext.class);
        responseContext = mock(ContainerResponseContext.class);
        responseHeaders = new MultivaluedHashMap<>();
        
        when(responseContext.getHeaders()).thenReturn(responseHeaders);
    }

    @Test
    void shouldAddCorsHeadersForValidOrigin() {
        // Arrange
        var config = createDefaultConfigBuilder(typeManager, monitor)
                .allowedOrigins(TEST_ORIGIN_1)
                .allowedHeaders("content-type, accept")
                .allowedMethods(DEFAULT_METHODS)
                .build();
        
        var filter = new CorsFilter(config);
        when(requestContext.getHeaderString("Origin")).thenReturn(TEST_ORIGIN_1);

        // Act
        filter.filter(requestContext, responseContext);

        // Assert
        verifyCorsHeadersAdded(TEST_ORIGIN_1, "content-type, accept", DEFAULT_METHODS);
    }

    @Test
    void shouldNotAddCorsHeadersForInvalidOrigin() {
        // Arrange
        var config = createDefaultConfigBuilder(typeManager, monitor).build();
        var filter = new CorsFilter(config);
        when(requestContext.getHeaderString("Origin")).thenReturn(TEST_ORIGIN_UNAUTHORIZED);

        // Act
        filter.filter(requestContext, responseContext);

        // Assert
        verifyNoCorsHeaders();
    }

    @ParameterizedTest
    @NullAndEmptySource
    void shouldNotAddCorsHeadersWhenOriginHeaderMissing(String origin) {
        // Arrange
        var config = JerseyConfiguration.Builder
                .newInstance()
                .allowedOrigins("https://example.com")
                .allowedHeaders("content-type")
                .allowedMethods("POST")
                .corsEnabled(true)
                .build();
        
        var filter = new CorsFilter(config);
        
        when(requestContext.getHeaderString("Origin")).thenReturn(origin);

        // Act
        filter.filter(requestContext, responseContext);

        // Assert - no CORS headers should be added
        assertThat(responseHeaders.get("Access-Control-Allow-Origin")).isNull();
        assertThat(responseHeaders.get("Access-Control-Allow-Headers")).isNull();
        assertThat(responseHeaders.get("Access-Control-Allow-Methods")).isNull();
    }

    @Test
    void shouldReturnWildcardForAnyOriginWhenConfiguredWithWildcard() {
        // Arrange
        var config = JerseyConfiguration.Builder
                .newInstance()
                .allowedOrigins("*")
                .allowedHeaders("content-type")
                .allowedMethods("POST, OPTIONS")
                .corsEnabled(true)
                .build();
        
        var filter = new CorsFilter(config);
        
        when(requestContext.getHeaderString("Origin")).thenReturn("https://any-origin.com");

        // Act
        filter.filter(requestContext, responseContext);

        // Assert
        assertThat(responseHeaders.get("Access-Control-Allow-Origin"))
                .containsExactly("*");
        
        assertThat(responseHeaders.get("Access-Control-Allow-Headers"))
                .containsExactly("content-type");
        
        assertThat(responseHeaders.get("Access-Control-Allow-Methods"))
                .containsExactly("POST, OPTIONS");
    }

    @Test
    void shouldHandleMultipleValidOrigins() {
        // Arrange
        var config = JerseyConfiguration.Builder
                .newInstance()
                .allowedOrigins("https://example1.com,https://example2.com,https://example3.com")
                .allowedHeaders("content-type")
                .allowedMethods("POST")
                .corsEnabled(true)
                .build();
        
        var filter = new CorsFilter(config);

        // Test each valid origin
        for (String origin : new String[]{"https://example1.com", "https://example2.com", "https://example3.com"}) {
            // Arrange
            responseHeaders.clear();
            when(requestContext.getHeaderString("Origin")).thenReturn(origin);

            // Act
            filter.filter(requestContext, responseContext);

            // Assert
            assertThat(responseHeaders.get("Access-Control-Allow-Origin"))
                    .containsExactly(origin);
        }
    }

    @Test
    void shouldHandleCaseSensitiveOrigins() {
        // Arrange
        var config = JerseyConfiguration.Builder
                .newInstance()
                .allowedOrigins("https://Example.com")
                .allowedHeaders("content-type")
                .allowedMethods("POST")
                .corsEnabled(true)
                .build();
        
        var filter = new CorsFilter(config);

        // Test correct case
        when(requestContext.getHeaderString("Origin")).thenReturn("https://Example.com");
        filter.filter(requestContext, responseContext);
        
        assertThat(responseHeaders.get("Access-Control-Allow-Origin"))
                .containsExactly("https://Example.com");

        // Test incorrect case
        responseHeaders.clear();
        when(requestContext.getHeaderString("Origin")).thenReturn("https://example.com");
        filter.filter(requestContext, responseContext);
        
        assertThat(responseHeaders.get("Access-Control-Allow-Origin")).isNull();
    }

    @Test
    void shouldHandleOriginsWithPorts() {
        // Arrange
        var config = JerseyConfiguration.Builder
                .newInstance()
                .allowedOrigins("https://example.com:8080")
                .allowedHeaders("content-type")
                .allowedMethods("POST")
                .corsEnabled(true)
                .build();
        
        var filter = new CorsFilter(config);

        // Test with port
        when(requestContext.getHeaderString("Origin")).thenReturn("https://example.com:8080");
        filter.filter(requestContext, responseContext);

        assertThat(responseHeaders.get("Access-Control-Allow-Origin"))
                .containsExactly("https://example.com:8080");

        // Test without port (different origin)
        responseHeaders.clear();
        when(requestContext.getHeaderString("Origin")).thenReturn("https://example.com");
        filter.filter(requestContext, responseContext);
        
        assertThat(responseHeaders.get("Access-Control-Allow-Origin")).isNull();
    }

    @Test
    void shouldHandlePreflightOptionsRequest() {
        // Arrange
        var config = JerseyConfiguration.Builder
                .newInstance()
                .allowedOrigins("https://example.com")
                .allowedHeaders("content-type, authorization")
                .allowedMethods("POST, OPTIONS, GET")
                .corsEnabled(true)
                .build();
        
        var filter = new CorsFilter(config);
        
        when(requestContext.getHeaderString("Origin")).thenReturn("https://example.com");

        // Act
        filter.filter(requestContext, responseContext);

        // Assert - all CORS headers should be present
        assertThat(responseHeaders.get("Access-Control-Allow-Origin"))
                .containsExactly("https://example.com");
        
        assertThat(responseHeaders.get("Access-Control-Allow-Headers"))
                .containsExactly("content-type, authorization");
        
        assertThat(responseHeaders.get("Access-Control-Allow-Methods"))
                .containsExactly("POST, OPTIONS, GET");
    }

    @Test
    void shouldHandleActualPostRequest() {
        // Arrange
        var config = JerseyConfiguration.Builder
                .newInstance()
                .allowedOrigins("https://example.com")
                .allowedHeaders("content-type")
                .allowedMethods("POST, OPTIONS")
                .corsEnabled(true)
                .build();
        
        var filter = new CorsFilter(config);
        
        when(requestContext.getHeaderString("Origin")).thenReturn("https://example.com");

        // Act
        filter.filter(requestContext, responseContext);

        // Assert - same headers as preflight (filter handles both)
        assertThat(responseHeaders.get("Access-Control-Allow-Origin"))
                .containsExactly("https://example.com");
        
        assertThat(responseHeaders.get("Access-Control-Allow-Headers"))
                .containsExactly("content-type");
        
        assertThat(responseHeaders.get("Access-Control-Allow-Methods"))
                .containsExactly("POST, OPTIONS");
    }

    @Test
    void shouldNotModifyOtherResponseHeaders() {
        // Arrange
        var config = JerseyConfiguration.Builder
                .newInstance()
                .allowedOrigins("https://example.com")
                .allowedHeaders("content-type")
                .allowedMethods("POST")
                .corsEnabled(true)
                .build();
        
        var filter = new CorsFilter(config);
        
        // Add some other headers
        responseHeaders.add("Content-Type", "application/json");
        responseHeaders.add("X-Custom-Header", "custom-value");
        
        when(requestContext.getHeaderString("Origin")).thenReturn("https://example.com");

        // Act
        filter.filter(requestContext, responseContext);

        // Assert - other headers should remain unchanged
        assertThat(responseHeaders.get("Content-Type")).containsExactly("application/json");
        assertThat(responseHeaders.get("X-Custom-Header")).containsExactly("custom-value");
        
        // CORS headers should be added
        assertThat(responseHeaders.get("Access-Control-Allow-Origin"))
                .containsExactly("https://example.com");
    }

    @Test
    void shouldHandleLocalhostOrigins() {
        // Arrange
        var config = JerseyConfiguration.Builder
                .newInstance()
                .allowedOrigins("http://localhost:3000,http://localhost:8080")
                .allowedHeaders("content-type")
                .allowedMethods("POST")
                .corsEnabled(true)
                .build();
        
        var filter = new CorsFilter(config);

        // Test localhost:3000
        when(requestContext.getHeaderString("Origin")).thenReturn("http://localhost:3000");
        filter.filter(requestContext, responseContext);
        
        assertThat(responseHeaders.get("Access-Control-Allow-Origin"))
                .containsExactly("http://localhost:3000");

        // Test localhost:8080
        responseHeaders.clear();
        when(requestContext.getHeaderString("Origin")).thenReturn("http://localhost:8080");
        filter.filter(requestContext, responseContext);
        
        assertThat(responseHeaders.get("Access-Control-Allow-Origin"))
                .containsExactly("http://localhost:8080");
    }

    @Test
    void shouldHandleIPAddressOrigins() {
        // Arrange
        var config = JerseyConfiguration.Builder
                .newInstance()
                .allowedOrigins("http://192.168.1.1:8080")
                .allowedHeaders("content-type")
                .allowedMethods("POST")
                .corsEnabled(true)
                .build();
        
        var filter = new CorsFilter(config);
        
        when(requestContext.getHeaderString("Origin")).thenReturn("http://192.168.1.1:8080");

        // Act
        filter.filter(requestContext, responseContext);

        // Assert
        assertThat(responseHeaders.get("Access-Control-Allow-Origin"))
                .containsExactly("http://192.168.1.1:8080");
    }

    @Test
    void shouldReturnSingleOriginNotCommaSeparatedList() {
        // Arrange - this is the core fix this filter provides
        var config = JerseyConfiguration.Builder
                .newInstance()
                .allowedOrigins("https://example1.com,https://example2.com,https://example3.com")
                .allowedHeaders("content-type")
                .allowedMethods("POST")
                .corsEnabled(true)
                .build();
        
        var filter = new CorsFilter(config);
        
        when(requestContext.getHeaderString("Origin")).thenReturn("https://example2.com");

        // Act
        filter.filter(requestContext, responseContext);

        // Assert - should return ONLY the matching origin, not all of them
        assertThat(responseHeaders.get("Access-Control-Allow-Origin"))
                .hasSize(1)
                .containsExactly("https://example2.com")
                .doesNotContain("https://example1.com,https://example2.com,https://example3.com");
    }

    // ========== Helper Methods ==========

    /**
     * Verifies that all CORS headers are properly added to the response.
     */
    private void verifyCorsHeadersAdded(String origin, String headers, String methods) {
        assertThat(responseHeaders.get("Access-Control-Allow-Origin")).containsExactly(origin);
        assertThat(responseHeaders.get("Access-Control-Allow-Headers")).containsExactly(headers);
        assertThat(responseHeaders.get("Access-Control-Allow-Methods")).containsExactly(methods);
    }

    /**
     * Verifies that no CORS headers are added to the response.
     */
    private void verifyNoCorsHeaders() {
        assertThat(responseHeaders.get("Access-Control-Allow-Origin")).isNull();
        assertThat(responseHeaders.get("Access-Control-Allow-Headers")).isNull();
        assertThat(responseHeaders.get("Access-Control-Allow-Methods")).isNull();
    }
}
