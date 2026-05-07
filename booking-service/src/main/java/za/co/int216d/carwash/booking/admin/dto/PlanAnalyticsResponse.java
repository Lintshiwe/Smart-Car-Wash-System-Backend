package za.co.int216d.carwash.booking.admin.dto;

import lombok.*;
import java.math.BigDecimal;

/**
 * Membership plan analytics
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlanAnalyticsResponse {
    private Long planId;
    private String planName;
    private BigDecimal monthlyPrice;
    private Long activeSubscriptions;
    private Long totalSubscriptions;
    private BigDecimal totalMonthlyRevenue;
    private Double conversionRate;
}
