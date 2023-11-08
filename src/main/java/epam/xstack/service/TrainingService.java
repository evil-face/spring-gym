package epam.xstack.service;

import epam.xstack.dto.training.TrainingCreateRequestDTO;
import epam.xstack.exception.NoSuchTraineeExistException;
import epam.xstack.exception.NoSuchTrainerExistException;
import epam.xstack.model.Trainee;
import epam.xstack.model.Trainer;
import epam.xstack.model.Training;
import epam.xstack.model.TrainingType;
import epam.xstack.repository.TrainingRepository;
import epam.xstack.repository.TrainingTypeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public final class TrainingService {
    private final TrainingRepository trainingRepository;
    private final TrainingTypeRepository trainingTypeRepository;

    private final TraineeService traineeService;
    private final TrainerService trainerService;

    private static final Logger LOGGER = LoggerFactory.getLogger(TrainingService.class);


    @Autowired
    public TrainingService(TrainingRepository trainingRepository, TraineeService traineeService,
                           TrainerService trainerService, TrainingTypeRepository trainingTypeRepository) {
        this.trainingRepository = trainingRepository;
        this.traineeService = traineeService;
        this.trainerService = trainerService;
        this.trainingTypeRepository = trainingTypeRepository;
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

        Trainee trainee = traineeOpt.get();
        Trainer trainer = trainerOpt.get();
        Training newTraining = new Training(traineeOpt.get(), trainerOpt.get(), trainingDTO.getTrainingName(),
                trainerOpt.get().getSpecialization(), trainingDTO.getTrainingDate(), trainingDTO.getTrainingDuration());

        trainingRepository.save(newTraining);
        LOGGER.info("TX ID: {} — Successfully saved new training '{}' with '{}' trainee and '{}' trainer",
                txID, newTraining.getTrainingName(), trainee.getUsername(), trainer.getUsername());

        trainee.getTrainers().add(trainer);
        trainer.getTrainees().add(trainee);
        traineeService.update(txID, trainee);
        trainerService.update(txID, trainer);

        return newTraining;
    }

    public List<TrainingType> findAllTrainingTypes() {
        return trainingTypeRepository.findAll();
    }
}
