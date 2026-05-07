package za.co.int216d.carwash.client.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import za.co.int216d.carwash.client.domain.Client;

import java.util.Optional;
import java.util.UUID;

public interface ClientRepository extends JpaRepository<Client, UUID> {
    Optional<Client> findByUserId(UUID userId);
}
