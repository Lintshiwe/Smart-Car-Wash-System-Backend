package za.co.int216d.carwash.booking.membership.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import za.co.int216d.carwash.booking.membership.domain.MembershipCreditLog;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface MembershipCreditLogRepository extends JpaRepository<MembershipCreditLog, Long> {
    Page<MembershipCreditLog> findByMembershipIdOrderByCreatedAtDesc(Long membershipId, Pageable pageable);

    List<MembershipCreditLog> findByMembershipIdAndCreatedAtBetween(
        Long membershipId,
        LocalDateTime startDate,
        LocalDateTime endDate
    );

    List<MembershipCreditLog> findByMembershipIdAndType(
        Long membershipId,
        MembershipCreditLog.TransactionType type
    );
}
