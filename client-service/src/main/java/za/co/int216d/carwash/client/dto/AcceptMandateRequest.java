package za.co.int216d.carwash.client.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AcceptMandateRequest(
        @NotBlank @Size(max = 20) String mandateVersion,
        @NotBlank @Size(max = 160) String agreedFullName
) {
}
