package pl.edu.healthapp.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.edu.healthapp.model.Notification;
import pl.edu.healthapp.model.NotificationType;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findAllByUserId(UUID userId);
    Optional<Notification> findByUserIdAndNotificationType(UUID userId, NotificationType notificationType);
    Optional<Notification> findByIdAndUserId(Long id, UUID userId);
}
