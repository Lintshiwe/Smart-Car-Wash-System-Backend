package za.co.int216d.carwash.client.dto;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record ClientProfileDto(
        UUID id,
        UUID userId,
        String firstName,
        String lastName,
        String phone,
        String idNumber,
        LocalDate dateOfBirth,
        Instant createdAt
) {
}
