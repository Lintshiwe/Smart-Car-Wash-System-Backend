package za.co.int216d.carwash.common.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

@Service
public class JwtService {

    public static final String CLAIM_ROLE = "role";
    public static final String TYPE_ACCESS = "access";
    public static final String TYPE_REFRESH = "refresh";
    private static final String CLAIM_TYPE = "typ";

    private final JwtProperties props;
    private final SecretKey key;

    public JwtService(JwtProperties props) {
        this.props = props;
        if (props.getSecret() == null || props.getSecret().length() < 32) {
            throw new IllegalStateException("jwt.secret must be configured and at least 32 characters long");
        }
        this.key = Keys.hmacShaKeyFor(props.getSecret().getBytes(StandardCharsets.UTF_8));
    }

    public String generateAccessToken(UUID userId, Role role) {
        return build(userId, Map.of(CLAIM_ROLE, role.name(), CLAIM_TYPE, TYPE_ACCESS), props.getAccessExpiryMs());
    }

    public String generateRefreshToken(UUID userId) {
        return build(userId, Map.of(CLAIM_TYPE, TYPE_REFRESH), props.getRefreshExpiryMs());
    }

    public Jws<Claims> parse(String token) {
        return Jwts.parser().verifyWith(key).build().parseSignedClaims(token);
    }

    public UUID extractUserId(String token) {
        return UUID.fromString(parse(token).getPayload().getSubject());
    }

    public Role extractRole(String token) {
        return Role.valueOf(parse(token).getPayload().get(CLAIM_ROLE, String.class));
    }

    public boolean isAccessToken(String token) {
        return TYPE_ACCESS.equals(parse(token).getPayload().get(CLAIM_TYPE, String.class));
    }

    public boolean isRefreshToken(String token) {
        return TYPE_REFRESH.equals(parse(token).getPayload().get(CLAIM_TYPE, String.class));
    }

    public long getAccessExpiryMs() {
        return props.getAccessExpiryMs();
    }

    public long getRefreshExpiryMs() {
        return props.getRefreshExpiryMs();
    }

    private String build(UUID userId, Map<String, Object> claims, long ttlMs) {
        Date now = new Date();
        return Jwts.builder()
                .claims(claims)
                .subject(userId.toString())
                .issuer(props.getIssuer())
                .issuedAt(now)
                .expiration(new Date(now.getTime() + ttlMs))
                .signWith(key, Jwts.SIG.HS256)
                .compact();
    }
}
