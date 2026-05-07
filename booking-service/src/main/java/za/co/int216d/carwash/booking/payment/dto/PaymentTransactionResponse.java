package za.co.int216d.carwash.booking.payment.dto;

import lombok.Builder;
import za.co.int216d.carwash.booking.payment.domain.PaymentGatewayType;
import za.co.int216d.carwash.booking.payment.domain.PaymentPurpose;
import za.co.int216d.carwash.booking.payment.domain.PaymentStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Builder
public record PaymentTransactionResponse(
    Long id,
    String reference,
    Long clientId,
    Long bookingId,
    Long membershipId,
    PaymentPurpose purpose,
    PaymentStatus status,
    PaymentGatewayType gateway,
    BigDecimal amount,
    String currency,
    String gatewayTransactionId,
    String failureReason,
    String description,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
}
