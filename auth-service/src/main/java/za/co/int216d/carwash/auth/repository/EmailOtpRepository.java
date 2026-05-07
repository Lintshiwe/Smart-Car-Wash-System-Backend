package za.co.int216d.carwash.auth.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import za.co.int216d.carwash.auth.domain.EmailOtp;

import java.util.List;
import java.util.UUID;

public interface EmailOtpRepository extends JpaRepository<EmailOtp, UUID> {
    List<EmailOtp> findByUserIdAndConsumedAtIsNullOrderByCreatedAtDesc(UUID userId);
}
