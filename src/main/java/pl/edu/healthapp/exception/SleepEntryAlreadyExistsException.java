package pl.edu.healthapp.exception;

public class SleepEntryAlreadyExistsException extends RuntimeException {
    public SleepEntryAlreadyExistsException(String message) {
        super(message);
    }
}
