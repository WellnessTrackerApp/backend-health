package pl.edu.healthapp.service;


import org.springframework.stereotype.Service;
import pl.edu.healthapp.exception.NotificationNotFoundException;
import pl.edu.healthapp.model.*;
import pl.edu.healthapp.repository.NotificationRepository;

import java.time.OffsetDateTime;
import java.util.List;


@Service
public class NotificationService {
    private final NotificationRepository notificationRepository;
    private final UserService userService;

    public NotificationService(NotificationRepository notificationRepository, UserService userService){
        this.notificationRepository = notificationRepository;
        this.userService = userService;
    }

    public Notification addNotification(NotificationType notificationType,
                                        String message,
                                        OffsetDateTime updatedAt,
                                        User user){
        Notification notification = notificationRepository.findByUserIdAndNotificationType(user.getId(), notificationType)
                                                            .orElse(Notification.builder()
                                                                                .notificationType(notificationType)
                                                                                .user(user)
                                                                                .build());
        notification.setMessage(message);
        notification.setUpdatedAt(updatedAt);

        notificationRepository.save(notification);
        return notification;
    }

    public List<Notification> getUserNotifications(String username) {
        User user = userService.findByUsername(username);

        return notificationRepository.findAllByUserId(user.getId());
    }


    public void markAsRead(String username, Long notificationId) {
        User user = userService.findByUsername(username);
        Notification notification = notificationRepository.findByIdAndUserId(notificationId, user.getId())
                                .orElseThrow(() ->
                                        new NotificationNotFoundException("Notification with id " + notificationId + " not found or does not belong to user " + username));
        notificationRepository.delete(notification);
    }

}
