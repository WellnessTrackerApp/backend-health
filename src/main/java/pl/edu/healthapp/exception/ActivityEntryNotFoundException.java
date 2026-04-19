package pl.edu.healthapp.exception;

public class ActivityEntryNotFoundException extends RuntimeException {
    public ActivityEntryNotFoundException(String message) {
        super(message);
    }
}
