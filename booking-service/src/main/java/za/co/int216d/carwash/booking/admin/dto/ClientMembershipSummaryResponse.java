package za.co.int216d.carwash.booking.admin.dto;

import lombok.*;
import java.time.LocalDateTime;

/**
 * Client membership summary for admin view
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClientMembershipSummaryResponse {
    private Long clientId;
    private String planName;
    private String status;
    private LocalDateTime startDate;
    private LocalDateTime expiryDate;
    private Integer creditsRemaining;
    private Boolean autoRenew;
    private Integer daysUntilExpiry;
}
