package com.example.studentattendance.repository;

import com.example.studentattendance.model.User;
import com.example.studentattendance.network.ApiCallback;

/**
 * Contract for authentication operations. Production stores hashed
 * email/password accounts in Room so register and login work offline.
 */
public interface AuthRepository {
    void login(String email, String password, ApiCallback<User> callback);
    /** Phase 4: creates a new, private professor account, then behaves like a successful login. */
    void register(String name, String email, String password, String designation, ApiCallback<User> callback);
    void loginWithGoogle(String idToken, ApiCallback<User> callback);
    void logout();
    User getCurrentUser();
    boolean isLoggedIn();
    void updateProfile(User user, ApiCallback<User> callback);
}
