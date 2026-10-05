package com.example.studentattendance.data.local.auth;

import java.util.UUID;
import java.util.function.Consumer;

import com.example.studentattendance.data.local.LocalDataException;
import com.example.studentattendance.model.User;
import com.example.studentattendance.utils.ValidationUtils;

/**
 * Email/password and Google identity persistence. Passwords are hashed;
 * Google users are stored after a verified ID token. No cloud database.
 */
public final class LocalAuthEngine {

    private final AccountStore store;
    private final PasswordHasher hasher;
    private final Consumer<String> onNewAccount;

    public LocalAuthEngine(AccountStore store, PasswordHasher hasher, Consumer<String> onNewAccount) {
        this.store = store;
        this.hasher = hasher;
        this.onNewAccount = onNewAccount;
    }

    public AuthOutcome register(String name, String email, String password, String designation) {
        if (name == null || name.trim().length() < 2) {
            throw new LocalDataException("Please enter your name.");
        }
        if (!ValidationUtils.isValidEmail(email)) {
            throw new LocalDataException("Please enter a valid email address.");
        }
        if (!ValidationUtils.isValidNewPassword(password)) {
            throw new LocalDataException("Password must be at least 8 characters and include a letter and a number.");
        }
        String normalizedEmail = email.trim().toLowerCase();
        if (store.findByEmail(normalizedEmail) != null) {
            throw new LocalDataException("An account with this email already exists.");
        }
        AccountRecord account = new AccountRecord();
        account.id = "user_" + UUID.randomUUID();
        account.email = normalizedEmail;
        account.name = name.trim();
        account.role = "Teacher";
        account.designation = designation != null && !designation.trim().isEmpty()
                ? designation.trim() : "Professor";
        account.passwordHash = hasher.hash(password.toCharArray());
        account.lowAttendanceThreshold = 75.0;
        account.createdAtMillis = System.currentTimeMillis();
        store.insert(account);
        notifyNewAccount(account.id);
        return session(account, true);
    }

    public AuthOutcome login(String email, String password) {
        if (email == null || password == null) {
            throw new LocalDataException("Please enter both email and password.");
        }
        String normalizedEmail = email.trim().toLowerCase();
        if (normalizedEmail.isEmpty() || password.isEmpty()) {
            throw new LocalDataException("Please enter both email and password.");
        }
        AccountRecord account = store.findByEmail(normalizedEmail);
        if (account == null) {
            throw new LocalDataException("Invalid email or password.");
        }
        if (account.passwordHash == null || account.passwordHash.isEmpty()) {
            throw new LocalDataException("This account uses Google Sign-In. Tap Continue with Google.");
        }
        if (!hasher.verify(password.toCharArray(), account.passwordHash)) {
            if (account.googleSubject != null && !account.googleSubject.isEmpty()) {
                throw new LocalDataException("This account uses Google Sign-In. Tap Continue with Google.");
            }
            throw new LocalDataException("Invalid email or password.");
        }
        return session(account, false);
    }

    public AuthOutcome loginWithGoogle(GoogleIdentity identity) {
        if (identity == null || identity.subject == null || identity.subject.isEmpty()
                || identity.email == null || identity.email.isEmpty()) {
            throw new LocalDataException("Google Sign-In did not return a verified account.");
        }
        String email = identity.email.trim().toLowerCase();
        AccountRecord account = store.findByGoogleSubject(identity.subject);
        boolean created = false;
        if (account == null) {
            account = store.findByEmail(email);
        }
        if (account == null) {
            account = new AccountRecord();
            account.id = "user_" + UUID.randomUUID();
            account.email = email;
            account.name = identity.name != null && !identity.name.trim().isEmpty()
                    ? identity.name.trim() : email.substring(0, email.indexOf('@'));
            account.role = "Teacher";
            account.designation = "Professor";
            account.googleSubject = identity.subject;
            account.lowAttendanceThreshold = 75.0;
            account.createdAtMillis = System.currentTimeMillis();
            store.insert(account);
            created = true;
            notifyNewAccount(account.id);
        } else if (account.googleSubject == null || account.googleSubject.isEmpty()) {
            account.googleSubject = identity.subject;
            store.update(account);
        }
        return session(account, created);
    }

    public User updateProfile(String userId, User incoming) {
        if (incoming == null || incoming.getName() == null || incoming.getName().trim().isEmpty()) {
            throw new LocalDataException("Name cannot be empty.");
        }
        AccountRecord account = store.findById(userId);
        if (account == null) {
            throw new LocalDataException("Please sign in again.");
        }
        account.name = incoming.getName().trim();
        if (incoming.getDesignation() != null) {
            account.designation = incoming.getDesignation().trim();
        }
        double threshold = incoming.getLowAttendanceThreshold();
        if (threshold > 0) {
            account.lowAttendanceThreshold = Math.max(1, Math.min(100, threshold));
        }
        store.update(account);
        return account.toUser();
    }

    private AuthOutcome session(AccountRecord account, boolean newAccount) {
        return new AuthOutcome(account.toUser(), "local_" + UUID.randomUUID(), newAccount);
    }

    private void notifyNewAccount(String userId) {
        if (onNewAccount != null) {
            onNewAccount.accept(userId);
        }
    }
}
