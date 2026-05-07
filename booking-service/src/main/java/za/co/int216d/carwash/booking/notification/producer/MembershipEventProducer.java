package za.co.int216d.carwash.booking.notification.producer;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import za.co.int216d.carwash.booking.notification.event.MembershipEvent;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Produces membership events to Kafka
 * Topic: membership.events
 */
@Service
@Slf4j
@ConditionalOnProperty(name = "app.kafka.enabled", havingValue = "true", matchIfMissing = false)
public class MembershipEventProducer {

    private static final String TOPIC = "membership.events";
    private final KafkaTemplate<String, MembershipEvent> kafkaTemplate;

    public MembershipEventProducer(KafkaTemplate<String, MembershipEvent> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    /**
     * Publish membership event
     */
    public void publishEvent(MembershipEvent event) {
        event.setEventId(UUID.randomUUID().toString());
        event.setTimestamp(LocalDateTime.now());
        
        log.info("Publishing membership event: type={}, clientId={}, eventId={}",
            event.getEventType(), event.getClientId(), event.getEventId());
        
        try {
            CompletableFuture<?> sendResult = kafkaTemplate.send(TOPIC, event.getClientId().toString(), event);
            sendResult.whenComplete((result, error) -> {
                if (error != null) {
                    log.warn("Kafka publish failed for membership event {}: {}", event.getEventId(), error.getMessage());
                }
            });
        } catch (Exception ex) {
            log.warn("Kafka publish skipped for membership event {}: {}", event.getEventId(), ex.getMessage());
        }
    }

    /**
     * Publish subscription event
     */
    public void publishSubscriptionEvent(Long clientId, Long planId, String planName, String email, String phone) {
        MembershipEvent event = MembershipEvent.builder()
            .eventType("SUBSCRIBED")
            .clientId(clientId)
            .planId(planId)
            .planName(planName)
            .email(email)
            .phone(phone)
            .details("Client subscribed to " + planName + " plan")
            .build();
        publishEvent(event);
    }

    /**
     * Publish renewal event
     */
    public void publishRenewalEvent(Long clientId, Long planId, String planName, String email, String phone) {
        MembershipEvent event = MembershipEvent.builder()
            .eventType("RENEWED")
            .clientId(clientId)
            .planId(planId)
            .planName(planName)
            .email(email)
            .phone(phone)
            .details("Membership renewed for " + planName + " plan")
            .build();
        publishEvent(event);
    }

    /**
     * Publish expiry event
     */
    public void publishExpiryEvent(Long clientId, Long planId, String planName, String email, String phone) {
        MembershipEvent event = MembershipEvent.builder()
            .eventType("EXPIRED")
            .clientId(clientId)
            .planId(planId)
            .planName(planName)
            .email(email)
            .phone(phone)
            .details("Membership expired for " + planName + " plan")
            .build();
        publishEvent(event);
    }

    /**
     * Publish expiry warning event (sent N days before expiry)
     */
    public void publishExpiryWarningEvent(Long clientId, Long planId, String planName, Integer daysUntilExpiry, String email, String phone) {
        MembershipEvent event = MembershipEvent.builder()
            .eventType("EXPIRING_SOON")
            .clientId(clientId)
            .planId(planId)
            .planName(planName)
            .email(email)
            .phone(phone)
            .details("Membership expires in " + daysUntilExpiry + " days")
            .build();
        publishEvent(event);
    }

    /**
     * Publish cancellation event
     */
    public void publishCancellationEvent(Long clientId, Long planId, String planName, String email, String phone) {
        MembershipEvent event = MembershipEvent.builder()
            .eventType("CANCELLED")
            .clientId(clientId)
            .planId(planId)
            .planName(planName)
            .email(email)
            .phone(phone)
            .details("Membership cancelled for " + planName + " plan")
            .build();
        publishEvent(event);
    }

    /**
     * Publish suspension event
     */
    public void publishSuspensionEvent(Long clientId, Long planId, String planName, String email, String phone) {
        MembershipEvent event = MembershipEvent.builder()
            .eventType("SUSPENDED")
            .clientId(clientId)
            .planId(planId)
            .planName(planName)
            .email(email)
            .phone(phone)
            .details("Membership suspended for " + planName + " plan")
            .build();
        publishEvent(event);
    }
}
