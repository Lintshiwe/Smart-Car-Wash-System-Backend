package za.co.int216d.carwash.booking.membership.domain;

import lombok.*;
import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Client membership subscription
 * Tracks which client has which membership plan and their active status
 */
@Entity
@Table(name = "memberships", schema = "booking_schema", indexes = {
    @Index(name = "idx_memberships_client_id", columnList = "client_id"),
    @Index(name = "idx_memberships_status", columnList = "status"),
    @Index(name = "idx_memberships_expiry", columnList = "expiry_date")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Membership {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private Long clientId;  // Reference to client in client_schema

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "plan_id", nullable = false, foreignKey = @ForeignKey(name = "fk_memberships_plan_id"))
    private MembershipPlan plan;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MembershipStatus status;  // ACTIVE, EXPIRED, SUSPENDED, CANCELLED

    @Column(nullable = false)
    private LocalDateTime startDate;

    @Column(nullable = false)
    private LocalDateTime expiryDate;

    @Column(nullable = false, columnDefinition = "INTEGER DEFAULT 0")
    private Integer creditsRemaining;  // Available credits this month

    @Column(nullable = false, columnDefinition = "INTEGER DEFAULT 0")
    private Integer washesUsedThisMonth;

    @Column(nullable = false, columnDefinition = "BOOLEAN DEFAULT false")
    private Boolean autoRenew;

    @Column(length = 64)
    private String latestPaymentReference;

    @Column(length = 20)
    private String latestPaymentStatus;

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

    public enum MembershipStatus {
        ACTIVE,
        EXPIRED,
        SUSPENDED,
        CANCELLED
    }
}
