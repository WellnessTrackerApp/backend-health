package pl.edu.healthapp.dto.response;

import pl.edu.healthapp.model.SleepQuality;

import java.time.OffsetDateTime;

public record SleepDTO (
        Long id,
        OffsetDateTime start,
        OffsetDateTime end,
        SleepQuality quality
) {}
