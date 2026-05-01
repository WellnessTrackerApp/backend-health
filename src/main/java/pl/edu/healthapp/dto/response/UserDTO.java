package pl.edu.healthapp.dto.response;

import pl.edu.healthapp.model.EventType;
import pl.edu.healthapp.model.Gender;
import pl.edu.healthapp.model.NotificationType;

import java.time.OffsetDateTime;
import java.util.Set;
import java.util.UUID;

public record UserDTO(
        UUID id,
        String username,
        String email,
        String password,
        OffsetDateTime birthDate,
        double height,
        double weight,
        Gender gender,
        Set<HealthGoalDTO> goals,
        Set<SleepDTO> sleepHistory,
        Set<ActivityDTO> activityHistory,
        Set<DietDTO> dietHistory,
        Set<NotificationType> notifications,
        Set<EventType> events
) {}
