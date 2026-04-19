package pl.edu.healthapp.exception;

public class UserAlreadyExistsException extends RuntimeException {
    public UserAlreadyExistsException(String property, String value){
        super(property + " already taken: " + value);
    }
}
