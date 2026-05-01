package pl.edu.healthapp.service;

import org.springframework.stereotype.Service;
import pl.edu.healthapp.dto.response.GoalProgress;
import pl.edu.healthapp.exception.HealthGoalNotFoundException;
import pl.edu.healthapp.mapper.HealthGoalMapper;
import pl.edu.healthapp.model.*;
import pl.edu.healthapp.repository.EventRepository;
import pl.edu.healthapp.repository.HealthGoalRepository;

import java.time.OffsetDateTime;
import java.util.List;

@Service
public class HealthGoalService {
    private final DietService dietService;
    private final ActivityService activityService;
    private final SleepService sleepService;
    private final HealthGoalRepository healthGoalRepository;
    private final UserService userService;
    private final EventRepository eventRepository;

    public HealthGoalService(DietService dietService, ActivityService activityService, SleepService sleepService, HealthGoalRepository healthGoalRepository, UserService userService, EventRepository eventRepository){
        this.dietService = dietService;
        this.activityService = activityService;
        this.sleepService = sleepService;
        this.healthGoalRepository = healthGoalRepository;
        this.userService = userService;
        this.eventRepository = eventRepository;
    }

    public HealthGoal setGoal(String username,
                              HealthGoal newHealthGoal,
                              boolean saveEvent){
        User user = userService.findByUsername(username);

        HealthGoal healthGoal =  healthGoalRepository.findByUserIdAndHealthGoalType(user.getId(), newHealthGoal.getHealthGoalType())
                        .orElse(newHealthGoal);
        healthGoal.setTarget(newHealthGoal.getTarget());
        healthGoal.setUser(user);

        healthGoal = healthGoalRepository.save(healthGoal);

        if(saveEvent)
            saveEvent(user, EventType.ADD_HEALTH_GOAL, healthGoal);

        return healthGoal;
    }

    public void deleteGoal(String username, HealthGoalType healthGoalType){
        User user = userService.findByUsername(username);

        HealthGoal healthGoal = healthGoalRepository.findByUserIdAndHealthGoalType(user.getId(), healthGoalType)
                .orElseThrow(() -> new HealthGoalNotFoundException("Health goal " + healthGoalType + " not found or does not belong to user " + user.getId()));

        healthGoalRepository.delete(healthGoal);

        saveEvent(user, EventType.DELETE_HEALTH_GOAL, healthGoal);
    }

    protected void deleteGoalById(String username, Long id){
        User user = userService.findByUsername(username);

        HealthGoal healthGoal = healthGoalRepository.findByIdAndUserId(id, user.getId())
                .orElseThrow(() -> new HealthGoalNotFoundException("Health goal " + id + " not found or does not belong to user " + user.getId()));

        healthGoalRepository.delete(healthGoal);
    }

    public List<HealthGoal> getGoals(String username){
        User user = userService.findByUsername(username);

        return healthGoalRepository.findAllByUserId(user.getId());
    }

    public GoalProgress getProgress(String username, HealthGoalType healthGoalType){
        User user = userService.findByUsername(username);

        HealthGoal healthGoal = healthGoalRepository.findByUserIdAndHealthGoalType(user.getId(), healthGoalType)
                .orElseThrow(() -> new HealthGoalNotFoundException("Health goal " + healthGoalType + " not found or does not belong to user " + user.getId()));

        return GoalProgress.builder()
                .goalId(healthGoal.getId())
                .healthGoalType(healthGoalType)
                .target(healthGoal.getTarget())
                .actual(calculateActual(username, healthGoal))
                .build();
    }

    public List<GoalProgress> getProgressForAllGoals(String username){
        User user = userService.findByUsername(username);

        return healthGoalRepository.findAllByUserId(user.getId())
                                .stream()
                                .map(goal -> GoalProgress.builder()
                                                            .goalId(goal.getId())
                                                            .healthGoalType(goal.getHealthGoalType())
                                                            .target(goal.getTarget())
                                                            .actual(calculateActual(username, goal))
                                                            .build())
                                .toList();
    }

    private double calculateActual(String username, HealthGoal healthGoal){
        HealthGoalType goalType = healthGoal.getHealthGoalType();

        return switch(goalType){
                case SLEEP -> sleepService.getDailySleepDurationInHours(username);
                case STEPS -> (double) activityService.getDailySteps(username);
                case CALORIES -> dietService.getDailyCalories(username);
                case ACTIVITY -> activityService.getWeeklyActivityInMinutes(username);
            };
    }

    private void saveEvent(User user, EventType eventType, HealthGoal healthGoal){
        String payload = HealthGoalMapper.toJSON(healthGoal);
        Event event = Event.builder()
                .entityId(healthGoal.getId())
                .eventType(eventType)
                .createdAt(OffsetDateTime.now())
                .payload(payload)
                .user(user)
                .build();

        eventRepository.save(event);
    }

}
