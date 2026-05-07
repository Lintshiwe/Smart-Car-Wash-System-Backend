package za.co.int216d.carwash.booking.membership.scheduler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import za.co.int216d.carwash.booking.membership.domain.Membership;
import za.co.int216d.carwash.booking.membership.domain.MembershipPlan;
import za.co.int216d.carwash.booking.membership.repository.MembershipRepository;
import za.co.int216d.carwash.booking.notification.producer.MembershipEventProducer;
import za.co.int216d.carwash.booking.notification.service.EmailNotificationService;
import za.co.int216d.carwash.booking.payment.domain.PaymentPurpose;
import za.co.int216d.carwash.booking.payment.dto.PaymentProcessResult;
import za.co.int216d.carwash.booking.payment.service.PaymentService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Component
@Slf4j
@ConditionalOnProperty(value = "app.scheduling.enabled", havingValue = "true", matchIfMissing = true)
public class MembershipRenewalScheduler {

    private final MembershipRepository membershipRepository;
    private final Optional<MembershipEventProducer> eventProducer;
    private final EmailNotificationService emailNotificationService;

    public MembershipRenewalScheduler(MembershipRepository membershipRepository,
                                       Optional<MembershipEventProducer> eventProducer,
                                       EmailNotificationService emailNotificationService) {
        this.membershipRepository = membershipRepository;
        this.eventProducer = eventProducer;
        this.emailNotificationService = emailNotificationService;
    }

    @Scheduled(cron = "${app.scheduling.renewal-cron:0 0 3 * * ?}")
    public void processExpiredAutoRenewals() {
        log.info("Running auto-renewal check for expired memberships");
        List<Membership> expiringMemberships = membershipRepository.findExpiredAutoRenewMemberships();

        for (Membership membership : expiringMemberships) {
            log.info("Auto-renew triggered for client {} (membership {})",
                membership.getClientId(), membership.getId());

            membership.setExpiryDate(LocalDateTime.now().plusMonths(1));
            membership.setCreditsRemaining(membership.getPlan().getCreditsPerMonth());
            membership.setWashesUsedThisMonth(0);
            membership.setStatus(Membership.MembershipStatus.ACTIVE);
            membershipRepository.save(membership);

            eventProducer.ifPresent(ep -> {
                ep.publishRenewalEvent(
                membership.getClientId(),
                membership.getPlan().getId(),
                membership.getPlan().getName(),
                null,
                null
                );
            });
        }

        if (!expiringMemberships.isEmpty()) {
            log.info("Auto-renewed {} memberships", expiringMemberships.size());
        }
    }

    @Scheduled(cron = "${app.scheduling.expiry-check-cron:0 30 0 * * ?}")
    public void checkAndExpireMemberships() {
        log.info("Running membership expiry check");
        List<Membership> activeMemberships = membershipRepository.findAllByStatus(Membership.MembershipStatus.ACTIVE);

        int expiredCount = 0;
        for (Membership membership : activeMemberships) {
            if (membership.getExpiryDate().isBefore(LocalDateTime.now())
                && !Boolean.TRUE.equals(membership.getAutoRenew())) {
                membership.setStatus(Membership.MembershipStatus.EXPIRED);
                membershipRepository.save(membership);
                expiredCount++;

                eventProducer.ifPresent(ep -> {
                    ep.publishCancellationEvent(
                    membership.getClientId(),
                    membership.getPlan().getId(),
                    membership.getPlan().getName(),
                    null,
                    null
                    );
                });
            }
        }

        if (expiredCount > 0) {
            log.info("Expired {} memberships", expiredCount);
        }
    }

    @Scheduled(cron = "${app.scheduling.warning-cron:0 0 8 * * ?}")
    public void sendExpiryWarnings() {
        log.info("Running membership expiry warning check");
        LocalDateTime now = LocalDateTime.now();

        int[] warningDays = {1, 3, 7};
        for (int days : warningDays) {
            LocalDateTime start = now.plusDays(days).withHour(0).withMinute(0).withSecond(0);
            LocalDateTime end = now.plusDays(days).withHour(23).withMinute(59).withSecond(59);

            List<Membership> expiringSoon = membershipRepository.findAllByStatusAndExpiryDateBetween(
                Membership.MembershipStatus.ACTIVE, start, end);

            for (Membership m : expiringSoon) {
                log.info("Sending expiry warning for client {} ({} days until expiry)", m.getClientId(), days);
                eventProducer.ifPresent(ep -> {
                    ep.publishExpiryWarningEvent(
                    m.getClientId(), m.getPlan().getId(), m.getPlan().getName(), days, null, null);
                });

                try {
                    emailNotificationService.sendExpiryWarningEmail("customer@int216d.co.za", "Valued Customer", days);
                } catch (Exception e) {
                    log.warn("Failed to send expiry warning email: {}", e.getMessage());
                }
            }
        }
    }
}
