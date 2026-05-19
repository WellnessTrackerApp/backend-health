package pl.edu.healthapp.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.edu.healthapp.dto.response.GoalProgress;
import pl.edu.healthapp.exception.HealthGoalNotFoundException;
import pl.edu.healthapp.model.*;
import pl.edu.healthapp.repository.EventRepository;
import pl.edu.healthapp.repository.HealthGoalRepository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HealthGoalServiceTest {

    @Mock
    private DietService dietService;

    @Mock
    private ActivityService activityService;

    @Mock
    private SleepService sleepService;

    @Mock
    private HealthGoalRepository healthGoalRepository;

    @Mock
    private UserService userService;

    @Mock
    private EventRepository eventRepository;

    @InjectMocks
    private HealthGoalService healthGoalService;

    private User sampleUser;
    private HealthGoal sampleGoal;
    private OffsetDateTime now;

    @BeforeEach
    void setUp() {
        now = OffsetDateTime.now();

        sampleUser = new User();
        sampleUser.setId(UUID.randomUUID());
        sampleUser.setUsername("goalGetter");

        sampleGoal = HealthGoal.builder()
                .id(1L)
                .healthGoalType(HealthGoalType.CALORIES)
                .target(2500.0)
                .createdAt(now)
                .user(sampleUser)
                .build();
    }

    @Test
    void setGoal_validData_createsHealthGoal() {
        when(userService.findByUsername("goalGetter")).thenReturn(sampleUser);
        when(healthGoalRepository.findByUserIdAndHealthGoalType(sampleUser.getId(), HealthGoalType.CALORIES))
                .thenReturn(Optional.empty());
        when(healthGoalRepository.save(any(HealthGoal.class))).thenAnswer(invocation -> invocation.getArgument(0));
        
        HealthGoal result = healthGoalService.setGoal("goalGetter", sampleGoal, true);
        
        assertNotNull(result);
        assertEquals(2500.0, result.getTarget());
        assertEquals(sampleUser, result.getUser());
        verify(healthGoalRepository, times(1)).save(any(HealthGoal.class));
        verify(eventRepository, times(1)).save(any(Event.class));
    }

    @Test
    void setGoal_goalAlreadyExists_updatesGoal() {
        when(userService.findByUsername("goalGetter")).thenReturn(sampleUser);
        HealthGoal existingGoal = HealthGoal.builder()
                .id(5L)
                .healthGoalType(HealthGoalType.CALORIES)
                .target(2000.0)
                .user(sampleUser)
                .build();
        when(healthGoalRepository.findByUserIdAndHealthGoalType(sampleUser.getId(), HealthGoalType.CALORIES))
                .thenReturn(Optional.of(existingGoal));
        when(healthGoalRepository.save(any(HealthGoal.class))).thenAnswer(invocation -> invocation.getArgument(0));
        
        HealthGoal newGoalData = HealthGoal.builder()
                .healthGoalType(HealthGoalType.CALORIES)
                .target(3000.0)
                .build();
        
        HealthGoal result = healthGoalService.setGoal("goalGetter", newGoalData, false);
        
        assertNotNull(result);
        assertEquals(5L, result.getId());
        assertEquals(3000.0, result.getTarget());
        verify(eventRepository, never()).save(any(Event.class));
    }

    @Test
    void deleteGoal_validData_deletesHealthGoal() {
        when(userService.findByUsername("goalGetter")).thenReturn(sampleUser);
        when(healthGoalRepository.findByUserIdAndHealthGoalType(sampleUser.getId(), HealthGoalType.CALORIES))
                .thenReturn(Optional.of(sampleGoal));
        
        healthGoalService.deleteGoal("goalGetter", HealthGoalType.CALORIES);
        
        verify(healthGoalRepository, times(1)).delete(sampleGoal);
        verify(eventRepository, times(1)).save(any(Event.class));
    }

    @Test
    void deleteGoal_goalDoesNotExist_throwsException() {
        when(userService.findByUsername("goalGetter")).thenReturn(sampleUser);
        when(healthGoalRepository.findByUserIdAndHealthGoalType(sampleUser.getId(), HealthGoalType.SLEEP))
                .thenReturn(Optional.empty());
        
        assertThrows(HealthGoalNotFoundException.class, () ->
                healthGoalService.deleteGoal("goalGetter", HealthGoalType.SLEEP));
        verify(healthGoalRepository, never()).delete(any());
    }

    @Test
    void deleteGoalById_whenExistsById_deleteWithoutEventCreation() {
        when(userService.findByUsername("goalGetter")).thenReturn(sampleUser);
        when(healthGoalRepository.findByIdAndUserId(1L, sampleUser.getId())).thenReturn(Optional.of(sampleGoal));
        
        healthGoalService.deleteGoalById("goalGetter", 1L);
        
        verify(healthGoalRepository, times(1)).delete(sampleGoal);
        verify(eventRepository, never()).save(any());
    }
    
    @Test
    void getProgress_validData_returnsProgress() {
        when(userService.findByUsername("goalGetter")).thenReturn(sampleUser);
        when(healthGoalRepository.findByUserIdAndHealthGoalType(sampleUser.getId(), HealthGoalType.CALORIES))
                .thenReturn(Optional.of(sampleGoal));
        when(dietService.getDailyCalories("goalGetter")).thenReturn(1850);
        
        GoalProgress progress = healthGoalService.getProgress("goalGetter", HealthGoalType.CALORIES);
        
        assertNotNull(progress);
        assertEquals(1L, progress.goalId());
        assertEquals(2500.0, progress.target());
        assertEquals(1850.0, progress.actual());
        verify(dietService, times(1)).getDailyCalories("goalGetter");
        verifyNoInteractions(sleepService, activityService);
    }

    @Test
    void getProgress_validData_returnsProgressesForSleepGoals() {
        when(userService.findByUsername("goalGetter")).thenReturn(sampleUser);
        HealthGoal sleepGoal = HealthGoal.builder()
                .id(2L)
                .healthGoalType(HealthGoalType.SLEEP)
                .target(8.0)
                .build();
        when(healthGoalRepository.findByUserIdAndHealthGoalType(sampleUser.getId(), HealthGoalType.SLEEP))
                .thenReturn(Optional.of(sleepGoal));
        when(sleepService.getDailySleepDurationInHours("goalGetter")).thenReturn(7L);
        
        GoalProgress progress = healthGoalService.getProgress("goalGetter", HealthGoalType.SLEEP);
        
        assertEquals(7.0, progress.actual());
        verify(sleepService, times(1)).getDailySleepDurationInHours("goalGetter");
        verifyNoInteractions(dietService, activityService);
    }

    @Test
    void getProgress_validData_returnsProgressesForStepGoals() {
        when(userService.findByUsername("goalGetter")).thenReturn(sampleUser);
        HealthGoal stepsGoal = HealthGoal.builder()
                .id(3L)
                .healthGoalType(HealthGoalType.STEPS)
                .target(10000.0)
                .build();
        when(healthGoalRepository.findByUserIdAndHealthGoalType(sampleUser.getId(), HealthGoalType.STEPS))
                .thenReturn(Optional.of(stepsGoal));
        when(activityService.getDailySteps("goalGetter")).thenReturn(8500);
        
        GoalProgress progress = healthGoalService.getProgress("goalGetter", HealthGoalType.STEPS);
        
        assertEquals(8500.0, progress.actual());
        verify(activityService, times(1)).getDailySteps("goalGetter");
    }

    @Test
    void getProgress_validData_returnsProgressesForActivityGoals() {
        when(userService.findByUsername("goalGetter")).thenReturn(sampleUser);
        HealthGoal activityGoal = HealthGoal.builder()
                .id(4L)
                .healthGoalType(HealthGoalType.ACTIVITY)
                .target(150.0)
                .build();
        when(healthGoalRepository.findByUserIdAndHealthGoalType(sampleUser.getId(), HealthGoalType.ACTIVITY))
                .thenReturn(Optional.of(activityGoal));
        when(activityService.getWeeklyActivityInMinutes("goalGetter")).thenReturn(120);
        
        GoalProgress progress = healthGoalService.getProgress("goalGetter", HealthGoalType.ACTIVITY);
        
        assertEquals(120.0, progress.actual());
        verify(activityService, times(1)).getWeeklyActivityInMinutes("goalGetter");
    }

    @Test
    void getProgressForAllGoals_validData_returnsProgressesForAllGoals() {
        when(userService.findByUsername("goalGetter")).thenReturn(sampleUser);
        HealthGoal g1 = HealthGoal.builder().id(1L).healthGoalType(HealthGoalType.CALORIES).target(2000.0).build();
        HealthGoal g2 = HealthGoal.builder().id(2L).healthGoalType(HealthGoalType.SLEEP).target(8.0).build();
        when(healthGoalRepository.findAllByUserId(sampleUser.getId())).thenReturn(List.of(g1, g2));
        when(dietService.getDailyCalories("goalGetter")).thenReturn(1500);
        when(sleepService.getDailySleepDurationInHours("goalGetter")).thenReturn(6L);
        
        List<GoalProgress> results = healthGoalService.getProgressForAllGoals("goalGetter");
        
        assertNotNull(results);
        assertEquals(2, results.size());
        GoalProgress caloriesProgress = results.stream().filter(r -> r.healthGoalType() == HealthGoalType.CALORIES).findFirst().orElseThrow();
        assertEquals(1500.0, caloriesProgress.actual());
        GoalProgress sleepProgress = results.stream().filter(r -> r.healthGoalType() == HealthGoalType.SLEEP).findFirst().orElseThrow();
        assertEquals(6.0, sleepProgress.actual());
    }
}
