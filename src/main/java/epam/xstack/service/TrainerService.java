package epam.xstack.service;

import epam.xstack.dao.TrainerDAO;
import epam.xstack.model.Trainer;
import epam.xstack.model.Training;
import epam.xstack.model.TrainingType;
import epam.xstack.model.User;
import epam.xstack.validator.GymValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.naming.AuthenticationException;
import javax.validation.ValidationException;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
public final class TrainerService {
    private final TrainerDAO trainerDAO;
    private final UserService userService;
    private final AuthenticationService authService;
    private final GymValidator<Trainer> validator;
    private static final Logger LOGGER = LoggerFactory.getLogger(TrainerService.class);

    @Autowired
    public TrainerService(TrainerDAO trainerDAO, UserService userService,
                          AuthenticationService authService, GymValidator<Trainer> validator) {
        this.trainerDAO = trainerDAO;
        this.userService = userService;
        this.authService = authService;
        this.validator = validator;
    }

    public void createTrainer(String firstName, String lastName,
                                 boolean isActive, TrainingType specialization) {
        String username = userService.generateUsername(firstName, lastName);
        String password = userService.generatePassword();

        Trainer trainer = new Trainer(firstName, lastName,
                username, password, isActive, specialization);

        Set<String> violations = validator.validate(trainer);
        if (!violations.isEmpty()) {
            LOGGER.info("Could not save new trainer because of violations: " + violations);
            throw new ValidationException();
        }

        trainerDAO.save(trainer);
        LOGGER.info("Saved new trainer with id {} to the DB", trainer.getId());
    }

    public List<Trainer> findAll(String username, String password) throws AuthenticationException {
        if (authService.authenticate(username, password)) {
            return trainerDAO.findAll();
        } else {
            LOGGER.info("Failed attempt to find all trainers with credentials {}:{}", username, password);
            throw new AuthenticationException("Authentication failed");
        }
    }

    public Optional<Trainer> findById(long id, String username, String password) throws AuthenticationException {
        if (authService.authenticate(username, password)) {
            return trainerDAO.findById(id);
        } else {
            LOGGER.info("Failed attempt to find trainer by id with credentials {}:{}", username, password);
            throw new AuthenticationException("Authentication failed");
        }
    }

    public Optional<Trainer> findByUsername(String query, String username, String password) throws AuthenticationException {
        if (authService.authenticate(username, password)) {
            Optional<User> user = userService.findByUsername(query);

            return user.isPresent() && user.get() instanceof Trainer ?
                    Optional.of((Trainer) user.get()) : Optional.empty();
        } else {
            LOGGER.info("Failed attempt to find trainer by username with credentials {}:{}", username, password);
            throw new AuthenticationException("Authentication failed");
        }
    }

    public void update(Trainer trainer, String username, String password) throws AuthenticationException {
        if (authService.authenticate(username, password)) {
            Set<String> violations = validator.validate(trainer);
            if (!violations.isEmpty()) {
                LOGGER.info("Could not update trainer because of violations: " + violations);
                throw new ValidationException();
            }

            trainerDAO.update(trainer);
            LOGGER.info("Updated trainer with id {} in the DB", trainer.getId());
        } else {
            LOGGER.info("Failed attempt update trainer with credentials {}:{}", username, password);
            throw new AuthenticationException("Authentication failed");
        }
    }

    public void updatePassword(long id, String newPassword, String username, String oldPassword) throws AuthenticationException {
        if (authService.authenticate(username, oldPassword)) {
            trainerDAO.updatePassword(id, newPassword);
            LOGGER.info("Updated password of trainer with id {} in the DB", id);
        } else {
            LOGGER.info("Failed attempt to update trainer password with credentials {}:{}", username, oldPassword);
            throw new AuthenticationException("Authentication failed");
        }
    }

    public void changeActivationStatus(long id, String username, String password) throws AuthenticationException {
        if (authService.authenticate(username, password)) {
            userService.changeActivationStatus(id);
            LOGGER.info("Changed trainer activation status for id {}", id);
        } else {
            LOGGER.info("Failed attempt to change trainer activation status with credentials {}:{}", username, password);
            throw new AuthenticationException("Authentication failed");
        }
    }

    public List<Training> getTrainingsByTrainerUsername(String trainerUsername,
                                                        String username, String password) throws AuthenticationException {
        if (authService.authenticate(username, password)) {
            return trainerDAO.getTrainingsByTraineeUsername(trainerUsername);
        } else {
            LOGGER.info("Failed attempt to get trainings for trainer {} with credentials {}:{}",
                    trainerUsername, username, password);
            throw new AuthenticationException("Authentication failed");
        }
    }

    public List<Training> getTrainingsByTrainerUsernameAndTraineeLastName(String trainerUsername, String traineeLastname,
                                                                          String username, String password) throws AuthenticationException {
        return getTrainingsByTrainerUsername(trainerUsername, username, password).stream()
                .filter(training -> training.getTrainee().getLastName().equals(traineeLastname))
                .toList();
    }
}
