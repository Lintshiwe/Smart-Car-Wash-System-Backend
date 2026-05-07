package za.co.int216d.carwash.client.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import za.co.int216d.carwash.client.domain.VehicleType;

public record CreateVehicleRequest(
        @NotBlank @Size(max = 60) String vehicleMake,
        @NotBlank @Size(max = 60) String vehicleModel,
        @NotBlank @Size(max = 20) String licensePlate,
        @NotNull VehicleType vehicleType
) {
}
