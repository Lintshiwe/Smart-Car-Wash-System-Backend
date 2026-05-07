package za.co.int216d.carwash.client.dto;

import za.co.int216d.carwash.client.domain.VehicleType;

import java.time.Instant;
import java.util.UUID;

public record VehicleDto(
        UUID id,
        UUID clientId,
        String make,
        String model,
        String licensePlate,
        VehicleType vehicleType,
        Instant createdAt
) {
}
