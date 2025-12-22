package com.nttdata.dataspace.edc.fc.web.jersey;

import org.eclipse.edc.spi.monitor.Monitor;
import org.eclipse.edc.spi.types.TypeManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.nio.file.Path;

import static com.nttdata.dataspace.edc.fc.web.jersey.TestConstants.*;
import static com.nttdata.dataspace.edc.fc.web.jersey.TestDataFactory.*;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link JerseyConfiguration}.
 * Tests configuration validation, origin matching, and participant file loading.
 */
class JerseyConfigurationTest {

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
    void shouldValidateOriginAgainstConfiguredOrigins() {
        // Arrange
        String origins = TEST_ORIGIN_1 + "," + TEST_ORIGIN_2;
        var config = createMultiOriginConfig(typeManager, monitor, origins);

        var ctx = createMockContainerRequestContext();
        ctx.getHeaders().putSingle("Origin", TEST_ORIGIN_1);
        // Act & Assert
        assertThat(config.getAllowedOrigin(ctx)).isEqualTo(TEST_ORIGIN_1);
        
        ctx.getHeaders().putSingle("Origin", TEST_ORIGIN_2);
        assertThat(config.getAllowedOrigin(ctx)).isEqualTo(TEST_ORIGIN_2);
        
        ctx.getHeaders().putSingle("Origin", TEST_ORIGIN_UNAUTHORIZED);
        assertThat(config.getAllowedOrigin(ctx)).isNull();
    }

