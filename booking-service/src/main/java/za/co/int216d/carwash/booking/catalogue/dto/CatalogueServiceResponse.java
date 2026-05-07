package za.co.int216d.carwash.booking.catalogue.dto;

import lombok.Builder;

import java.math.BigDecimal;

@Builder
public record CatalogueServiceResponse(
        Long id,
        String code,
        String name,
        String description,
        String serviceType,
        BigDecimal basePrice,
        Integer durationMinutes,
        Boolean isActive
) {
}
