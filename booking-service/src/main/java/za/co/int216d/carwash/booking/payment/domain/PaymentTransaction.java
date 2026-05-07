package za.co.int216d.carwash.booking.payment.domain;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "payment_transactions", schema = "booking_schema", indexes = {
    @Index(name = "idx_payment_tx_client_id", columnList = "client_id"),
    @Index(name = "idx_payment_tx_booking_id", columnList = "booking_id"),
    @Index(name = "idx_payment_tx_membership_id", columnList = "membership_id"),
    @Index(name = "idx_payment_tx_reference", columnList = "reference"),
    @Index(name = "idx_payment_tx_status", columnList = "status"),
    @Index(name = "idx_payment_tx_created_at", columnList = "created_at")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 64)
    private String reference;

    @Column(nullable = false)
    private Long clientId;

    @Column
    private Long bookingId;

    @Column
    private Long membershipId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private PaymentPurpose purpose;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PaymentStatus status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PaymentGatewayType gateway;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false, length = 3)
    private String currency;

    @Column(length = 120)
    private String gatewayTransactionId;

    @Column(length = 255)
    private String failureReason;

    @Column(length = 255)
    private String description;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
