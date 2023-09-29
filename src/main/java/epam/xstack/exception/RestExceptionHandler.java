package epam.xstack.exception;

import epam.xstack.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@RestControllerAdvice
public class RestExceptionHandler extends ResponseEntityExceptionHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(UserService.class);

    @ExceptionHandler(PersonAlreadyRegisteredException.class)
    protected ResponseEntity<String> handlePersonAlreadyRegistered(RuntimeException e) {
        String errorBody = "This person is already registered as a trainee or a trainer";
        LOGGER.warn("TX ID: {} — {} — {}", e.getMessage(), HttpStatus.UNPROCESSABLE_ENTITY, errorBody);

        return ResponseEntity.unprocessableEntity().body(errorBody);
    }

    @ExceptionHandler(NoSuchTrainingTypeException.class)
    protected ResponseEntity<String> handleBadTrainingTypeInput(RuntimeException e) {
        String errorBody = "No such training type exists";
        LOGGER.warn("TX ID: {} — {} — {}", e.getMessage(), HttpStatus.UNPROCESSABLE_ENTITY, errorBody);

        return ResponseEntity.unprocessableEntity().body(errorBody);
    }

    @ExceptionHandler(UnauthorizedException.class)
    protected ResponseEntity<String> handleBadLoginAttempt(RuntimeException e) {
        LOGGER.warn("TX ID: {} — {}", e.getMessage(), HttpStatus.UNAUTHORIZED);

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    @ExceptionHandler(ForbiddenException.class)
    protected ResponseEntity<String> handleForbiddenException(RuntimeException e) {
        LOGGER.warn("TX ID: {} — {}", e.getMessage(), HttpStatus.FORBIDDEN);

        return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }

    @ExceptionHandler(EntityNotFoundException.class)
    protected ResponseEntity<String> handleNotFound(RuntimeException e) {
        LOGGER.warn("TX ID: {} — {}", e.getMessage(), HttpStatus.NOT_FOUND);

        return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }

    @ExceptionHandler(NoSuchTrainerExistException.class)
    protected ResponseEntity<String> handleBadTrainerInput(RuntimeException e) {
        String errorBody = "No such trainer exists";
        LOGGER.warn("TX ID: {} — {} — {}", e.getMessage(), HttpStatus.UNPROCESSABLE_ENTITY, errorBody);

        return ResponseEntity.unprocessableEntity().body(errorBody);
    }

    @ExceptionHandler(NoSuchTraineeExistException.class)
    protected ResponseEntity<String> handleBadTraineeInput(RuntimeException e) {
        String errorBody = "No such trainee exists";
        LOGGER.warn("TX ID: {} — {} — {}", e.getMessage(), HttpStatus.UNPROCESSABLE_ENTITY, errorBody);

        return ResponseEntity.unprocessableEntity().body(errorBody);
    }
}
