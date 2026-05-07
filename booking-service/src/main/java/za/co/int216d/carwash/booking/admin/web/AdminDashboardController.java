package za.co.int216d.carwash.booking.admin.web;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import za.co.int216d.carwash.booking.admin.dto.*;
import za.co.int216d.carwash.booking.admin.service.AdminAnalyticsService;
import za.co.int216d.carwash.booking.core.domain.Booking;
import za.co.int216d.carwash.booking.core.repository.BookingRepository;
import za.co.int216d.carwash.booking.membership.service.MembershipService;
import za.co.int216d.carwash.booking.payment.domain.PaymentTransaction;
import za.co.int216d.carwash.booking.payment.repository.PaymentTransactionRepository;

import java.util.List;

/**
 * REST Controller for admin dashboard and analytics
 * All endpoints require ADMIN role
 */
@RestController
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
@Slf4j
public class AdminDashboardController {

    private final AdminAnalyticsService analyticsService;
    private final MembershipService membershipService;
    private final BookingRepository bookingRepository;
    private final PaymentTransactionRepository paymentRepository;

    public AdminDashboardController(
        AdminAnalyticsService analyticsService,
        MembershipService membershipService,
        BookingRepository bookingRepository,
        PaymentTransactionRepository paymentRepository
    ) {
        this.analyticsService = analyticsService;
        this.membershipService = membershipService;
        this.bookingRepository = bookingRepository;
        this.paymentRepository = paymentRepository;
    }

    /**
     * Get overall dashboard statistics
     */
    @GetMapping("/dashboard")
    public ResponseEntity<AdminDashboardResponse> getDashboard() {
        log.info("GET /admin/dashboard - Fetching admin dashboard");
        AdminDashboardResponse dashboard = analyticsService.getDashboardStats();
        return ResponseEntity.ok(dashboard);
    }

    /**
     * Get membership status breakdown
     */
    @GetMapping("/memberships/status-breakdown")
    public ResponseEntity<MembershipStatusBreakdownResponse> getStatusBreakdown() {
        log.info("GET /admin/memberships/status-breakdown - Fetching status breakdown");
        MembershipStatusBreakdownResponse breakdown = analyticsService.getMembershipStatusBreakdown();
        return ResponseEntity.ok(breakdown);
    }

    /**
     * Get analytics for all plans
     */
    @GetMapping("/plans/analytics")
    public ResponseEntity<List<PlanAnalyticsResponse>> getAllPlanAnalytics() {
        log.info("GET /admin/plans/analytics - Fetching analytics for all plans");
        List<PlanAnalyticsResponse> analytics = analyticsService.getPlanAnalytics();
        return ResponseEntity.ok(analytics);
    }

    /**
     * Get analytics for a specific plan
     */
    @GetMapping("/plans/{planId}/analytics")
    public ResponseEntity<PlanAnalyticsResponse> getPlanAnalytics(
        @PathVariable Long planId
    ) {
        log.info("GET /admin/plans/{}/analytics - Fetching plan analytics", planId);
        PlanAnalyticsResponse analytics = analyticsService.getPlanAnalyticsById(planId);
        return ResponseEntity.ok(analytics);
    }

    /**
     * Get all client memberships (paginated)
     */
    @GetMapping("/memberships")
    public ResponseEntity<Page<ClientMembershipSummaryResponse>> getAllMemberships(
        @PageableDefault(size = 50, sort = "startDate", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        log.info("GET /admin/memberships - Fetching all memberships (page: {})", pageable.getPageNumber());
        Page<ClientMembershipSummaryResponse> memberships = analyticsService.getAllClientMemberships(pageable);
        return ResponseEntity.ok(memberships);
    }

    /**
     * Get memberships expiring in N days
     */
    @GetMapping("/memberships/expiring/{days}")
    public ResponseEntity<List<ClientMembershipSummaryResponse>> getMembershipsExpiringIn(
        @PathVariable Integer days
    ) {
        log.info("GET /admin/memberships/expiring/{} - Fetching memberships expiring in {} days", days, days);
        List<ClientMembershipSummaryResponse> expiringMemberships =
            analyticsService.getMembershipsExpiringInDays(days);
        return ResponseEntity.ok(expiringMemberships);
    }

    /**
     * Suspend a client membership (admin action)
     */
    @PostMapping("/memberships/{clientId}/suspend")
    public ResponseEntity<Void> suspendMembership(
        @PathVariable Long clientId
    ) {
        log.info("POST /admin/memberships/{}/suspend - Suspending membership", clientId);
        membershipService.suspendMembership(clientId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Get total active memberships
     */
    @GetMapping("/stats/active-memberships")
    public ResponseEntity<Long> getTotalActiveMemberships() {
        log.info("GET /admin/stats/active-memberships - Fetching total active memberships");
        Long count = membershipService.getTotalActiveMemberships();
        return ResponseEntity.ok(count);
    }

    /**
     * Get active memberships for a plan
     */
    @GetMapping("/stats/plan/{planId}/active-memberships")
    public ResponseEntity<Long> getActiveMembershipsForPlan(
        @PathVariable Long planId
    ) {
        log.info("GET /admin/stats/plan/{}/active-memberships - Fetching active memberships", planId);
        Long count = membershipService.getActiveMembershipsForPlan(planId);
        return ResponseEntity.ok(count);
    }

    @GetMapping("/bookings")
    public ResponseEntity<Page<Booking>> getAllBookings(
        @PageableDefault(size = 50, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        log.info("GET /admin/bookings - Fetching all bookings (page: {})", pageable.getPageNumber());
        return ResponseEntity.ok(bookingRepository.findAll(pageable));
    }

    @GetMapping("/payments")
    public ResponseEntity<Page<PaymentTransaction>> getAllPayments(
        @PageableDefault(size = 50, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        log.info("GET /admin/payments - Fetching all payments (page: {})", pageable.getPageNumber());
        return ResponseEntity.ok(paymentRepository.findAll(pageable));
    }
}
