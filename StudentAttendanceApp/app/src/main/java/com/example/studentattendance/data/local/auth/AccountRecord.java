package com.example.studentattendance.data.local.auth;

import com.example.studentattendance.model.User;

/** Plain account row used by {@link LocalAuthEngine} (Room or in-memory). */
public final class AccountRecord {
    public String id;
    public String email;
    public String name;
    public String role;
    public String designation;
    public String passwordHash;
    public String googleSubject;
    public double lowAttendanceThreshold = 75.0;
    public long createdAtMillis;

    public User toUser() {
        User user = new User(id, name, email, role != null ? role : "Teacher",
                designation != null && !designation.isEmpty() ? designation : "Professor");
        user.setLowAttendanceThreshold(lowAttendanceThreshold <= 0 ? 75.0 : lowAttendanceThreshold);
        return user;
    }
}
