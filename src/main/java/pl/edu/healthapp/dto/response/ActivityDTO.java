package pl.edu.healthapp.dto.response;

import pl.edu.healthapp.model.ActivityType;

import java.time.OffsetDateTime;

public record ActivityDTO(
        Long id,
        OffsetDateTime startedAt,
        ActivityType activityType,
        int durationInMinutes,
        double caloriesBurned
) {}
