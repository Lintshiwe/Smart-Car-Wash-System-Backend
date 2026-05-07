package za.co.int216d.carwash.booking.payment.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import za.co.int216d.carwash.booking.catalogue.domain.CatalogueAddon;
import za.co.int216d.carwash.booking.catalogue.domain.CatalogueServiceItem;
import za.co.int216d.carwash.booking.catalogue.repository.CatalogueAddonRepository;
import za.co.int216d.carwash.booking.catalogue.repository.CatalogueServiceRepository;
import za.co.int216d.carwash.common.exception.BadRequestException;
import za.co.int216d.carwash.common.exception.ResourceNotFoundException;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PaymentCalculationService {

    private final CatalogueServiceRepository serviceRepository;
    private final CatalogueAddonRepository addonRepository;

    public BigDecimal calculateBookingAmount(String serviceCode, List<String> addOnCodes) {
        CatalogueServiceItem serviceItem = serviceRepository.findByCode(serviceCode)
            .orElseThrow(() -> new ResourceNotFoundException("Unknown service code: " + serviceCode));

        if (!serviceItem.getIsActive()) {
            throw new BadRequestException("Selected service is currently inactive");
        }

        BigDecimal amount = serviceItem.getBasePrice();

        if (addOnCodes == null || addOnCodes.isEmpty()) {
            return amount;
        }

        for (String code : addOnCodes) {
            CatalogueAddon addon = addonRepository.findByCodeIgnoreCase(code)
                .orElseThrow(() -> new ResourceNotFoundException("Unknown add-on code: " + code));

            if (!addon.getIsActive()) {
                throw new BadRequestException("Selected add-on is currently inactive: " + code);
            }

            amount = amount.add(addon.getPrice());
        }

        return amount;
    }
}
