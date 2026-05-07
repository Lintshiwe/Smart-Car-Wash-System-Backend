package za.co.int216d.carwash.booking.membership.dto;

import lombok.*;
import java.time.LocalDateTime;

/**
 * DTO for membership credit transaction
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreditTransactionResponse {

    private Long id;
    private String type;  // PURCHASE, USAGE, REFUND, MONTHLY_ALLOCATION, ADJUSTMENT
    private Integer creditsChanged;
    private Integer balanceBefore;
    private Integer balanceAfter;
    private String description;
    private LocalDateTime createdAt;
}
