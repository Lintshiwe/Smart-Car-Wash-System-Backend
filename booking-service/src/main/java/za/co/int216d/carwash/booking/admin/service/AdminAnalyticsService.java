package za.co.int216d.carwash.booking.admin.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import za.co.int216d.carwash.booking.admin.dto.*;
import za.co.int216d.carwash.booking.membership.domain.Membership;
import za.co.int216d.carwash.booking.membership.domain.MembershipPlan;
import za.co.int216d.carwash.booking.membership.repository.MembershipPlanRepository;
import za.co.int216d.carwash.booking.membership.repository.MembershipRepository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Admin analytics and reporting service
 */
@Service
@Slf4j
@Transactional(readOnly = true)
public class AdminAnalyticsService {

    private final MembershipRepository membershipRepository;
    private final MembershipPlanRepository planRepository;

    public AdminAnalyticsService(
        MembershipRepository membershipRepository,
        MembershipPlanRepository planRepository
    ) {
        this.membershipRepository = membershipRepository;
        this.planRepository = planRepository;
    }

    /**
     * Get overall dashboard statistics
     */
    public AdminDashboardResponse getDashboardStats() {
        log.info("Fetching admin dashboard statistics");

        Long totalActive = membershipRepository.countByStatus(Membership.MembershipStatus.ACTIVE);
        Long totalSuspended = membershipRepository.countByStatus(Membership.MembershipStatus.SUSPENDED);
        Long totalExpired = membershipRepository.countByStatus(Membership.MembershipStatus.EXPIRED);
        Long totalCancelled = membershipRepository.countByStatus(Membership.MembershipStatus.CANCELLED);
        Long totalMembers = totalActive + totalExpired; // Active + Expired (no Suspended/Cancelled)

        List<MembershipPlan> allPlans = planRepository.findAll();
        List<MembershipPlan> activePlans = planRepository.findAllByIsActiveTrue();

        BigDecimal totalRevenue = calculateTotalMonthlyRevenue();
        BigDecimal avgPrice = allPlans.isEmpty() ? BigDecimal.ZERO :
            allPlans.stream()
                .map(MembershipPlan::getMonthlyPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(allPlans.size()), 2, RoundingMode.HALF_UP);

        return AdminDashboardResponse.builder()
            .totalActiveMemberships(totalActive)
            .totalSuspendedMemberships(totalSuspended)
            .totalExpiredMemberships(totalExpired)
            .totalCancelledMemberships(totalCancelled)
            .totalMembers(totalMembers)
            .totalMonthlyRevenue(totalRevenue)
            .averagePlanPrice(avgPrice)
            .totalPlans(allPlans.size())
            .activePlans(activePlans.size())
            .build();
    }

    /**
     * Get membership status breakdown
     */
    public MembershipStatusBreakdownResponse getMembershipStatusBreakdown() {
        log.info("Fetching membership status breakdown");

        Long active = membershipRepository.countByStatus(Membership.MembershipStatus.ACTIVE);
        Long expired = membershipRepository.countByStatus(Membership.MembershipStatus.EXPIRED);
        Long suspended = membershipRepository.countByStatus(Membership.MembershipStatus.SUSPENDED);
        Long cancelled = membershipRepository.countByStatus(Membership.MembershipStatus.CANCELLED);

        return MembershipStatusBreakdownResponse.builder()
            .activeMemberships(active)
            .expiredMemberships(expired)
            .suspendedMemberships(suspended)
            .cancelledMemberships(cancelled)
            .total(active + expired + suspended + cancelled)
            .build();
    }

    /**
     * Get analytics for all membership plans
     */
    public List<PlanAnalyticsResponse> getPlanAnalytics() {
        log.info("Fetching plan analytics for all plans");

        return planRepository.findAll()
            .stream()
            .map(this::buildPlanAnalytics)
            .collect(Collectors.toList());
    }

    /**
     * Get analytics for a specific plan
     */
    public PlanAnalyticsResponse getPlanAnalyticsById(Long planId) {
        log.info("Fetching plan analytics for plan {}", planId);

        MembershipPlan plan = planRepository.findById(planId)
            .orElseThrow(() -> new IllegalArgumentException("Plan not found: " + planId));

        return buildPlanAnalytics(plan);
    }

    /**
     * Get paginated list of all client memberships
     */
    public Page<ClientMembershipSummaryResponse> getAllClientMemberships(Pageable pageable) {
        log.info("Fetching all client memberships (page: {})", pageable.getPageNumber());

        Page<Membership> page = membershipRepository.findAll(pageable);
        List<ClientMembershipSummaryResponse> content = page.getContent()
            .stream()
            .map(this::mapToClientSummary)
            .collect(Collectors.toList());

        return new PageImpl<>(content, pageable, page.getTotalElements());
    }

    /**
     * Get memberships expiring in the next N days
     */
    public List<ClientMembershipSummaryResponse> getMembershipsExpiringInDays(Integer days) {
        log.info("Fetching memberships expiring in {} days", days);

        LocalDateTime expiryThreshold = LocalDateTime.now().plusDays(days);
        List<Membership> expiringMemberships = membershipRepository.findAllByStatusAndExpiryDateBetween(
            Membership.MembershipStatus.ACTIVE,
            LocalDateTime.now(),
            expiryThreshold
        );

        return expiringMemberships.stream()
            .map(this::mapToClientSummary)
            .collect(Collectors.toList());
    }

    /**
     * Calculate total monthly revenue based on active subscriptions
     */
    private BigDecimal calculateTotalMonthlyRevenue() {
        List<Membership> activeMemberships = membershipRepository.findAllByStatus(Membership.MembershipStatus.ACTIVE);
        return activeMemberships.stream()
            .map(m -> m.getPlan().getMonthlyPrice())
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Build plan analytics
     */
    private PlanAnalyticsResponse buildPlanAnalytics(MembershipPlan plan) {
        Long activeCount = membershipRepository.countActiveByPlan(plan.getId());
        Long totalCount = membershipRepository.countByPlanId(plan.getId());
        BigDecimal revenue = plan.getMonthlyPrice().multiply(BigDecimal.valueOf(activeCount));
        Double conversionRate = totalCount == 0 ? 0.0 : (double) activeCount / totalCount * 100;

        return PlanAnalyticsResponse.builder()
            .planId(plan.getId())
            .planName(plan.getName())
            .monthlyPrice(plan.getMonthlyPrice())
            .activeSubscriptions(activeCount)
            .totalSubscriptions(totalCount)
            .totalMonthlyRevenue(revenue)
            .conversionRate(conversionRate)
            .build();
    }

    /**
     * Map membership to client summary
     */
    private ClientMembershipSummaryResponse mapToClientSummary(Membership membership) {
        LocalDateTime now = LocalDateTime.now();
        long daysUntilExpiry = ChronoUnit.DAYS.between(now, membership.getExpiryDate());

        return ClientMembershipSummaryResponse.builder()
            .clientId(membership.getClientId())
            .planName(membership.getPlan().getName())
            .status(membership.getStatus().toString())
            .startDate(membership.getStartDate())
            .expiryDate(membership.getExpiryDate())
            .creditsRemaining(membership.getCreditsRemaining())
            .autoRenew(membership.getAutoRenew())
            .daysUntilExpiry((int) daysUntilExpiry)
            .build();
    }
}
