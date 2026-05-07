package za.co.int216d.carwash.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record PasswordResetConfirmRequest(
    @NotBlank @Email String email,
    @NotBlank String resetCode,
    @NotBlank @Size(min = 8, max = 128) String newPassword
) {}
