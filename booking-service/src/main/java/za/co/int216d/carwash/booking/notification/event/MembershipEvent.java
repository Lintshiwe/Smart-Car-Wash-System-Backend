package za.co.int216d.carwash.booking.notification.event;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import java.time.LocalDateTime;

/**
 * Base event class for all membership events
 * Published to Kafka topic: membership.events
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MembershipEvent {

    @JsonProperty("event_id")
    private String eventId;

    @JsonProperty("event_type")
    private String eventType; // SUBSCRIBED, RENEWED, UPGRADED, CANCELLED, EXPIRED, SUSPENDED

    @JsonProperty("client_id")
    private Long clientId;

    @JsonProperty("plan_id")
    private Long planId;

    @JsonProperty("plan_name")
    private String planName;

    @JsonProperty("timestamp")
    private LocalDateTime timestamp;

    @JsonProperty("details")
    private String details;

    @JsonProperty("email")
    private String email;

    @JsonProperty("phone")
    private String phone;
}
