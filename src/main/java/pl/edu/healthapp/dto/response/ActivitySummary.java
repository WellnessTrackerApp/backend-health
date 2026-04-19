package pl.edu.healthapp.dto.response;

public record ActivitySummary(
        int durationInMinutes,
        double caloriesBurned
){}
