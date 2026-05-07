package za.co.int216d.carwash.common.security;

import java.util.UUID;

public record AuthPrincipal(UUID userId, Role role) {
}
