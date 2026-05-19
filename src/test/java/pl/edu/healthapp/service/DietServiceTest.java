package pl.edu.healthapp.service;

import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.edu.healthapp.exception.DietEntryNotFoundException;
import pl.edu.healthapp.exception.PDFCreationException;
import pl.edu.healthapp.model.*;
import pl.edu.healthapp.repository.DietRepository;
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
class DietServiceTest {

    @Mock
    private DietRepository dietRepository;

    @Mock
    private UserService userService;

    @Mock
    private NotificationService notificationService;

    @Mock
    private EventRepository eventRepository;

    @InjectMocks
    private DietService dietService;

    private User sampleUser;
    private DietEntry sampleMeal;
    private OffsetDateTime now;

    @BeforeEach
    void setUp() {
        now = OffsetDateTime.now();
        sampleUser = new User();
        sampleUser.setId(UUID.randomUUID());
        sampleUser.setUsername("fitUser");
        sampleUser.setEmail("fit@healthapp.edu.pl");
        sampleUser.setGender(Gender.MALE);
        sampleUser.setWeight(80.0);
        sampleUser.setHeight(1.80);
        sampleUser.setBirthDate(now.minusYears(30));
        Macronutrients macros = new Macronutrients(500, 30.0, 60.0, 15.0);

        sampleMeal = DietEntry.builder()
                .id(1L)
                .mealName("Kurczak z ryżem")
                .macronutrients(macros)
                .eatenAt(now.minusHours(2))
                .user(sampleUser)
                .build();
    }

    @Test
    void addMeal_powinienDodacMealIZapisacEvent_gdySaveEventJestTrue() {
        when(userService.findByUsername("fitUser")).thenReturn(sampleUser);
        when(dietRepository.save(any(DietEntry.class))).thenReturn(sampleMeal);
        when(dietRepository.findAllByUserIdAndEatenAtBetween(any(), any(), any())).thenReturn(Collections.emptyList());

        DietEntry result = dietService.addMeal("fitUser", sampleMeal, true);

        assertNotNull(result);
        assertEquals(sampleUser, result.getUser());
        verify(dietRepository, times(1)).save(sampleMeal);
        verify(eventRepository, times(1)).save(any(Event.class));
        verify(notificationService, atLeastOnce()).addNotification(any(), anyString(), any(), eq(sampleUser));
    }

    @Test
    void addMeal_validData_returnsEntryAndDoesNotSaveEvent() {
        when(userService.findByUsername("fitUser")).thenReturn(sampleUser);
        when(dietRepository.save(any(DietEntry.class))).thenReturn(sampleMeal);

        DietEntry result = dietService.addMeal("fitUser", sampleMeal, false);

        assertNotNull(result);
        verify(dietRepository, times(1)).save(sampleMeal);
        verify(eventRepository, never()).save(any(Event.class));
        verify(notificationService, never()).addNotification(any(), anyString(), any(), any());
    }

    @Test
    void deleteMeal_validData_deletesEntry() {
        when(userService.findByUsername("fitUser")).thenReturn(sampleUser);
        when(dietRepository.findByIdAndUserId(1L, sampleUser.getId())).thenReturn(Optional.of(sampleMeal));

        dietService.deleteMeal("fitUser", 1L, true);

        verify(dietRepository, times(1)).delete(sampleMeal);
        verify(eventRepository, times(1)).save(any(Event.class));
    }

    @Test
    void deleteMeal_entryDoesNotExist_throwsException() {
        when(userService.findByUsername("fitUser")).thenReturn(sampleUser);
        when(dietRepository.findByIdAndUserId(99L, sampleUser.getId())).thenReturn(Optional.empty());

        assertThrows(DietEntryNotFoundException.class, () -> dietService.deleteMeal("fitUser", 99L, false));
        verify(dietRepository, never()).delete(any(DietEntry.class));
    }

    @Test
    void getDailyMacros_validData_returnDailySum() {
        when(userService.findByUsername("fitUser")).thenReturn(sampleUser);
        DietEntry meal1 = DietEntry.builder().macronutrients(new Macronutrients(400, 20.0, 50.0, 10.0)).build();
        DietEntry meal2 = DietEntry.builder().macronutrients(new Macronutrients(600, 40.0, 70.0, 20.0)).build();
        when(dietRepository.findAllByUserIdAndEatenAtBetween(any(), any(), any())).thenReturn(List.of(meal1, meal2));

        Macronutrients totalMacros = dietService.getDailyMacros("fitUser");

        assertNotNull(totalMacros);
        assertEquals(1000, totalMacros.calories());
        assertEquals(60.0, totalMacros.protein(), 0.001);
        assertEquals(120.0, totalMacros.carbs(), 0.001);
        assertEquals(30.0, totalMacros.fat(), 0.001);
    }

