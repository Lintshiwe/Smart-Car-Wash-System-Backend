package za.co.int216d.carwash.auth.dto;

import java.util.UUID;

public record RegisterResponse(UUID userId, String message) {
}
