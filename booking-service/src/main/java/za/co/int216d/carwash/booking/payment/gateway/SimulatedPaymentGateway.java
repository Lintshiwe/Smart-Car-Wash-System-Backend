package za.co.int216d.carwash.booking.payment.gateway;

import org.springframework.stereotype.Component;
import za.co.int216d.carwash.booking.payment.config.PaymentProperties;
import za.co.int216d.carwash.booking.payment.domain.PaymentGatewayType;

import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

@Component
public class SimulatedPaymentGateway implements PaymentGateway {

    private final PaymentProperties properties;

    public SimulatedPaymentGateway(PaymentProperties properties) {
        this.properties = properties;
    }

    @Override
    public PaymentGatewayType type() {
        return PaymentGatewayType.SIMULATED;
    }

    @Override
    public GatewayChargeResult charge(ChargeRequest request) {
        boolean autoApprove = properties.getSimulated().isAutoApprove();
        double failRate = properties.getSimulated().getFailRate();

        boolean shouldFail = !autoApprove || ThreadLocalRandom.current().nextDouble(0, 1) < failRate;
        if (shouldFail) {
            return new GatewayChargeResult(false, null, "Simulated gateway rejected this transaction");
        }

        String txId = "sim_" + UUID.randomUUID();
        return new GatewayChargeResult(true, txId, "Payment captured by simulated gateway");
    }
}
