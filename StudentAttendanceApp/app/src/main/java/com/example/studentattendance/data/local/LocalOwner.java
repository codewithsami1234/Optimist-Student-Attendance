package com.example.studentattendance.data.local;

import com.example.studentattendance.model.User;
import com.example.studentattendance.utils.PreferenceManager;

public final class LocalOwner {

    private LocalOwner() {
    }

    public static String requireId(PreferenceManager prefs) {
        User user = prefs.getSavedUser();
        if (user == null || user.getId() == null || user.getId().isEmpty()) {
            throw new LocalDataException("Please sign in again.");
        }
        return user.getId();
    }

    public static User requireUser(PreferenceManager prefs) {
        User user = prefs.getSavedUser();
        if (user == null || user.getId() == null) {
            throw new LocalDataException("Please sign in again.");
        }
        return user;
    }
}
