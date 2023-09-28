package epam.xstack.service;

import epam.xstack.dao.TraineeDAO;
import epam.xstack.dto.trainee.req.TraineeUpdateTrainerListRequestDTO;
import epam.xstack.dto.training.TrainingGetListForTraineeRequestDTO;
import epam.xstack.exception.EntityNotFoundException;
import epam.xstack.exception.NoSuchTrainerExistException;
import epam.xstack.exception.PersonAlreadyRegisteredException;
import epam.xstack.model.Trainee;
import epam.xstack.model.Trainer;
import epam.xstack.model.Training;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
public final class TraineeService {
    private final TraineeDAO traineeDAO;
    private final UserService userService;
    private final TrainerService trainerService;
    private final AuthenticationService authService;

    @Autowired
    public TraineeService(TraineeDAO traineeDAO, UserService userService,
                          AuthenticationService authService, TrainerService trainerService) {
        this.traineeDAO = traineeDAO;
        this.userService = userService;
        this.authService = authService;
        this.trainerService = trainerService;
    }

    public Trainee createTrainee(String txID, Trainee newTrainee) {
        String username = userService.generateUsername(newTrainee.getFirstName(), newTrainee.getLastName());
        String password = userService.generatePassword();

        newTrainee.setUsername(username);
        newTrainee.setPassword(password);
        newTrainee.setIsActive(true);

        checkIfNotRegisteredYet(txID, newTrainee);

        traineeDAO.save(txID, newTrainee);

        return newTrainee;
    }

//    public List<Trainee> findAll(String username, String password) throws AuthenticationException {
//        if (authService.authenticate("stub", username, password)) {
//            return traineeDAO.findAll();
//        } else {
//            LOGGER.info("Failed attempt to show all trainees with credentials {}:{}", username, password);
//            return new ArrayList<>();
//        }
//    }

    public Optional<Trainee> findById(String txID, long id, String username, String password) {
        if (authService.authenticate(txID, id, username, password)) {
            return traineeDAO.findById(txID, id);
        }

        return Optional.empty();
    }

    public Optional<Trainee> update(String txID, Trainee updatedTrainee, String username, String password) {
        if (authService.authenticate(txID, updatedTrainee.getId(), username, password)) {
            return traineeDAO.update(txID, updatedTrainee);
        }

        return Optional.empty();
    }

    public void delete(String txID, Trainee traineeToDelete) {
        if (authService.authenticate(txID, traineeToDelete.getId(),
                traineeToDelete.getUsername(), traineeToDelete.getPassword())) {
            traineeDAO.delete(txID, traineeToDelete);
        }
    }

    public void changeActivationStatus(String txID, long id, Boolean newStatus, String username, String password) {
        if (authService.authenticate(txID, id, username, password)) {
            traineeDAO.changeActivationStatus(txID, id, newStatus, username);
        }
    }

//    public List<Training> getTrainingsByTraineeUsername(String traineeUsername, String username, String password)
//            throws AuthenticationException {
//        if (authService.authenticate("stub", username, password)) {
//            return traineeDAO.getTrainingsByTraineeUsername(traineeUsername);
//        } else {
//            LOGGER.info("Failed attempt to get trainings for trainee {} with credentials {}:{}",
//                    traineeUsername, username, password);
//            throw new AuthenticationException(AUTHENTICATION_FAILED);
//        }
//    }

    public List<Training> getTrainingsWithFiltering(String txID, long id, String username, String password,
                                                    TrainingGetListForTraineeRequestDTO requestDTO) {
        if (authService.authenticate(txID, id, username, password)) {
            return traineeDAO.getTrainingsWithFiltering(txID, id, requestDTO);
        }

        return new ArrayList<>();
    }

    public List<Trainer> getPotentialTrainersForTrainee(String txID, long id, String username, String password) {
        if (authService.authenticate(txID, id, username, password)) {
            Optional<Trainee> traineeOpt = traineeDAO.findByUsername(txID, username);

            if (traineeOpt.isEmpty()) {
                throw new EntityNotFoundException(txID);
            }

            List<Trainer> allTrainers = trainerService.findAll(txID);
            Set<Trainer> assignedTrainers = traineeOpt.get().getTrainers();
            allTrainers.removeAll(assignedTrainers);

            return allTrainers.stream().filter(Trainer::getIsActive).toList();
        }

        return new ArrayList<>();
    }

    // no auth in task requirements!
    public List<Trainer> updateTrainerList(String txID, long id, TraineeUpdateTrainerListRequestDTO requestDTO) {
        List<Trainer> allTrainers = trainerService.findAll(txID);

        List<Trainer> updatedList = allTrainers.stream()
                .filter(trainer -> requestDTO.getTrainers().contains(trainer.getUsername()))
                .toList();

        if (updatedList.isEmpty()) {
            throw new NoSuchTrainerExistException(txID);
        }

        return traineeDAO.updateTrainerList(txID, id, requestDTO.getUsername(), updatedList);
    }

    private void checkIfNotRegisteredYet(String txID, Trainee newTrainee) {
        List<Trainee> candidates = traineeDAO.findAllByUsernamePartialMatch(newTrainee.getUsername());

        for (Trainee trainee : candidates) {
            if (newTrainee.getAddress().equals(trainee.getAddress()) &&
            newTrainee.getDateOfBirth().equals(trainee.getDateOfBirth())) {
                throw new PersonAlreadyRegisteredException(txID);
            }
        }
    }
}
