package za.co.int216d.carwash.client.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import za.co.int216d.carwash.client.domain.Mandate;

import java.util.List;
import java.util.UUID;

public interface MandateRepository extends JpaRepository<Mandate, UUID> {
    List<Mandate> findByClientIdOrderByAcceptedAtDesc(UUID clientId);
}
