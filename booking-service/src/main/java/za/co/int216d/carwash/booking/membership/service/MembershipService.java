package za.co.int216d.carwash.booking.membership.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import za.co.int216d.carwash.booking.membership.domain.Membership;
import za.co.int216d.carwash.booking.membership.domain.MembershipCreditLog;
import za.co.int216d.carwash.booking.membership.domain.MembershipPlan;
import za.co.int216d.carwash.booking.membership.dto.MembershipDetailResponse;
import za.co.int216d.carwash.booking.membership.dto.MembershipPaymentRequest;
import za.co.int216d.carwash.booking.membership.dto.MembershipPlanResponse;
import za.co.int216d.carwash.booking.membership.dto.SubscribeMembershipRequest;
import za.co.int216d.carwash.booking.membership.repository.MembershipCreditLogRepository;
import za.co.int216d.carwash.booking.membership.repository.MembershipPlanRepository;
import za.co.int216d.carwash.booking.membership.repository.MembershipRepository;
import za.co.int216d.carwash.booking.notification.producer.MembershipEventProducer;
import za.co.int216d.carwash.booking.notification.service.EmailNotificationService;
import za.co.int216d.carwash.booking.payment.domain.PaymentPurpose;
import za.co.int216d.carwash.booking.payment.dto.PaymentProcessResult;
import za.co.int216d.carwash.booking.payment.service.PaymentService;
import za.co.int216d.carwash.common.exception.BadRequestException;
import za.co.int216d.carwash.common.exception.ResourceNotFoundException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.time.temporal.ChronoUnit;

/**
 * Service for managing client memberships and subscriptions
 */
@Service
@Slf4j
@Transactional
public class MembershipService {

    private final MembershipRepository membershipRepository;
    private final MembershipPlanRepository planRepository;
    private final MembershipCreditLogRepository creditLogRepository;
    private final Optional<MembershipEventProducer> eventProducer;
    private final PaymentService paymentService;
    private final EmailNotificationService emailNotificationService;

    public MembershipService(
        MembershipRepository membershipRepository,
        MembershipPlanRepository planRepository,
        MembershipCreditLogRepository creditLogRepository,
        Optional<MembershipEventProducer> eventProducer,
        PaymentService paymentService,
        EmailNotificationService emailNotificationService
    ) {
        this.membershipRepository = membershipRepository;
        this.planRepository = planRepository;
        this.creditLogRepository = creditLogRepository;
        this.eventProducer = eventProducer != null ? eventProducer : Optional.empty();
        this.paymentService = paymentService;
        this.emailNotificationService = emailNotificationService;
    }

    /**
     * Subscribe a client to a membership plan
     */
    public MembershipDetailResponse subscribeToPlan(Long clientId, SubscribeMembershipRequest request) {
        log.info("Client {} subscribing to plan {}", clientId, request.getPlanId());

        // Check if client already has an active membership
        membershipRepository.findByClientId(clientId)
            .ifPresent(existing -> {
                if (existing.getStatus() == Membership.MembershipStatus.ACTIVE) {
                    throw new BadRequestException("Client already has an active membership. Cancel it first.");
                }
            });

        // Get the plan
        MembershipPlan plan = planRepository.findById(request.getPlanId())
            .orElseThrow(() -> new ResourceNotFoundException("Membership plan not found with ID: " + request.getPlanId()));

        if (!plan.getIsActive()) {
            throw new BadRequestException("Membership plan is no longer active");
        }

        BigDecimal chargeAmount = plan.getMonthlyPrice();
        PaymentProcessResult paymentResult = paymentService.processPayment(
            clientId,
            null,
            null,
            PaymentPurpose.MEMBERSHIP_SUBSCRIPTION,
            chargeAmount,
            null,
            "Membership subscription for plan " + plan.getName(),
            request.getPayment()
        );

        // Create new membership
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expiryDate = now.plusMonths(1);

        Membership membership = Membership.builder()
            .clientId(clientId)
            .plan(plan)
            .status(Membership.MembershipStatus.ACTIVE)
            .startDate(now)
            .expiryDate(expiryDate)
            .creditsRemaining(plan.getCreditsPerMonth())
            .washesUsedThisMonth(0)
            .autoRenew(request.getAutoRenew())
            .latestPaymentReference(paymentResult.reference())
            .latestPaymentStatus(paymentResult.status().name())
            .build();

        membership = membershipRepository.save(membership);

        // Log the initial allocation
        MembershipCreditLog creditLog = MembershipCreditLog.builder()
            .membership(membership)
            .type(MembershipCreditLog.TransactionType.MONTHLY_ALLOCATION)
            .creditsChanged(plan.getCreditsPerMonth())
            .balanceBefore(0)
            .balanceAfter(plan.getCreditsPerMonth())
            .description("Initial monthly allocation for " + plan.getName() + " plan")
            .build();

        creditLogRepository.save(creditLog);
        log.info("Client {} subscribed to plan {}", clientId, plan.getName());

        // Publish subscription event
        eventProducer.ifPresent(ep -> ep.publishSubscriptionEvent(clientId, plan.getId(), plan.getName(), request.getEmail(), null));

        // Send email directly
        if (request.getEmail() != null && !request.getEmail().isBlank()) {
            try {
                emailNotificationService.sendSubscriptionEmail(request.getEmail(), "Valued Customer", plan.getName(), plan.getMonthlyPrice().doubleValue());
            } catch (Exception e) {
                log.warn("Failed to send subscription email: {}", e.getMessage());
            }
        }

        return mapToDetailResponse(membership);
    }

