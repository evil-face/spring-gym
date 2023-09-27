package epam.xstack.exception;

public class PersonAlreadyRegisteredException extends RuntimeException {
    public PersonAlreadyRegisteredException(String message) {
        super(message);
    }
}
