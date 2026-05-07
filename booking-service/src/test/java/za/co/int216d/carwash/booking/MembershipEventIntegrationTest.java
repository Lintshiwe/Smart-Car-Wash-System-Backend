package za.co.int216d.carwash.booking;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.context.TestPropertySource;
import za.co.int216d.carwash.booking.membership.domain.Membership;
import za.co.int216d.carwash.booking.membership.domain.MembershipPlan;
import za.co.int216d.carwash.booking.notification.domain.Notification;
import za.co.int216d.carwash.booking.notification.event.MembershipEvent;
import za.co.int216d.carwash.booking.notification.producer.MembershipEventProducer;
import za.co.int216d.carwash.booking.notification.repository.NotificationRepository;
import za.co.int216d.carwash.common.security.JwtAuthenticationFilter;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.awaitility.Awaitility.await;

/**
 * Kafka integration tests for membership events
 */
@SpringBootTest
@EmbeddedKafka(partitions = 1, brokerProperties = {
    "listeners=PLAINTEXT://localhost:9092",
    "port=9092"
})
@TestPropertySource(properties = {
    "spring.kafka.bootstrap-servers=localhost:9092",
    "spring.flyway.enabled=false",
    "spring.datasource.url=jdbc:h2:mem:testdb;MODE=PostgreSQL;DB_CLOSE_DELAY=-1;DATABASE_TO_LOWER=TRUE",
    "spring.datasource.hikari.connection-init-sql=CREATE SCHEMA IF NOT EXISTS booking_schema",
    "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "spring.jpa.properties.hibernate.default_schema=booking_schema",
    "spring.jpa.properties.hibernate.hbm2ddl.create_namespaces=true",
    "jwt.secret=testsecretkeytestsecretkeytestsecretkey123"
})
class MembershipEventIntegrationTest {

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockBean
    private JavaMailSender javaMailSender;

    @Autowired
    private MembershipEventProducer eventProducer;

    @Autowired
    private KafkaTemplate<String, MembershipEvent> kafkaTemplate;

    @Autowired
    private NotificationRepository notificationRepository;

    @BeforeEach
    void setUp() {
        notificationRepository.deleteAll();
    }

    @Test
    void testPublishSubscriptionEvent() throws Exception {
        Long clientId = 100L;
        Long planId = 1L;
        String planName = "Premium";

        eventProducer.publishSubscriptionEvent(clientId, planId, planName, "test@example.com", "customer");

        // Give Kafka time to process
        Thread.sleep(1000);

        // Verify notification was created
        var notifications = notificationRepository.findByClientIdOrderByCreatedAtDesc(clientId, 
            org.springframework.data.domain.PageRequest.of(0, 10));
        
        assertTrue(notifications.hasContent(), "Notification should be created");
        assertEquals(Notification.NotificationType.MEMBERSHIP_SUBSCRIBED, notifications.getContent().get(0).getType());
    }

    @Test
    void testPublishRenewalEvent() throws Exception {
        Long clientId = 101L;
        Long planId = 1L;
        String planName = "Premium";

        eventProducer.publishRenewalEvent(clientId, planId, planName, "test@example.com", "customer");

        Thread.sleep(1000);

        var notifications = notificationRepository.findByClientIdOrderByCreatedAtDesc(clientId, 
            org.springframework.data.domain.PageRequest.of(0, 10));
        
        assertTrue(notifications.hasContent());
        assertEquals(Notification.NotificationType.MEMBERSHIP_RENEWED, notifications.getContent().get(0).getType());
    }

    @Test
    void testPublishCancellationEvent() throws Exception {
        Long clientId = 102L;
        Long planId = 1L;
        String planName = "Premium";

        eventProducer.publishCancellationEvent(clientId, planId, planName, "test@example.com", "customer");

        Thread.sleep(1000);

        var notifications = notificationRepository.findByClientIdOrderByCreatedAtDesc(clientId, 
            org.springframework.data.domain.PageRequest.of(0, 10));
        
        assertTrue(notifications.hasContent());
        assertEquals(Notification.NotificationType.MEMBERSHIP_CANCELLED, notifications.getContent().get(0).getType());
    }

    @Test
    void testEventHandlerIntegration() throws Exception {
        Long clientId = 103L;

        // Publish event
        MembershipEvent event = MembershipEvent.builder()
            .eventId("test-event-1")
            .eventType("SUBSCRIBED")
            .clientId(clientId)
            .planId(1L)
            .planName("Premium")
            .timestamp(LocalDateTime.now())
            .details("Test subscription")
            .email("test@example.com")
            .phone("+1234567890")
            .build();

        kafkaTemplate.send("membership.events", event.getEventId(), event);

        // Wait for consumer to process
        await()
            .atMost(5, TimeUnit.SECONDS)
            .pollInterval(100, TimeUnit.MILLISECONDS)
            .untilAsserted(() -> {
                var notifications = notificationRepository.findByClientIdOrderByCreatedAtDesc(clientId, 
                    org.springframework.data.domain.PageRequest.of(0, 10));
                assertTrue(notifications.hasContent(), "Notification should be created by consumer");
            });
    }
}
