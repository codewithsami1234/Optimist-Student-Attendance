package com.example.studentattendance.network;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ApiUrlPolicyTest {

    @Test
    public void physicalDevicesNeverAcceptEmulatorLoopback() {
        assertFalse(ApiUrlPolicy.isHttpsCloudUrl("http://10.0.2.2:8080/"));
        assertFalse(ApiUrlPolicy.isAllowed("http://10.0.2.2:8080/"));
        assertFalse(ApiUrlPolicy.isAllowed("http://192.168.1.10:8080/"));
        assertTrue(ApiUrlPolicy.resolve("http://10.0.2.2:8080/").startsWith("https://"));
    }

    @Test
    public void httpsCloudUrlsAreAccepted() {
        String url = "https://student-attendance-api.onrender.com";
        assertTrue(ApiUrlPolicy.isAllowed(url));
        assertEquals("https://student-attendance-api.onrender.com/", ApiUrlPolicy.normalize(url));
        assertEquals("https://student-attendance-api.onrender.com/", ApiUrlPolicy.resolve(url));
    }
}
