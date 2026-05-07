package za.co.int216d.carwash.booking.notification.web;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import za.co.int216d.carwash.booking.notification.domain.Notification;
import za.co.int216d.carwash.booking.notification.service.NotificationService;
import za.co.int216d.carwash.common.security.SecurityUtils;

@RestController
@RequestMapping("/notifications")
@Slf4j
public class NotificationController {

    private final NotificationService notificationService;
    private final SecurityUtils securityUtils;

    public NotificationController(NotificationService notificationService, SecurityUtils securityUtils) {
        this.notificationService = notificationService;
        this.securityUtils = securityUtils;
    }

    @GetMapping
    public ResponseEntity<Page<Notification>> getAllNotifications(
        @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Long clientId = securityUtils.getCurrentUserIdAsLong();
        log.info("GET /notifications - Fetching all notifications for client {}", clientId);
        Page<Notification> notifications = notificationService.getAllNotifications(clientId, pageable);
        return ResponseEntity.ok(notifications);
    }

    @GetMapping("/unread")
    public ResponseEntity<Page<Notification>> getUnreadNotifications(
        @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Long clientId = securityUtils.getCurrentUserIdAsLong();
        log.info("GET /notifications/unread - Fetching unread notifications for client {}", clientId);
        Page<Notification> notifications = notificationService.getUnreadNotifications(clientId, pageable);
        return ResponseEntity.ok(notifications);
    }

    @GetMapping("/unread/count")
    public ResponseEntity<Long> getUnreadCount() {
        Long clientId = securityUtils.getCurrentUserIdAsLong();
        log.info("GET /notifications/unread/count - Fetching unread count for client {}", clientId);
        Long count = notificationService.getUnreadCount(clientId);
        return ResponseEntity.ok(count);
    }

    @PostMapping("/{notificationId}/read")
    public ResponseEntity<Void> markAsRead(@PathVariable Long notificationId) {
        log.info("POST /notifications/{}/read - Marking notification as read", notificationId);
        notificationService.markAsRead(notificationId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/read-all")
    public ResponseEntity<Void> markAllAsRead() {
        Long clientId = securityUtils.getCurrentUserIdAsLong();
        log.info("POST /notifications/read-all - Marking all notifications as read for client {}", clientId);
        notificationService.markAllAsRead(clientId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{notificationId}")
    public ResponseEntity<Void> deleteNotification(@PathVariable Long notificationId) {
        log.info("DELETE /notifications/{} - Deleting notification", notificationId);
        notificationService.deleteNotification(notificationId);
        return ResponseEntity.noContent().build();
    }
}
