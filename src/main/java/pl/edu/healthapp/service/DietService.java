package pl.edu.healthapp.service;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.UnitValue;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Service;
import pl.edu.healthapp.exception.DietEntryNotFoundException;
import pl.edu.healthapp.exception.PDFCreationException;
import pl.edu.healthapp.mapper.DietMapper;
import pl.edu.healthapp.model.*;
import pl.edu.healthapp.repository.DietRepository;
import pl.edu.healthapp.repository.EventRepository;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.Period;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class DietService {
    private final DietRepository dietRepository;
    private final UserService userService;
    private final NotificationService notificationService;
    private final EventRepository eventRepository;

    public DietService(DietRepository dietRepository, UserService userService, NotificationService notificationService,  EventRepository eventRepository){
        this.dietRepository = dietRepository;
        this.userService = userService;
        this.notificationService = notificationService;
        this.eventRepository = eventRepository;
    }

    public DietEntry addMeal(String username,
                             DietEntry dietEntry, boolean saveEvent){
        User user = userService.findByUsername(username);

        dietEntry.setUser(user);

        DietEntry meal = dietRepository.save(dietEntry);

        if (saveEvent) {
            sendNotificationIfInsufficientMacroLastWeek(user);
            saveEvent(user, EventType.ADD_MEAL, meal);
        }

        return meal;
    }

    public void deleteMeal(String username, Long mealId, boolean saveEvent){
        User user = userService.findByUsername(username);

        DietEntry meal = dietRepository.findByIdAndUserId(mealId, user.getId())
                                                .orElseThrow(() -> new DietEntryNotFoundException("Diet entry " + mealId + " not found or does not belong to user " + username));

        dietRepository.delete(meal);

        if (saveEvent)
            saveEvent(user, EventType.DELETE_MEAL, meal);
    }

    public List<DietEntry> getDailyDiet(String username){
        User user = userService.findByUsername(username);

        OffsetDateTime now = OffsetDateTime.now();
        OffsetDateTime start = now.minusDays(1);
        return dietRepository.findAllByUserIdAndEatenAtBetween(user.getId(), start, now);
    }

    public int getDailyCalories(String username){
        List<DietEntry> dailyDietHistory = getDailyDiet(username);
        return dailyDietHistory
                .stream()
                .mapToInt(dietEntry -> dietEntry.getMacronutrients().calories())
                .sum();
    }

    public double getDailyProteins(String username){
        List<DietEntry> dailyDietHistory = getDailyDiet(username);
        return dailyDietHistory
                .stream()
                .mapToDouble(dietEntry -> dietEntry.getMacronutrients().protein())
                .sum();
    }

    public double getDailyCarbs(String username){
        List<DietEntry> dailyDietHistory = getDailyDiet(username);
        return dailyDietHistory
                .stream()
                .mapToDouble(dietEntry -> dietEntry.getMacronutrients().carbs())
                .sum();
    }

    public double getDailyFats(String username){
        List<DietEntry> dailyDietHistory = getDailyDiet(username);
        return dailyDietHistory
                .stream()
                .mapToDouble(dietEntry -> dietEntry.getMacronutrients().fat())
                .sum();
    }

    public Map<LocalDate, Integer> getCaloriesLast7Days(String username) {
        User user = userService.findByUsername(username);

        OffsetDateTime now = OffsetDateTime.now();
        OffsetDateTime start = now.minusWeeks(1);
        return dietRepository.findAllByUserIdAndEatenAtBetween(user.getId(), start, now)
                .stream()
                .collect(Collectors.groupingBy(
                        e -> e.getEatenAt().toLocalDate(),
                        Collectors.summingInt(dietEntry -> dietEntry.getMacronutrients().calories())
                ));
    }

    public Map<LocalDate, Double> getProteinsLast7Days(String username) {
        User user = userService.findByUsername(username);

        OffsetDateTime now = OffsetDateTime.now();
        OffsetDateTime start = now.minusWeeks(1);
        return dietRepository.findAllByUserIdAndEatenAtBetween(user.getId(), start, now)
                .stream()
                .collect(Collectors.groupingBy(
                        e -> e.getEatenAt().toLocalDate(),
                        Collectors.summingDouble(dietEntry -> dietEntry.getMacronutrients().protein())
                ));
    }

    public Map<LocalDate, Double> getFatsLast7Days(String username) {
        User user = userService.findByUsername(username);

        OffsetDateTime now = OffsetDateTime.now();
        OffsetDateTime start = now.minusWeeks(1);
        return dietRepository.findAllByUserIdAndEatenAtBetween(user.getId(), start, now)
                .stream()
                .collect(Collectors.groupingBy(
                        e -> e.getEatenAt().toLocalDate(),
                        Collectors.summingDouble(dietEntry -> dietEntry.getMacronutrients().fat())
                ));
    }

    public Map<LocalDate, Double> getCarbsLast7Days(String username) {
        User user = userService.findByUsername(username);

        OffsetDateTime now = OffsetDateTime.now();
        OffsetDateTime start = now.minusWeeks(1);
        return dietRepository.findAllByUserIdAndEatenAtBetween(user.getId(), start, now)
                .stream()
                .collect(Collectors.groupingBy(
                        e -> e.getEatenAt().toLocalDate(),
                        Collectors.summingDouble(dietEntry -> dietEntry.getMacronutrients().carbs())
                ));
    }

    public void getPDFReport(String username, HttpServletResponse response){
        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "attachment; filename=diet_report_" + username + ".pdf");

        try {
            List<DietEntry> history = monthlyDiet(username);

            PdfWriter writer = new PdfWriter(response.getOutputStream());
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);

            document.add(new Paragraph("Diet overview for the last month").setBold().setFontSize(20));
            document.add(new Paragraph("\n"));

            Table table = new Table(UnitValue.createPercentArray(new float[]{20, 20, 15, 15, 15, 15})).useAllAvailableWidth();
            table.addHeaderCell("Date");
            table.addHeaderCell("Meal Name");
            table.addHeaderCell("Calories");
            table.addHeaderCell("Protein");
            table.addHeaderCell("Carbs");
            table.addHeaderCell("Fat");

            for (DietEntry entry : history) {
                table.addCell(entry.getEatenAt().toLocalDate().toString());
                table.addCell(entry.getMealName());
                table.addCell(String.valueOf(entry.getMacronutrients().calories()));
                table.addCell(String.valueOf(entry.getMacronutrients().protein()));
                table.addCell(String.valueOf(entry.getMacronutrients().carbs()));
                table.addCell(String.valueOf(entry.getMacronutrients().fat()));
            }

            document.add(table);
            document.close();
        } catch (Exception e) {
            throw new PDFCreationException("Failed to create a diet report");
        }
    }

    public Macronutrients getDailyMacros(String username){
        int calories = getDailyCalories(username);
        double proteins = getDailyProteins(username);
        double carbs = getDailyCarbs(username);
        double fats = getDailyFats(username);
        return new Macronutrients(calories, proteins, carbs, fats);
    }

    private List<DietEntry> monthlyDiet(String username){
        User user = userService.findByUsername(username);

        OffsetDateTime now = OffsetDateTime.now();
        OffsetDateTime start = now.minusMonths(1);
        return dietRepository.findAllByUserIdAndEatenAtBetweenOrderByEatenAt(user.getId(), start, now);
    }

    private void sendNotificationIfInsufficientMacroLastWeek(User user){
        Macronutrients weeklyRecommendation = weeklyMacroRecommendation(user);

        OffsetDateTime now = OffsetDateTime.now();
        OffsetDateTime start = now.minusWeeks(1);
        List<DietEntry> dailyDiet = dietRepository.findAllByUserIdAndEatenAtBetween(user.getId(), start, now);

        int calories = dailyDiet.stream()
                .mapToInt(dietEntry -> dietEntry.getMacronutrients().calories()).sum();
        if (calories < weeklyRecommendation.calories())
            notificationService.addNotification(NotificationType.LOW_KCAL, "Low calorie intake detected this week. Try to eat balanced meals to stay energized!", now, user);

        double proteins = dailyDiet.stream()
                                    .mapToDouble(dietEntry -> dietEntry.getMacronutrients().protein()).sum();
        if (proteins < weeklyRecommendation.protein())
            notificationService.addNotification(NotificationType.LOW_PROTEIN, "Protein alert: You’ve consumed less protein than needed this week. Consider adding eggs, beans, or lean meat!", now, user);
        double carbs = dailyDiet.stream()
                .mapToDouble(dietEntry -> dietEntry.getMacronutrients().carbs()).sum();
        if (carbs < weeklyRecommendation.carbs())
            notificationService.addNotification(NotificationType.LOW_CARBS,  "Low carb alert: Include whole grains, fruits, or vegetables in your meals.", now, user);

        double fats = dailyDiet.stream()
                .mapToDouble(dietEntry -> dietEntry.getMacronutrients().fat()).sum();
        if (fats < weeklyRecommendation.fat())
            notificationService.addNotification(NotificationType.LOW_FAT, "Low fat warning: Include sources of healthy fats like nuts, olive oil, or avocado.", now, user);


    }

    private Macronutrients weeklyMacroRecommendation(User user){
        LocalDate now =  OffsetDateTime.now().toLocalDate();
        LocalDate birthDate = user.getBirthDate().toLocalDate();

        int caloriesRecommended = (int) (10 * user.getWeight() + 6.25 * user.getHeight() * 100 - 5 * Period.between(birthDate, now).getYears() + user.getGender().getCalorieNormFactor()) * 7;
        double proteinsRecommended = caloriesRecommended * 0.45 / 4;
        double carbsRecommended = caloriesRecommended * 0.3 / 4;
        double fatRecommended = caloriesRecommended * 0.25 / 9;

        return new Macronutrients(caloriesRecommended, proteinsRecommended, carbsRecommended, fatRecommended);
    }

    private void saveEvent(User user, EventType eventType, DietEntry dietEntry){
        String payload = DietMapper.toJSON(dietEntry);
        Event event = Event.builder()
                .entityId(dietEntry.getId())
                .eventType(eventType)
                .createdAt(OffsetDateTime.now())
                .payload(payload)
                .user(user)
                .build();

        eventRepository.save(event);
    }
}
