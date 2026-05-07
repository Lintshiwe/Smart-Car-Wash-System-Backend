package za.co.int216d.carwash.booking.membership.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import za.co.int216d.carwash.booking.membership.domain.Membership;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface MembershipRepository extends JpaRepository<Membership, Long> {
    Optional<Membership> findByClientId(Long clientId);

    List<Membership> findAllByStatus(Membership.MembershipStatus status);

    List<Membership> findAllByStatusAndExpiryDateBetween(
        Membership.MembershipStatus status,
        LocalDateTime startDate,
        LocalDateTime endDate
    );

    @Query("SELECT m FROM Membership m WHERE m.status = ?1 AND m.expiryDate <= ?2")
    List<Membership> findExpiringMemberships(Membership.MembershipStatus status, LocalDateTime date);

    @Query("SELECT m FROM Membership m WHERE m.status = 'ACTIVE' AND m.autoRenew = true AND m.expiryDate <= CURRENT_TIMESTAMP")
    List<Membership> findExpiredAutoRenewMemberships();

    @Query("SELECT COUNT(m) FROM Membership m WHERE m.status = ?1")
    Long countByStatus(Membership.MembershipStatus status);

    @Query("SELECT COUNT(m) FROM Membership m WHERE m.status = 'ACTIVE'")
    Long countActiveMemberships();

    @Query("SELECT COUNT(m) FROM Membership m WHERE m.plan.id = ?1 AND m.status = 'ACTIVE'")
    Long countActiveByPlan(Long planId);

    @Query("SELECT COUNT(m) FROM Membership m WHERE m.plan.id = ?1")
    Long countByPlanId(Long planId);
}
