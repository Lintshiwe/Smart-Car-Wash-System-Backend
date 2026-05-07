package za.co.int216d.carwash.booking.notification.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import za.co.int216d.carwash.booking.notification.domain.Notification;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    Page<Notification> findByClientIdOrderByCreatedAtDesc(Long clientId, Pageable pageable);

    @Query("SELECT n FROM Notification n WHERE n.clientId = ?1 AND n.isRead = false ORDER BY n.createdAt DESC")
    Page<Notification> findUnreadByClientId(Long clientId, Pageable pageable);

    @Query("SELECT COUNT(n) FROM Notification n WHERE n.clientId = ?1 AND n.isRead = false")
    Long countUnreadByClientId(Long clientId);

    List<Notification> findByClientIdAndType(Long clientId, Notification.NotificationType type);
}
