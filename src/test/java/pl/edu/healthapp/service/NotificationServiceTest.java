package pl.edu.healthapp.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.edu.healthapp.exception.NotificationNotFoundException;
import pl.edu.healthapp.model.*;
import pl.edu.healthapp.repository.NotificationRepository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private UserService userService;

    @InjectMocks
    private NotificationService notificationService;

    private User sampleUser;
    private Notification sampleNotification;
    private OffsetDateTime now;

    @BeforeEach
    void setUp() {
        now = OffsetDateTime.now();

        sampleUser = new User();
        sampleUser.setId(UUID.randomUUID());
        sampleUser.setUsername("testUser");

        sampleNotification = Notification.builder()
                .id(1L)
                .notificationType(NotificationType.ACTIVITY)
                .message("Your activity dropped")
                .updatedAt(now)
                .user(sampleUser)
                .build();
    }
    
    @Test
    void addNotification_validData_createsNotification() {
        when(notificationRepository.findByUserIdAndNotificationType(sampleUser.getId(), NotificationType.ACTIVITY))
                .thenReturn(Optional.empty());
        
        Notification result = notificationService.addNotification(
                NotificationType.ACTIVITY,
                "New notification",
                now,
                sampleUser
        );
        
        assertNotNull(result);
        assertEquals(NotificationType.ACTIVITY, result.getNotificationType());
        assertEquals("New notification", result.getMessage());
        assertEquals(now, result.getUpdatedAt());
        assertEquals(sampleUser, result.getUser());

        verify(notificationRepository, times(1)).save(any(Notification.class));
    }

    @Test
    void addNotification_notificationOfSuchTypeAlreadyExists_updateNotification() {
       when(notificationRepository.findByUserIdAndNotificationType(sampleUser.getId(), NotificationType.ACTIVITY))
                .thenReturn(Optional.of(sampleNotification));
       OffsetDateTime newTime = now.plusHours(1);
       String newMessage = "New notification body";
       
       Notification result = notificationService.addNotification(
                NotificationType.ACTIVITY,
                newMessage,
                newTime,
                sampleUser
       );
       
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals(newMessage, result.getMessage());
        assertEquals(newTime, result.getUpdatedAt());

        verify(notificationRepository, times(1)).save(sampleNotification);
    }

    @Test
    void getUserNotifications_validaData_returnsNotifications() {
        when(userService.findByUsername("testUser")).thenReturn(sampleUser);
        Notification n2 = Notification.builder()
                .id(2L)
                .notificationType(NotificationType.LOW_KCAL)
                .message("Looks like you didn't eat that much today, good time to have a meal.")
                .user(sampleUser)
                .build();
        when(notificationRepository.findAllByUserId(sampleUser.getId())).thenReturn(List.of(sampleNotification, n2));
        
        List<Notification> result = notificationService.getUserNotifications("testUser");
        
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(notificationRepository, times(1)).findAllByUserId(sampleUser.getId());
    }

    @Test
    void markAsRead_validData_deletesNotification() {
        when(userService.findByUsername("testUser")).thenReturn(sampleUser);
        when(notificationRepository.findByIdAndUserId(1L, sampleUser.getId()))
                .thenReturn(Optional.of(sampleNotification));
        
        assertDoesNotThrow(() -> notificationService.markAsRead("testUser", 1L));
        
        verify(notificationRepository, times(1)).delete(sampleNotification);
    }

    @Test
    void markAsRead_notificationDoesNotExist_throwsException() {
        when(userService.findByUsername("testUser")).thenReturn(sampleUser);
        when(notificationRepository.findByIdAndUserId(99L, sampleUser.getId()))
                .thenReturn(Optional.empty());
        
        NotificationNotFoundException exception = assertThrows(NotificationNotFoundException.class, () ->
                notificationService.markAsRead("testUser", 99L)
        );

        assertTrue(exception.getMessage().contains("Notification with id 99 not found"));
        verify(notificationRepository, never()).delete(any());
    }
}
