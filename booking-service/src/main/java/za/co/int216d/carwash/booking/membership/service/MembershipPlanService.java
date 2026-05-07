package za.co.int216d.carwash.booking.membership.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import za.co.int216d.carwash.booking.membership.domain.MembershipPlan;
import za.co.int216d.carwash.booking.membership.dto.CreateMembershipPlanRequest;
import za.co.int216d.carwash.booking.membership.dto.MembershipPlanResponse;
import za.co.int216d.carwash.booking.membership.repository.MembershipPlanRepository;
import za.co.int216d.carwash.booking.membership.repository.MembershipRepository;
import za.co.int216d.carwash.common.exception.ResourceNotFoundException;
import za.co.int216d.carwash.common.exception.BadRequestException;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for managing membership plans
 */
@Service
@Slf4j
@Transactional
public class MembershipPlanService {

    private final MembershipPlanRepository planRepository;
    private final MembershipRepository membershipRepository;

    public MembershipPlanService(MembershipPlanRepository planRepository,
                                  MembershipRepository membershipRepository) {
        this.planRepository = planRepository;
        this.membershipRepository = membershipRepository;
    }

    /**
     * Create a new membership plan (Admin only)
     */
    public MembershipPlanResponse createPlan(CreateMembershipPlanRequest request) {
        log.info("Creating new membership plan: {}", request.getName());

        // Check if plan already exists
        if (planRepository.findByName(request.getName()).isPresent()) {
            throw new BadRequestException("Membership plan with name '" + request.getName() + "' already exists");
        }

        MembershipPlan plan = MembershipPlan.builder()
            .name(request.getName())
            .description(request.getDescription())
            .monthlyPrice(request.getMonthlyPrice())
            .creditsPerMonth(request.getCreditsPerMonth())
            .freeWashes(request.getFreeWashes())
            .isActive(request.getIsActive())
            .discountEligible(request.getDiscountEligible())
            .discountPercentage(request.getDiscountPercentage())
            .build();

        plan = planRepository.save(plan);
        log.info("Membership plan created with ID: {}", plan.getId());
        return mapToResponse(plan);
    }

    /**
     * Get all membership plans
     */
    @Transactional(readOnly = true)
    public List<MembershipPlanResponse> getAllPlans() {
        return planRepository.findAll()
            .stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
    }

    /**
     * Get all active membership plans
     */
    @Transactional(readOnly = true)
    public List<MembershipPlanResponse> getActivePlans() {
        return planRepository.findAllByIsActiveTrue()
            .stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
    }

    /**
     * Get a specific membership plan by ID
     */
    @Transactional(readOnly = true)
    public MembershipPlanResponse getPlanById(Long planId) {
        MembershipPlan plan = planRepository.findById(planId)
            .orElseThrow(() -> new ResourceNotFoundException("Membership plan not found with ID: " + planId));
        return mapToResponse(plan);
    }

    /**
     * Update a membership plan (Admin only)
     */
    public MembershipPlanResponse updatePlan(Long planId, CreateMembershipPlanRequest request) {
        log.info("Updating membership plan with ID: {}", planId);

        MembershipPlan plan = planRepository.findById(planId)
            .orElseThrow(() -> new ResourceNotFoundException("Membership plan not found with ID: " + planId));

        // Check for name conflicts
        planRepository.findByName(request.getName())
            .ifPresent(existing -> {
                if (!existing.getId().equals(planId)) {
                    throw new BadRequestException("Membership plan with name '" + request.getName() + "' already exists");
                }
            });

        plan.setName(request.getName());
        plan.setDescription(request.getDescription());
        plan.setMonthlyPrice(request.getMonthlyPrice());
        plan.setCreditsPerMonth(request.getCreditsPerMonth());
        plan.setFreeWashes(request.getFreeWashes());
        plan.setIsActive(request.getIsActive());
        plan.setDiscountEligible(request.getDiscountEligible());
        plan.setDiscountPercentage(request.getDiscountPercentage());

        plan = planRepository.save(plan);
        log.info("Membership plan updated: {}", planId);
        return mapToResponse(plan);
    }

    /**
     * Delete a membership plan (Admin only)
     * Only allowed if no active subscriptions
     */
    public void deletePlan(Long planId) {
        log.info("Deleting membership plan with ID: {}", planId);

        MembershipPlan plan = planRepository.findById(planId)
            .orElseThrow(() -> new ResourceNotFoundException("Membership plan not found with ID: " + planId));

        // Check if plan has active memberships
        long activeCount = membershipRepository.countActiveByPlan(planId);
        if (activeCount > 0) {
            throw new BadRequestException(
                "Cannot delete plan with " + activeCount + " active membership(s). Deactivate or reassign them first."
            );
        }

        planRepository.delete(plan);
        log.info("Membership plan deleted: {}", planId);
    }

    /**
     * Get all discount-eligible plans
     */
    @Transactional(readOnly = true)
    public List<MembershipPlanResponse> getDiscountEligiblePlans() {
        return planRepository.findAllByDiscountEligibleTrue()
            .stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
    }

    private MembershipPlanResponse mapToResponse(MembershipPlan plan) {
        return MembershipPlanResponse.builder()
            .id(plan.getId())
            .name(plan.getName())
            .description(plan.getDescription())
            .monthlyPrice(plan.getMonthlyPrice())
            .creditsPerMonth(plan.getCreditsPerMonth())
            .freeWashes(plan.getFreeWashes())
            .isActive(plan.getIsActive())
            .discountEligible(plan.getDiscountEligible())
            .discountPercentage(plan.getDiscountPercentage())
            .createdAt(plan.getCreatedAt())
            .updatedAt(plan.getUpdatedAt())
            .build();
    }
}
