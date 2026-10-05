package com.example.studentattendance.model;

import java.io.Serializable;

/**
 * An in-app notification. Field names mirror
 * com.studentattendance.backend.entity.Notification exactly for Gson.
 */
public class Notification implements Serializable {
    private String id;
    private String userId;
    private String type;
    private String message;
    private String relatedStudentId;
    private boolean read;
    private long createdAtMillis;

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
