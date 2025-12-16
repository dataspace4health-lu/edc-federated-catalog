package com.nttdata.dataspace.federatedcatalog.jersey.mapper;

import jakarta.ws.rs.core.Response;
import org.eclipse.edc.web.spi.ApiErrorDetail;
import org.eclipse.edc.web.spi.exception.*;
import org.eclipse.edc.validator.spi.Violation;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class EdcApiExceptionMapperTest {

    private final EdcApiExceptionMapper mapper = new EdcApiExceptionMapper();

    @Test
    void toResponse_shouldMapAuthenticationFailedException() {
        var ex = new AuthenticationFailedException("auth failed");
        Response response = mapper.toResponse(ex);

        assertEquals(Response.Status.UNAUTHORIZED.getStatusCode(), response.getStatus());
        assertErrorDetail(response, "auth failed", ex.getType());
    }

    @Test
    void toResponse_shouldMapNotAuthorizedException() {
        var ex = new NotAuthorizedException("not authorized");
        Response response = mapper.toResponse(ex);

        assertEquals(Response.Status.FORBIDDEN.getStatusCode(), response.getStatus());
        assertErrorDetail(response, "not authorized", ex.getType());
    }

    @Test
    void toResponse_shouldMapInvalidRequestException() {
        var ex = new InvalidRequestException("bad request");
        Response response = mapper.toResponse(ex);

        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
        assertErrorDetail(response, "bad request", ex.getType());
    }

    @Test
    void toResponse_shouldMapObjectNotFoundException() {
        class Dummy {}
        var ex = new ObjectNotFoundException(Dummy.class, "not found");
        Response response = mapper.toResponse(ex);

        assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());
        assertErrorDetail(response, "Object of type Dummy with ID=not found was not found", ex.getType());
    }

    @Test
    void toResponse_shouldMapObjectConflictException() {
        var ex = new ObjectConflictException("conflict");
        Response response = mapper.toResponse(ex);

        assertEquals(Response.Status.CONFLICT.getStatusCode(), response.getStatus());
        assertErrorDetail(response, "conflict", ex.getType());
    }

    @Test
    void toResponse_shouldMapBadGatewayException() {
        var ex = new BadGatewayException("bad gateway");
        Response response = mapper.toResponse(ex);

        assertEquals(Response.Status.BAD_GATEWAY.getStatusCode(), response.getStatus());
        assertErrorDetail(response, "bad gateway", ex.getType());
    }

    @Test
    void toResponse_shouldMapValidationFailureException() {
        // Use the actual Violation class from org.eclipse.edc.validator.spi
        Violation violation = Violation.violation("msg", "path", "val");
        var ex = new ValidationFailureException(List.of(violation));
        Response response = mapper.toResponse(ex);

        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
        @SuppressWarnings("unchecked")
        List<ApiErrorDetail> details = (List<ApiErrorDetail>) response.getEntity();
        assertEquals(1, details.size());
        ApiErrorDetail detail = details.get(0);
        assertEquals("msg", detail.getMessage());
        assertEquals("path", detail.getPath());
        assertEquals(ex.getType(), detail.getType());
    }

    @Test
    void toResponse_shouldMapUnknownExceptionToInternalServerError() {
        var ex = new InvalidRequestException("unknown");
        Response response = mapper.toResponse(ex);

        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
        assertErrorDetail(response, "unknown", ex.getType());
    }

    private void assertErrorDetail(Response response, String expectedMessage, String expectedType) {
        @SuppressWarnings("unchecked")
        List<ApiErrorDetail> details = (List<ApiErrorDetail>) response.getEntity();
        assertEquals(1, details.size());
        ApiErrorDetail detail = details.get(0);
        assertEquals(expectedMessage, detail.getMessage());
        assertEquals(expectedType, detail.getType());
    }
}
