package pl.edu.healthapp.dto.response;

import pl.edu.healthapp.model.HealthGoalType;

import java.time.OffsetDateTime;

public record HealthGoalDTO(
        Long id,
        HealthGoalType healthGoalType,
        double target,
        OffsetDateTime createdAt
) {}
