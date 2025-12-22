package com.nttdata.dataspace.edc.fc.participant.api;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ApiResponseTest {

    @Test
    void success_withData_setsFieldsCorrectly() {
        String data = "payload";
        ApiResponse<String> response = ApiResponse.success(data);

        assertTrue(response.isSuccess());
        assertEquals(data, response.getData());
        assertNull(response.getError());
        assertNull(response.getMessage());
    }

    @Test
    void success_withDataAndMessage_setsFieldsCorrectly() {
        String data = "payload";
        String message = "Operation successful";
        ApiResponse<String> response = ApiResponse.success(data, message);

        assertTrue(response.isSuccess());
        assertEquals(data, response.getData());
        assertNull(response.getError());
        assertEquals(message, response.getMessage());
    }

    @Test
    void error_withError_setsFieldsCorrectly() {
        String error = "ERROR_CODE";
        ApiResponse<String> response = ApiResponse.error(error);

        assertFalse(response.isSuccess());
        assertNull(response.getData());
        assertEquals(error, response.getError());
        assertNull(response.getMessage());
    }

    @Test
    void error_withErrorAndMessage_setsFieldsCorrectly() {
        String error = "ERROR_CODE";
        String message = "Something went wrong";
        ApiResponse<String> response = ApiResponse.error(error, message);

        assertFalse(response.isSuccess());
        assertNull(response.getData());
        assertEquals(error, response.getError());
        assertEquals(message, response.getMessage());
    }
}
