/*
 *  Copyright (c) 2025
 *
 *  This program and the accompanying materials are made available under the
 *  terms of the Apache License, Version 2.0 which is available at
 *  https://www.apache.org/licenses/LICENSE-2.0
 *
 *  SPDX-License-Identifier: Apache-2.0
 *
 *  Contributors:
 *       Initial API and implementation
 *
 */

package org.eclipse.edc.extensions.federatedcatalog.store.sql.api;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Standard API response wrapper for consistent error handling.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {
    
    @JsonProperty("success")
    private boolean success;
    
    @JsonProperty("data")
    private T data;
    
    @JsonProperty("error")
    private String error;
    
    @JsonProperty("message")
    private String message;
    
    private ApiResponse(boolean success, T data, String error, String message) {
        this.success = success;
        this.data = data;
        this.error = error;
        this.message = message;
    }
    
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(true, data, null, null);
    }
    
    public static <T> ApiResponse<T> success(T data, String message) {
        return new ApiResponse<>(true, data, null, message);
    }
    
    public static <T> ApiResponse<T> error(String error) {
        return new ApiResponse<>(false, null, error, null);
    }
    
    public static <T> ApiResponse<T> error(String error, String message) {
        return new ApiResponse<>(false, null, error, message);
    }
    
    public boolean isSuccess() {
        return success;
    }
    
    public T getData() {
        return data;
    }
    
    public String getError() {
        return error;
    }
    
    public String getMessage() {
        return message;
    }
}
