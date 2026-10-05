package com.example.studentattendance.data.local.auth;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.example.studentattendance.data.local.LocalDataException;
import com.example.studentattendance.model.User;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class LocalAuthEngineTest {

    private InMemoryAccountStore store;
    private LocalAuthEngine engine;
    private final List<String> seededUsers = new ArrayList<>();

    @Before
    public void setUp() {
        store = new InMemoryAccountStore();
        seededUsers.clear();
        engine = new LocalAuthEngine(store, new PasswordHasher(), seededUsers::add);
    }

    @Test
    public void registerThenLoginWithSameCredentialsSucceeds() {
        AuthOutcome registered = engine.register("Ayesha Khan", "teacher@school.edu", "password123", "Professor");
        assertNotNull(registered.user.getId());
        assertTrue(registered.sessionToken.startsWith("local_"));
        assertEquals("teacher@school.edu", registered.user.getEmail());
        assertEquals(1, seededUsers.size());
        AccountRecord stored = store.findByEmail("teacher@school.edu");
        assertNotNull(stored.passwordHash);
        assertNotEquals("password123", stored.passwordHash);

        AuthOutcome loggedIn = engine.login("teacher@school.edu", "password123");
        assertEquals(registered.user.getId(), loggedIn.user.getId());
        assertEquals("Ayesha Khan", loggedIn.user.getName());
    }

    @Test
    public void accountPersistsAfterLogoutSoLoginWorksAgain() {
        AuthOutcome registered = engine.register("Ayesha Khan", "teacher@school.edu", "password123", "Professor");
        String userId = registered.user.getId();
        String sessionToken = registered.sessionToken;
        AccountRecord stored = store.findByEmail("teacher@school.edu");
        assertNotNull(stored);
        assertNotEquals(sessionToken, stored.passwordHash);

        AuthOutcome afterRestart = engine.login("  Teacher@school.edu ", "password123");
        assertEquals(userId, afterRestart.user.getId());
        assertNotEquals(sessionToken, afterRestart.sessionToken);
    }

    @Test
    public void loginRejectsWrongPasswordAndUnknownEmail() {
        engine.register("Ayesha Khan", "teacher@school.edu", "password123", "Professor");
        try {
            engine.login("teacher@school.edu", "wrongpass1");
            fail();
        } catch (LocalDataException e) {
            assertEquals("Invalid email or password.", e.getMessage());
        }
        try {
            engine.login("missing@school.edu", "password123");
            fail();
        } catch (LocalDataException e) {
            assertEquals("Invalid email or password.", e.getMessage());
        }
    }

    @Test
    public void duplicateEmailIsRejected() {
        engine.register("Ayesha Khan", "teacher@school.edu", "password123", "Professor");
        try {
            engine.register("Other User", "teacher@school.edu", "password123", "Professor");
            fail();
        } catch (LocalDataException e) {
            assertEquals("An account with this email already exists.", e.getMessage());
        }
    }

    @Test
    public void googleUserIsPersistedAndCanSignInAgain() {
        GoogleIdentity identity = new GoogleIdentity("google-sub-1", "prof@gmail.com", "Google Professor");
        AuthOutcome first = engine.loginWithGoogle(identity);
        assertTrue(first.newAccount);
        assertEquals("prof@gmail.com", first.user.getEmail());
        assertEquals("google-sub-1", store.findById(first.user.getId()).googleSubject);
        assertNull(store.findById(first.user.getId()).passwordHash);

        AuthOutcome second = engine.loginWithGoogle(identity);
        assertEquals(first.user.getId(), second.user.getId());
        assertEquals(1, store.size());
    }

    @Test
    public void googleOnlyAccountCannotUsePasswordLogin() {
        engine.loginWithGoogle(new GoogleIdentity("google-sub-2", "google@school.edu", "Google User"));
        try {
            engine.login("google@school.edu", "password123");
            fail();
        } catch (LocalDataException e) {
            assertEquals("This account uses Google Sign-In. Tap Continue with Google.", e.getMessage());
        }
    }

    @Test
    public void googleSignInLinksExistingPasswordAccount() {
        User passwordUser = engine.register("Ayesha Khan", "shared@school.edu", "password123", "Professor").user;
        AuthOutcome google = engine.loginWithGoogle(
                new GoogleIdentity("google-sub-3", "shared@school.edu", "Ayesha Khan"));
        assertEquals(passwordUser.getId(), google.user.getId());
        assertEquals("google-sub-3", store.findById(passwordUser.getId()).googleSubject);
        AuthOutcome passwordAgain = engine.login("shared@school.edu", "password123");
        assertEquals(passwordUser.getId(), passwordAgain.user.getId());
    }

    private static final class InMemoryAccountStore implements AccountStore {
        private final List<AccountRecord> accounts = new ArrayList<>();

        @Override
        public AccountRecord findById(String id) {
            for (AccountRecord account : accounts) {
                if (account.id.equals(id)) {
                    return account;
                }
            }
            return null;
        }

        @Override
        public AccountRecord findByEmail(String email) {
            for (AccountRecord account : accounts) {
                if (account.email.equals(email)) {
                    return account;
                }
            }
            return null;
        }

        @Override
        public AccountRecord findByGoogleSubject(String googleSubject) {
            for (AccountRecord account : accounts) {
                if (googleSubject != null && googleSubject.equals(account.googleSubject)) {
                    return account;
                }
            }
            return null;
        }

        @Override
        public void insert(AccountRecord account) {
            accounts.add(account);
        }

        @Override
        public void update(AccountRecord account) {
            for (int i = 0; i < accounts.size(); i++) {
                if (accounts.get(i).id.equals(account.id)) {
                    accounts.set(i, account);
                    return;
                }
            }
        }

        int size() {
            return accounts.size();
        }
    }
}
