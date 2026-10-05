package com.example.studentattendance.data.local;

/**
 * Domain error raised on the Room I/O thread and mapped to
 * {@link com.example.studentattendance.network.ApiCallback#onError(String)}
 * on the main thread.
 */
public final class LocalDataException extends RuntimeException {
    public LocalDataException(String message) {
        super(message);
    }
}
