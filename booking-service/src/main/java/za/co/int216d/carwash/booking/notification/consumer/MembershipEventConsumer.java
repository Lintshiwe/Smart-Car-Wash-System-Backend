package za.co.int216d.carwash.booking.notification.consumer;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import za.co.int216d.carwash.booking.notification.domain.Notification;
import za.co.int216d.carwash.booking.notification.event.MembershipEvent;
import za.co.int216d.carwash.booking.notification.service.EmailNotificationService;
import za.co.int216d.carwash.booking.notification.service.NotificationService;

/**
 * Kafka consumer for membership events
 * Listens to membership.events topic and sends notifications
 */
@Service
@Slf4j
@ConditionalOnProperty(name = "app.kafka.enabled", havingValue = "true", matchIfMissing = false)
public class MembershipEventConsumer {

    private final NotificationService notificationService;
    private final EmailNotificationService emailService;

    public MembershipEventConsumer(
        NotificationService notificationService,
        EmailNotificationService emailService
    ) {
        this.notificationService = notificationService;
        this.emailService = emailService;
    }

    /**
     * Listen for membership events and process them
     */
    @KafkaListener(topics = "membership.events", groupId = "booking-service-group")
    public void handleMembershipEvent(MembershipEvent event) {
        log.info("Received membership event: type={}, clientId={}, eventId={}",
            event.getEventType(), event.getClientId(), event.getEventId());

        try {
            switch (event.getEventType()) {
                case "SUBSCRIBED":
                    handleSubscriptionEvent(event);
                    break;
                case "RENEWED":
                    handleRenewalEvent(event);
                    break;
                case "EXPIRED":
                    handleExpiryEvent(event);
                    break;
                case "EXPIRING_SOON":
                    handleExpiryWarningEvent(event);
                    break;
                case "CANCELLED":
                    handleCancellationEvent(event);
                    break;
                case "SUSPENDED":
                    handleSuspensionEvent(event);
                    break;
                default:
                    log.warn("Unknown event type: {}", event.getEventType());
            }
        } catch (Exception e) {
            log.error("Error handling membership event: {}", event.getEventId(), e);
        }
    }

    private void handleSubscriptionEvent(MembershipEvent event) {
        log.info("Handling subscription event for client: {}", event.getClientId());

        // Create in-app notification
        notificationService.createNotification(
            event.getClientId(),
            Notification.NotificationType.MEMBERSHIP_SUBSCRIBED,
            "Welcome to " + event.getPlanName() + "!",
            "You have successfully subscribed to " + event.getPlanName() + " membership.",
            event.getDetails()
        );

        // Send email
        if (event.getEmail() != null) {
            emailService.sendSubscriptionEmail(event.getEmail(), "Valued Customer", event.getPlanName(), 0.0);
        }
    }

    private void handleRenewalEvent(MembershipEvent event) {
        log.info("Handling renewal event for client: {}", event.getClientId());

        // Create in-app notification
        notificationService.createNotification(
            event.getClientId(),
            Notification.NotificationType.MEMBERSHIP_RENEWED,
            "Membership Renewed",
            "Your " + event.getPlanName() + " membership has been renewed.",
            event.getDetails()
        );

        // Send email
        if (event.getEmail() != null) {
            emailService.sendRenewalEmail(event.getEmail(), "Valued Customer", event.getPlanName(), null);
        }
    }

    private void handleExpiryEvent(MembershipEvent event) {
        log.info("Handling expiry event for client: {}", event.getClientId());

        // Create in-app notification
        notificationService.createNotification(
            event.getClientId(),
            Notification.NotificationType.MEMBERSHIP_EXPIRED,
            "Membership Expired",
            "Your " + event.getPlanName() + " membership has expired.",
            event.getDetails()
        );

        // Send email
        if (event.getEmail() != null) {
            emailService.sendExpiryEmail(event.getEmail(), "Valued Customer", event.getPlanName());
        }
    }

    private void handleExpiryWarningEvent(MembershipEvent event) {
        log.info("Handling expiry warning event for client: {}", event.getClientId());

        String daysStr = event.getDetails().replaceAll("[^0-9]", "");
        Integer days = daysStr.isEmpty() ? 7 : Integer.parseInt(daysStr);

        // Create in-app notification
        notificationService.createNotification(
            event.getClientId(),
            Notification.NotificationType.MEMBERSHIP_EXPIRING_SOON,
            "Membership Expiring Soon",
            "Your " + event.getPlanName() + " membership expires in " + days + " days.",
            event.getDetails()
        );

        // Send email
        if (event.getEmail() != null) {
            emailService.sendExpiryWarningEmail(event.getEmail(), "Valued Customer", days);
        }
    }

    private void handleCancellationEvent(MembershipEvent event) {
        log.info("Handling cancellation event for client: {}", event.getClientId());

        // Create in-app notification
        notificationService.createNotification(
            event.getClientId(),
            Notification.NotificationType.MEMBERSHIP_CANCELLED,
            "Membership Cancelled",
            "Your " + event.getPlanName() + " membership has been cancelled.",
            event.getDetails()
        );

        // Send email
        if (event.getEmail() != null) {
            emailService.sendCancellationEmail(event.getEmail(), "Valued Customer", event.getPlanName());
        }
    }

    private void handleSuspensionEvent(MembershipEvent event) {
        log.info("Handling suspension event for client: {}", event.getClientId());

        // Create in-app notification
        notificationService.createNotification(
            event.getClientId(),
            Notification.NotificationType.MEMBERSHIP_SUSPENDED,
            "Membership Suspended",
            "Your " + event.getPlanName() + " membership has been suspended.",
            event.getDetails()
        );

        // Send email
        if (event.getEmail() != null) {
            emailService.sendCancellationEmail(event.getEmail(), "Valued Customer", event.getPlanName());
        }
    }
}
