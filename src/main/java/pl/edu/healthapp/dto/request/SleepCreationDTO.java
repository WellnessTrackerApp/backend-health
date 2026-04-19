package pl.edu.healthapp.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import lombok.NoArgsConstructor;
import pl.edu.healthapp.model.SleepQuality;

import java.time.OffsetDateTime;

public record SleepCreationDTO(
        @NotNull(message = "Start time is required")
        @PastOrPresent(message = "Sleep cannot start in the future")
        OffsetDateTime start,
        @NotNull(message = "End time is required")
        @PastOrPresent(message = "Sleep cannot end in the future")
        OffsetDateTime end,
        @Min(value = 1, message = "Quality should be >= 1")
        @Max(value = 5, message = "Quality should be <= 5")
        SleepQuality quality
){}
