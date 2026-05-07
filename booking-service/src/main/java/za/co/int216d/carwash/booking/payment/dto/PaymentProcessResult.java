package za.co.int216d.carwash.booking.payment.dto;

import lombok.Builder;
import za.co.int216d.carwash.booking.payment.domain.PaymentStatus;

import java.math.BigDecimal;

@Builder
public record PaymentProcessResult(
    Long transactionId,
    String reference,
    PaymentStatus status,
    BigDecimal amount,
    String currency,
    String message
) {
}
