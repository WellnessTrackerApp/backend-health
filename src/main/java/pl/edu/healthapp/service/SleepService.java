package pl.edu.healthapp.service;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;

import com.itextpdf.layout.properties.UnitValue;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Service;
import pl.edu.healthapp.exception.PDFCreationException;
import pl.edu.healthapp.exception.SleepEntryAlreadyExistsException;
import pl.edu.healthapp.exception.SleepEntryNotFoundException;

import pl.edu.healthapp.mapper.SleepMapper;
import pl.edu.healthapp.model.*;
import pl.edu.healthapp.repository.EventRepository;
import pl.edu.healthapp.repository.SleepRepository;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class SleepService {
    private final SleepRepository sleepRepository;
    private final UserService userService;
    private final NotificationService notificationService;
    private final EventRepository eventRepository;

    public SleepService(SleepRepository sleepRepository, UserService userService, NotificationService notificationService, EventRepository eventRepository){
        this.sleepRepository = sleepRepository;
        this.userService = userService;
        this.notificationService = notificationService;
        this.eventRepository = eventRepository;
    }

    public SleepEntry addSleep(String username,
                               SleepEntry sleep,
                               boolean saveEvent){
        User user = userService.findByUsername(username);

        if (sleepRepository.existsByUserIdAndSleepStartGreaterThanEqualAndSleepEndLessThanEqual(user.getId(), sleep.getSleepStart(), sleep.getSleepEnd()) ||
                sleepRepository.existsByUserIdAndSleepStartLessThanEqualAndSleepEndGreaterThanEqual(user.getId(), sleep.getSleepStart(), sleep.getSleepEnd()))
            throw new SleepEntryAlreadyExistsException("Sleep in this timeslot already exists for user: " + user.getUsername());

        sleep.setUser(user);
        sleepRepository.save(sleep);
        if (saveEvent) {
            sendNotificationIfSleepInsufficient(user);
            saveEvent(user, EventType.ADD_SLEEP, sleep);
        }

        return sleep;
    }

    public void deleteSleep(String username, Long sleepId, boolean saveEvent){
        User user = userService.findByUsername(username);

        SleepEntry sleep = sleepRepository.findByIdAndUserId(sleepId, user.getId())
                .orElseThrow(() -> new SleepEntryNotFoundException("Sleep entry " + sleepId + " not found or does not belong to user " + username));

        sleepRepository.delete(sleep);

        if (saveEvent)
            saveEvent(user, EventType.DELETE_SLEEP, sleep);
    }

    public Long getDailySleepDurationInHours(String username){
        List<SleepEntry> history = getDailySleepHistory(username);
        return history.stream().mapToLong(sleep -> sleep.durationInMinutes() / 60).sum();
    }

    public SleepQuality getYearlyMedianSleepQuality(String username){
        List<SleepEntry> history = getYearlySleepHistory(username);
        return medianSleepQuality(history);
    }

    public SleepQuality getMonthlyMedianSleepQuality(String username){
        List<SleepEntry> history = getMonthlySleepHistory(username);
        return medianSleepQuality(history);
    }

    public SleepQuality getWeeklyMedianSleepQuality(String username){
        List<SleepEntry> history = getWeeklySleepHistory(username);
        return medianSleepQuality(history);
    }

    public SleepQuality getDailyMedianSleepQuality(String username){
        List<SleepEntry> history = getDailySleepHistory(username);
        return medianSleepQuality(history);
    }

    public Double getYearlyMedianSleepDuration(String username){
        List<SleepEntry> history = getYearlySleepHistory(username);
        return medianSleepDuration(history);
    }

    public Double getMonthlyMedianSleepDuration(String username){
        List<SleepEntry> history = getMonthlySleepHistory(username);
        return medianSleepDuration(history);
    }

    public Double getWeeklyMedianSleepDuration(String username){
        List<SleepEntry> history = getWeeklySleepHistory(username);
        return medianSleepDuration(history);
    }

    public Double getDailyMedianSleepDuration(String username){
        List<SleepEntry> history = getDailySleepHistory(username);
        return medianSleepDuration(history);
    }


    public List<SleepEntry> getYearlySleepHistory(String username){
        User user = userService.findByUsername(username);

        OffsetDateTime now = OffsetDateTime.now();
        OffsetDateTime start = now.minusYears(1);
        return sleepRepository.findAllByUserIdAndSleepEndBetween(user.getId(), start, now);
    }

    public List<SleepEntry> getMonthlySleepHistory(String username){
        User user = userService.findByUsername(username);

        OffsetDateTime now = OffsetDateTime.now();
        OffsetDateTime start = now.minusMonths(1);
        return sleepRepository.findAllByUserIdAndSleepEndBetween(user.getId(), start, now);
    }

    public List<SleepEntry> getWeeklySleepHistory(String username){
        User user = userService.findByUsername(username);

        OffsetDateTime now = OffsetDateTime.now();
        OffsetDateTime start = now.minusWeeks(1);
        return sleepRepository.findAllByUserIdAndSleepEndBetween(user.getId(), start, now);
    }

    public List<SleepEntry> getDailySleepHistory(String username){
        User user = userService.findByUsername(username);

        OffsetDateTime now = OffsetDateTime.now();
        OffsetDateTime start = now.minusDays(1);
        return sleepRepository.findAllByUserIdAndSleepEndBetween(user.getId(), start, now);
    }

    public Map<LocalDate, Long> getSleepDurationLast7Days(String username) {
        return getWeeklySleepHistory(username)
                .stream()
                .collect(Collectors.groupingBy(
                        e -> e.getSleepStart().toLocalDate(),
                        Collectors.summingLong(SleepEntry::durationInMinutes)
                ));
    }

    private SleepQuality medianSleepQuality(List<SleepEntry> history){
        if (history.isEmpty()) {
            throw new SleepEntryNotFoundException("No sleep found for current user");
        }

        var sortedQualities = history
                .stream()
                .mapToInt(sleep -> sleep.getQuality().getRating())
                .sorted();
        return SleepQuality.fromAverageRating(
                history.size() % 2 == 0 ?
                        sortedQualities
                                .skip((history.size() / 2) - 1)
                                .limit(2)
                                .average()
                                .orElseThrow(() -> new SleepEntryNotFoundException("No sleep found for current user")) :
                        sortedQualities
                                .skip(history.size() / 2)
                                .findFirst().orElseThrow(() -> new SleepEntryNotFoundException("No sleep found for current user")));
    }

    private Double medianSleepDuration(List<SleepEntry> history){
        if (history.isEmpty()) {
            throw new SleepEntryNotFoundException("No sleep found for current user");
        }

        var sortedDurations = history
                .stream()
                .mapToDouble(SleepEntry::durationInHours)
                .sorted();
        return history.size() % 2 == 0 ?
                sortedDurations
                        .skip(Math.max((history.size() / 2) - 1, 0))
                        .limit(2)
                        .average()
                        .orElseThrow(() -> new SleepEntryNotFoundException("No sleep found for current user")) :
                sortedDurations
                        .skip(history.size() / 2)
                        .findFirst().orElseThrow(() -> new SleepEntryNotFoundException("No sleep found for current user"));
    }

    private void sendNotificationIfSleepInsufficient(User user) {
        OffsetDateTime now = OffsetDateTime.now();
        OffsetDateTime start = now.truncatedTo(ChronoUnit.DAYS).minusWeeks(1);

        long weeklySleepDurationInMinutes = sleepRepository.findAllByUserIdAndSleepEndBetween(user.getId(), start, now).stream().mapToLong(SleepEntry::durationInMinutes).sum();

        if(weeklySleepDurationInMinutes < 2800)
            notificationService.addNotification(NotificationType.SLEEP, "You’ve slept less than 48 hours this week. Remember, getting enough sleep is essential for your health and recovery. Try to find time to rest in the upcoming week!", now, user);
    }

    public void getPDFReport(String username, HttpServletResponse response){
        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "attachment; filename=sleep_report_" + username + ".pdf");

        try {
            List<SleepEntry> history = getMonthlySleepHistory(username);

            PdfWriter writer = new PdfWriter(response.getOutputStream());
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);

            document.add(new Paragraph("Sleep overview for the last month").setBold().setFontSize(20));
            document.add(new Paragraph("\n"));

            Table table = new Table(UnitValue.createPercentArray(new float[]{20, 20, 20, 20, 20})).useAllAvailableWidth();
            table.addHeaderCell("Date");
            table.addHeaderCell("Quality");
            table.addHeaderCell("Duration");
            table.addHeaderCell("Start");
            table.addHeaderCell("End");

            for (SleepEntry entry : history) {
                table.addCell(entry.getSleepStart().toLocalDate().toString());
                table.addCell(entry.getQuality().name());
                table.addCell(String.valueOf(entry.durationInHours()));
                table.addCell(entry.getSleepStart().toLocalTime().toString());
                table.addCell(entry.getSleepEnd().toLocalTime().toString());
            }

            document.add(table);
            document.close();
        } catch (Exception e) {
            throw new PDFCreationException("Failed to create a sleep report");
        }
    }

    private void saveEvent(User user, EventType eventType, SleepEntry sleep){
        String payload = SleepMapper.toJSON(sleep);
        Event event = Event.builder()
                .entityId(sleep.getId())
                .eventType(eventType)
                .createdAt(OffsetDateTime.now())
                .payload(payload)
                .user(user)
                .build();

        eventRepository.save(event);
    }
}
