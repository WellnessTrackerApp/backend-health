package pl.edu.healthapp.exception;

public class ActivityEntryAlreadyExistsException extends RuntimeException {
    public ActivityEntryAlreadyExistsException(String message) {
        super(message);
    }
}
