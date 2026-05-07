package za.co.int216d.carwash.booking.catalogue.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import za.co.int216d.carwash.booking.catalogue.domain.CatalogueServiceItem;

import java.util.List;
import java.util.Optional;

@Repository
public interface CatalogueServiceRepository extends JpaRepository<CatalogueServiceItem, Long> {
    Optional<CatalogueServiceItem> findByCode(String code);
    List<CatalogueServiceItem> findAllByIsActiveTrueOrderByBasePriceAsc();
}
