package za.co.int216d.carwash.booking.payment.gateway;

import org.springframework.stereotype.Component;
import za.co.int216d.carwash.booking.payment.domain.PaymentGatewayType;

@Component
public class OfflinePaymentGateway implements PaymentGateway {

    @Override
    public PaymentGatewayType type() {
        return PaymentGatewayType.OFFLINE;
    }

    @Override
    public GatewayChargeResult charge(ChargeRequest request) {
        return new GatewayChargeResult(false, null, "Offline payments cannot be auto-charged");
    }
}
