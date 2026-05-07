package za.co.int216d.carwash.booking.membership.web;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import za.co.int216d.carwash.booking.membership.dto.CreateMembershipPlanRequest;
import za.co.int216d.carwash.booking.membership.dto.MembershipPlanResponse;
import za.co.int216d.carwash.booking.membership.service.MembershipPlanService;

import java.util.List;

/**
 * REST Controller for managing membership plans
 * Admin endpoints only
 */
@RestController
@RequestMapping("/membership/plans")
@Slf4j
public class MembershipPlanController {

    private final MembershipPlanService planService;

    public MembershipPlanController(MembershipPlanService planService) {
        this.planService = planService;
    }

    /**
     * Create a new membership plan (Admin only)
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MembershipPlanResponse> createPlan(
        @Valid @RequestBody CreateMembershipPlanRequest request
    ) {
        log.info("POST /membership/plans - Creating new membership plan");
        MembershipPlanResponse response = planService.createPlan(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Get all membership plans (public - for clients to browse)
     */
    @GetMapping
    public ResponseEntity<List<MembershipPlanResponse>> getAllPlans() {
        log.info("GET /membership/plans - Fetching all membership plans");
        List<MembershipPlanResponse> plans = planService.getAllPlans();
        return ResponseEntity.ok(plans);
    }

    /**
     * Get all active membership plans
     */
    @GetMapping("/active")
    public ResponseEntity<List<MembershipPlanResponse>> getActivePlans() {
        log.info("GET /membership/plans/active - Fetching active membership plans");
        List<MembershipPlanResponse> plans = planService.getActivePlans();
        return ResponseEntity.ok(plans);
    }

    /**
     * Get a specific membership plan by ID
     */
    @GetMapping("/{planId}")
    public ResponseEntity<MembershipPlanResponse> getPlanById(
        @PathVariable Long planId
    ) {
        log.info("GET /membership/plans/{} - Fetching membership plan", planId);
        MembershipPlanResponse plan = planService.getPlanById(planId);
        return ResponseEntity.ok(plan);
    }

    /**
     * Update a membership plan (Admin only)
     */
    @PutMapping("/{planId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MembershipPlanResponse> updatePlan(
        @PathVariable Long planId,
        @Valid @RequestBody CreateMembershipPlanRequest request
    ) {
        log.info("PUT /membership/plans/{} - Updating membership plan", planId);
        MembershipPlanResponse response = planService.updatePlan(planId, request);
        return ResponseEntity.ok(response);
    }

    /**
     * Delete a membership plan (Admin only)
     */
    @DeleteMapping("/{planId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deletePlan(
        @PathVariable Long planId
    ) {
        log.info("DELETE /membership/plans/{} - Deleting membership plan", planId);
        planService.deletePlan(planId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Get all discount-eligible plans
     */
    @GetMapping("/discounts/eligible")
    public ResponseEntity<List<MembershipPlanResponse>> getDiscountEligiblePlans() {
        log.info("GET /membership/plans/discounts/eligible - Fetching discount-eligible plans");
        List<MembershipPlanResponse> plans = planService.getDiscountEligiblePlans();
        return ResponseEntity.ok(plans);
    }
}
