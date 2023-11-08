package epam.xstack.exception;

public class UserTemporarilyBlockedException extends RuntimeException {
    public UserTemporarilyBlockedException(String message) {
        super(message);
    }
}
