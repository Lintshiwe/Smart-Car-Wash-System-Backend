package za.co.int216d.carwash.booking.core.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import za.co.int216d.carwash.booking.catalogue.service.CatalogueService;
import za.co.int216d.carwash.booking.core.domain.Booking;
import za.co.int216d.carwash.booking.core.domain.BookingStatus;
import za.co.int216d.carwash.booking.core.dto.BookingResponse;
import za.co.int216d.carwash.booking.core.dto.CreateBookingRequest;
import za.co.int216d.carwash.booking.core.dto.SlotAvailabilityResponse;
import za.co.int216d.carwash.booking.core.repository.BookingRepository;
import za.co.int216d.carwash.booking.notification.service.EmailNotificationService;
import za.co.int216d.carwash.booking.payment.domain.PaymentPurpose;
import za.co.int216d.carwash.booking.payment.dto.PaymentProcessResult;
import za.co.int216d.carwash.booking.payment.service.PaymentCalculationService;
import za.co.int216d.carwash.booking.payment.service.PaymentService;
import za.co.int216d.carwash.common.exception.BadRequestException;
import za.co.int216d.carwash.common.exception.ResourceNotFoundException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class BookingService {

    private static final List<BookingStatus> ACTIVE_SLOT_STATUSES = List.of(BookingStatus.PENDING, BookingStatus.CONFIRMED);

    private final BookingRepository bookingRepository;
    private final CatalogueService catalogueService;
    private final PaymentCalculationService paymentCalculationService;
    private final PaymentService paymentService;
    private final EmailNotificationService emailNotificationService;

    public BookingResponse createBooking(Long clientId, CreateBookingRequest request) {
        if (request.scheduledAt().isBefore(LocalDateTime.now().plusMinutes(15))) {
            throw new BadRequestException("Booking time must be at least 15 minutes in the future");
        }

        String normalizedCode = normalizeServiceCode(request.packageCode(), request.serviceType());
        if (!catalogueService.serviceCodeExists(normalizedCode)) {
            throw new BadRequestException("Unknown service code: " + normalizedCode);
        }

        boolean slotTaken = bookingRepository.existsByLocationIgnoreCaseAndScheduledAtAndStatusIn(
                request.location(),
                request.scheduledAt().withSecond(0).withNano(0),
                ACTIVE_SLOT_STATUSES
        );
        if (slotTaken) {
            throw new BadRequestException("Selected slot is no longer available");
        }

        Booking booking = Booking.builder()
                .clientId(clientId)
                .serviceCode(normalizedCode)
                .serviceType(request.serviceType().toUpperCase())
                .fullName(request.fullName())
                .email(request.email())
                .phone(request.phone())
                .vehicleType(request.vehicleType())
                .location(request.location())
                .scheduledAt(request.scheduledAt().withSecond(0).withNano(0))
                .status(BookingStatus.PENDING)
                .notes(request.notes())
                .addOns(String.join(",", request.addOns() == null ? List.of() : request.addOns()))
                .build();

            booking = bookingRepository.save(booking);

            BigDecimal amount = paymentCalculationService.calculateBookingAmount(normalizedCode, request.addOns());
            PaymentProcessResult paymentResult = paymentService.processPayment(
                clientId,
                booking.getId(),
                null,
                PaymentPurpose.BOOKING,
                amount,
                request.email(),
                "Booking payment for service " + normalizedCode,
                request.payment()
            );

            booking.setPaymentReference(paymentResult.reference());
            booking.setPaymentStatus(paymentResult.status().name());
            booking = bookingRepository.save(booking);

            // Send booking confirmation email to client
            if (request.email() != null && !request.email().isBlank()) {
                try {
                    emailNotificationService.sendBookingConfirmationEmail(
                        request.email(), request.fullName(), request.serviceType().toUpperCase(),
                        normalizedCode, request.location(), request.scheduledAt(), paymentResult.reference()
                    );
                } catch (Exception e) {
                    log.warn("Failed to send booking confirmation email: {}", e.getMessage());
                }
            }

            // Notify admin about new booking
            try {
                emailNotificationService.sendAdminNewBookingEmail(
                    request.fullName(), request.email(), request.serviceType().toUpperCase(),
                    normalizedCode, request.location(), request.scheduledAt(), paymentResult.reference()
                );
            } catch (Exception e) {
                log.warn("Failed to send admin notification email: {}", e.getMessage());
            }

            return toResponse(booking);
    }

    @Transactional(readOnly = true)
    public List<BookingResponse> listMyBookings(Long clientId) {
        return bookingRepository.findAllByClientIdOrderByCreatedAtDesc(clientId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public BookingResponse getMyBooking(Long clientId, Long bookingId) {
        Booking booking = bookingRepository.findByIdAndClientId(bookingId, clientId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found: " + bookingId));
        return toResponse(booking);
    }

    public BookingResponse cancelMyBooking(Long clientId, Long bookingId) {
        Booking booking = bookingRepository.findByIdAndClientId(bookingId, clientId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found: " + bookingId));

        if (booking.getStatus() == BookingStatus.CANCELLED || booking.getStatus() == BookingStatus.COMPLETED) {
            throw new BadRequestException("Booking cannot be cancelled from status: " + booking.getStatus());
        }

        booking.setStatus(BookingStatus.CANCELLED);
        return toResponse(bookingRepository.save(booking));
    }

    public BookingResponse completeBooking(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found: " + bookingId));

        if (booking.getStatus() == BookingStatus.CANCELLED) {
            throw new BadRequestException("Cancelled booking cannot be completed");
        }

        booking.setStatus(BookingStatus.COMPLETED);
        return toResponse(bookingRepository.save(booking));
    }

    @Transactional(readOnly = true)
    public List<SlotAvailabilityResponse> getDailySlots(String location, LocalDate date) {
        LocalDateTime start = date.atTime(8, 0);

        return java.util.stream.IntStream.range(0, 10)
                .mapToObj(i -> start.plusHours(i))
                .map(slot -> {
                    long count = bookingRepository.countByLocationIgnoreCaseAndScheduledAtAndStatusIn(
                            location,
                            slot,
                            ACTIVE_SLOT_STATUSES
                    );
                    return SlotAvailabilityResponse.builder()
                            .slot(slot)
                            .currentBookings(count)
                            .available(count == 0)
                            .build();
                })
                .toList();
    }

    private String normalizeServiceCode(String packageCode, String serviceType) {
        String code = packageCode == null ? "" : packageCode.trim().toUpperCase();
        // Strip common frontend suffixes (_WASH, _SERVICE, etc.)
        code = code.replaceAll("_WASH$", "").replaceAll("_SERVICE$", "");
        // Map known frontend values to backend DB codes
        code = code.replace("MOBILE_BASIC", "MOBILE_BASIC")  // keep correct ones
                   .replace("MOBILE_WASH", "MOBILE_BASIC");
        if (code.equals("MOBILE")) code = "MOBILE_BASIC";

        if (code.contains("_")) {
            // Already has prefix like BAY_BASIC or MOBILE_PREMIUM
            return code;
        }
        String normalizedType = serviceType == null ? "" : serviceType.trim().toUpperCase();
        if (!"BAY".equals(normalizedType) && !"MOBILE".equals(normalizedType)) {
            throw new BadRequestException("serviceType must be BAY or MOBILE");
        }
        return normalizedType + "_" + code;
    }

    private BookingResponse toResponse(Booking booking) {
        List<String> addOns = booking.getAddOns() == null || booking.getAddOns().isBlank()
                ? List.of()
                : Arrays.stream(booking.getAddOns().split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .toList();

        return BookingResponse.builder()
                .id(booking.getId())
                .clientId(booking.getClientId())
                .serviceCode(booking.getServiceCode())
                .serviceType(booking.getServiceType())
                .fullName(booking.getFullName())
                .email(booking.getEmail())
                .phone(booking.getPhone())
                .vehicleType(booking.getVehicleType())
                .location(booking.getLocation())
                .scheduledAt(booking.getScheduledAt())
                .status(booking.getStatus().name())
                .paymentReference(booking.getPaymentReference())
                .paymentStatus(booking.getPaymentStatus())
                .notes(booking.getNotes())
                .addOns(addOns)
                .createdAt(booking.getCreatedAt())
                .updatedAt(booking.getUpdatedAt())
                .build();
    }
}
