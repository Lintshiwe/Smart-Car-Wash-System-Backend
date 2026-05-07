package za.co.int216d.carwash.client.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import za.co.int216d.carwash.client.domain.Address;

import java.util.List;
import java.util.UUID;

public interface AddressRepository extends JpaRepository<Address, UUID> {
    List<Address> findByClientIdOrderByCreatedAtDesc(UUID clientId);
    long countByClientId(UUID clientId);
}
