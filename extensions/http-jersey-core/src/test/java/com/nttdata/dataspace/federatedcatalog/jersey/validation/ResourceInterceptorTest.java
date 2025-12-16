package com.nttdata.dataspace.federatedcatalog.jersey.validation;

import org.eclipse.edc.spi.result.Result;
import org.eclipse.edc.web.spi.exception.InvalidRequestException;
import org.eclipse.edc.web.spi.validation.InterceptorFunction;
import org.junit.jupiter.api.Test;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ResourceInterceptorTest {

    interface TestService {
        String doSomething(String arg);
    }

    @Test
    void invoke_shouldCallMethodIfAllInterceptorsSucceed() throws Throwable {
        InterceptorFunction interceptor1 = args -> Result.success();
        InterceptorFunction interceptor2 = args -> Result.success();

        TestService target = mock(TestService.class);
        when(target.doSomething("foo")).thenReturn("bar");

        Method method = TestService.class.getMethod("doSomething", String.class);
        InvocationHandler handler = new ResourceInterceptor(List.of(interceptor1, interceptor2));

        // The proxy is the target in this test context
        Object result = handler.invoke(target, method, new Object[]{"foo"});

        assertEquals("bar", result);
        verify(target).doSomething("foo");
    }

    @Test
    void invoke_shouldThrowIfAnyInterceptorFails() throws Throwable {
        InterceptorFunction interceptor1 = args -> Result.success();
        InterceptorFunction interceptor2 = args -> Result.failure("fail");

        TestService target = mock(TestService.class);
        Method method = TestService.class.getMethod("doSomething", String.class);
        InvocationHandler handler = new ResourceInterceptor(List.of(interceptor1, interceptor2));

        InvocationTargetException thrown = assertThrows(InvocationTargetException.class,
                () -> handler.invoke(target, method, new Object[]{"foo"}));

        assertTrue(thrown.getCause() instanceof InvalidRequestException);
        assertEquals(List.of("fail"), ((InvalidRequestException) thrown.getCause()).getMessages());
        verify(target, never()).doSomething(any());
    }
}
