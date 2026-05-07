package za.co.int216d.carwash.auth.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import za.co.int216d.carwash.auth.domain.User;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByEmailIgnoreCase(String email);
    boolean existsByEmailIgnoreCase(String email);
}
