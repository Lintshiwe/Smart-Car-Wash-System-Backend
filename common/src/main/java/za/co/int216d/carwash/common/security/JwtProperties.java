package za.co.int216d.carwash.common.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "jwt")
public class JwtProperties {

    private String secret;
    private long accessExpiryMs = 900_000L;
    private long refreshExpiryMs = 604_800_000L;
    private String issuer = "int216d-carwash";

    public String getSecret() { return secret; }
    public void setSecret(String secret) { this.secret = secret; }

    public long getAccessExpiryMs() { return accessExpiryMs; }
    public void setAccessExpiryMs(long accessExpiryMs) { this.accessExpiryMs = accessExpiryMs; }

    public long getRefreshExpiryMs() { return refreshExpiryMs; }
    public void setRefreshExpiryMs(long refreshExpiryMs) { this.refreshExpiryMs = refreshExpiryMs; }

    public String getIssuer() { return issuer; }
    public void setIssuer(String issuer) { this.issuer = issuer; }
}
