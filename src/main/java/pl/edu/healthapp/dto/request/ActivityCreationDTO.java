package pl.edu.healthapp.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import pl.edu.healthapp.model.ActivityType;

import java.time.OffsetDateTime;

public record ActivityCreationDTO(
        @NotNull(message = "Activity start time is required")
        @PastOrPresent(message = "Activity cannot start in the future")
        OffsetDateTime startedAt,
        @NotNull(message = "Activity type is required")
        ActivityType type,
        @Min(value = 1, message = "Duration must be at least 1 minute")
        @Max(value = 1440, message = "Duration cannot exceed 24 hours")
        int durationInMinutes
){}
