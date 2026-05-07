package za.co.int216d.carwash.booking.notification.domain;

import lombok.*;
import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * In-app notification record
 * Stores notifications sent to clients
 */
@Entity
@Table(name = "notifications", schema = "booking_schema", indexes = {
    @Index(name = "idx_notifications_client_id", columnList = "client_id"),
    @Index(name = "idx_notifications_type", columnList = "notification_type"),
    @Index(name = "idx_notifications_read", columnList = "is_read"),
    @Index(name = "idx_notifications_created", columnList = "created_at")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long clientId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationType type;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String message;

    @Column(columnDefinition = "TEXT")
    private String details;

    @Column(nullable = false, columnDefinition = "BOOLEAN DEFAULT false")
    private Boolean isRead;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column
    private LocalDateTime readAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public enum NotificationType {
        MEMBERSHIP_SUBSCRIBED,
        MEMBERSHIP_RENEWED,
        MEMBERSHIP_UPGRADED,
        MEMBERSHIP_DOWNGRADED,
        MEMBERSHIP_CANCELLED,
        MEMBERSHIP_EXPIRED,
        MEMBERSHIP_EXPIRING_SOON,
        MEMBERSHIP_SUSPENDED,
        CREDITS_PURCHASED,
        CREDITS_USED,
        BOOKING_CONFIRMED,
        BOOKING_CANCELLED,
        BOOKING_COMPLETED,
        PROMOTIONAL_OFFER,
        SYSTEM_ALERT
    }
}
