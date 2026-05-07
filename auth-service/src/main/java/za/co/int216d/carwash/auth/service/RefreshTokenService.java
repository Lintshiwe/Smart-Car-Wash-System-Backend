package za.co.int216d.carwash.auth.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import za.co.int216d.carwash.auth.domain.RefreshToken;
import za.co.int216d.carwash.auth.repository.RefreshTokenRepository;
import za.co.int216d.carwash.common.exception.ApiException;
import za.co.int216d.carwash.common.security.JwtService;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.HexFormat;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final RefreshTokenRepository repository;
    private final JwtService jwtService;

    @Transactional
    public String issue(UUID userId) {
        String raw = jwtService.generateRefreshToken(userId);
        RefreshToken record = RefreshToken.builder()
                .userId(userId)
                .tokenHash(hash(raw))
                .expiresAt(Instant.now().plusMillis(jwtService.getRefreshExpiryMs()))
                .build();
        repository.save(record);
        return raw;
    }

    @Transactional
    public UUID rotate(String rawToken) {
        if (rawToken == null || rawToken.isBlank()) {
            throw ApiException.unauthorized("Missing refresh token");
        }
        UUID userId;
        try {
            if (!jwtService.isRefreshToken(rawToken)) {
                throw ApiException.unauthorized("Invalid refresh token");
            }
            userId = jwtService.extractUserId(rawToken);
        } catch (Exception e) {
            throw ApiException.unauthorized("Invalid refresh token");
        }

        Optional<RefreshToken> stored = repository.findByTokenHash(hash(rawToken));
        RefreshToken record = stored.orElseThrow(() -> ApiException.unauthorized("Refresh token not recognised"));
        if (!record.isActive()) {
            throw ApiException.unauthorized("Refresh token expired or revoked");
        }
        record.setRevokedAt(Instant.now());
        repository.save(record);
        return userId;
    }

    @Transactional
    public void revokeAll(UUID userId) {
        repository.revokeAllForUser(userId, Instant.now());
    }

    private static String hash(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] out = digest.digest(value.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(out);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}
