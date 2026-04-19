package pl.edu.healthapp.mapper;

import pl.edu.healthapp.dto.request.ActivityCreationDTO;
import pl.edu.healthapp.dto.response.ActivityDTO;
import pl.edu.healthapp.model.ActivityEntry;
import pl.edu.healthapp.model.ActivityType;

import java.time.OffsetDateTime;

public class ActivityMapper {

    public static ActivityEntry fromJSON(String json) {
        String clean = json.replace("{", "").replace("}", "").replace("\"", "");

        String[] pairs = clean.split(",");

        ActivityEntry activity = new ActivityEntry();

        for (String pair : pairs) {
            String[] keyValue = pair.split(":", 2);
            String key = keyValue[0].trim();
            String value = keyValue[1].trim();

            if (key.equals("startedAt"))
                activity.setStartedAt(OffsetDateTime.parse(value));
            else if (key.equals("type"))
                activity.setActivityType(ActivityType.valueOf(value));
            else if (key.equals("durationInMinutes"))
                activity.setDurationInMinutes(Integer.parseInt(value));
        }

        return activity;
    }

    public static String toJSON(ActivityEntry activity){
        return "\"startedAt\": \"%s\",\n\"type\": \"%s\",\n\"durationInMinutes\": \"%d\"".formatted(activity.getStartedAt().toString(), activity.getActivityType().name(), activity.getDurationInMinutes());
    }

    public static ActivityDTO fromEntity(ActivityEntry activity){
        return new ActivityDTO(
                                activity.getId(),
                                activity.getStartedAt(),
                                activity.getActivityType(),
                                activity.getDurationInMinutes(),
                                activity.getCaloriesBurned()
        );
    }

    public static ActivityEntry toEntity(ActivityCreationDTO activity){
        return ActivityEntry.builder()
                .startedAt(activity.startedAt())
                .activityType(activity.type())
                .durationInMinutes(activity.durationInMinutes())
                .build();
    }
}
