package pl.edu.healthapp.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import pl.edu.healthapp.dto.request.UserUpdateDTO;
import pl.edu.healthapp.exception.UserAlreadyExistsException;
import pl.edu.healthapp.exception.UserNotFoundException;
import pl.edu.healthapp.model.Event;
import pl.edu.healthapp.model.EventType;
import pl.edu.healthapp.model.Gender;
import pl.edu.healthapp.model.User;
import pl.edu.healthapp.repository.EventRepository;
import pl.edu.healthapp.repository.UserRepository;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private EventRepository eventRepository;

    @InjectMocks
    private UserService userService;

    private User sampleUser;

    @BeforeEach
    void setUp() {
        sampleUser = new User();
        sampleUser.setId(UUID.randomUUID());
        sampleUser.setUsername("testuser");
        sampleUser.setEmail("test@healthapp.edu.pl");
        sampleUser.setPassword("plainPassword123");
        sampleUser.setBirthDate(OffsetDateTime.now().minusYears(25));
        sampleUser.setHeight(180.0);
        sampleUser.setWeight(75.0);
        sampleUser.setGender(Gender.MALE);
    }

    @Test
    void registerUser_validData_returnUser() {
        when(userRepository.existsByUsername(sampleUser.getUsername())).thenReturn(false);
        when(userRepository.existsByEmail(sampleUser.getEmail())).thenReturn(false);
        when(passwordEncoder.encode("plainPassword123")).thenReturn("hashedPasswordXYZ");
        when(userRepository.save(any(User.class))).thenReturn(sampleUser);
        User result = userService.registerUser(sampleUser);
        assertNotNull(result);
        assertEquals("hashedPasswordXYZ", result.getPassword());
        verify(userRepository, times(1)).save(sampleUser);
        verify(eventRepository, times(1)).save(any(Event.class));
    }

    @Test
    void registerUser_usernameAlreadyUsed_throwsException() {
        when(userRepository.existsByUsername(sampleUser.getUsername())).thenReturn(true);
        assertThrows(UserAlreadyExistsException.class, () -> userService.registerUser(sampleUser));
        verify(passwordEncoder, never()).encode(anyString());
        verify(userRepository, never()).save(any(User.class));
        verify(eventRepository, never()).save(any(Event.class));
    }

    @Test
    void registerUser_emailAlreadyUsed_throwsException() {
        when(userRepository.existsByUsername(sampleUser.getUsername())).thenReturn(false);
        when(userRepository.existsByEmail(sampleUser.getEmail())).thenReturn(true);
        assertThrows(UserAlreadyExistsException.class, () -> userService.registerUser(sampleUser));
        verify(passwordEncoder, never()).encode(anyString());
        verify(userRepository, never()).save(any(User.class));
        verify(eventRepository, never()).save(any(Event.class));
    }

    @Test
    void deleteUser_validData_deleteUser() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(sampleUser));
        userService.deleteUser("testuser");
        verify(userRepository, times(1)).delete(sampleUser);
    }

    @Test
    void deleteUser_userDoesNotExist_throwsException() {
        when(userRepository.findByUsername("unknown")).thenReturn(Optional.empty());
        assertThrows(UserNotFoundException.class, () -> userService.deleteUser("unknown"));
        verify(userRepository, never()).delete(any(User.class));
    }

    @Test
    void updateUser_validData_shouldSaveEvent() {
        UserUpdateDTO updateDTO = new UserUpdateDTO(185.0, 80.0);
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(sampleUser));
        when(userRepository.save(any(User.class))).thenReturn(sampleUser);
        User updatedUser = userService.updateUser("testuser", updateDTO, true);
        assertEquals(185.0, updatedUser.getHeight(), 0.001);
        assertEquals(80.0, updatedUser.getWeight(), 0.001);
        verify(userRepository, times(1)).save(sampleUser);
        ArgumentCaptor<Event> eventCaptor = ArgumentCaptor.forClass(Event.class);
        verify(eventRepository, times(1)).save(eventCaptor.capture());
        assertEquals(EventType.UPDATE_USER, eventCaptor.getValue().getEventType());
    }

    @Test
    void updateUser_validData_shouldNotSaveEvent() {
        UserUpdateDTO updateDTO = new UserUpdateDTO(190.0, 85.0);
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(sampleUser));
        userService.updateUser("testuser", updateDTO, false);
        assertEquals(190.0, sampleUser.getHeight(), 0.001);
        assertEquals(85.0, sampleUser.getWeight(), 0.001);
        verify(userRepository, times(1)).save(sampleUser);
        verify(eventRepository, never()).save(any(Event.class));
    }

    @Test
    void updateUser_userDoesNotExist_throwsException() {
        UserUpdateDTO updateDTO = new UserUpdateDTO(180.0, 75.0);
        when(userRepository.findByUsername("unknown")).thenReturn(Optional.empty());
        assertThrows(UserNotFoundException.class, () -> userService.updateUser("unknown", updateDTO, true));
        verify(userRepository, never()).save(any(User.class));
        verify(eventRepository, never()).save(any(Event.class));
    }

    @Test
    void findByUsername_validData_returnUser() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(sampleUser));
        User result = userService.findByUsername("testuser");
        assertNotNull(result);
        assertEquals("testuser", result.getUsername());
    }

    @Test
    void findByUsername_userDoesNotExist_throwsException() {
        when(userRepository.findByUsername("unknown")).thenReturn(Optional.empty());
        assertThrows(UserNotFoundException.class, () -> userService.findByUsername("unknown"));
    }
}