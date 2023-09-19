package epam.xstack.service;

import epam.xstack.dao.TraineeDAO;
import epam.xstack.model.Trainee;
import epam.xstack.model.Trainer;
import epam.xstack.model.Training;
import epam.xstack.model.User;
import epam.xstack.validator.GymValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.naming.AuthenticationException;
import javax.validation.ValidationException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
public final class TraineeService {
    public static final String AUTHENTICATION_FAILED = "Authentication failed";
    private final TraineeDAO traineeDAO;
    private final UserService userService;
    private final TrainerService trainerService;
    private final AuthenticationService authService;
    private final GymValidator<Trainee> validator;
    private static final Logger LOGGER = LoggerFactory.getLogger(TraineeService.class);

    @Autowired
    public TraineeService(TraineeDAO traineeDAO, UserService userService,
                          AuthenticationService authService, GymValidator<Trainee> validator,
                          TrainerService trainerService) {
        this.traineeDAO = traineeDAO;
        this.userService = userService;
        this.authService = authService;
        this.validator = validator;
        this.trainerService = trainerService;
    }

    public void createTrainee(String firstName, String lastName,
                              boolean isActive, LocalDate dateOfBirth, String address) {
        String username = userService.generateUsername(firstName, lastName);
        String password = userService.generatePassword();

        Trainee trainee = new Trainee(firstName, lastName,
                username, password, isActive,
                dateOfBirth, address);

        Set<String> violations = validator.validate(trainee);
        if (!violations.isEmpty()) {
            LOGGER.info("Could not save new trainee because of violations: {}", violations);
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
            throw new AuthenticationException(AUTHENTICATION_FAILED);
        }
    }

    public Optional<Trainee> findById(long id, String username, String password) throws AuthenticationException {
        if (authService.authenticate(username, password)) {
            return traineeDAO.findById(id);
        } else {
            LOGGER.info("Failed attempt to find trainee by id with credentials {}:{}", username, password);
            throw new AuthenticationException(AUTHENTICATION_FAILED);
        }
    }

    public Optional<Trainee> findByUsername(String query, String username, String password)
            throws AuthenticationException {
        if (authService.authenticate(username, password)) {
            Optional<User> user = userService.findByUsername(query);

            return user.isPresent() && user.get() instanceof Trainee trainee
                    ? Optional.of(trainee) : Optional.empty();
        } else {
            LOGGER.info("Failed attempt to find trainee by username with credentials {}:{}", username, password);
            throw new AuthenticationException(AUTHENTICATION_FAILED);
        }
    }

    public void update(Trainee trainee, String username, String password) throws AuthenticationException {
        if (authService.authenticate(username, password)) {
            Set<String> violations = validator.validate(trainee);
            if (!violations.isEmpty()) {
                LOGGER.info("Could not update trainee because of violations: {}", violations);
                throw new ValidationException();
            }

            traineeDAO.update(trainee);
            LOGGER.info("Updated trainee with id {} in the DB", trainee.getId());
        } else {
            LOGGER.info("Failed attempt update trainee with credentials {}:{}", username, password);
            throw new AuthenticationException(AUTHENTICATION_FAILED);
        }
    }

    public void delete(Trainee trainee, String username, String password) throws AuthenticationException {
        if (authService.authenticate(username, password)) {
            traineeDAO.delete(trainee);
            LOGGER.info("Deleted trainee with id {} from the DB", trainee.getId());
        } else {
            LOGGER.info("Failed attempt to delete trainee with credentials {}:{}", username, password);
            throw new AuthenticationException(AUTHENTICATION_FAILED);
        }
    }

    public void deleteByUsername(String usernameToDelete, String username, String password)
            throws AuthenticationException {
        if (authService.authenticate(username, password)) {
            traineeDAO.delete(usernameToDelete);
        } else {
            LOGGER.info("Failed attempt to delete trainee with credentials {}:{}", username, password);
            throw new AuthenticationException(AUTHENTICATION_FAILED);
        }
    }

    public void updatePassword(long id, String newPassword, String username, String oldPassword)
            throws AuthenticationException {
        if (authService.authenticate(username, oldPassword)) {
            traineeDAO.updatePassword(id, newPassword);
            LOGGER.info("Updated password of trainee with id {} in the DB", id);
        } else {
            LOGGER.info("Failed attempt to update trainee password with credentials {}:{}", username, oldPassword);
            throw new AuthenticationException(AUTHENTICATION_FAILED);
        }
    }

    public void changeActivationStatus(long id, String username, String password) throws AuthenticationException {
        if (authService.authenticate(username, password)) {
            userService.changeActivationStatus(id);
            LOGGER.info("Changed trainee activation status for id {}", id);
        } else {
            LOGGER.info("Failed attempt to change trainee activation status with credentials {}:{}",
                    username, password);
            throw new AuthenticationException(AUTHENTICATION_FAILED);
        }
    }

    public List<Training> getTrainingsByTraineeUsername(String traineeUsername, String username, String password)
            throws AuthenticationException {
        if (authService.authenticate(username, password)) {
            return traineeDAO.getTrainingsByTraineeUsername(traineeUsername);
        } else {
            LOGGER.info("Failed attempt to get trainings for trainee {} with credentials {}:{}",
                    traineeUsername, username, password);
            throw new AuthenticationException(AUTHENTICATION_FAILED);
        }
    }

    public List<Training> getTrainingsByTraineeUsernameAndTrainerUsername(
            String traineeUsername, String trainerUsername,
            String username, String password) throws AuthenticationException {
        return getTrainingsByTraineeUsername(traineeUsername, username, password).stream()
                .filter(training -> training.getTrainer().getUsername().equals(trainerUsername))
                .toList();
    }

    public List<Trainer> getPotentialTrainersForTrainee(String traineeUsername, String username, String password)
            throws AuthenticationException {
        List<Trainer> allTrainers = trainerService.findAll(username, password);

        List<Training> trainings = getTrainingsByTraineeUsername(traineeUsername, username, password);

        List<Trainer> assignedTrainers = trainings.stream()
                .map(Training::getTrainer)
                .toList();

        allTrainers.removeAll(assignedTrainers);

        return allTrainers.stream().filter(Trainer::isActive).toList();
    }

}
