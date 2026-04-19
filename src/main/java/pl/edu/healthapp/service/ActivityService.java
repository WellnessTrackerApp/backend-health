package pl.edu.healthapp.service;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.UnitValue;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Service;
import pl.edu.healthapp.dto.response.ActivitySummary;
import pl.edu.healthapp.exception.ActivityEntryAlreadyExistsException;
import pl.edu.healthapp.exception.ActivityEntryNotFoundException;
import pl.edu.healthapp.exception.PDFCreationException;
import pl.edu.healthapp.mapper.ActivityMapper;
import pl.edu.healthapp.model.*;
import pl.edu.healthapp.repository.ActivityRepository;
import pl.edu.healthapp.repository.EventRepository;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.Period;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ActivityService {
    private final ActivityRepository activityRepository;
    private final UserService userService;
    private final NotificationService notificationService;
    private final EventRepository eventRepository;

    public ActivityService(ActivityRepository activityRepository, UserService userService, NotificationService notificationService, EventRepository eventRepository){
        this.activityRepository = activityRepository;
        this.userService = userService;
        this.notificationService = notificationService;
        this.eventRepository = eventRepository;
    }

    public ActivityEntry addActivity(String username,
                                     ActivityEntry activity,
                                     boolean saveEvent){
        User user = userService.findByUsername(username);

        OffsetDateTime startOfActivity = activity.getStartedAt();
        OffsetDateTime endOfActivity = activity.getStartedAt().plusMinutes(activity.getDurationInMinutes());
        if (activityRepository.existsByUserIdAndStartedAtBetween(user.getId(), startOfActivity, endOfActivity))
            throw new ActivityEntryAlreadyExistsException("Activity entry for user "+ user.getUsername() + " started at " + activity.getStartedAt().toString() + " already exists.");

        activity.setUser(user);
        activity.setCaloriesBurned(
                activity.getActivityType()
                        .caloriesBurned(activity.getDurationInMinutes(), user.getWeight()));

        activity = activityRepository.save(activity);

        if (saveEvent) {
            sendNotificationIfWeeklyActivityLevelDropped(user);
            saveEvent(user, EventType.ADD_ACTIVITY, activity);
        }


        return activity;
    }

    public void deleteActivity(String username, Long activityId, boolean saveEvent){
        User user = userService.findByUsername(username);

        ActivityEntry activity = activityRepository.findByIdAndUserId(activityId, user.getId())
                                                            .orElseThrow(() -> new ActivityEntryNotFoundException("Activity entry " + activityId + " not found or does not belong to user " + username));

        activityRepository.delete(activity);

        if (saveEvent)
            saveEvent(user, EventType.DELETE_ACTIVITY, activity);
    }

    public List<ActivityEntry> getDailyActivities(String username){
        User user = userService.findByUsername(username);

        OffsetDateTime now = OffsetDateTime.now();
        OffsetDateTime start = now.minusDays(1);
        return activityRepository.findAllByUserIdAndStartedAtBetweenOrderByStartedAt(user.getId(), start, now);
    }

    public List<ActivityEntry> getWeeklyActivities(String username){
        User user = userService.findByUsername(username);

        OffsetDateTime now = OffsetDateTime.now();
        OffsetDateTime start = now.minusWeeks(1);
        return activityRepository.findAllByUserIdAndStartedAtBetweenOrderByStartedAt(user.getId(), start, now);
    }

    public List<ActivityEntry> getMonthlyActivities(String username){
        User user = userService.findByUsername(username);

        OffsetDateTime now = OffsetDateTime.now();
        OffsetDateTime start = now.minusMonths(1);
        return activityRepository.findAllByUserIdAndStartedAtBetweenOrderByStartedAt(user.getId(), start, now);
    }

    public List<ActivityEntry> getYearlyActivities(String username){
        User user = userService.findByUsername(username);

        OffsetDateTime now = OffsetDateTime.now();
        OffsetDateTime start = now.minusYears(1);
        return activityRepository.findAllByUserIdAndStartedAtBetweenOrderByStartedAt(user.getId(), start, now);
    }

    public int getWeeklyActivityInMinutes(String username){
        List<ActivityEntry> activities = getWeeklyActivities(username);
        return activities.stream()
                         .mapToInt(ActivityEntry::getDurationInMinutes)
                         .sum();
    }

    public int getDailyCaloriesBurnt(String username){
       List<ActivityEntry> dailyActivity = getDailyActivities(username);
        return (int) dailyActivity
                .stream()
                .mapToDouble(ActivityEntry::getCaloriesBurned)
                .sum();
    }

    public int getDailySteps(String username) {
        User user = userService.findByUsername(username);

        List<ActivityEntry> dailyActivity = getDailyActivities(username);
        double stepLength = user.getStepLength();
        double dailyWalkDistance = dailyActivity
                .stream()
                .filter(activity -> activity.getActivityType() == ActivityType.WALKING)
                .mapToDouble(activity -> activity.getDurationInMinutes() * 75)
                .sum();

        double dailyRunDistance = dailyActivity
                .stream()
                .filter(activity -> activity.getActivityType() == ActivityType.RUNNING)
                .mapToDouble(activity -> (double) activity.getDurationInMinutes() * 150)
                .sum();

        return (int) ((dailyWalkDistance + dailyRunDistance) / stepLength);
    }

    public Map<LocalDate, ActivitySummary> getActivitySummaryLast7Days(String username) {
       return getWeeklyActivities(username)
                .stream()
                .collect(Collectors.groupingBy(
                        a -> a.getStartedAt().toLocalDate(),
                        Collectors.collectingAndThen(
                                Collectors.toList(),
                                this::summarizeDay
                        )
                ));
    }

    public void getPDFReport(String username, HttpServletResponse response){
        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "attachment; filename=activity_report_" + username + ".pdf");

        try {
            List<ActivityEntry> history = getMonthlyActivities(username);

            PdfWriter writer = new PdfWriter(response.getOutputStream());
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);

            document.add(new Paragraph("Activity overview for the last month").setBold().setFontSize(20));
            document.add(new Paragraph("\n"));

            Table table = new Table(UnitValue.createPercentArray(new float[]{25, 25, 25, 25})).useAllAvailableWidth();
            table.addHeaderCell("Date");
            table.addHeaderCell("Type");
            table.addHeaderCell("Duration(in min)");
            table.addHeaderCell("Calories Burned");

            for (ActivityEntry entry : history) {
                table.addCell(entry.getStartedAt().toLocalDate().toString());
                table.addCell(entry.getActivityType().name());
                table.addCell(String.valueOf(entry.getDurationInMinutes()));
                table.addCell(String.valueOf(entry.getCaloriesBurned()));
            }

            document.add(table);
            document.close();
        } catch (Exception e) {
            throw new PDFCreationException("Failed to create an activity report");
        }
    }

    private ActivitySummary summarizeDay(List<ActivityEntry> activities){
        int durationInMinutes = activities.stream()
                .mapToInt(ActivityEntry::getDurationInMinutes)
                .sum();

        double caloriesBurned = activities.stream()
                .mapToDouble(ActivityEntry::getCaloriesBurned)
                .sum();

        return new ActivitySummary(durationInMinutes, caloriesBurned);
    }

    private int getMedianYearlyActivityInMinutesPerWeek(String username){
        User user = userService.findByUsername(username);

        OffsetDateTime now = OffsetDateTime.now();
        OffsetDateTime start = now.minusYears(1);
        OffsetDateTime end = now.minusWeeks(1);
        List<ActivityEntry> activities =  activityRepository.findAllByUserIdAndStartedAtBetween(user.getId(), start, end);
        int days = Period.between(start.toLocalDate(), end.toLocalDate()).getDays();
        return activities.stream().mapToInt(ActivityEntry::getDurationInMinutes).sum() * 7 / days;
    }

    private int lastWeekActivityInMinutes(String username){
        List<ActivityEntry> activities = getWeeklyActivities(username);
        return activities.stream().mapToInt(ActivityEntry::getDurationInMinutes).sum();
    }

    private void sendNotificationIfWeeklyActivityLevelDropped(User user){
        int weeklyActivityInMinutes = lastWeekActivityInMinutes(user.getUsername());
        int lastYearMedianWeeklyActivityInMinutes = getMedianYearlyActivityInMinutesPerWeek(user.getUsername());
        if(weeklyActivityInMinutes < lastYearMedianWeeklyActivityInMinutes)
            notificationService.addNotification(NotificationType.ACTIVITY, "Your activity level dropped significantly this week compared to your usual routine. Consider adding some light activity to stay on track.", OffsetDateTime.now(), user);
    }

    private void saveEvent(User user, EventType eventType, ActivityEntry activity){
        String payload = ActivityMapper.toJSON(activity);
        Event event = Event.builder()
                .entityId(activity.getId())
                .eventType(eventType)
                .createdAt(OffsetDateTime.now())
                .payload(payload)
                .user(user)
                .build();

        eventRepository.save(event);
    }
}
