package com.studentattendance.backend.entity;

import jakarta.persistence.*;

/**
 * A single in-app notification for a professor - generated automatically
 * for events they'd want to know about (an attendance adjustment they made,
 * a student's attendance dropping below the low-attendance threshold after
 * a session is saved). The Android client polls
 * {@code GET /api/notifications} regularly (see NotificationsViewModel) to
 * give a "live updates" feel without requiring a push-notification backend.
 */
@Entity
@Table(name = "notifications")
public class Notification {

    @Id
    @Column(name = "id", length = 64)
    private String id;

    @Column(name = "user_id", nullable = false, length = 64)
    private String userId;

    @Column(nullable = false, length = 64)
    private String type;

    @Column(nullable = false, length = 500)
    private String message;

    @Column(name = "related_student_id", length = 64)
    private String relatedStudentId;

    @Column(nullable = false)
    private boolean read;

    @Column(name = "created_at_millis", nullable = false)
    private long createdAtMillis;

    public Notification() {
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public String getRelatedStudentId() { return relatedStudentId; }
    public void setRelatedStudentId(String relatedStudentId) { this.relatedStudentId = relatedStudentId; }
    public boolean isRead() { return read; }
    public void setRead(boolean read) { this.read = read; }
    public long getCreatedAtMillis() { return createdAtMillis; }
    public void setCreatedAtMillis(long createdAtMillis) { this.createdAtMillis = createdAtMillis; }
}
