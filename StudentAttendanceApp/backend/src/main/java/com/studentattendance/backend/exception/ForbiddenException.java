package com.studentattendance.backend.exception;

/** Thrown when an authenticated user tries to access or modify data owned by a different professor. */
public class ForbiddenException extends RuntimeException {
    public ForbiddenException(String message) {
        super(message);
    }
}
