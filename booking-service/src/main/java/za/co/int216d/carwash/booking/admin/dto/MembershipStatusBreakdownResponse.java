package za.co.int216d.carwash.booking.admin.dto;

import lombok.*;

/**
 * Membership status breakdown
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MembershipStatusBreakdownResponse {
    private Long activeMemberships;
    private Long expiredMemberships;
    private Long suspendedMemberships;
    private Long cancelledMemberships;
    private Long total;
}
