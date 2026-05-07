package za.co.int216d.carwash.booking.membership.domain;

import lombok.*;
import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Membership credit transaction log
 * Tracks all credit usage, purchases, and refunds
 */
@Entity
@Table(name = "membership_credit_logs", schema = "booking_schema", indexes = {
    @Index(name = "idx_credit_logs_membership_id", columnList = "membership_id"),
    @Index(name = "idx_credit_logs_type", columnList = "transaction_type"),
    @Index(name = "idx_credit_logs_date", columnList = "created_at")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MembershipCreditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "membership_id", nullable = false, foreignKey = @ForeignKey(name = "fk_credit_logs_membership_id"))
    private Membership membership;

    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_type", nullable = false)
    private TransactionType type;  // PURCHASE, USAGE, REFUND, MONTHLY_ALLOCATION

    @Column(nullable = false)
    private Integer creditsChanged;  // Positive or negative

    @Column(nullable = false)
    private Integer balanceBefore;

    @Column(nullable = false)
    private Integer balanceAfter;

    @Column(length = 255)
    private String description;  // e.g., "Booking #123", "Monthly allocation", etc.

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public enum TransactionType {
        PURCHASE,
        USAGE,
        REFUND,
        MONTHLY_ALLOCATION,
        ADJUSTMENT
    }
}
