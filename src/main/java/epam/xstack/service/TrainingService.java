package epam.xstack.service;

import epam.xstack.dto.training.TrainingCreateRequestDTO;
import epam.xstack.dto.training.TrainingGetListRequestDTO;
import epam.xstack.dto.training.TrainingResponseDTO;
import epam.xstack.dto.workload.Action;
import epam.xstack.exception.NoSuchTraineeExistException;
import epam.xstack.exception.NoSuchTrainerExistException;
import epam.xstack.model.Trainee;
import epam.xstack.model.Trainer;
import epam.xstack.model.Training;
import epam.xstack.model.TrainingType;
import epam.xstack.repository.TrainingRepository;
import epam.xstack.repository.TrainingTypeRepository;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static epam.xstack.repository.TrainingSpecs.hasPeriodFrom;
import static epam.xstack.repository.TrainingSpecs.hasPeriodTo;
import static epam.xstack.repository.TrainingSpecs.hasTrainingType;
import static epam.xstack.repository.TrainingSpecs.traineeHasId;
import static epam.xstack.repository.TrainingSpecs.traineeHasUsername;
import static epam.xstack.repository.TrainingSpecs.trainerHasId;
import static epam.xstack.repository.TrainingSpecs.trainerHasUsername;
import static org.springframework.data.jpa.domain.Specification.where;

@Service
public class TrainingService {
    private final TrainingRepository trainingRepository;
    private final TrainingTypeRepository trainingTypeRepository;

    private final TraineeService traineeService;
    private final TrainerService trainerService;

    private final ModelMapper modelMapper;
    private static final Logger LOGGER = LoggerFactory.getLogger(TrainingService.class);

    @Autowired
    public TrainingService(TrainingRepository trainingRepository, TraineeService traineeService,
                           TrainerService trainerService, TrainingTypeRepository trainingTypeRepository,
                           ModelMapper modelMapper) {
        this.trainingRepository = trainingRepository;
        this.traineeService = traineeService;
        this.trainerService = trainerService;
        this.trainingTypeRepository = trainingTypeRepository;
        this.modelMapper = modelMapper;
    }

    @Transactional
    public Training createTraining(String txID, TrainingCreateRequestDTO trainingDTO) {

        Optional<Trainee> traineeOpt = traineeService.findByUsername(trainingDTO.getTraineeUsername());
        if (traineeOpt.isEmpty()) {
            throw new NoSuchTraineeExistException(txID);
        }

        Optional<Trainer> trainerOpt = trainerService.findByUsername(trainingDTO.getTrainerUsername());
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
        traineeService.save(trainee);
        trainer.getTrainees().add(trainee);
        trainerService.save(trainer);

        trainerService.updateTrainerWorkload(txID, newTraining, Action.ADD);

        return newTraining;
    }

    public List<TrainingType> findAllTrainingTypes() {
        return trainingTypeRepository.findAll();
    }

    public List<TrainingResponseDTO> getTraineeTrainingsWithFiltering(long id, TrainingGetListRequestDTO requestDTO) {
        List<Training> trainings = trainingRepository.findAll(where(traineeHasId(id))
                .and(trainerHasUsername(requestDTO.getTrainerName()))
                .and(hasTrainingType(requestDTO.getTrainingType()))
                .and(hasPeriodFrom(requestDTO.getPeriodFrom()))
                .and(hasPeriodTo(requestDTO.getPeriodTo())));

        trainings.forEach(training -> training.setTrainee(null));

        return trainings.stream()
                .map(training -> modelMapper.map(training, TrainingResponseDTO.class))
                .toList();
    }

    public List<TrainingResponseDTO> getTrainerTrainingsWithFiltering(long id, TrainingGetListRequestDTO requestDTO) {
        List<Training> trainings = trainingRepository.findAll(where(trainerHasId(id))
                .and(traineeHasUsername(requestDTO.getTraineeName()))
                .and(hasPeriodFrom(requestDTO.getPeriodFrom()))
                .and(hasPeriodTo(requestDTO.getPeriodTo())));

        trainings.forEach(training -> training.setTrainer(null));

        return trainings.stream()
                .map(e -> modelMapper.map(e, TrainingResponseDTO.class))
                .toList();
    }
}
