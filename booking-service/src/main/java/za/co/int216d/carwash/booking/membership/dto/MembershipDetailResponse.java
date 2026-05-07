package za.co.int216d.carwash.booking.membership.dto;

import lombok.*;
import java.time.LocalDateTime;

/**
 * DTO for returning current client membership details
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MembershipDetailResponse {

    private Long id;
    private Long clientId;
    private MembershipPlanResponse plan;
    private String status;  // ACTIVE, EXPIRED, SUSPENDED, CANCELLED
    private LocalDateTime startDate;
    private LocalDateTime expiryDate;
    private Integer creditsRemaining;
    private Integer washesUsedThisMonth;
    private Boolean autoRenew;
    private String latestPaymentReference;
    private String latestPaymentStatus;
    private Boolean isExpired;
    private Integer daysUntilExpiry;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
