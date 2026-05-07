package za.co.int216d.carwash.client.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record UpsertProfileRequest(
        @NotBlank @Size(max = 80) String firstName,
        @NotBlank @Size(max = 80) String lastName,
        @NotBlank @Pattern(regexp = "^0[6-8]\\d{8}$", message = "Phone must be 10 digits, e.g. 0821234567")
        String phone,
        @Size(min = 6, max = 20) String idNumber,
        @Past @JsonFormat(pattern = "yyyy-MM-dd") LocalDate dateOfBirth
) {
}
