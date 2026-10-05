package com.example.studentattendance.data.local.auth;

public final class GoogleIdentity {
    public final String subject;
    public final String email;
    public final String name;

    public GoogleIdentity(String subject, String email, String name) {
        this.subject = subject;
        this.email = email;
        this.name = name;
    }
}
