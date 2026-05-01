package pl.edu.healthapp.service;


import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import pl.edu.healthapp.dto.request.UserUpdateDTO;

import pl.edu.healthapp.exception.UserAlreadyExistsException;
import pl.edu.healthapp.exception.UserNotFoundException;
import pl.edu.healthapp.mapper.UserMapper;
import pl.edu.healthapp.model.Event;
import pl.edu.healthapp.model.EventType;
import pl.edu.healthapp.model.User;
import pl.edu.healthapp.repository.EventRepository;
import pl.edu.healthapp.repository.UserRepository;

import java.time.OffsetDateTime;
import java.util.List;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EventRepository eventRepository;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, EventRepository eventRepository){
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.eventRepository = eventRepository;
    }

    public User registerUser(User user){
        validateUniqueness(user);
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        user = userRepository.save(user);

        saveEvent(EventType.REGISTER_USER, user);
        return user;
    }

    public void deleteUser(String username){
        User user = userRepository.findByUsername(username).orElseThrow(() -> new UserNotFoundException("username", username));
        userRepository.delete(user);
    }

    public User updateUser(String username, UserUpdateDTO userUpdateDTO, boolean saveEvent){
        User user = userRepository.findByUsername(username).orElseThrow(() -> new UserNotFoundException("username", username));

        if(userUpdateDTO.height() != null){
            user.setHeight(userUpdateDTO.height());
        }

        if(userUpdateDTO.weight() != null){
            user.setWeight(userUpdateDTO.weight());
        }

        userRepository.save(user);

        if (saveEvent)
            saveEvent(EventType.UPDATE_USER, user);

        return user;
    }

    public User findByUsername(String username){
        return userRepository.findByUsername(username).orElseThrow(() -> new UserNotFoundException("username", username));
    }
    public List<User> findAll(){
        return userRepository.findAll();
    }

    private void validateUniqueness(User user) {
        if (userRepository.existsByUsername(user.getUsername())) {
            throw new UserAlreadyExistsException("Username", user.getUsername());
        }
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new UserAlreadyExistsException("Email", user.getEmail());
        }
    }

    private void saveEvent(EventType eventType, User user){
        String payload = UserMapper.toJSON(user);
        Event event = Event.builder()
                .entityId(user.getId().getLeastSignificantBits())
                .eventType(eventType)
                .createdAt(OffsetDateTime.now())
                .payload(payload)
                .user(user)
                .build();

        eventRepository.save(event);
    }
}
