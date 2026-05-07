package za.co.int216d.carwash.booking.membership.web;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import za.co.int216d.carwash.booking.membership.dto.MembershipDetailResponse;
import za.co.int216d.carwash.booking.membership.dto.MembershipPaymentRequest;
import za.co.int216d.carwash.booking.membership.dto.SubscribeMembershipRequest;
import za.co.int216d.carwash.booking.membership.service.MembershipService;
import za.co.int216d.carwash.common.security.SecurityUtils;

/**
 * REST Controller for managing client memberships
 */
@RestController
@RequestMapping("/membership")
@Slf4j
public class MembershipController {

    private final MembershipService membershipService;
    private final SecurityUtils securityUtils;

    public MembershipController(
        MembershipService membershipService,
        SecurityUtils securityUtils
    ) {
        this.membershipService = membershipService;
        this.securityUtils = securityUtils;
    }

    /**
     * Client subscribes to a membership plan
     */
    @PostMapping("/subscribe")
    @PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<MembershipDetailResponse> subscribeToPlan(
        @Valid @RequestBody SubscribeMembershipRequest request
    ) {
        Long clientId = securityUtils.getCurrentUserIdAsLong();
        log.info("POST /membership/subscribe - Client {} subscribing to plan {}", clientId, request.getPlanId());
        MembershipDetailResponse response = membershipService.subscribeToPlan(clientId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Get client's current membership
     */
    @GetMapping
    @PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<MembershipDetailResponse> getClientMembership() {
        Long clientId = securityUtils.getCurrentUserIdAsLong();
        log.info("GET /membership - Fetching membership for client {}", clientId);
        MembershipDetailResponse response = membershipService.getClientMembership(clientId);
        return ResponseEntity.ok(response);
    }

    /**
     * Client renews their membership for another month
     */
    @PostMapping("/renew")
    @PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<MembershipDetailResponse> renewMembership(
        @Valid @RequestBody MembershipPaymentRequest request
    ) {
        Long clientId = securityUtils.getCurrentUserIdAsLong();
        log.info("POST /membership/renew - Renewing membership for client {}", clientId);
        MembershipDetailResponse response = membershipService.renewMembership(clientId, request);
        return ResponseEntity.ok(response);
    }

    /**
     * Client upgrades to a different membership plan
     */
    @PostMapping("/upgrade/{planId}")
    @PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<MembershipDetailResponse> upgradePlan(
        @PathVariable Long planId,
        @Valid @RequestBody MembershipPaymentRequest request
    ) {
        Long clientId = securityUtils.getCurrentUserIdAsLong();
        log.info("POST /membership/upgrade/{} - Client {} upgrading plan", planId, clientId);
        MembershipDetailResponse response = membershipService.upgradePlan(clientId, planId, request);
        return ResponseEntity.ok(response);
    }

    /**
     * Client cancels their membership
     */
    @PostMapping("/cancel")
    @PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<Void> cancelMembership() {
        Long clientId = securityUtils.getCurrentUserIdAsLong();
        log.info("POST /membership/cancel - Client {} cancelling membership", clientId);
        membershipService.cancelMembership(clientId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Admin suspends a client's membership
     */
    @PostMapping("/{clientId}/suspend")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> suspendMembership(
        @PathVariable Long clientId
    ) {
        log.info("POST /membership/{}/suspend - Admin suspending membership", clientId);
        membershipService.suspendMembership(clientId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Get total active memberships count (Admin only)
     */
    @GetMapping("/stats/active-count")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Long> getTotalActiveMemberships() {
        log.info("GET /membership/stats/active-count - Fetching total active memberships");
        Long count = membershipService.getTotalActiveMemberships();
        return ResponseEntity.ok(count);
    }

    /**
     * Get active memberships count for a specific plan (Admin only)
     */
    @GetMapping("/stats/plan/{planId}/active-count")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Long> getActiveMembershipsForPlan(
        @PathVariable Long planId
    ) {
        log.info("GET /membership/stats/plan/{}/active-count - Fetching active memberships for plan", planId);
        Long count = membershipService.getActiveMembershipsForPlan(planId);
        return ResponseEntity.ok(count);
    }
}
