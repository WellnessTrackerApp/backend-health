package pl.edu.healthapp.mapper;

import pl.edu.healthapp.dto.response.NotificationDTO;
import pl.edu.healthapp.model.Notification;


public class NotificationMapper {
    public static NotificationDTO fromEntity(Notification notification){
        return new NotificationDTO(
                                notification.getId(),
                                notification.getMessage(),
                                notification.getNotificationType(),
                                notification.getUpdatedAt()
        );
    }
}
