package pl.edu.healthapp.service;

import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.edu.healthapp.exception.PDFCreationException;
import pl.edu.healthapp.exception.SleepEntryAlreadyExistsException;
import pl.edu.healthapp.exception.SleepEntryNotFoundException;
import pl.edu.healthapp.model.*;
import pl.edu.healthapp.repository.EventRepository;
import pl.edu.healthapp.repository.SleepRepository;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SleepServiceTest {

    @Mock
    private SleepRepository sleepRepository;

    @Mock
    private UserService userService;

    @Mock
    private NotificationService notificationService;

    @Mock
    private EventRepository eventRepository;

    @InjectMocks
    private SleepService sleepService;

    private User sampleUser;
    private SleepEntry sampleSleep;
    private OffsetDateTime now;

    @BeforeEach
    void setUp() {
        now = OffsetDateTime.now();

        sampleUser = new User();
        sampleUser.setId(UUID.randomUUID());
        sampleUser.setUsername("sleeper123");
        sampleUser.setEmail("sleep@healthapp.edu.pl");
        sampleUser.setGender(Gender.MALE);

        sampleSleep = SleepEntry.builder()
                .id(1L)
                .sleepStart(now.minusHours(8))
                .sleepEnd(now)
                .quality(SleepQuality.GOOD)
                .user(sampleUser)
                .build();
    }

    @Test
    void addSleep_validData_returnsSleepEntry() {
        when(userService.findByUsername("sleeper123")).thenReturn(sampleUser);
        when(sleepRepository.existsByUserIdAndSleepStartGreaterThanEqualAndSleepEndLessThanEqual(any(), any(), any())).thenReturn(false);
        when(sleepRepository.existsByUserIdAndSleepStartLessThanEqualAndSleepEndGreaterThanEqual(any(), any(), any())).thenReturn(false);
        when(sleepRepository.findAllByUserIdAndSleepEndBetween(any(), any(), any())).thenReturn(Collections.emptyList());
        SleepEntry result = sleepService.addSleep("sleeper123", sampleSleep, true);
        assertNotNull(result);
        assertEquals(sampleUser, result.getUser());
        verify(sleepRepository, times(1)).save(sampleSleep);
        verify(notificationService, times(1)).addNotification(eq(NotificationType.SLEEP), anyString(), any(), eq(sampleUser));
        verify(eventRepository, times(1)).save(any(Event.class));
    }

    @Test
    void addSleep_timeOverlaps_throwsException() {
        when(userService.findByUsername("sleeper123")).thenReturn(sampleUser);
        when(sleepRepository.existsByUserIdAndSleepStartGreaterThanEqualAndSleepEndLessThanEqual(any(), any(), any())).thenReturn(true);
        assertThrows(SleepEntryAlreadyExistsException.class, () -> sleepService.addSleep("sleeper123", sampleSleep, true));
        verify(sleepRepository, never()).save(any(SleepEntry.class));
        verify(eventRepository, never()).save(any(Event.class));
    }

    @Test
    void deleteSleep_validData_deletesSleepEntry() {
        when(userService.findByUsername("sleeper123")).thenReturn(sampleUser);
        when(sleepRepository.findByIdAndUserId(1L, sampleUser.getId())).thenReturn(Optional.of(sampleSleep));
        sleepService.deleteSleep("sleeper123", 1L, true);
        verify(sleepRepository, times(1)).delete(sampleSleep);
        verify(eventRepository, times(1)).save(any(Event.class));
    }

    @Test
    void deleteSleep_entryDoesNotExist_throwsException() {
        when(userService.findByUsername("sleeper123")).thenReturn(sampleUser);
        when(sleepRepository.findByIdAndUserId(99L, sampleUser.getId())).thenReturn(Optional.empty());
        assertThrows(SleepEntryNotFoundException.class, () -> sleepService.deleteSleep("sleeper123", 99L, false));
        verify(sleepRepository, never()).delete(any(SleepEntry.class));
    }

    @Test
    void getDailySleepDurationInHours_validData_returnsSleepHoursSumForThisDay() {
        when(userService.findByUsername("sleeper123")).thenReturn(sampleUser);
        SleepEntry sleep1 = SleepEntry.builder().sleepStart(now.minusHours(8)).sleepEnd(now).build();
        SleepEntry sleep2 = SleepEntry.builder().sleepStart(now.minusHours(3)).sleepEnd(now).build();
        when(sleepRepository.findAllByUserIdAndSleepEndBetween(any(), any(), any())).thenReturn(List.of(sleep1, sleep2));
        Long totalHours = sleepService.getDailySleepDurationInHours("sleeper123");
        assertEquals(11L, totalHours);
    }

    @Test
    void getWeeklyMedianSleepQuality_validData_mediaForNotEvenNumOfEntries() {
        when(userService.findByUsername("sleeper123")).thenReturn(sampleUser);
        SleepEntry s1 = SleepEntry.builder().quality(SleepQuality.POOR).build();
        SleepEntry s2 = SleepEntry.builder().quality(SleepQuality.GOOD).build();
        SleepEntry s3 = SleepEntry.builder().quality(SleepQuality.FAIR).build();
        when(sleepRepository.findAllByUserIdAndSleepEndBetween(any(), any(), any())).thenReturn(List.of(s1, s2, s3));
        SleepQuality medianQuality = sleepService.getWeeklyMedianSleepQuality("sleeper123");
        assertNotNull(medianQuality);
    }

    @Test
    void getWeeklyMedianSleepDuration_validData_mediaForEvenNumOfEntries() {
        when(userService.findByUsername("sleeper123")).thenReturn(sampleUser);
        SleepEntry s1 = SleepEntry.builder().sleepStart(now.minusHours(5)).sleepEnd(now).build();
        SleepEntry s2 = SleepEntry.builder().sleepStart(now.minusHours(9)).sleepEnd(now).build();
        SleepEntry s3 = SleepEntry.builder().sleepStart(now.minusHours(6)).sleepEnd(now).build();
        SleepEntry s4 = SleepEntry.builder().sleepStart(now.minusHours(10)).sleepEnd(now).build();
        when(sleepRepository.findAllByUserIdAndSleepEndBetween(any(), any(), any())).thenReturn(List.of(s1, s2, s3, s4));
        Double medianDuration = sleepService.getWeeklyMedianSleepDuration("sleeper123");
        assertEquals(7.5, medianDuration, 0.001);
    }

    @Test
    void medianSleepDuration_noEntries_throwsException() {
        when(userService.findByUsername("sleeper123")).thenReturn(sampleUser);
        when(sleepRepository.findAllByUserIdAndSleepEndBetween(any(), any(), any())).thenReturn(Collections.emptyList());
        assertThrows(SleepEntryNotFoundException.class, () -> sleepService.getDailyMedianSleepDuration("sleeper123"));
    }

    @Test
    void getSleepDurationLast7Days_validData_groupsResultByDays() {
        when(userService.findByUsername("sleeper123")).thenReturn(sampleUser);
        LocalDate today = now.toLocalDate();
        SleepEntry s1 = SleepEntry.builder().sleepStart(now.minusHours(8)).sleepEnd(now).build();
        SleepEntry s2 = SleepEntry.builder().sleepStart(now.minusDays(1).minusHours(4)).sleepEnd(now.minusDays(1)).build();
        when(sleepRepository.findAllByUserIdAndSleepEndBetween(any(), any(), any())).thenReturn(List.of(s1, s2));
        Map<LocalDate, Long> result = sleepService.getSleepDurationLast7Days("sleeper123");
        assertEquals(2, result.size());
        assertEquals(480L, result.get(today));
        assertEquals(240L, result.get(today.minusDays(1)));
    }

    @Test
    void getPDFReport_validData_generatesPDF() throws IOException {
        when(userService.findByUsername("sleeper123")).thenReturn(sampleUser);
        when(sleepRepository.findAllByUserIdAndSleepEndBetween(any(), any(), any())).thenReturn(List.of(sampleSleep));
        HttpServletResponse response = mock(HttpServletResponse.class);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        when(response.getOutputStream()).thenReturn(new jakarta.servlet.ServletOutputStream() {
            @Override public boolean isReady() { return true; }
            @Override public void setWriteListener(jakarta.servlet.WriteListener writeListener) {}
            @Override public void write(int b) { outputStream.write(b); }
        });
        assertDoesNotThrow(() -> sleepService.getPDFReport("sleeper123", response));
        verify(response, times(1)).setContentType("application/pdf");
        assertTrue(outputStream.size() > 0, "PDF nie powinien być pusty");
    }

    @Test
    void getPDFReport_streamException_throwsException() throws IOException {
        when(userService.findByUsername("sleeper123")).thenReturn(sampleUser);
        when(sleepRepository.findAllByUserIdAndSleepEndBetween(any(), any(), any())).thenReturn(List.of(sampleSleep));
        HttpServletResponse response = mock(HttpServletResponse.class);
        when(response.getOutputStream()).thenThrow(new IOException("Stream closed"));
        assertThrows(PDFCreationException.class, () -> sleepService.getPDFReport("sleeper123", response));
    }
}
