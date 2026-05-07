package za.co.int216d.carwash.client.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import za.co.int216d.carwash.client.domain.Vehicle;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface VehicleRepository extends JpaRepository<Vehicle, UUID> {
    List<Vehicle> findByClientIdOrderByCreatedAtDesc(UUID clientId);
    Optional<Vehicle> findByIdAndClientId(UUID id, UUID clientId);
    boolean existsByClientIdAndLicensePlateIgnoreCase(UUID clientId, String licensePlate);
}
