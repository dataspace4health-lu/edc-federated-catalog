package com.nttdata.dataspace.edc.fc.participant.api;

import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SwaggerControllerTest {

    @Test
    void getOpenApiSpec_returnsJsonWithOpenapiField() {
        SwaggerController controller = new SwaggerController("/api");
        Response response = controller.getOpenApiSpec();

        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        String entity = (String) response.getEntity();
        assertNotNull(entity);
        assertTrue(entity.contains("\"openapi\""));
    }

    @Test
    void getSwaggerUI_returnsHtml() {
        SwaggerController controller = new SwaggerController("/api");
        Response response = controller.getSwaggerUI();

        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        String entity = (String) response.getEntity();
        assertNotNull(entity);
        assertTrue(entity.toLowerCase().contains("<html"));
    }
}
