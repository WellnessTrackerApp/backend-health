package pl.edu.healthapp.exception;

public class DietEntryNotFoundException extends RuntimeException {
    public DietEntryNotFoundException(String message) {
        super(message);
    }
}
