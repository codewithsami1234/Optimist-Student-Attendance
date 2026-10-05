package com.example.studentattendance.data.local.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import com.example.studentattendance.model.Notification;

@Entity(tableName = "notifications",
        foreignKeys = @ForeignKey(
                entity = AccountEntity.class,
                parentColumns = "id",
                childColumns = "ownerUserId",
                onDelete = ForeignKey.CASCADE),
        indices = {
                @Index("ownerUserId")
        })
public class NotificationEntity {
    @PrimaryKey
    @NonNull
    public String id = "";
    @NonNull
    public String ownerUserId = "";
    public String type;
    public String message;
    public String relatedStudentId;
    public boolean read;
    public long createdAtMillis;

    public Notification toModel() {
        Notification notification = new Notification();
        notification.setId(id);
        notification.setUserId(ownerUserId);
        notification.setType(type);
        notification.setMessage(message);
        notification.setRelatedStudentId(relatedStudentId);
        notification.setRead(read);
        notification.setCreatedAtMillis(createdAtMillis);
        return notification;
    }
}
