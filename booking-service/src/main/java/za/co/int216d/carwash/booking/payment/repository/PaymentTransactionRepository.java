package za.co.int216d.carwash.booking.payment.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import za.co.int216d.carwash.booking.payment.domain.PaymentTransaction;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentTransactionRepository extends JpaRepository<PaymentTransaction, Long> {

    Optional<PaymentTransaction> findByReference(String reference);

    List<PaymentTransaction> findAllByClientIdOrderByCreatedAtDesc(Long clientId);
}
