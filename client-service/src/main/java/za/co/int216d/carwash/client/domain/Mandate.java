package za.co.int216d.carwash.client.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "mandates")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Mandate {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(name = "client_id", nullable = false)
    private UUID clientId;

    @Column(name = "mandate_version", nullable = false)
    private String mandateVersion;

    @Column(name = "agreed_full_name", nullable = false)
    private String agreedFullName;

    @Column(name = "ip_address")
    private String ipAddress;

    @Column(name = "pdf_url")
    private String pdfUrl;

    @Column(name = "accepted_at", nullable = false, updatable = false)
    private Instant acceptedAt;

    @PrePersist
    void prePersist() {
        if (acceptedAt == null) acceptedAt = Instant.now();
    }
}
