package pl.edu.healthapp.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import pl.edu.healthapp.model.HealthGoalType;

public record HealthGoalCreationDTO(
        @NotNull(message = "Health goal type is required")
        HealthGoalType healthGoalType,
        @Min(value = 1, message = "Traget value must be at least 1")
        double target
){}
