package epam.xstack.service;

import epam.xstack.dao.TraineeDAO;
import epam.xstack.model.Trainee;
import epam.xstack.model.User;
import epam.xstack.validator.GymValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.naming.AuthenticationException;
import javax.validation.ValidationException;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
public final class TraineeService {
    private final TraineeDAO traineeDAO;
    private final UserService userService;
    private final AuthenticationService authService;
    private final GymValidator<Trainee> validator;
    private static final Logger LOGGER = LoggerFactory.getLogger(TraineeService.class);

    @Autowired
    public TraineeService(TraineeDAO traineeDAO, UserService userService,
                          AuthenticationService authService, GymValidator<Trainee> validator) {
        this.traineeDAO = traineeDAO;
        this.userService = userService;
        this.authService = authService;
        this.validator = validator;
    }

    public void createTrainee(String firstName, String lastName,
                                 boolean isActive, Date dateOfBirth, String address) {
        String username = userService.generateUsername(firstName, lastName);
        String password = userService.generatePassword();

        Trainee trainee = new Trainee(firstName, lastName,
                username, password, isActive,
                dateOfBirth, address);

        Set<String> violations = validator.validate(trainee);
        if (!violations.isEmpty()) {
            LOGGER.info("Could not save new trainee because of violations: " + violations);
            throw new ValidationException();
        }

        traineeDAO.save(trainee);
        LOGGER.info("Saved new trainee with id {} to the DB", trainee.getId());
    }

    public List<Trainee> findAll(String username, String password) throws AuthenticationException {
        if (authService.authenticate(username, password)) {
            return traineeDAO.findAll();
        } else {
            LOGGER.info("Failed attempt to show all trainees with credentials {}:{}", username, password);
            throw new AuthenticationException("Authentication failed");
        }
    }

    public Optional<Trainee> findById(long id, String username, String password) throws AuthenticationException {
        if (authService.authenticate(username, password)) {
            return traineeDAO.findById(id);
        } else {
            LOGGER.info("Failed attempt to find trainee by id with credentials {}:{}", username, password);
            throw new AuthenticationException("Authentication failed");
        }
    }

    public Optional<Trainee> findByUsername(String query, String username, String password) throws AuthenticationException {
        if (authService.authenticate(username, password)) {
            Optional<User> user = userService.findByUsername(query);

            return user.isPresent() && user.get() instanceof Trainee ?
                    Optional.of((Trainee) user.get()) : Optional.empty();
        } else {
            LOGGER.info("Failed attempt to find trainee by username with credentials {}:{}", username, password);
            throw new AuthenticationException("Authentication failed");
        }
    }

    public void update(Trainee trainee, String username, String password) throws AuthenticationException {
        if (authService.authenticate(username, password)) {
            Set<String> violations = validator.validate(trainee);
            if (!violations.isEmpty()) {
                LOGGER.info("Could not update trainee because of violations: " + violations);
                throw new ValidationException();
            }

            traineeDAO.update(trainee);
            LOGGER.info("Updated trainee with id {} in the DB", trainee.getId());
        } else {
            LOGGER.info("Failed attempt update trainee with credentials {}:{}", username, password);
            throw new AuthenticationException("Authentication failed");
        }
    }

    public void delete(Trainee trainee, String username, String password) throws AuthenticationException {
        if (authService.authenticate(username, password)) {
            traineeDAO.delete(trainee);
            LOGGER.info("Deleted trainee with id {} from the DB", trainee.getId());
        } else {
            LOGGER.info("Failed attempt to delete trainee with credentials {}:{}", username, password);
            throw new AuthenticationException("Authentication failed");
        }
    }

    public void updatePassword(long id, String newPassword, String username, String oldPassword) throws AuthenticationException {
        if (authService.authenticate(username, oldPassword)) {
            traineeDAO.updatePassword(id, newPassword);
            LOGGER.info("Updated password of trainee with id {} in the DB", id);
        } else {
            LOGGER.info("Failed attempt to update trainee password with credentials {}:{}", username, oldPassword);
            throw new AuthenticationException("Authentication failed");
        }
    }
}
