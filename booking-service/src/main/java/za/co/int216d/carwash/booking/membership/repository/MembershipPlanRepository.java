package za.co.int216d.carwash.booking.membership.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import za.co.int216d.carwash.booking.membership.domain.MembershipPlan;

import java.util.List;
import java.util.Optional;

@Repository
public interface MembershipPlanRepository extends JpaRepository<MembershipPlan, Long> {
    Optional<MembershipPlan> findByName(String name);
    List<MembershipPlan> findAllByIsActiveTrue();
    List<MembershipPlan> findAllByDiscountEligibleTrue();
}
