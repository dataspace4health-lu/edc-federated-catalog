package com.nttdata.dataspace.federatedcatalog.jersey.mapper;

import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import org.eclipse.edc.spi.monitor.Monitor;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UnexpectedExceptionMapperTest {

    @Test
    void toResponse_shouldReturnWebApplicationExceptionResponse() {
        Monitor monitor = mock(Monitor.class);
        UnexpectedExceptionMapper mapper = new UnexpectedExceptionMapper(monitor);

        Response expected = Response.status(418).build();
        WebApplicationException ex = new WebApplicationException(expected);

        Response response = mapper.toResponse(ex);

        assertSame(expected, response);
        verify(monitor, never()).severe(anyString(), any());
    }

    @Test
    void toResponse_shouldReturnNotImplementedForUnsupportedOperationException() {
        Monitor monitor = mock(Monitor.class);
        UnexpectedExceptionMapper mapper = new UnexpectedExceptionMapper(monitor);

        Response response = mapper.toResponse(new UnsupportedOperationException("not implemented"));

        assertEquals(Response.Status.NOT_IMPLEMENTED.getStatusCode(), response.getStatus());
        verify(monitor).severe(anyString(), any());
    }

    @Test
    void toResponse_shouldReturnBadRequestForIllegalArgumentException() {
        Monitor monitor = mock(Monitor.class);
        UnexpectedExceptionMapper mapper = new UnexpectedExceptionMapper(monitor);

        Response response = mapper.toResponse(new IllegalArgumentException("bad request"));

        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
        verify(monitor).severe(anyString(), any());
    }

    @Test
    void toResponse_shouldReturnInternalServerErrorForOtherExceptions() {
        Monitor monitor = mock(Monitor.class);
        UnexpectedExceptionMapper mapper = new UnexpectedExceptionMapper(monitor);

        Response response = mapper.toResponse(new NullPointerException("null"));

        assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatus());
        verify(monitor).severe(anyString(), any());
    }
}
