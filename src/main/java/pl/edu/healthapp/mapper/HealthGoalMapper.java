package pl.edu.healthapp.mapper;


import pl.edu.healthapp.dto.request.HealthGoalCreationDTO;
import pl.edu.healthapp.dto.response.HealthGoalDTO;
import pl.edu.healthapp.model.HealthGoal;
import pl.edu.healthapp.model.HealthGoalType;

import java.time.OffsetDateTime;

public class HealthGoalMapper {

    public static HealthGoal fromJSON(String json) {
        String clean = json.replace("{", "").replace("}", "").replace("\"", "");

        String[] pairs = clean.split(",");

        HealthGoal healthGoal = new HealthGoal();

        for (String pair : pairs) {
            String[] keyValue = pair.split(":", 2);
            String key = keyValue[0].trim();
            String value = keyValue[1].trim();

            if (key.equals("healthGoalType"))
                healthGoal.setHealthGoalType(HealthGoalType.valueOf(value));
            else if (key.equals("target"))
                healthGoal.setTarget(Double.parseDouble(value));
        }

        return healthGoal;
    }

    public static String toJSON(HealthGoal healthGoal){
        return "\"healthGoalType\": \"%s\",\n\"target\": \"%.1f\"".formatted(healthGoal.getHealthGoalType().name(), healthGoal.getTarget());
    }

    public static HealthGoalDTO fromEntity(HealthGoal healthGoal){
        return new HealthGoalDTO(
                                healthGoal.getId(),
                                healthGoal.getHealthGoalType(),
                                healthGoal.getTarget(),
                                healthGoal.getCreatedAt()
        );
    }

    public static HealthGoal toEntity(HealthGoalCreationDTO healthGoal){
        return HealthGoal.builder()
                .healthGoalType(healthGoal.healthGoalType())
                .target(healthGoal.target())
                .createdAt(OffsetDateTime.now())
                .build();
    }
}
