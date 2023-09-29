package epam.xstack.service;

import epam.xstack.dao.TrainingDAO;
import epam.xstack.dto.training.TrainingCreateRequestDTO;
import epam.xstack.exception.NoSuchTraineeExistException;
import epam.xstack.exception.NoSuchTrainerExistException;
import epam.xstack.model.Trainee;
import epam.xstack.model.Trainer;
import epam.xstack.model.Training;
import epam.xstack.model.TrainingType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public final class TrainingService {
    private final TraineeService traineeService;
    private final TrainerService trainerService;
    private final TrainingDAO trainingDAO;

    @Autowired
    public TrainingService(TrainingDAO trainingDAO, TraineeService traineeService,
                           TrainerService trainerService) {
        this.trainingDAO = trainingDAO;
        this.traineeService = traineeService;
        this.trainerService = trainerService;
    }

    public Training createTraining(String txID, TrainingCreateRequestDTO trainingDTO) {

        Optional<Trainee> traineeOpt = traineeService.findByUsername(txID, trainingDTO.getTraineeUsername());
        if (traineeOpt.isEmpty()) {
            throw new NoSuchTraineeExistException(txID);
        }

        Optional<Trainer> trainerOpt = trainerService.findByUsername(txID, trainingDTO.getTrainerUsername());
        if (trainerOpt.isEmpty()) {
            throw new NoSuchTrainerExistException(txID);
        }

        Training newTraining = new Training(traineeOpt.get(), trainerOpt.get(), trainingDTO.getTrainingName(),
                trainerOpt.get().getSpecialization(), trainingDTO.getTrainingDate(), trainingDTO.getTrainingDuration());
        trainingDAO.save(txID, newTraining);

        return newTraining;
    }

    public List<TrainingType> findAllTrainingTypes(String txID) {
        return trainingDAO.findAllTrainingTypes(txID);
    }

//    public List<Training> findAll(String username, String password) throws AuthenticationException {
//        if (authService.authenticate("stub", 0, username, password)) {
//            return trainingDAO.findAll();
//        } else {
//            LOGGER.info("Failed attempt to find all trainings with credentials {}:{}", username, password);
//            throw new AuthenticationException(AUTHENTICATION_FAILED);
//        }

//    }
//    public Optional<Training> findById(long id, String username, String password) throws AuthenticationException {
//        if (authService.authenticate("stub", 0, username, password)) {
//            return trainingDAO.findById(id);
//        } else {
//            LOGGER.info("Failed attempt to find training by id with credentials {}:{}", username, password);
//            throw new AuthenticationException(AUTHENTICATION_FAILED);
//        }

//    }
}
