package pl.edu.healthapp.exception;

public class UserNotFoundException extends RuntimeException{
    public UserNotFoundException(String property, String value){
        super("User with " + property + ": " + value + " not found");
    }
}
