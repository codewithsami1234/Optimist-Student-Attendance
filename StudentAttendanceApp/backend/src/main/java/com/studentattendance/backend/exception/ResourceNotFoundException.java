package com.studentattendance.backend.exception;

/** Thrown when a requested entity (department, semester, section, student, session, user) does not exist. */
public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}
