package pl.edu.healthapp.dto.response;

import pl.edu.healthapp.model.NotificationType;

import java.time.OffsetDateTime;

public record NotificationDTO(
        Long id,
        String message,
        NotificationType notificationType,
        OffsetDateTime updatedAt
) {}
