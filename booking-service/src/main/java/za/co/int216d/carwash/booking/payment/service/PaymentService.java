package za.co.int216d.carwash.booking.payment.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import za.co.int216d.carwash.booking.payment.config.PaymentProperties;
import za.co.int216d.carwash.booking.payment.domain.PaymentGatewayType;
import za.co.int216d.carwash.booking.payment.domain.PaymentPurpose;
import za.co.int216d.carwash.booking.payment.domain.PaymentStatus;
import za.co.int216d.carwash.booking.payment.domain.PaymentTransaction;
import za.co.int216d.carwash.booking.payment.dto.PaymentProcessResult;
import za.co.int216d.carwash.booking.payment.dto.PaymentRequest;
import za.co.int216d.carwash.booking.payment.dto.PaymentTransactionResponse;
import za.co.int216d.carwash.booking.payment.gateway.PaymentGateway;
import za.co.int216d.carwash.booking.payment.repository.PaymentTransactionRepository;
import za.co.int216d.carwash.common.exception.BadRequestException;
import za.co.int216d.carwash.common.exception.ResourceNotFoundException;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class PaymentService {

    private final PaymentTransactionRepository transactionRepository;
    private final PaymentProperties paymentProperties;
    private final List<PaymentGateway> paymentGateways;

    public PaymentProcessResult processPayment(
        Long clientId,
        Long bookingId,
        Long membershipId,
        PaymentPurpose purpose,
        BigDecimal amount,
        String customerEmail,
        String description,
        PaymentRequest paymentRequest
    ) {
        if (!paymentProperties.isEnabled()) {
            throw new BadRequestException("Payments are currently disabled");
        }

        if (amount == null || amount.signum() <= 0) {
            throw new BadRequestException("Payment amount must be greater than zero");
        }

        PaymentGatewayType gatewayType = resolveGateway(paymentRequest == null ? null : paymentRequest.getGateway());
        validateGatewayEnabled(gatewayType);

        if (paymentRequest == null || paymentRequest.getPaymentMethodToken() == null || paymentRequest.getPaymentMethodToken().isBlank()) {
            throw new BadRequestException("payment.paymentMethodToken is required");
        }

        String reference = "PAY-" + UUID.randomUUID().toString().replace("-", "").substring(0, 12).toUpperCase();

        PaymentTransaction transaction = PaymentTransaction.builder()
            .reference(reference)
            .clientId(clientId)
            .bookingId(bookingId)
            .membershipId(membershipId)
            .purpose(purpose)
            .status(PaymentStatus.PENDING)
            .gateway(gatewayType)
            .amount(amount)
            .currency(paymentProperties.getCurrency())
            .description(description)
            .build();

        transaction = transactionRepository.save(transaction);

        PaymentGateway gateway = gatewayByType(gatewayType);
        PaymentGateway.GatewayChargeResult gatewayResult = gateway.charge(new PaymentGateway.ChargeRequest(
            reference,
            amount,
            paymentProperties.getCurrency(),
            clientId,
            customerEmail,
            description,
            paymentRequest.getPaymentMethodToken()
        ));

        if (gatewayResult.success()) {
            transaction.setStatus(PaymentStatus.SUCCEEDED);
            transaction.setGatewayTransactionId(gatewayResult.gatewayTransactionId());
            transaction.setFailureReason(null);
            transactionRepository.save(transaction);
            log.info("Payment succeeded for reference {}", reference);
        } else {
            transaction.setStatus(PaymentStatus.FAILED);
            transaction.setFailureReason(gatewayResult.message());
            transactionRepository.save(transaction);
            log.warn("Payment failed for reference {}: {}", reference, gatewayResult.message());
            throw new BadRequestException("Payment failed: " + gatewayResult.message());
        }

        return PaymentProcessResult.builder()
            .transactionId(transaction.getId())
            .reference(transaction.getReference())
            .status(transaction.getStatus())
            .amount(transaction.getAmount())
            .currency(transaction.getCurrency())
            .message(gatewayResult.message())
            .build();
    }

    @Transactional(readOnly = true)
    public List<PaymentTransactionResponse> getClientTransactions(Long clientId) {
        return transactionRepository.findAllByClientIdOrderByCreatedAtDesc(clientId)
            .stream()
            .map(this::toResponse)
            .toList();
    }

    @Transactional(readOnly = true)
    public PaymentTransactionResponse getClientTransactionByReference(Long clientId, String reference) {
        PaymentTransaction tx = transactionRepository.findByReference(reference)
            .orElseThrow(() -> new ResourceNotFoundException("Payment transaction not found: " + reference));

        if (!tx.getClientId().equals(clientId)) {
            throw new ResourceNotFoundException("Payment transaction not found: " + reference);
        }

        return toResponse(tx);
    }

    private PaymentGatewayType resolveGateway(PaymentGatewayType requested) {
        if (requested != null) {
            return requested;
        }
        return paymentProperties.getDefaultGateway();
    }

    private void validateGatewayEnabled(PaymentGatewayType gatewayType) {
        if (paymentProperties.getEnabledGateways() == null || !paymentProperties.getEnabledGateways().contains(gatewayType)) {
            throw new BadRequestException("Payment gateway is not enabled: " + gatewayType);
        }
    }

    private PaymentGateway gatewayByType(PaymentGatewayType gatewayType) {
        Map<PaymentGatewayType, PaymentGateway> gatewayMap = paymentGateways.stream()
            .collect(Collectors.toMap(PaymentGateway::type, Function.identity(), (first, second) -> first));

        PaymentGateway gateway = gatewayMap.get(gatewayType);
        if (gateway == null) {
            throw new BadRequestException("Gateway implementation not found: " + gatewayType);
        }
        return gateway;
    }

    private PaymentTransactionResponse toResponse(PaymentTransaction tx) {
        return PaymentTransactionResponse.builder()
            .id(tx.getId())
            .reference(tx.getReference())
            .clientId(tx.getClientId())
            .bookingId(tx.getBookingId())
            .membershipId(tx.getMembershipId())
            .purpose(tx.getPurpose())
            .status(tx.getStatus())
            .gateway(tx.getGateway())
            .amount(tx.getAmount())
            .currency(tx.getCurrency())
            .gatewayTransactionId(tx.getGatewayTransactionId())
            .failureReason(tx.getFailureReason())
            .description(tx.getDescription())
            .createdAt(tx.getCreatedAt())
            .updatedAt(tx.getUpdatedAt())
            .build();
    }
}
