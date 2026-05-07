package za.co.int216d.carwash.booking.membership.dto;

import jakarta.validation.constraints.*;
import lombok.*;
import java.math.BigDecimal;

/**
 * DTO for creating/updating membership plans
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateMembershipPlanRequest {

    @NotBlank(message = "Plan name is required")
    @Size(min = 3, max = 50, message = "Plan name must be between 3 and 50 characters")
    private String name;

    @NotBlank(message = "Description is required")
    private String description;

    @NotNull(message = "Monthly price is required")
    @Positive(message = "Monthly price must be positive")
    private BigDecimal monthlyPrice;

    @NotNull(message = "Credits per month is required")
    @PositiveOrZero(message = "Credits per month must be zero or positive")
    private Integer creditsPerMonth;

    @NotNull(message = "Free washes per month is required")
    @PositiveOrZero(message = "Free washes must be zero or positive")
    private Integer freeWashes;

    @NotNull(message = "Active flag is required")
    private Boolean isActive;

    @NotNull(message = "Discount eligible flag is required")
    private Boolean discountEligible;

    @NotNull(message = "Discount percentage is required")
    @PositiveOrZero(message = "Discount percentage must be zero or positive")
    @Max(value = 100, message = "Discount percentage cannot exceed 100%")
    private BigDecimal discountPercentage;
}
