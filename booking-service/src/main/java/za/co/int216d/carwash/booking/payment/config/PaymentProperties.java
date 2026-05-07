package za.co.int216d.carwash.booking.payment.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import za.co.int216d.carwash.booking.payment.domain.PaymentGatewayType;

import java.util.ArrayList;
import java.util.List;

@Component
@ConfigurationProperties(prefix = "app.payment")
public class PaymentProperties {

    private boolean enabled = true;
    private String currency = "ZAR";
    private PaymentGatewayType defaultGateway = PaymentGatewayType.SIMULATED;
    private List<PaymentGatewayType> enabledGateways = new ArrayList<>(List.of(PaymentGatewayType.SIMULATED));
    private Simulated simulated = new Simulated();

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public PaymentGatewayType getDefaultGateway() {
        return defaultGateway;
    }

    public void setDefaultGateway(PaymentGatewayType defaultGateway) {
        this.defaultGateway = defaultGateway;
    }

    public List<PaymentGatewayType> getEnabledGateways() {
        return enabledGateways;
    }

    public void setEnabledGateways(List<PaymentGatewayType> enabledGateways) {
        this.enabledGateways = enabledGateways;
    }

    public Simulated getSimulated() {
        return simulated;
    }

    public void setSimulated(Simulated simulated) {
        this.simulated = simulated;
    }

    public static class Simulated {
        private boolean autoApprove = true;
        private double failRate = 0.0;

        public boolean isAutoApprove() {
            return autoApprove;
        }

        public void setAutoApprove(boolean autoApprove) {
            this.autoApprove = autoApprove;
        }

        public double getFailRate() {
            return failRate;
        }

        public void setFailRate(double failRate) {
            this.failRate = failRate;
        }
    }
}
