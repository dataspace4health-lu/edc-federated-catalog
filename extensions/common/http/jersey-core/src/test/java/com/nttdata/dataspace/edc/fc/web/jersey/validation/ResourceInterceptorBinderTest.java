package com.nttdata.dataspace.edc.fc.web.jersey.validation;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ResourceInterceptorBinderTest {

    @Test
    void configure_shouldNotThrow() {
        ResourceInterceptorProvider provider = new ResourceInterceptorProvider();
        ResourceInterceptorBinder binder = new ResourceInterceptorBinder(provider);

        // Just ensure configure() does not throw
        assertDoesNotThrow(binder::configure);
    }
}
