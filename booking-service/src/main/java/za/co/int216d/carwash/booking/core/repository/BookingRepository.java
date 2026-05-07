package za.co.int216d.carwash.booking.core.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import za.co.int216d.carwash.booking.core.domain.Booking;
import za.co.int216d.carwash.booking.core.domain.BookingStatus;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    List<Booking> findAllByClientIdOrderByCreatedAtDesc(Long clientId);

    Optional<Booking> findByIdAndClientId(Long bookingId, Long clientId);

    boolean existsByLocationIgnoreCaseAndScheduledAtAndStatusIn(
            String location,
            LocalDateTime scheduledAt,
            Collection<BookingStatus> statuses
    );

    long countByLocationIgnoreCaseAndScheduledAtAndStatusIn(
            String location,
            LocalDateTime scheduledAt,
            Collection<BookingStatus> statuses
    );
}
