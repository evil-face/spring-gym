package epam.xstack.service;

import epam.xstack.dao.TrainerDAO;
import epam.xstack.dto.trainer.req.TrainerGetTrainingListRequestDTO;
import epam.xstack.exception.NoSuchTrainingTypeException;
import epam.xstack.exception.PersonAlreadyRegisteredException;
import epam.xstack.model.Trainer;
import epam.xstack.model.Training;
import epam.xstack.model.TrainingType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public final class TrainerService {
    private final TrainerDAO trainerDAO;
    private final UserService userService;
    private final AuthenticationService authService;

    @Autowired
    public TrainerService(TrainerDAO trainerDAO, UserService userService,
                          AuthenticationService authService) {
        this.trainerDAO = trainerDAO;
        this.userService = userService;
        this.authService = authService;
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

    public List<Trainer> findAll() {
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

//    public List<Training> getTrainingsByTrainerUsername(String trainerUsername, String username, String password)
//            throws AuthenticationException {
//        if (authService.authenticate("stub", 0, username, password)) {
//            return trainerDAO.getTrainingsByTrainerUsername(trainerUsername);
//        } else {
//            LOGGER.info("Failed attempt to get trainings for trainer {} with credentials {}:{}",
//                    trainerUsername, username, password);
//            throw new AuthenticationException(AUTHENTICATION_FAILED);
//        }
//    }

    public List<Training> getTrainingsWithFiltering(String txID, long id, String username, String password,
                                                    TrainerGetTrainingListRequestDTO requestDTO) {
        if (authService.authenticate(txID, id, username, password)) {
            return trainerDAO.getTrainingsWithFiltering(txID, id, requestDTO);
        }

        return new ArrayList<>();
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
        Optional<TrainingType> trainingTypeOpt = trainerDAO.trainingTypeExistsById(
                trainer.getSpecialization().getId());

        if (trainingTypeOpt.isEmpty()) {
            throw new NoSuchTrainingTypeException(txID);
        } else {
            return trainingTypeOpt.get();
        }
    }
}
