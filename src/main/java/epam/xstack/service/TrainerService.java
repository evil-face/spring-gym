package epam.xstack.service;

import epam.xstack.dao.TrainerDAO;
import epam.xstack.exception.NoSuchTrainingTypeException;
import epam.xstack.exception.PersonAlreadyRegisteredException;
import epam.xstack.model.Trainer;
import epam.xstack.model.Training;
import epam.xstack.model.TrainingType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.naming.AuthenticationException;
import java.util.List;
import java.util.Optional;

@Service
public final class TrainerService {
    public static final String AUTHENTICATION_FAILED = "Authentication failed";
    private final TrainerDAO trainerDAO;
    private final UserService userService;
    private final TrainingService trainingService;
    private final AuthenticationService authService;
    private static final Logger LOGGER = LoggerFactory.getLogger(TrainerService.class);

    @Autowired
    public TrainerService(TrainerDAO trainerDAO, UserService userService,
                          AuthenticationService authService, TrainingService trainingService) {
        this.trainerDAO = trainerDAO;
        this.userService = userService;
        this.authService = authService;
        this.trainingService = trainingService;
    }

    public Trainer createTrainer(String txID, Trainer newTrainer) {
        String username = userService.generateUsername(newTrainer.getFirstName(), newTrainer.getLastName());
        String password = userService.generatePassword();

        newTrainer.setUsername(username);
        newTrainer.setPassword(password);
        newTrainer.setIsActive(true);

        checkIfSpecializationExists(txID, newTrainer);
        checkIfNotRegisteredYet(txID, newTrainer);

        trainerDAO.save(txID, newTrainer);

        return newTrainer;
    }

    public List<Trainer> findAll(String txID) {
            return trainerDAO.findAll();
    }

    public Optional<Trainer> findById(String txID, long id, String username, String password) {
        if (authService.authenticate(txID, id, username, password)) {
            return trainerDAO.findById(txID, id);
        }

        return Optional.empty();
    }

    public Optional<Trainer> findByUsername(String txID, String username) {
        return trainerDAO.findByUsername(txID, username);
    }

    public Optional<Trainer> update(String txID, Trainer updatedTrainer, String username, String password) {
        if (authService.authenticate(txID, updatedTrainer.getId(), username, password)) {
            TrainingType confirmedTrainingType = checkIfSpecializationExists(txID, updatedTrainer);
            updatedTrainer.setSpecialization(confirmedTrainingType);

            return trainerDAO.update(txID, updatedTrainer);
        }

        return Optional.empty();
    }

    public void changeActivationStatus(String txID, long id, Boolean newStatus, String username, String password) {
        if (authService.authenticate(txID, id, username, password)) {
            trainerDAO.changeActivationStatus(txID, id, newStatus, username);
        }
    }

    public List<Training> getTrainingsByTrainerUsername(String trainerUsername, String username, String password)
            throws AuthenticationException {
        if (authService.authenticate("stub", 0, username, password)) {
            return trainerDAO.getTrainingsByTrainerUsername(trainerUsername);
        } else {
            LOGGER.info("Failed attempt to get trainings for trainer {} with credentials {}:{}",
                    trainerUsername, username, password);
            throw new AuthenticationException(AUTHENTICATION_FAILED);
        }
    }

    public List<Training> getTrainingsByTrainerUsernameAndTraineeUsername(
            String trainerUsername, String traineeUsername,
            String username, String password) throws AuthenticationException {
        return getTrainingsByTrainerUsername(trainerUsername, username, password).stream()
                .filter(training -> training.getTrainee().getUsername().equals(traineeUsername))
                .toList();
    }

    private void checkIfNotRegisteredYet(String txID, Trainer newTrainer) {
        List<Trainer> candidates = trainerDAO.findAllByUsernamePartialMatch(newTrainer.getUsername());

        for (Trainer trainer : candidates) {
            if (newTrainer.getSpecialization().getId() == trainer.getSpecialization().getId()) {
                throw new PersonAlreadyRegisteredException(txID);
            }
        }
    }

    private TrainingType checkIfSpecializationExists(String txID, Trainer trainer) {
        Optional<TrainingType> trainingTypeOpt = trainingService.specializationExistsById(
                trainer.getSpecialization().getId());

        if (trainingTypeOpt.isEmpty()) {
            throw new NoSuchTrainingTypeException(txID);
        } else {
            return trainingTypeOpt.get();
        }
    }
}
