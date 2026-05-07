package za.co.int216d.carwash.auth.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app")
public class AuthAppProperties {

    private Mail mail = new Mail();
    private Otp otp = new Otp();
    private Cookie cookie = new Cookie();

    public Mail getMail() { return mail; }
    public Otp getOtp() { return otp; }
    public Cookie getCookie() { return cookie; }

    public static class Mail {
        private String projectName = "INT216D Smart Car Wash";
        private String fromName = "INT216D Smart Car Wash";
        private String fromAddress = "noreply@int216d.co.za";
        public String getProjectName() { return projectName; }
        public void setProjectName(String projectName) { this.projectName = projectName; }
        public String getFromName() { return fromName; }
        public void setFromName(String fromName) { this.fromName = fromName; }
        public String getFromAddress() { return fromAddress; }
        public void setFromAddress(String fromAddress) { this.fromAddress = fromAddress; }
    }

    public static class Otp {
        private int length = 6;
        private int ttlMinutes = 10;
        public int getLength() { return length; }
        public void setLength(int length) { this.length = length; }
        public int getTtlMinutes() { return ttlMinutes; }
        public void setTtlMinutes(int ttlMinutes) { this.ttlMinutes = ttlMinutes; }
    }

    public static class Cookie {
        private String refreshName = "int216d_refresh";
        private boolean secure = false;
        private String sameSite = "Lax";
        private String path = "/api/v1/auth";
        public String getRefreshName() { return refreshName; }
        public void setRefreshName(String refreshName) { this.refreshName = refreshName; }
        public boolean isSecure() { return secure; }
        public void setSecure(boolean secure) { this.secure = secure; }
        public String getSameSite() { return sameSite; }
        public void setSameSite(String sameSite) { this.sameSite = sameSite; }
        public String getPath() { return path; }
        public void setPath(String path) { this.path = path; }
    }
}
