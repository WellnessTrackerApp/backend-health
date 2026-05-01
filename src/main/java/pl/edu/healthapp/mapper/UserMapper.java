package pl.edu.healthapp.mapper;


import pl.edu.healthapp.dto.request.UserCreationDTO;
import pl.edu.healthapp.dto.request.UserUpdateDTO;
import pl.edu.healthapp.dto.response.UserDTO;
import pl.edu.healthapp.model.Event;
import pl.edu.healthapp.model.Gender;
import pl.edu.healthapp.model.Notification;
import pl.edu.healthapp.model.User;

import java.time.OffsetDateTime;
import java.util.UUID;
import java.util.stream.Collectors;

public class UserMapper {

    public static UserUpdateDTO fromJSON(String json) {
        String clean = json.replace("{", "").replace("}", "").replace("\"", "");

        String[] pairs = clean.split(",");

        double height = 0;
        double weight = 0;

        for (String pair : pairs) {
            String[] keyValue = pair.split(":", 2);
            String key = keyValue[0].trim();
            String value = keyValue[1].trim();

            if (key.equals("height"))
                height = Double.parseDouble(value);
            else if (key.equals("weight"))
                weight = Double.parseDouble(value);
        }

        return new UserUpdateDTO(height, weight);
    }

    public static String toJSON(User user){
        return "\"username\": \"%s\",\n\"email\": \"%s\",\n\"password\": \"%s\",\n\"birthDate\": \"%s\",\n\"height\": \"%.2f\",\n\"weight\": \"%.1f\",\n\"gender\": \"%s\"".formatted(user.getUsername(), user.getEmail(), user.getPassword(), user.getBirthDate().toString(), user.getHeight(), user.getWeight(), user.getGender().name());
    }

    public static UserDTO fromEntity(User user){
        return new UserDTO(
                            user.getId(),
                            user.getUsername(),
                            user.getEmail(),
                            user.getPassword(),
                            user.getBirthDate(),
                            user.getHeight(),
                            user.getWeight(),
                            user.getGender(),
                            user.getGoals().stream().map(HealthGoalMapper::fromEntity).collect(Collectors.toSet()),
                            user.getSleepHistory().stream().map(SleepMapper::fromEntity).collect(Collectors.toSet()),
                            user.getActivityHistory().stream().map(ActivityMapper::fromEntity).collect(Collectors.toSet()),
                            user.getDietHistory().stream().map(DietMapper::fromEntity).collect(Collectors.toSet()),
                            user.getNotifications().stream().map(Notification::getNotificationType).collect(Collectors.toSet()),
                            user.getEvents().stream().map(Event::getEventType).collect(Collectors.toSet())
        );
    }

    public static User toEntity(UserCreationDTO user){
        return User.builder()
                .id(user.id() == null ? UUID.randomUUID() : user.id())
                .username(user.username())
                .email(user.email())
                .password(user.password())
                .birthDate(user.birthDate())
                .height(user.height())
                .weight(user.weight())
                .gender(user.gender())
                .build();
    }
}
