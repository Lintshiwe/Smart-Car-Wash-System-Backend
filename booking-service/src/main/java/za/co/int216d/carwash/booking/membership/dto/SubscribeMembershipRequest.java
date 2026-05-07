package za.co.int216d.carwash.booking.membership.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.*;
import za.co.int216d.carwash.booking.payment.dto.PaymentRequest;

/**
 * DTO for client to subscribe to a membership plan
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubscribeMembershipRequest {

    @NotNull(message = "Plan ID is required")
    @Positive(message = "Plan ID must be positive")
    private Long planId;

    @NotNull(message = "Auto-renewal preference is required")
    private Boolean autoRenew;

    @NotNull(message = "Payment details are required")
    @Valid
    private PaymentRequest payment;

    @Email(message = "Email must be valid")
    private String email;
}
