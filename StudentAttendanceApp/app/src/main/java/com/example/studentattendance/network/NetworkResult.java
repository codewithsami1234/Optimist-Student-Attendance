package com.example.studentattendance.network;

/**
 * A generic wrapper describing the outcome of any repository operation
 * (regardless of whether the underlying data source is a mock, local DB,
 * or a real REST API). UI code should only ever depend on this contract.
 *
 * @param <T> the type of data returned on success.
 */
public class NetworkResult<T> {

    public enum Status { SUCCESS, ERROR, LOADING }

    private final Status status;
    private final T data;
    private final String message;

    private NetworkResult(Status status, T data, String message) {
        this.status = status;
        this.data = data;
        this.message = message;
    }

    public static <T> NetworkResult<T> loading() {
        return new NetworkResult<>(Status.LOADING, null, null);
    }

    public static <T> NetworkResult<T> success(T data) {
        return new NetworkResult<>(Status.SUCCESS, data, null);
    }

    /** Success with an offline/stale warning the UI should still display. */
    public static <T> NetworkResult<T> success(T data, String message) {
        return new NetworkResult<>(Status.SUCCESS, data, message);
    }

    public static <T> NetworkResult<T> error(String message) {
        return new NetworkResult<>(Status.ERROR, null, message);
    }

    public Status getStatus() {
        return status;
    }

    public T getData() {
        return data;
    }

    public String getMessage() {
        return message;
    }

    public boolean isSuccess() {
        return status == Status.SUCCESS;
    }

    public boolean isError() {
        return status == Status.ERROR;
    }

    public boolean isLoading() {
        return status == Status.LOADING;
    }
}
