package za.co.int216d.carwash.booking.catalogue.dto;

import lombok.Builder;

import java.math.BigDecimal;

@Builder
public record CatalogueAddonResponse(
        Long id,
        String code,
        String name,
        String description,
        BigDecimal price,
        Boolean isActive
) {
}
