package za.co.int216d.carwash.booking.core.dto;

import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;

@Builder
public record BookingResponse(
        Long id,
        Long clientId,
        String serviceCode,
        String serviceType,
        String fullName,
        String email,
        String phone,
        String vehicleType,
        String location,
        LocalDateTime scheduledAt,
        String status,
        String paymentReference,
        String paymentStatus,
        String notes,
        List<String> addOns,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
