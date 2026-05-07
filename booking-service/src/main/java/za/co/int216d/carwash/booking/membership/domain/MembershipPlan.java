package za.co.int216d.carwash.booking.membership.domain;

import lombok.*;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Membership tier/plan configuration
 * Defines available membership levels (Basic, Standard, Premium, VIP, etc.)
 */
@Entity
@Table(name = "membership_plans", schema = "booking_schema")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MembershipPlan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String name;  // e.g., "Basic", "Standard", "Premium", "VIP"

    @Column(nullable = false)
    private String description;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal monthlyPrice;

    @Column(nullable = false)
    private Integer creditsPerMonth;

    @Column(nullable = false)
    private Integer freeWashes;  // How many free washes per month

    @Column(nullable = false, columnDefinition = "BOOLEAN DEFAULT true")
    private Boolean isActive;

    @Column(nullable = false, columnDefinition = "BOOLEAN DEFAULT false")
    private Boolean discountEligible;  // Tier-specific discounts

    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal discountPercentage;  // e.g., 10% discount for premium members

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
