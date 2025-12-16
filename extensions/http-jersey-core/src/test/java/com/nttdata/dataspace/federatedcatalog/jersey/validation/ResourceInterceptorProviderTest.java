package com.nttdata.dataspace.federatedcatalog.jersey.validation;

import org.eclipse.edc.web.spi.validation.InterceptorFunction;
import org.glassfish.jersey.server.model.Invocable;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ResourceInterceptorProviderTest {

    interface TestService {
        void foo(String arg);
        void bar(Integer arg);
    }

    @Test
    void create_shouldReturnNullIfNoFunctions() throws NoSuchMethodException {
        ResourceInterceptorProvider provider = new ResourceInterceptorProvider();

        Method method = TestService.class.getMethod("foo", String.class);
        Invocable invocable = mock(Invocable.class);
        when(invocable.getDefinitionMethod()).thenReturn(method);

        assertNull(provider.create(invocable));
    }

    @Test
    void create_shouldReturnHandlerIfGlobalFunctionAdded() throws NoSuchMethodException {
        ResourceInterceptorProvider provider = new ResourceInterceptorProvider();
        provider.addFunction(args -> null);

        Method method = TestService.class.getMethod("foo", String.class);
        Invocable invocable = mock(Invocable.class);
        when(invocable.getDefinitionMethod()).thenReturn(method);

        assertNotNull(provider.create(invocable));
    }

    @Test
    void create_shouldReturnHandlerIfTypeBoundFunctionAdded() throws NoSuchMethodException {
        ResourceInterceptorProvider provider = new ResourceInterceptorProvider();
        provider.addFunction(String.class, args -> null);

        Method method = TestService.class.getMethod("foo", String.class);
        Invocable invocable = mock(Invocable.class);
        when(invocable.getDefinitionMethod()).thenReturn(method);

        assertNotNull(provider.create(invocable));
    }

    @Test
    void create_shouldReturnHandlerIfMethodBoundFunctionAdded() throws NoSuchMethodException {
        ResourceInterceptorProvider provider = new ResourceInterceptorProvider();
        Method method = TestService.class.getMethod("foo", String.class);
        provider.addFunction(method, args -> null);

        Invocable invocable = mock(Invocable.class);
        when(invocable.getDefinitionMethod()).thenReturn(method);

        assertNotNull(provider.create(invocable));
    }
}