    @Test
    void shouldReturnWildcardWhenConfigured() {
        // Arrange
        var config = createWildcardConfig(typeManager, monitor);

        var ctx = createMockContainerRequestContext();

        ctx.getHeaders().putSingle("Origin", "https://any-origin.com");
        assertThat(config.getAllowedOrigin(ctx))
                .isEqualTo(TEST_ORIGIN_WILDCARD);

        ctx.getHeaders().putSingle("Origin", TEST_ORIGIN_LOCALHOST_8080);
        assertThat(config.getAllowedOrigin(ctx))
                .isEqualTo(TEST_ORIGIN_WILDCARD);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"  ", "\t", "\n"})
    void shouldReturnNullForInvalidOriginStrings(String invalidOrigin) {
        // Arrange
        var config = createDefaultConfigBuilder(typeManager, monitor).build();

        var ctx = createMockContainerRequestContext();

        ctx.getHeaders().putSingle("Origin", invalidOrigin);
        assertThat(config.getAllowedOrigin(ctx)).isNull();
    }

    @Test
    void shouldHandleOriginsWithSpaces() {
        // Arrange
        var config = JerseyConfiguration.Builder
                .newInstance()
                .allowedOrigins(" https://example1.com , https://example2.com ")
                .allowedHeaders("content-type")
                .allowedMethods("POST")
                .corsEnabled(true)
                .build();

        var ctx = createMockContainerRequestContext();

        ctx.getHeaders().putSingle("Origin", "https://example1.com");
        assertThat(config.getAllowedOrigin(ctx))
                .isEqualTo("https://example1.com");
        
        ctx.getHeaders().putSingle("Origin", "https://example2.com");
        assertThat(config.getAllowedOrigin(ctx))
                .isEqualTo("https://example2.com");
    }

    @Test
    void shouldHandleEmptyOriginsList() {
        // Arrange
        var config = JerseyConfiguration.Builder
                .newInstance()
                .allowedOrigins("")
                .allowedHeaders("content-type")
                .allowedMethods("POST")
                .corsEnabled(true)
                .build();

        var ctx = createMockContainerRequestContext();
        ctx.getHeaders().putSingle("Origin", "https://example.com");
        assertThat(config.getAllowedOrigin(ctx)).isNull();
    }

    @Test
    void shouldHandleNullOriginsList() {
        // Arrange
        var config = JerseyConfiguration.Builder
                .newInstance()
                .allowedOrigins(null)
                .allowedHeaders("content-type")
                .allowedMethods("POST")
                .corsEnabled(true)
                .build();

        var ctx = createMockContainerRequestContext();
        ctx.getHeaders().putSingle("Origin", "https://example.com");
        assertThat(config.getAllowedOrigin(ctx)).isNull();
    }

    @Test
    void shouldReturnConfiguredHeaders() {
        // Arrange
        String headers = "content-type, accept, authorization";
        var config = JerseyConfiguration.Builder
                .newInstance()
                .allowedOrigins("*")
                .allowedHeaders(headers)
                .allowedMethods("POST")
                .corsEnabled(true)
                .build();

        // Act & Assert
        assertThat(config.getAllowedHeaders()).isEqualTo(headers);
    }

    @Test
    void shouldReturnConfiguredMethods() {
        // Arrange
        String methods = "POST, OPTIONS, GET";
        var config = JerseyConfiguration.Builder
                .newInstance()
                .allowedOrigins("*")
                .allowedHeaders("content-type")
                .allowedMethods(methods)
                .corsEnabled(true)
                .build();

        // Act & Assert
        assertThat(config.getAllowedMethods()).isEqualTo(methods);
    }

    @Test
    void shouldReturnCorsEnabledStatus() {
        // Arrange & Act
        var enabledConfig = JerseyConfiguration.Builder
                .newInstance()
                .allowedOrigins("*")
                .allowedHeaders("content-type")
                .allowedMethods("POST")
                .corsEnabled(true)
                .build();
        
        var disabledConfig = JerseyConfiguration.Builder
                .newInstance()
                .allowedOrigins("*")
                .allowedHeaders("content-type")
                .allowedMethods("POST")
                .corsEnabled(false)
                .build();

        // Assert
        assertThat(enabledConfig.isCorsEnabled()).isTrue();
        assertThat(disabledConfig.isCorsEnabled()).isFalse();
    }

    @Test
    void shouldHandleMultipleOriginsInConfiguration() {
        // Arrange
        var config = JerseyConfiguration.Builder
                .newInstance()
                .allowedOrigins("https://example1.com,https://example2.com,https://example3.com")
                .allowedHeaders("content-type")
                .allowedMethods("POST")
                .corsEnabled(true)
                .build();

        var ctx = createMockContainerRequestContext();

        ctx.getHeaders().putSingle("Origin", "https://example1.com");
        assertThat(config.getAllowedOrigin(ctx))
                .isEqualTo("https://example1.com");
        
        ctx.getHeaders().putSingle("Origin", "https://example2.com");
        assertThat(config.getAllowedOrigin(ctx))
                .isEqualTo("https://example2.com");
        
        ctx.getHeaders().putSingle("Origin", "https://example3.com");
        assertThat(config.getAllowedOrigin(ctx))
                .isEqualTo("https://example3.com");
        
        ctx.getHeaders().putSingle("Origin", "https://example4.com");
        assertThat(config.getAllowedOrigin(ctx))
                .isNull();
    }

    @Test
    void shouldPreferWildcardOverSpecificOrigins() {
        // Arrange
        var config = JerseyConfiguration.Builder
                .newInstance()
                .allowedOrigins("*")
                .allowedHeaders("content-type")
                .allowedMethods("POST")
                .corsEnabled(true)
                .build();

        var ctx = createMockContainerRequestContext();

        ctx.getHeaders().putSingle("Origin", "https://any-origin.com");
        assertThat(config.getAllowedOrigin(ctx))
                .isEqualTo("*");
        
        ctx.getHeaders().putSingle("Origin", "http://localhost");
        assertThat(config.getAllowedOrigin(ctx))
                .isEqualTo("*");
    }

    @Test
    void shouldBeCaseSensitiveForOrigins() {
        // Arrange
        var config = JerseyConfiguration.Builder
                .newInstance()
                .allowedOrigins("https://Example.com")
                .allowedHeaders("content-type")
                .allowedMethods("POST")
                .corsEnabled(true)
                .build();

        var ctx = createMockContainerRequestContext();

        ctx.getHeaders().putSingle("Origin", "https://Example.com");
        assertThat(config.getAllowedOrigin(ctx))
                .isEqualTo("https://Example.com");
        
        // Origins are case-sensitive according to RFC 6454
        ctx.getHeaders().putSingle("Origin", "https://example.com");
        assertThat(config.getAllowedOrigin(ctx))
                .isNull();
    }

    @Test
    void shouldHandleOriginsWithPorts() {
        // Arrange
        var config = JerseyConfiguration.Builder
                .newInstance()
                .allowedOrigins("https://example.com:8080,https://example.com:9090")
                .allowedHeaders("content-type")
                .allowedMethods("POST")
                .corsEnabled(true)
                .build();

        var ctx = createMockContainerRequestContext();

        ctx.getHeaders().putSingle("Origin", "https://example.com:8080");
        assertThat(config.getAllowedOrigin(ctx))
                .isEqualTo("https://example.com:8080");
        
        ctx.getHeaders().putSingle("Origin", "https://example.com:9090");
        assertThat(config.getAllowedOrigin(ctx))
                .isEqualTo("https://example.com:9090");
        
        // Different port = different origin
        ctx.getHeaders().putSingle("Origin", "https://example.com:7070");
        assertThat(config.getAllowedOrigin(ctx))
                .isNull();
    }

    @ParameterizedTest
    @CsvSource({
            "https://example.com, https://example.com, true",
            "https://example.com, http://example.com, false",
            "https://example.com:443, https://example.com, false",
            "http://localhost, http://localhost, true",
            "http://localhost:8080, http://localhost:8080, true"
    })
    void shouldMatchOriginsExactly(String configuredOrigin, String requestOrigin, boolean shouldMatch) {
        // Arrange
        var config = JerseyConfiguration.Builder
                .newInstance()
                .allowedOrigins(configuredOrigin)
                .allowedHeaders("content-type")
                .allowedMethods("POST")
                .corsEnabled(true)
                .build();

        // Act
        var ctx = createMockContainerRequestContext();
        ctx.getHeaders().putSingle("Origin", requestOrigin);
        String result = config.getAllowedOrigin(ctx);

        // Assert
        if (shouldMatch) {
            assertThat(result).isEqualTo(requestOrigin);
        } else {
            assertThat(result).isNull();
        }
    }

    @Test
    void shouldHandleEmptyCommaSeparatedList() {
        // Arrange
        var config = JerseyConfiguration.Builder
                .newInstance()
                .allowedOrigins(",,,")
                .allowedHeaders("content-type")
                .allowedMethods("POST")
                .corsEnabled(true)
                .build();

        var ctx = createMockContainerRequestContext();

        ctx.getHeaders().putSingle("Origin", "https://example.com");
        assertThat(config.getAllowedOrigin(ctx)).isNull();
    }

    @Test
    void shouldReturnConfiguredOriginsString() {
        // Arrange
        String origins = "https://example1.com,https://example2.com";
        var config = JerseyConfiguration.Builder
                .newInstance()
                .allowedOrigins(origins)
                .allowedHeaders("content-type")
                .allowedMethods("POST")
                .corsEnabled(true)
                .build();

        // Act & Assert
        assertThat(config.getAllowedOrigins()).isEqualTo(origins);
    }

    @Test
    void shouldHandleNullAllowedOrigins() {
        // Arrange
        var config = JerseyConfiguration.Builder
                .newInstance()
                .allowedOrigins(null)
                .allowedHeaders("content-type")
                .allowedMethods("POST")
                .corsEnabled(true)
                .build();

        assertThat(config.getAllowedOrigins()).isNull();

        var ctx = createMockContainerRequestContext();
        ctx.getHeaders().putSingle("Origin", "https://example.com");
        assertThat(config.getAllowedOrigin(ctx)).isNull();
    }

    @Test
    void shouldAllowDirectSetAllowedOrigin() throws Exception {
        var config = JerseyConfiguration.Builder.newInstance().build();
        var method = JerseyConfiguration.class.getDeclaredMethod("setAllowedOrigin", String.class);
        method.setAccessible(true);
        method.invoke(config, "https://direct-set.com");
        assertThat(config.getAllowedOrigins()).isEqualTo("https://direct-set.com");
    }

    @Test
    void builderWithNoFieldsSetShouldHaveDefaults() {
        var config = JerseyConfiguration.Builder.newInstance().build();
        assertThat(config.getAllowedOrigins()).isNull();
        assertThat(config.getAllowedHeaders()).isNull();
        assertThat(config.getAllowedMethods()).isNull();
        assertThat(config.isCorsEnabled()).isFalse();
    }
}