    /**
     * Get client's current membership
     */
    @Transactional
    public MembershipDetailResponse getClientMembership(Long clientId) {
        Membership membership = membershipRepository.findByClientId(clientId)
            .orElseThrow(() -> new ResourceNotFoundException("No membership found for client ID: " + clientId));

        // Check if expired
        if (membership.getStatus() == Membership.MembershipStatus.ACTIVE &&
            membership.getExpiryDate().isBefore(LocalDateTime.now())) {
            membership.setStatus(Membership.MembershipStatus.EXPIRED);
            membership = membershipRepository.save(membership);
        }

        return mapToDetailResponse(membership);
    }

    /**
     * Renew a client's membership for another month
     */
    public MembershipDetailResponse renewMembership(Long clientId, MembershipPaymentRequest request) {
        log.info("Renewing membership for client {}", clientId);

        Membership membership = membershipRepository.findByClientId(clientId)
            .orElseThrow(() -> new ResourceNotFoundException("No membership found for client ID: " + clientId));

        MembershipPlan plan = membership.getPlan();

        BigDecimal chargeAmount = plan.getMonthlyPrice();
        PaymentProcessResult paymentResult = paymentService.processPayment(
            clientId,
            null,
            membership.getId(),
            PaymentPurpose.MEMBERSHIP_RENEWAL,
            chargeAmount,
            null,
            "Membership renewal for plan " + plan.getName(),
            request.getPayment()
        );

        // Update expiry and reset credits
        LocalDateTime newExpiryDate = LocalDateTime.now().plusMonths(1);
        membership.setExpiryDate(newExpiryDate);
        membership.setCreditsRemaining(plan.getCreditsPerMonth());
        membership.setWashesUsedThisMonth(0);
        membership.setStatus(Membership.MembershipStatus.ACTIVE);
        membership.setLatestPaymentReference(paymentResult.reference());
        membership.setLatestPaymentStatus(paymentResult.status().name());

        membership = membershipRepository.save(membership);

        // Log the monthly allocation
        MembershipCreditLog creditLog = MembershipCreditLog.builder()
            .membership(membership)
            .type(MembershipCreditLog.TransactionType.MONTHLY_ALLOCATION)
            .creditsChanged(plan.getCreditsPerMonth())
            .balanceBefore(0)
            .balanceAfter(plan.getCreditsPerMonth())
            .description("Monthly allocation for renewal - " + plan.getName() + " plan")
            .build();

        creditLogRepository.save(creditLog);
        log.info("Membership renewed for client {}", clientId);

        // Publish renewal event
        eventProducer.ifPresent(ep -> ep.publishRenewalEvent(clientId, plan.getId(), plan.getName(), request.getEmail(), null));

        // Send email directly
        if (request.getEmail() != null && !request.getEmail().isBlank()) {
            try {
                emailNotificationService.sendRenewalEmail(request.getEmail(), "Valued Customer", plan.getName(), membership.getExpiryDate());
            } catch (Exception e) {
                log.warn("Failed to send renewal email: {}", e.getMessage());
            }
        }

        return mapToDetailResponse(membership);
    }

