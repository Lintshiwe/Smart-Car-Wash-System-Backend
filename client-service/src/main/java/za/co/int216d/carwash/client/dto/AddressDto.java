package za.co.int216d.carwash.client.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record AddressDto(
        UUID id,
        UUID clientId,
        String street,
        String city,
        String province,
        String postalCode,
        BigDecimal latitude,
        BigDecimal longitude,
        boolean primary,
        Instant createdAt
) {
}
