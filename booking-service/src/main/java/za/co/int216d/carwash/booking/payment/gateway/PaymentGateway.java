package za.co.int216d.carwash.booking.payment.gateway;

import za.co.int216d.carwash.booking.payment.domain.PaymentGatewayType;

import java.math.BigDecimal;

public interface PaymentGateway {

    PaymentGatewayType type();

    GatewayChargeResult charge(ChargeRequest request);

    record ChargeRequest(
        String reference,
        BigDecimal amount,
        String currency,
        Long clientId,
        String customerEmail,
        String description,
        String paymentMethodToken
    ) {
    }

    record GatewayChargeResult(
        boolean success,
        String gatewayTransactionId,
        String message
    ) {
    }
}