    /**
     * Upgrade to a different plan
     */
    public MembershipDetailResponse upgradePlan(Long clientId, Long newPlanId, MembershipPaymentRequest request) {
        log.info("Upgrading membership for client {} to plan {}", clientId, newPlanId);

        Membership membership = membershipRepository.findByClientId(clientId)
            .orElseThrow(() -> new ResourceNotFoundException("No membership found for client ID: " + clientId));

        MembershipPlan newPlan = planRepository.findById(newPlanId)
            .orElseThrow(() -> new ResourceNotFoundException("Membership plan not found with ID: " + newPlanId));

        if (!newPlan.getIsActive()) {
            throw new BadRequestException("Target membership plan is no longer active");
        }

        BigDecimal chargeAmount = newPlan.getMonthlyPrice();
        PaymentProcessResult paymentResult = paymentService.processPayment(
            clientId,
            null,
            membership.getId(),
            PaymentPurpose.MEMBERSHIP_UPGRADE,
            chargeAmount,
            null,
            "Membership upgrade to plan " + newPlan.getName(),
            request.getPayment()
        );

        MembershipPlan oldPlan = membership.getPlan();
        // Update membership
        membership.setPlan(newPlan);
        membership.setCreditsRemaining(newPlan.getCreditsPerMonth());
        membership.setWashesUsedThisMonth(0);
        membership.setLatestPaymentReference(paymentResult.reference());
        membership.setLatestPaymentStatus(paymentResult.status().name());

        membership = membershipRepository.save(membership);
        log.info("Membership upgraded for client {}", clientId);

        // Send upgrade email directly
        if (request.getEmail() != null && !request.getEmail().isBlank()) {
            try {
                emailNotificationService.sendUpgradeEmail(request.getEmail(), "Valued Customer", oldPlan.getName(), newPlan.getName(), newPlan.getMonthlyPrice().doubleValue());
            } catch (Exception e) {
                log.warn("Failed to send upgrade email: {}", e.getMessage());
            }
        }

        return mapToDetailResponse(membership);
    }

    /**
     * Cancel a client's membership
     */
    public void cancelMembership(Long clientId) {
        log.info("Cancelling membership for client {}", clientId);

        Membership membership = membershipRepository.findByClientId(clientId)
            .orElseThrow(() -> new ResourceNotFoundException("No membership found for client ID: " + clientId));

        membership.setStatus(Membership.MembershipStatus.CANCELLED);
        membershipRepository.save(membership);
        log.info("Membership cancelled for client {}", clientId);

        // Publish cancellation event
        eventProducer.ifPresent(ep -> {
            ep.publishCancellationEvent(clientId, membership.getPlan().getId(), 
            membership.getPlan().getName(), null, null);
        });

        // Note: Email is null here because cancelMembership doesn't have email param.
        // The Kafka consumer will handle email sending if email is available in the event.
    }

    /**
     * Suspend a membership (Admin only)
     */
    public void suspendMembership(Long clientId) {
        log.info("Suspending membership for client {}", clientId);

        Membership membership = membershipRepository.findByClientId(clientId)
            .orElseThrow(() -> new ResourceNotFoundException("No membership found for client ID: " + clientId));

        membership.setStatus(Membership.MembershipStatus.SUSPENDED);
        membershipRepository.save(membership);
        log.info("Membership suspended for client {}", clientId);

        // Publish suspension event
        eventProducer.ifPresent(ep -> {
            ep.publishSuspensionEvent(clientId, membership.getPlan().getId(), 
            membership.getPlan().getName(), null, null);
        });
    }

    /**
     * Reactivate a suspended or expired membership (Admin only)
     */
    public void reactivateMembership(Long clientId) {
        log.info("Reactivating membership for client {}", clientId);

        Membership membership = membershipRepository.findByClientId(clientId)
            .orElseThrow(() -> new ResourceNotFoundException("No membership found for client ID: " + clientId));

        if (membership.getStatus() == Membership.MembershipStatus.ACTIVE) {
            throw new BadRequestException("Membership is already active");
        }

        membership.setStatus(Membership.MembershipStatus.ACTIVE);
        membership.setExpiryDate(LocalDateTime.now().plusMonths(1));
        membershipRepository.save(membership);
        log.info("Membership reactivated for client {}", clientId);
    }

