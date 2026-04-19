package pl.edu.healthapp.dto.request;

import jakarta.validation.constraints.*;

import java.time.OffsetDateTime;

public record DietCreationDTO(
        @NotBlank(message = "Name is required")
        String mealName,
        @NotNull(message = "Meal time is required")
        @PastOrPresent(message = "Meal cannot start in the future")
        OffsetDateTime eatenAt,
        @Min(value = 0, message = "Calories cannot be negative")
        int calories,
        @DecimalMin(value = "0.0", message = "Protein cannot be negative")
        double protein,
        @DecimalMin(value = "0.0", message = "Carbs cannot be negative")
        double carbs,
        @DecimalMin(value = "0.0", message = "Fats cannot be negative")
        double fat
){}
