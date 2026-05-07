package za.co.int216d.carwash.booking.payment.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;
import za.co.int216d.carwash.booking.payment.domain.PaymentGatewayType;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentRequest {

    private PaymentGatewayType gateway;

    @NotBlank(message = "Payment method token is required")
    private String paymentMethodToken;
}
