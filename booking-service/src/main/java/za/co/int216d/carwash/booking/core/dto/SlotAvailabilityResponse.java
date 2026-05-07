package za.co.int216d.carwash.booking.core.dto;

import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record SlotAvailabilityResponse(
        LocalDateTime slot,
        boolean available,
        long currentBookings
) {
}
