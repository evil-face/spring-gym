package epam.xstack.exception;

public final class ValidationException extends RuntimeException {
    private final String errors;

    public ValidationException(String txID, String errors) {
        super(txID);
        this.errors = errors;
    }

    public String getErrors() {
        return errors;
    }
}
