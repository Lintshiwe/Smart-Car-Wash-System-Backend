package za.co.int216d.carwash.client.dto;

import java.time.Instant;
import java.util.UUID;

public record MandateDto(
        UUID id,
        UUID clientId,
        String mandateVersion,
        String agreedFullName,
        String pdfUrl,
        Instant acceptedAt
) {
}
