package za.co.int216d.carwash.booking.admin.dto;

import lombok.*;
import java.math.BigDecimal;

/**
 * Dashboard statistics overview
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminDashboardResponse {
    private Long totalActiveMemberships;
    private Long totalSuspendedMemberships;
    private Long totalExpiredMemberships;
    private Long totalCancelledMemberships;
    private Long totalMembers;
    private BigDecimal totalMonthlyRevenue;
    private BigDecimal averagePlanPrice;
    private Integer totalPlans;
    private Integer activePlans;
}
