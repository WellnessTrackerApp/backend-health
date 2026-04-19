package pl.edu.healthapp.exception;

import pl.edu.healthapp.model.HealthGoalType;

public class HealthGoalNotFoundException extends RuntimeException {
    public HealthGoalNotFoundException(String message) {super(message);}
}
