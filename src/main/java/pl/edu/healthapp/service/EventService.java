package pl.edu.healthapp.service;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import pl.edu.healthapp.exception.UserNotFoundException;
import pl.edu.healthapp.mapper.*;
import pl.edu.healthapp.model.*;
import pl.edu.healthapp.repository.EventRepository;

import java.util.List;

@Service
public class EventService {
    private final EventRepository eventRepository;
    private final UserService userService;
    private final SleepService sleepService;
    private final DietService dietService;
    private final ActivityService activityService;
    private final HealthGoalService healthGoalService;

    public EventService(EventRepository eventRepository, UserService userService, SleepService sleepService, DietService dietService, ActivityService activityService, HealthGoalService healthGoalService){
        this.eventRepository = eventRepository;
        this.userService = userService;
        this.sleepService = sleepService;
        this.dietService = dietService;
        this.activityService = activityService;
        this.healthGoalService = healthGoalService;
    }

    public void undoLast(String username){
        User user = userService.findByUsername(username);

        Event last = eventRepository
                .findTopByUserIdOrderByCreatedAtDesc(user.getId())
                .orElseThrow();

        switch(last.getEventType()){
            case ADD_SLEEP -> sleepService.deleteSleep(username, last.getEntityId(), false);
            case ADD_MEAL -> dietService.deleteMeal(username, last.getEntityId(), false);
            case ADD_ACTIVITY -> activityService.deleteActivity(username, last.getEntityId(), false);
            case ADD_HEALTH_GOAL -> healthGoalService.deleteGoalById(username, last.getEntityId());
            case REGISTER_USER -> {
                eventRepository.delete(last);
                userService.deleteUser(username);
                SecurityContextHolder.clearContext();
                return;
            }
            case DELETE_SLEEP ->  sleepService.addSleep(username, SleepMapper.fromJSON(last.getPayload()), false);
            case DELETE_MEAL -> dietService.addMeal(username, DietMapper.fromJSON(last.getPayload()), false);
            case DELETE_ACTIVITY -> activityService.addActivity(username, ActivityMapper.fromJSON(last.getPayload()), false);
            case DELETE_HEALTH_GOAL -> healthGoalService.setGoal(username, HealthGoalMapper.fromJSON(last.getPayload()), false);
            case UPDATE_USER -> {
                eventRepository.delete(last);

                last = eventRepository
                        .findTopByUserIdOrderByCreatedAtDesc(user.getId())
                        .orElse(eventRepository.findTopByEventTypeAndUserIdOrderByCreatedAtDesc(EventType.REGISTER_USER, user.getId())
                                                .orElseThrow(() -> new UserNotFoundException("username", username)));

                userService.updateUser(username, UserMapper.fromJSON(last.getPayload()), false);
                return;
            }
            default -> {}
        }

        eventRepository.delete(last);
    }

    public List<Double> getWeightHistory(String username) {
        User user = userService.findByUsername(username);

        List<Event> events = eventRepository.findByEventTypeAndUserIdOrderByCreatedAtAsc(EventType.UPDATE_USER, user.getId());


        return events.stream()
                .mapToDouble(event -> UserMapper.fromJSON(event.getPayload()).weight())
                .boxed()
                .toList();
    }

    public List<Double> getHeightHistory(String username) {
        User user = userService.findByUsername(username);

        List<Event> events = eventRepository.findByEventTypeAndUserIdOrderByCreatedAtAsc(EventType.UPDATE_USER, user.getId());


        return events.stream()
                .mapToDouble(event -> UserMapper.fromJSON(event.getPayload()).height())
                .boxed()
                .toList();
    }
}
