package za.co.int216d.carwash.booking.catalogue.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import za.co.int216d.carwash.booking.catalogue.domain.CatalogueAddon;

import java.util.List;
import java.util.Optional;

@Repository
public interface CatalogueAddonRepository extends JpaRepository<CatalogueAddon, Long> {
    List<CatalogueAddon> findAllByIsActiveTrueOrderByPriceAsc();
    Optional<CatalogueAddon> findByCodeIgnoreCase(String code);
}
