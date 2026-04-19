package pl.edu.healthapp.dto.response;

import lombok.Builder;
import pl.edu.healthapp.model.HealthGoalType;

@Builder
public record GoalProgress(Long goalId,
                           HealthGoalType healthGoalType,
                           double target,
                           double actual){}