    /**
     * Deduct credits from membership
     */
    public void deductCredits(Long clientId, Integer creditsToDeduct, String description) {
        Membership membership = membershipRepository.findByClientId(clientId)
            .orElseThrow(() -> new ResourceNotFoundException("No membership found for client ID: " + clientId));

        if (membership.getStatus() != Membership.MembershipStatus.ACTIVE) {
            throw new BadRequestException("Cannot deduct credits from inactive membership");
        }

        if (membership.getCreditsRemaining() < creditsToDeduct) {
            throw new BadRequestException("Insufficient credits. Required: " + creditsToDeduct +
                ", Available: " + membership.getCreditsRemaining());
        }

        Integer balanceBefore = membership.getCreditsRemaining();
        membership.setCreditsRemaining(balanceBefore - creditsToDeduct);
        membership.setWashesUsedThisMonth(membership.getWashesUsedThisMonth() + 1);

        membershipRepository.save(membership);

        // Log the transaction
        MembershipCreditLog log = MembershipCreditLog.builder()
            .membership(membership)
            .type(MembershipCreditLog.TransactionType.USAGE)
            .creditsChanged(-creditsToDeduct)
            .balanceBefore(balanceBefore)
            .balanceAfter(membership.getCreditsRemaining())
            .description(description)
            .build();

        creditLogRepository.save(log);
    }

    /**
     * Add credits to membership (e.g., purchase additional credits)
     */
    public void addCredits(Long clientId, Integer creditsToAdd, String description) {
        Membership membership = membershipRepository.findByClientId(clientId)
            .orElseThrow(() -> new ResourceNotFoundException("No membership found for client ID: " + clientId));

        Integer balanceBefore = membership.getCreditsRemaining();
        membership.setCreditsRemaining(balanceBefore + creditsToAdd);

        membershipRepository.save(membership);

        // Log the transaction
        MembershipCreditLog log = MembershipCreditLog.builder()
            .membership(membership)
            .type(MembershipCreditLog.TransactionType.PURCHASE)
            .creditsChanged(creditsToAdd)
            .balanceBefore(balanceBefore)
            .balanceAfter(membership.getCreditsRemaining())
            .description(description)
            .build();

        creditLogRepository.save(log);
    }

    /**
     * Get total active memberships count
     */
    @Transactional(readOnly = true)
    public Long getTotalActiveMemberships() {
        return membershipRepository.countActiveMemberships();
    }

    /**
     * Get active memberships count for a specific plan
     */
    @Transactional(readOnly = true)
    public Long getActiveMembershipsForPlan(Long planId) {
        return membershipRepository.countActiveByPlan(planId);
    }

    private MembershipDetailResponse mapToDetailResponse(Membership membership) {
        LocalDateTime now = LocalDateTime.now();
        boolean isExpired = membership.getExpiryDate().isBefore(now);
        long daysUntilExpiry = ChronoUnit.DAYS.between(now, membership.getExpiryDate());

        MembershipPlanResponse planResponse = MembershipPlanResponse.builder()
            .id(membership.getPlan().getId())
            .name(membership.getPlan().getName())
            .description(membership.getPlan().getDescription())
            .monthlyPrice(membership.getPlan().getMonthlyPrice())
            .creditsPerMonth(membership.getPlan().getCreditsPerMonth())
            .freeWashes(membership.getPlan().getFreeWashes())
            .isActive(membership.getPlan().getIsActive())
            .discountEligible(membership.getPlan().getDiscountEligible())
            .discountPercentage(membership.getPlan().getDiscountPercentage())
            .createdAt(membership.getPlan().getCreatedAt())
            .updatedAt(membership.getPlan().getUpdatedAt())
            .build();

        return MembershipDetailResponse.builder()
            .id(membership.getId())
            .clientId(membership.getClientId())
            .plan(planResponse)
            .status(membership.getStatus().toString())
            .startDate(membership.getStartDate())
            .expiryDate(membership.getExpiryDate())
            .creditsRemaining(membership.getCreditsRemaining())
            .washesUsedThisMonth(membership.getWashesUsedThisMonth())
            .autoRenew(membership.getAutoRenew())
            .latestPaymentReference(membership.getLatestPaymentReference())
            .latestPaymentStatus(membership.getLatestPaymentStatus())
            .isExpired(isExpired)
            .daysUntilExpiry((int) daysUntilExpiry)
            .createdAt(membership.getCreatedAt())
            .updatedAt(membership.getUpdatedAt())
            .build();
    }
}
