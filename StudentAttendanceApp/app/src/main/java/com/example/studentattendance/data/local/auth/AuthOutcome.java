package com.example.studentattendance.data.local.auth;

import com.example.studentattendance.model.User;

public final class AuthOutcome {
    public final User user;
    public final String sessionToken;
    public final boolean newAccount;

    public AuthOutcome(User user, String sessionToken, boolean newAccount) {
        this.user = user;
        this.sessionToken = sessionToken;
        this.newAccount = newAccount;
    }
}
