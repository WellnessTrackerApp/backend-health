package pl.edu.healthapp.dto.response;

import java.time.OffsetDateTime;

public record DietDTO(
        Long id,
        String mealName,
        int calories,
        double proteins,
        double carbs,
        double fats,
        OffsetDateTime eatenAt
) {}
