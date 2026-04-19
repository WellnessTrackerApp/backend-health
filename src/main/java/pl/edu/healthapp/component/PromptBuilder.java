package pl.edu.healthapp.component;

import org.springframework.stereotype.Component;
import pl.edu.healthapp.dto.response.GoalProgress;
import pl.edu.healthapp.dto.response.ActivitySummary;
import pl.edu.healthapp.model.User;
import pl.edu.healthapp.service.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class PromptBuilder {
    private final SleepService sleepService;
    private final ActivityService activityService;
    private final DietService dietService;
    private final HealthGoalService healthGoalService;
    private final UserService userService;

    public PromptBuilder(SleepService sleepService,
                         ActivityService activityService,
                         DietService dietService,
                         HealthGoalService healthGoalService,
                         UserService userService) {
        this.sleepService = sleepService;
        this.activityService = activityService;
        this.dietService = dietService;
        this.healthGoalService = healthGoalService;
        this.userService = userService;
    }

    public String questionPrompt(String question) {
        return """
        You are a health assistant. Answer the following health question for the user:
        %s
        """.formatted(question);
    }

    public String predictionPrompt(String username) {
        User user = userService.findByUsername(username);
        double userWeight = user.getWeight();
        double userHeight = user.getHeight();
        String userGender = user.getGender().name();

        Map<LocalDate, Long> sleep = sleepService.getSleepDurationLast7Days(username);
        Map<LocalDate, Integer> calories = dietService.getCaloriesLast7Days(username);
        Map<LocalDate, Double> proteins = dietService.getProteinsLast7Days(username);
        Map<LocalDate, Double> fats = dietService.getFatsLast7Days(username);
        Map<LocalDate, Double> carbs = dietService.getCarbsLast7Days(username);
        Map<LocalDate, ActivitySummary> activities = activityService.getActivitySummaryLast7Days(username);

        List<GoalProgress> goalsAndProgresses = healthGoalService.getProgressForAllGoals(username);
        return """
    You are a health assistant. You get data from last month.
    User weight (in kilo): %s
    User height (in cm): %s
    User gender: %s

    Sleep history (minutes per day):
    %s

    Calorie intake (kcal per day):
    %s
    
    Proteins intake (grams per day):
    %s
    
    Fats intake (grams per day):
    %s
    
    Carbs intake (grams per day):
    %s

    Physical activity:
    %s
    
    Health goals and current progress for each goal of user:
    %s

    Based on this data, predict health trends based on recent data.
    """.formatted(
                userWeight,
                userHeight,
                userGender,
                formatHistory(sleep),
                formatHistory(calories),
                formatHistory(proteins),
                formatHistory(fats),
                formatHistory(carbs),
                formatActivitySummary(activities),
                formatGoals(goalsAndProgresses)
        );
    }

    public String advicePrompt(String username){
        Map<LocalDate, Long> sleep = sleepService.getSleepDurationLast7Days(username);
        Map<LocalDate, Integer> calories = dietService.getCaloriesLast7Days(username);
        Map<LocalDate, Double> proteins = dietService.getProteinsLast7Days(username);
        Map<LocalDate, Double> fats = dietService.getFatsLast7Days(username);
        Map<LocalDate, Double> carbs = dietService.getCarbsLast7Days(username);
        Map<LocalDate, ActivitySummary> activities = activityService.getActivitySummaryLast7Days(username);
        return """
    You are a health assistant. You get data from last 7 days.

    Sleep history (minutes per day):
    %s

    Calorie intake (kcal per day):
    %s
    
    Proteins intake (grams per day):
    %s
    
    Fats intake (grams per day):
    %s
    
    Carbs intake (grams per day):
    %s

    Physical activity:
    %s

    Based on this data:
    - detect unhealthy patterns
    - assess balance between activity and calories
    - give 2–3 short recommendations
    - be supportive and motivating
    """.formatted(
                formatHistory(sleep),
                formatHistory(calories),
                formatHistory(proteins),
                formatHistory(fats),
                formatHistory(carbs),
                formatActivitySummary(activities)
        );
    }

    private String formatHistory(Map<LocalDate, ? extends Number> data) {
        return data.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(e -> e.getKey() + ": " + e.getValue())
                .collect(Collectors.joining("\n"));
    }

    private String formatActivitySummary(Map<LocalDate, ActivitySummary> data) {
        return data.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(entry -> String.format("%s: %d min, %.1f kcal burned",
                        entry.getKey(),
                        entry.getValue().durationInMinutes(),
                        entry.getValue().caloriesBurned()))
                .collect(Collectors.joining("\n"));
    }

    private String formatGoals(List<GoalProgress> data) {
        return data.stream()
                .map(e -> String.format("%s: %.1f actual progress, %.1f target",
                        e.healthGoalType().name(),
                        e.actual(),
                        e.target()))
                .collect(Collectors.joining("\n"));
    }
}
