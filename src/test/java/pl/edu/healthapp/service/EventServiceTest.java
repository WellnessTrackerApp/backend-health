package pl.edu.healthapp.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.context.SecurityContextHolder;
import pl.edu.healthapp.model.*;
import pl.edu.healthapp.repository.EventRepository;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EventServiceTest {

    @Mock
    private EventRepository eventRepository;

    @Mock
    private UserService userService;

    @Mock
    private SleepService sleepService;

    @Mock
    private DietService dietService;

    @Mock
    private ActivityService activityService;

    @Mock
    private HealthGoalService healthGoalService;

    @InjectMocks
    private EventService eventService;

    private User sampleUser;
    private UUID userId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        sampleUser = new User();
        sampleUser.setId(userId);
        sampleUser.setUsername("undoMaster");
        SecurityContextHolder.clearContext();
    }

    @Test
    void undoLast_validData_deletesSleepEntry() {
        when(userService.findByUsername("undoMaster")).thenReturn(sampleUser);
        Event lastEvent = Event.builder()
                .id(100L)
                .entityId(1L)
                .eventType(EventType.ADD_SLEEP)
                .user(sampleUser)
                .build();
        when(eventRepository.findTopByUserIdOrderByCreatedAtDesc(userId)).thenReturn(Optional.of(lastEvent));

        eventService.undoLast("undoMaster");

        verify(sleepService, times(1)).deleteSleep("undoMaster", 1L, false);
        verify(eventRepository, times(1)).delete(lastEvent);
    }

    @Test
    void undoLast_validData_deletesDietEntry() {
        when(userService.findByUsername("undoMaster")).thenReturn(sampleUser);
        Event lastEvent = Event.builder()
                .id(101L)
                .entityId(2L)
                .eventType(EventType.ADD_MEAL)
                .user(sampleUser)
                .build();
        when(eventRepository.findTopByUserIdOrderByCreatedAtDesc(userId)).thenReturn(Optional.of(lastEvent));

        eventService.undoLast("undoMaster");

        verify(dietService, times(1)).deleteMeal("undoMaster", 2L, false);
        verify(eventRepository, times(1)).delete(lastEvent);
    }

    @Test
    void undoLast_validData_deletesActivityEntry() {
        when(userService.findByUsername("undoMaster")).thenReturn(sampleUser);
        Event lastEvent = Event.builder()
                .id(102L)
                .entityId(3L)
                .eventType(EventType.ADD_ACTIVITY)
                .user(sampleUser)
                .build();
        when(eventRepository.findTopByUserIdOrderByCreatedAtDesc(userId)).thenReturn(Optional.of(lastEvent));

        eventService.undoLast("undoMaster");

        verify(activityService, times(1)).deleteActivity("undoMaster", 3L, false);
        verify(eventRepository, times(1)).delete(lastEvent);
    }

    @Test
    void undoLast_validData_deletesHealthGoal() {
        when(userService.findByUsername("undoMaster")).thenReturn(sampleUser);
        Event lastEvent = Event.builder()
                .id(103L)
                .entityId(4L)
                .eventType(EventType.ADD_HEALTH_GOAL)
                .user(sampleUser)
                .build();
        when(eventRepository.findTopByUserIdOrderByCreatedAtDesc(userId)).thenReturn(Optional.of(lastEvent));

        eventService.undoLast("undoMaster");

        verify(healthGoalService, times(1)).deleteGoalById("undoMaster", 4L);
        verify(eventRepository, times(1)).delete(lastEvent);
    }

    @Test
    void undoLast_validData_deletesUser() {
        when(userService.findByUsername("undoMaster")).thenReturn(sampleUser);
        Event lastEvent = Event.builder()
                .id(104L)
                .eventType(EventType.REGISTER_USER)
                .user(sampleUser)
                .build();
        when(eventRepository.findTopByUserIdOrderByCreatedAtDesc(userId)).thenReturn(Optional.of(lastEvent));

        eventService.undoLast("undoMaster");

        verify(eventRepository, times(1)).delete(lastEvent);
        verify(userService, times(1)).deleteUser("undoMaster");
        assertNull(SecurityContextHolder.getContext().getAuthentication(), "Kontekst bezpieczeństwa powinien być wyczyszczony");
    }

    @Test
    void undoLast_validData_undoUpdateUser() {
        when(userService.findByUsername("undoMaster")).thenReturn(sampleUser);
        Event updateEvent = Event.builder()
                .id(105L)
                .eventType(EventType.UPDATE_USER)
                .user(sampleUser)
                .build();
        Event previousEvent = Event.builder()
                .id(104L)
                .eventType(EventType.REGISTER_USER)
                .payload("{\"username\":\"undoMaster\",\"weight\":80.0,\"height\":1.80}")
                .user(sampleUser)
                .build();
        when(eventRepository.findTopByUserIdOrderByCreatedAtDesc(userId))
                .thenReturn(Optional.of(updateEvent))
                .thenReturn(Optional.of(previousEvent));
        when(eventRepository.findTopByEventTypeAndUserIdOrderByCreatedAtDesc(EventType.REGISTER_USER, userId))
                .thenReturn(Optional.of(previousEvent));

        eventService.undoLast("undoMaster");

        verify(eventRepository, times(1)).delete(updateEvent);
        verify(userService, times(1)).updateUser(eq("undoMaster"), any(), eq(false));
    }

    @Test
    void undoLast_noEvents_throwsException() {
        when(userService.findByUsername("undoMaster")).thenReturn(sampleUser);
        when(eventRepository.findTopByUserIdOrderByCreatedAtDesc(userId)).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class, () -> eventService.undoLast("undoMaster"));
    }

    @Test
    void getWeightHistory_validData_returnsWeightList() {
        when(userService.findByUsername("undoMaster")).thenReturn(sampleUser);
        Event e1 = Event.builder().eventType(EventType.UPDATE_USER).payload("{\"weight\":75.5}").build();
        Event e2 = Event.builder().eventType(EventType.UPDATE_USER).payload("{\"weight\":73.2}").build();
        when(eventRepository.findByEventTypeAndUserIdOrderByCreatedAtAsc(EventType.UPDATE_USER, userId))
                .thenReturn(List.of(e1, e2));

        List<Double> weightHistory = eventService.getWeightHistory("undoMaster");

        assertNotNull(weightHistory);
        assertEquals(2, weightHistory.size());
        assertEquals(75.5, weightHistory.get(0));
        assertEquals(73.2, weightHistory.get(1));
    }

    @Test
    void getHeightHistory_validData_returnsHeightList() {
        when(userService.findByUsername("undoMaster")).thenReturn(sampleUser);
        Event e1 = Event.builder().eventType(EventType.UPDATE_USER).payload("{\"height\":1.80}").build();
        when(eventRepository.findByEventTypeAndUserIdOrderByCreatedAtAsc(EventType.UPDATE_USER, userId))
                .thenReturn(List.of(e1));

        List<Double> heightHistory = eventService.getHeightHistory("undoMaster");

        assertNotNull(heightHistory);
        assertEquals(1, heightHistory.size());
        assertEquals(1.80, heightHistory.get(0));
    }
}
