package za.co.int216d.carwash.booking.core.web;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import za.co.int216d.carwash.booking.core.dto.BookingResponse;
import za.co.int216d.carwash.booking.core.dto.CreateBookingRequest;
import za.co.int216d.carwash.booking.core.dto.SlotAvailabilityResponse;
import za.co.int216d.carwash.booking.core.service.BookingService;
import za.co.int216d.carwash.common.security.SecurityUtils;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;
    private final SecurityUtils securityUtils;

    @PostMapping
    @PreAuthorize("hasRole('CLIENT')")
    public BookingResponse create(@Valid @RequestBody CreateBookingRequest request) {
        Long clientId = securityUtils.getCurrentUserIdAsLong();
        return bookingService.createBooking(clientId, request);
    }

    @PostMapping("/guest")
    public BookingResponse createGuest(@Valid @RequestBody CreateBookingRequest request) {
        return bookingService.createBooking(0L, request);
    }

    @GetMapping("/me")
    @PreAuthorize("hasRole('CLIENT')")
    public List<BookingResponse> myBookings() {
        Long clientId = securityUtils.getCurrentUserIdAsLong();
        return bookingService.listMyBookings(clientId);
    }

    @GetMapping("/{bookingId}")
    @PreAuthorize("hasRole('CLIENT')")
    public BookingResponse getMyBooking(@PathVariable Long bookingId) {
        Long clientId = securityUtils.getCurrentUserIdAsLong();
        return bookingService.getMyBooking(clientId, bookingId);
    }

    @PostMapping("/{bookingId}/cancel")
    @PreAuthorize("hasRole('CLIENT')")
    public BookingResponse cancel(@PathVariable Long bookingId) {
        Long clientId = securityUtils.getCurrentUserIdAsLong();
        return bookingService.cancelMyBooking(clientId, bookingId);
    }

    @PostMapping("/{bookingId}/complete")
    @PreAuthorize("hasRole('ADMIN')")
    public BookingResponse complete(@PathVariable Long bookingId) {
        return bookingService.completeBooking(bookingId);
    }

    @GetMapping("/slots")
    public List<SlotAvailabilityResponse> slots(
            @RequestParam String location,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        return bookingService.getDailySlots(location, date);
    }
}
