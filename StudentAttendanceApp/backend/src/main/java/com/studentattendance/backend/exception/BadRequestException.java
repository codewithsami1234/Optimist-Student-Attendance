package com.studentattendance.backend.exception;

/** Thrown for any request that fails a business-rule check (duplicate roll number, no students marked, etc). */
public class BadRequestException extends RuntimeException {
    public BadRequestException(String message) {
        super(message);
    }
}
