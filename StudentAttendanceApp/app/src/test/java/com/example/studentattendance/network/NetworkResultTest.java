package com.example.studentattendance.network;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class NetworkResultTest {

    @Test
    public void successCanCarryOfflineWarning() {
        NetworkResult<String> result = NetworkResult.success("cached", "Couldn't reach the server.");
        assertTrue(result.isSuccess());
        assertEquals("cached", result.getData());
        assertEquals("Couldn't reach the server.", result.getMessage());
    }

    @Test
    public void plainSuccessHasNoMessage() {
        NetworkResult<String> result = NetworkResult.success("fresh");
        assertTrue(result.isSuccess());
        assertNull(result.getMessage());
        assertFalse(result.isError());
    }

    @Test
    public void retryableHttpCodes() {
        assertTrue(RetrofitExec.isRetryableHttp(500));
        assertTrue(RetrofitExec.isRetryableHttp(429));
        assertTrue(RetrofitExec.isRetryableHttp(408));
        assertFalse(RetrofitExec.isRetryableHttp(400));
        assertFalse(RetrofitExec.isRetryableHttp(401));
        assertFalse(RetrofitExec.isRetryableHttp(404));
    }
}
