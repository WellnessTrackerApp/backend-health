package pl.edu.healthapp.service;

import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.edu.healthapp.dto.response.ActivitySummary;
import pl.edu.healthapp.exception.ActivityEntryAlreadyExistsException;
import pl.edu.healthapp.exception.ActivityEntryNotFoundException;
import pl.edu.healthapp.exception.PDFCreationException;
import pl.edu.healthapp.model.*;
import pl.edu.healthapp.repository.ActivityRepository;
import pl.edu.healthapp.repository.EventRepository;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ActivityServiceTest {

    @Mock
    private ActivityRepository activityRepository;

    @Mock
    private UserService userService;

    @Mock
    private NotificationService notificationService;

    @Mock
    private EventRepository eventRepository;

    @InjectMocks
    private ActivityService activityService;

    private User sampleUser;
    private ActivityEntry sampleActivity;
    private OffsetDateTime now;

    @BeforeEach
    void setUp() {
        now = OffsetDateTime.now();

        sampleUser = new User();
        sampleUser.setId(UUID.randomUUID());
        sampleUser.setUsername("activeUser");
        sampleUser.setWeight(70.0);
        sampleUser.setHeight(1.80);
        sampleUser.setGender(Gender.MALE);

        sampleActivity = ActivityEntry.builder()
                .id(1L)
                .activityType(ActivityType.RUNNING)
                .durationInMinutes(30)
                .startedAt(now.minusHours(1))
                .user(sampleUser)
                .caloriesBurned(300.0)
                .build();
    }

    @Test
    void addActivity_validData_returnsEntry() {
        when(userService.findByUsername("activeUser")).thenReturn(sampleUser);
        when(activityRepository.existsByUserIdAndStartedAtBetween(any(), any(), any())).thenReturn(false);
        when(activityRepository.save(any(ActivityEntry.class))).thenReturn(sampleActivity);
        when(activityRepository.findAllByUserIdAndStartedAtBetweenOrderByStartedAt(any(), any(), any())).thenReturn(Collections.emptyList());
        when(activityRepository.findAllByUserIdAndStartedAtBetween(any(), any(), any())).thenReturn(Collections.emptyList());

        ActivityEntry result = activityService.addActivity("activeUser", sampleActivity, true);

        assertNotNull(result);
        verify(activityRepository, times(1)).save(sampleActivity);
        verify(eventRepository, times(1)).save(any(Event.class));
    }

    @Test
    void addActivity_entryAlreadyExists_throwsException() {
        when(userService.findByUsername("activeUser")).thenReturn(sampleUser);
        when(activityRepository.existsByUserIdAndStartedAtBetween(any(), any(), any())).thenReturn(true);

        assertThrows(ActivityEntryAlreadyExistsException.class, () ->
                activityService.addActivity("activeUser", sampleActivity, true));

        verify(activityRepository, never()).save(any());
        verify(eventRepository, never()).save(any());
    }

    @Test
    void deleteActivity_validData_deletesEntry() {
        when(userService.findByUsername("activeUser")).thenReturn(sampleUser);
        when(activityRepository.findByIdAndUserId(1L, sampleUser.getId())).thenReturn(Optional.of(sampleActivity));

        activityService.deleteActivity("activeUser", 1L, true);

        verify(activityRepository, times(1)).delete(sampleActivity);
        verify(eventRepository, times(1)).save(any(Event.class));
    }

    @Test
    void deleteActivity_entryDoesNotExist_throwsException() {
        when(userService.findByUsername("activeUser")).thenReturn(sampleUser);
        when(activityRepository.findByIdAndUserId(99L, sampleUser.getId())).thenReturn(Optional.empty());
        assertThrows(ActivityEntryNotFoundException.class, () ->
                activityService.deleteActivity("activeUser", 99L, false));

        verify(activityRepository, never()).delete(any());
    }

    @Test
    void getDailySteps_validData_returnsSteps() {
        when(userService.findByUsername("activeUser")).thenReturn(sampleUser);
        ActivityEntry walking = ActivityEntry.builder()
                .activityType(ActivityType.WALKING)
                .durationInMinutes(10)
                .startedAt(now.minusHours(5))
                .build();
        ActivityEntry running = ActivityEntry.builder()
                .activityType(ActivityType.RUNNING)
                .durationInMinutes(20)
                .startedAt(now.minusHours(3))
                .build();
        when(activityRepository.findAllByUserIdAndStartedAtBetweenOrderByStartedAt(any(), any(), any()))
                .thenReturn(List.of(walking, running));

        int totalSteps = activityService.getDailySteps("activeUser");

        assertEquals(5020, totalSteps);
    }

    @Test
    void getActivitySummaryLast7Days_validData_returnGroupedSummary() {
        when(userService.findByUsername("activeUser")).thenReturn(sampleUser);
        LocalDate today = now.toLocalDate();
        ActivityEntry act1 = ActivityEntry.builder().startedAt(now).durationInMinutes(30).caloriesBurned(250.0).build();
        ActivityEntry act2 = ActivityEntry.builder().startedAt(now).durationInMinutes(45).caloriesBurned(350.0).build();
        when(activityRepository.findAllByUserIdAndStartedAtBetweenOrderByStartedAt(any(), any(), any()))
                .thenReturn(List.of(act1, act2));

        Map<LocalDate, ActivitySummary> result = activityService.getActivitySummaryLast7Days("activeUser");

        assertNotNull(result);
        assertTrue(result.containsKey(today));
        ActivitySummary summary = result.get(today);
        assertEquals(75, summary.durationInMinutes()); // 30 + 45
        assertEquals(600.0, summary.caloriesBurned(), 0.001); // 250 + 350
    }

    @Test
    void addActivity_validData_sendsNotification() {
        when(userService.findByUsername("activeUser")).thenReturn(sampleUser);
        when(activityRepository.existsByUserIdAndStartedAtBetween(any(), any(), any())).thenReturn(false);
        when(activityRepository.save(any(ActivityEntry.class))).thenReturn(sampleActivity);
        ActivityEntry weeklyActivity = ActivityEntry.builder().durationInMinutes(30).startedAt(now).build();
        when(activityRepository.findAllByUserIdAndStartedAtBetweenOrderByStartedAt(any(), any(), any()))
                .thenReturn(List.of(weeklyActivity));
        ActivityEntry yearlyActivity = ActivityEntry.builder().durationInMinutes(20000).build();
        when(activityRepository.findAllByUserIdAndStartedAtBetween(any(), any(), any()))
                .thenReturn(List.of(yearlyActivity));

        activityService.addActivity("activeUser", sampleActivity, true);

        verify(notificationService, times(1))
                .addNotification(eq(NotificationType.ACTIVITY), anyString(), any(), eq(sampleUser));
    }

    @Test
    void getPDFReport_validData_generatesPDF() throws IOException {
        when(userService.findByUsername("activeUser")).thenReturn(sampleUser);
        when(activityRepository.findAllByUserIdAndStartedAtBetweenOrderByStartedAt(any(), any(), any()))
                .thenReturn(List.of(sampleActivity));
        HttpServletResponse response = mock(HttpServletResponse.class);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        when(response.getOutputStream()).thenReturn(new jakarta.servlet.ServletOutputStream() {
            @Override public boolean isReady() { return true; }
            @Override public void setWriteListener(jakarta.servlet.WriteListener writeListener) {}
            @Override public void write(int b) { outputStream.write(b); }
        });

        assertDoesNotThrow(() -> activityService.getPDFReport("activeUser", response));
        verify(response, times(1)).setContentType("application/pdf");
        assertTrue(outputStream.size() > 0, "PDF z raportem aktywności nie powinien być pusty");
    }

    @Test
    void getPDFReport_streamException_throwsException() throws IOException {
        when(userService.findByUsername("activeUser")).thenReturn(sampleUser);
        when(activityRepository.findAllByUserIdAndStartedAtBetweenOrderByStartedAt(any(), any(), any()))
                .thenReturn(List.of(sampleActivity));
        HttpServletResponse response = mock(HttpServletResponse.class);
        when(response.getOutputStream()).thenThrow(new IOException("Połączenie przerwane"));

        assertThrows(PDFCreationException.class, () -> activityService.getPDFReport("activeUser", response));
    }
}
