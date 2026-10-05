package com.studentattendance.backend.service;

import com.studentattendance.backend.entity.Notification;
import com.studentattendance.backend.exception.ForbiddenException;
import com.studentattendance.backend.exception.ResourceNotFoundException;
import com.studentattendance.backend.repository.NotificationRepository;
import com.studentattendance.backend.security.AuthContext;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final AuthContext authContext;

    public NotificationService(NotificationRepository notificationRepository, AuthContext authContext) {
        this.notificationRepository = notificationRepository;
        this.authContext = authContext;
    }

    @Transactional
    public void notify(String userId, String type, String message, String relatedStudentId) {
        Notification notification = new Notification();
        notification.setId("notif_" + UUID.randomUUID());
        notification.setUserId(userId);
        notification.setType(type);
        notification.setMessage(message);
        notification.setRelatedStudentId(relatedStudentId);
        notification.setRead(false);
        notification.setCreatedAtMillis(System.currentTimeMillis());
        notificationRepository.save(notification);
    }

    @Transactional
    public void notifyIfNotDuplicate(String userId, String type, String message, String relatedStudentId, long sinceMillis) {
        boolean alreadyNotified = notificationRepository
                .existsByUserIdAndRelatedStudentIdAndTypeAndCreatedAtMillisGreaterThan(userId, relatedStudentId, type, sinceMillis);
        if (!alreadyNotified) {
            notify(userId, type, message, relatedStudentId);
        }
    }

    @Transactional(readOnly = true)
    public List<Notification> getMyNotifications(Integer page, Integer size) {
        String userId = authContext.currentUserId();
        if (page == null || size == null) {
            return notificationRepository.findByUserIdOrderByCreatedAtMillisDesc(
                    userId, PageRequest.of(0, 50)).getContent();
        }
        int safeSize = Math.max(1, Math.min(size, 100));
        int safePage = Math.max(0, page);
        return notificationRepository.findByUserIdOrderByCreatedAtMillisDesc(
                userId, PageRequest.of(safePage, safeSize)).getContent();
    }

    @Transactional(readOnly = true)
    public long getUnreadCount() {
        return notificationRepository.countByUserIdAndReadFalse(authContext.currentUserId());
    }

    @Transactional
    public void markRead(String notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found."));
        if (!notification.getUserId().equals(authContext.currentUserId())) {
            throw new ForbiddenException("You don't have access to this notification.");
        }
        notification.setRead(true);
        notificationRepository.save(notification);
    }

    @Transactional
    public void markAllRead() {
        notificationRepository.markAllReadForUser(authContext.currentUserId());
    }
}
