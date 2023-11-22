package epam.xstack.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.Arrays;

@RestControllerAdvice
public final class RestExceptionHandler extends ResponseEntityExceptionHandler {
    private static final int LOG_STACK_TRACE_DEPTH = 10;
    private static final Logger EXCEPTION_HANDLER_LOGGER = LoggerFactory.getLogger(RestExceptionHandler.class);
    private static final String LOG_MESSAGE_WITH_ERRORS = "TX ID: {} — {} — {}";
    private static final String LOG_MESSAGE = "TX ID: {} — {}";

    @Value("${bruteforce.protection.block-period-minutes}")
    public int blockPeriod;

    @ExceptionHandler(Exception.class)
    private ResponseEntity<String> handleGlobalExceptions(Exception e) {
        String errorBody = "There was an error during request processing. Try again later.";
        String logMessage = "Global exception of '%s' was caught, stacktrace".formatted(e.getCause());
        EXCEPTION_HANDLER_LOGGER.error(LOG_MESSAGE_WITH_ERRORS, null, logMessage,
                e.getMessage() + Arrays.stream(e.getStackTrace())
                        .limit(LOG_STACK_TRACE_DEPTH).map(StackTraceElement::toString).toList());

        return ResponseEntity.internalServerError().body(errorBody);
    }

    @ExceptionHandler(PersonAlreadyRegisteredException.class)
    private ResponseEntity<String> handlePersonAlreadyRegistered(PersonAlreadyRegisteredException e) {
        String errorBody = "This person is already registered as a trainee or a trainer";
        EXCEPTION_HANDLER_LOGGER.warn(LOG_MESSAGE_WITH_ERRORS, e.getMessage(),
                HttpStatus.UNPROCESSABLE_ENTITY, errorBody);

        return ResponseEntity.unprocessableEntity().body(errorBody);
    }

    @ExceptionHandler(NoSuchTrainingTypeException.class)
    private ResponseEntity<String> handleBadTrainingTypeInput(NoSuchTrainingTypeException e) {
        String errorBody = "No such training type exists";
        EXCEPTION_HANDLER_LOGGER.warn(LOG_MESSAGE_WITH_ERRORS, e.getMessage(),
                HttpStatus.UNPROCESSABLE_ENTITY, errorBody);

        return ResponseEntity.unprocessableEntity().body(errorBody);
    }

    @ExceptionHandler(UnauthorizedException.class)
    private ResponseEntity<String> handleBadLoginAttempt(UnauthorizedException e) {
        EXCEPTION_HANDLER_LOGGER.warn(LOG_MESSAGE, e.getMessage(), HttpStatus.UNAUTHORIZED);

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    @ExceptionHandler(ForbiddenException.class)
    private ResponseEntity<String> handleForbiddenException(ForbiddenException e) {
        EXCEPTION_HANDLER_LOGGER.warn(LOG_MESSAGE, e.getMessage(), HttpStatus.FORBIDDEN);

        return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }

    @ExceptionHandler(NoSuchTrainerExistException.class)
    private ResponseEntity<String> handleBadTrainerInput(NoSuchTrainerExistException e) {
        String errorBody = "No such trainer exists";
        EXCEPTION_HANDLER_LOGGER.warn(LOG_MESSAGE_WITH_ERRORS, e.getMessage(),
                HttpStatus.UNPROCESSABLE_ENTITY, errorBody);

        return ResponseEntity.unprocessableEntity().body(errorBody);
    }

    @ExceptionHandler(NoSuchTraineeExistException.class)
    private ResponseEntity<String> handleBadTraineeInput(NoSuchTraineeExistException e) {
        String errorBody = "No such trainee exists";
        EXCEPTION_HANDLER_LOGGER.warn(LOG_MESSAGE_WITH_ERRORS, e.getMessage(),
                HttpStatus.UNPROCESSABLE_ENTITY, errorBody);

        return ResponseEntity.unprocessableEntity().body(errorBody);
    }

    @ExceptionHandler(ValidationException.class)
    private ResponseEntity<String> handleValidationException(ValidationException e) {
        EXCEPTION_HANDLER_LOGGER.warn(LOG_MESSAGE_WITH_ERRORS, e.getMessage(),
                HttpStatus.UNPROCESSABLE_ENTITY, e.getErrors());

        return ResponseEntity.unprocessableEntity().body(e.getErrors());
    }

    @ExceptionHandler(UserTemporarilyBlockedException.class)
    private ResponseEntity<String> handleUserIsTemporarilyBlockedException(UserTemporarilyBlockedException e) {
        String errorBody = "User is blocked for " + blockPeriod + " minutes after unsuccessful login attempts";
        EXCEPTION_HANDLER_LOGGER.warn(LOG_MESSAGE_WITH_ERRORS, e.getMessage(),
                HttpStatus.UNAUTHORIZED, errorBody);

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorBody);
    }
}
