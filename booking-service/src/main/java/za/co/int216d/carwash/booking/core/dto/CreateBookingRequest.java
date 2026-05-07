package za.co.int216d.carwash.booking.core.dto;

import jakarta.validation.constraints.*;
import za.co.int216d.carwash.booking.payment.dto.PaymentRequest;

import java.time.LocalDateTime;
import java.util.List;

public record CreateBookingRequest(
        @NotBlank String serviceType,
        @NotBlank String packageCode,
        @NotBlank @Size(max = 120) String fullName,
        @NotBlank @Email String email,
        @NotBlank String phone,
        @NotBlank String vehicleType,
        @NotBlank String location,
        @NotNull LocalDateTime scheduledAt,
        String notes,
        List<String> addOns,
        @NotNull @jakarta.validation.Valid PaymentRequest payment
) {
}
