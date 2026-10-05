package com.studentattendance.backend.exception;

/** Thrown for invalid credentials or missing/expired/invalid JWTs. */
public class UnauthorizedException extends RuntimeException {
    public UnauthorizedException(String message) {
        super(message);
    }
}