    @Test
    void getCaloriesLast7Days_validData_returnsGroupedByDayCalories() {
        when(userService.findByUsername("fitUser")).thenReturn(sampleUser);
        LocalDate today = now.toLocalDate();
        LocalDate yesterday = today.minusDays(1);
        DietEntry d1 = DietEntry.builder().eatenAt(now).macronutrients(new Macronutrients(500, 10, 10, 10)).build();
        DietEntry d2 = DietEntry.builder().eatenAt(now).macronutrients(new Macronutrients(300, 10, 10, 10)).build();
        DietEntry d3 = DietEntry.builder().eatenAt(now.minusDays(1)).macronutrients(new Macronutrients(400, 10, 10, 10)).build();
        when(dietRepository.findAllByUserIdAndEatenAtBetween(any(), any(), any())).thenReturn(List.of(d1, d2, d3));

        Map<LocalDate, Integer> result = dietService.getCaloriesLast7Days("fitUser");

        assertEquals(2, result.size());
        assertEquals(800, result.get(today)); // 500 + 300
        assertEquals(400, result.get(yesterday));
    }

    @Test
    void getProteinsLast7Days_validData_returnsGroupedByDayProteins() {
        when(userService.findByUsername("fitUser")).thenReturn(sampleUser);
        LocalDate today = now.toLocalDate();
        DietEntry d1 = DietEntry.builder().eatenAt(now).macronutrients(new Macronutrients(0, 25.5, 0, 0)).build();
        when(dietRepository.findAllByUserIdAndEatenAtBetween(any(), any(), any())).thenReturn(List.of(d1));
        Map<LocalDate, Double> result = dietService.getProteinsLast7Days("fitUser");

        assertEquals(25.5, result.get(today), 0.001);
    }

    @Test
    void addMeal_validData_sendsNotification() {
        when(userService.findByUsername("fitUser")).thenReturn(sampleUser);
        when(dietRepository.save(any(DietEntry.class))).thenReturn(sampleMeal);
        DietEntry tinyMeal = DietEntry.builder().macronutrients(new Macronutrients(10, 1.0, 1.0, 1.0)).build();
        when(dietRepository.findAllByUserIdAndEatenAtBetween(any(), any(), any())).thenReturn(List.of(tinyMeal));

        dietService.addMeal("fitUser", sampleMeal, true);

        verify(notificationService, times(1)).addNotification(eq(NotificationType.LOW_KCAL), anyString(), any(), eq(sampleUser));
        verify(notificationService, times(1)).addNotification(eq(NotificationType.LOW_PROTEIN), anyString(), any(), eq(sampleUser));
        verify(notificationService, times(1)).addNotification(eq(NotificationType.LOW_CARBS), anyString(), any(), eq(sampleUser));
        verify(notificationService, times(1)).addNotification(eq(NotificationType.LOW_FAT), anyString(), any(), eq(sampleUser));
    }

    @Test
    void getPDFReport_validData_generatesPDF() throws IOException {
        when(userService.findByUsername("fitUser")).thenReturn(sampleUser);
        when(dietRepository.findAllByUserIdAndEatenAtBetweenOrderByEatenAt(any(), any(), any())).thenReturn(List.of(sampleMeal));
        HttpServletResponse response = mock(HttpServletResponse.class);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        when(response.getOutputStream()).thenReturn(new jakarta.servlet.ServletOutputStream() {
            @Override public boolean isReady() { return true; }
            @Override public void setWriteListener(jakarta.servlet.WriteListener writeListener) {}
            @Override public void write(int b) { outputStream.write(b); }
        });


        assertDoesNotThrow(() -> dietService.getPDFReport("fitUser", response));
        verify(response, times(1)).setContentType("application/pdf");
        assertTrue(outputStream.size() > 0, "Wygenerowany PDF z dietą nie powinien być pusty");
    }

    @Test
    void getPDFReport_streamException_throwsException() throws IOException {
        when(userService.findByUsername("fitUser")).thenReturn(sampleUser);
        when(dietRepository.findAllByUserIdAndEatenAtBetweenOrderByEatenAt(any(), any(), any())).thenReturn(List.of(sampleMeal));

        HttpServletResponse response = mock(HttpServletResponse.class);
        when(response.getOutputStream()).thenThrow(new IOException("Błąd I/O generatora"));

        assertThrows(PDFCreationException.class, () -> dietService.getPDFReport("fitUser", response));
    }
}
