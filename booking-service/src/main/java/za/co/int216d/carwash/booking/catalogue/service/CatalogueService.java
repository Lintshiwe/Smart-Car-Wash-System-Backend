package za.co.int216d.carwash.booking.catalogue.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import za.co.int216d.carwash.booking.catalogue.domain.CatalogueAddon;
import za.co.int216d.carwash.booking.catalogue.domain.CatalogueServiceItem;
import za.co.int216d.carwash.booking.catalogue.dto.CatalogueAddonResponse;
import za.co.int216d.carwash.booking.catalogue.dto.CatalogueServiceResponse;
import za.co.int216d.carwash.booking.catalogue.repository.CatalogueAddonRepository;
import za.co.int216d.carwash.booking.catalogue.repository.CatalogueServiceRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CatalogueService {

    private final CatalogueServiceRepository serviceRepository;
    private final CatalogueAddonRepository addonRepository;

    public List<CatalogueServiceResponse> listServices(boolean activeOnly) {
        List<CatalogueServiceItem> items = activeOnly
                ? serviceRepository.findAllByIsActiveTrueOrderByBasePriceAsc()
                : serviceRepository.findAll();

        return items.stream().map(this::toServiceResponse).toList();
    }

    public List<CatalogueAddonResponse> listAddons(boolean activeOnly) {
        List<CatalogueAddon> items = activeOnly
                ? addonRepository.findAllByIsActiveTrueOrderByPriceAsc()
                : addonRepository.findAll();

        return items.stream().map(this::toAddonResponse).toList();
    }

    public boolean serviceCodeExists(String code) {
        return serviceRepository.findByCode(code).isPresent();
    }

    private CatalogueServiceResponse toServiceResponse(CatalogueServiceItem item) {
        return CatalogueServiceResponse.builder()
                .id(item.getId())
                .code(item.getCode())
                .name(item.getName())
                .description(item.getDescription())
                .serviceType(item.getServiceType())
                .basePrice(item.getBasePrice())
                .durationMinutes(item.getDurationMinutes())
                .isActive(item.getIsActive())
                .build();
    }

    private CatalogueAddonResponse toAddonResponse(CatalogueAddon item) {
        return CatalogueAddonResponse.builder()
                .id(item.getId())
                .code(item.getCode())
                .name(item.getName())
                .description(item.getDescription())
                .price(item.getPrice())
                .isActive(item.getIsActive())
                .build();
    }
}
