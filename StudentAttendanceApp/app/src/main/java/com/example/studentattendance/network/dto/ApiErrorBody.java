package com.example.studentattendance.network.dto;

/**
 * Mirrors the backends ApiErrorResponse shape so error bodies (validation
 * failures, 401s, 404s, etc) can be parsed into a human-readable message
 * instead of a raw HTTP status code.
 */
public class ApiErrorBody {
    private String message;
    private String error;

    public String getMessage() { return message; }
    public String getError() { return error; }
}
