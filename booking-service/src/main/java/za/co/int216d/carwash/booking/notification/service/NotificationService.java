package za.co.int216d.carwash.booking.notification.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import za.co.int216d.carwash.booking.notification.domain.Notification;
import za.co.int216d.carwash.booking.notification.repository.NotificationRepository;

import java.time.LocalDateTime;

/**
 * Notification service for in-app notifications
 */
@Service
@Slf4j
@Transactional
public class NotificationService {

    private final NotificationRepository notificationRepository;

    public NotificationService(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    /**
     * Create and save a notification
     */
    public Notification createNotification(
        Long clientId,
        Notification.NotificationType type,
        String title,
        String message,
        String details
    ) {
        log.info("Creating notification for client: {}, type: {}", clientId, type);

        Notification notification = Notification.builder()
            .clientId(clientId)
            .type(type)
            .title(title)
            .message(message)
            .details(details)
            .isRead(false)
            .build();

        return notificationRepository.save(notification);
    }

    /**
     * Get unread notifications for a client (paginated)
     */
    @Transactional(readOnly = true)
    public Page<Notification> getUnreadNotifications(Long clientId, Pageable pageable) {
        log.info("Fetching unread notifications for client: {}", clientId);
        return notificationRepository.findUnreadByClientId(clientId, pageable);
    }

    /**
     * Get all notifications for a client (paginated)
     */
    @Transactional(readOnly = true)
    public Page<Notification> getAllNotifications(Long clientId, Pageable pageable) {
        log.info("Fetching all notifications for client: {}", clientId);
        return notificationRepository.findByClientIdOrderByCreatedAtDesc(clientId, pageable);
    }

    /**
     * Get count of unread notifications
     */
    @Transactional(readOnly = true)
    public Long getUnreadCount(Long clientId) {
        return notificationRepository.countUnreadByClientId(clientId);
    }

    /**
     * Mark notification as read
     */
    public void markAsRead(Long notificationId) {
        log.info("Marking notification {} as read", notificationId);

        Notification notification = notificationRepository.findById(notificationId)
            .orElseThrow(() -> new IllegalArgumentException("Notification not found: " + notificationId));

        notification.setIsRead(true);
        notification.setReadAt(LocalDateTime.now());
        notificationRepository.save(notification);
    }

    /**
     * Mark all notifications as read for a client
     */
    public void markAllAsRead(Long clientId) {
        log.info("Marking all notifications as read for client: {}", clientId);

        notificationRepository.findUnreadByClientId(clientId, Pageable.unpaged())
            .getContent()
            .forEach(notification -> {
                notification.setIsRead(true);
                notification.setReadAt(LocalDateTime.now());
                notificationRepository.save(notification);
            });
    }

    /**
     * Delete a notification
     */
    public void deleteNotification(Long notificationId) {
        log.info("Deleting notification: {}", notificationId);
        notificationRepository.deleteById(notificationId);
    }
}
