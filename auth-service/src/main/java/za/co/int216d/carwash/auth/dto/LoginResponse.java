package za.co.int216d.carwash.auth.dto;

import za.co.int216d.carwash.common.security.Role;

import java.util.UUID;

public record LoginResponse(
        String accessToken,
        UUID userId,
        Role role,
        long expiresInMs
) {
}
