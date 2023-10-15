package epam.xstack.service;

import epam.xstack.dto.training.TrainingGetListRequestDTO;
import epam.xstack.exception.NoSuchTrainingTypeException;
import epam.xstack.exception.PersonAlreadyRegisteredException;
import epam.xstack.model.Trainer;
import epam.xstack.model.Training;
import epam.xstack.model.TrainingType;
import epam.xstack.repository.TrainerRepository;
import epam.xstack.repository.TrainingRepository;
import epam.xstack.repository.TrainingTypeRepository;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

import static epam.xstack.repository.TrainingSpecs.*;
import static org.springframework.data.jpa.domain.Specification.where;

@Service
public final class TrainerService {
    private final TrainerRepository trainerRepository;
    private final TrainingRepository trainingRepository;
    private final TrainingTypeRepository trainingTypeRepository;

    private final AuthenticationService authService;
    private final UserService userService;

    private final MeterRegistry meterRegistry;

    private static final Logger LOGGER = LoggerFactory.getLogger(TrainerService.class);

    @Autowired
    public TrainerService(TrainerRepository trainerRepository, UserService userService,
                          AuthenticationService authService, TrainingRepository trainingRepository,
                          TrainingTypeRepository trainingTypeRepository, MeterRegistry meterRegistry) {
        this.trainerRepository = trainerRepository;
        this.userService = userService;
        this.authService = authService;
        this.trainingRepository = trainingRepository;
        this.trainingTypeRepository = trainingTypeRepository;
        this.meterRegistry = meterRegistry;

        Gauge.builder("custom.trainer.count", trainerRepository, TrainerRepository::count)
                .register(meterRegistry);
    }

    public Trainer createTrainer(String txID, Trainer newTrainer) {
        String username = userService.generateUsername(newTrainer.getFirstName(), newTrainer.getLastName());
        String password = userService.generatePassword();

        newTrainer.setUsername(username);
        newTrainer.setPassword(password);
        newTrainer.setIsActive(true);

        checkIfSpecializationExists(txID, newTrainer);
        checkIfNotRegisteredYet(txID, newTrainer);

        trainerRepository.save(newTrainer);

        LOGGER.info("TX ID: {} — Successfully saved new trainer with username '{}' and id '{}'",
                txID, newTrainer.getUsername(), newTrainer.getId());
        return newTrainer;
    }

    public List<Trainer> findAll() {
        return trainerRepository.findAll();
    }

    public Optional<Trainer> findById(String txID, long id, String username, String password) {
        authService.authenticate(txID, id, username, password);

        Optional<Trainer> trainerOpt = trainerRepository.findById(id);
        trainerOpt.ifPresent(value -> value.setUsername(null));

        return trainerOpt;
    }

    public Optional<Trainer> findByUsername(String txID, String username) {
        Optional<Trainer> trainerOpt = trainerRepository.findByUsername(username);

        if (trainerOpt.isEmpty()) {
            LOGGER.warn("TX ID: {} — No trainer records found for username {}", txID, username);
        }

        return trainerOpt;
    }

    public Optional<Trainer> update(String txID, Trainer updatedTrainer) {
        authService.authenticate(txID, updatedTrainer.getId(),
                updatedTrainer.getUsername(), updatedTrainer.getPassword());

        TrainingType confirmedTrainingType = checkIfSpecializationExists(txID, updatedTrainer);
        updatedTrainer.setSpecialization(confirmedTrainingType);

        return trainerRepository.findById(updatedTrainer.getId())
                .map(trainer -> {
                    updateFields(trainer, updatedTrainer);
                    trainerRepository.save(trainer);

                    LOGGER.info("TX ID: {} — Successfully updated trainer with username '{}' and id '{}'",
                            txID, trainer.getUsername(), trainer.getId());

                    return trainer;
                });
    }

    public void changeActivationStatus(String txID, long id, Boolean newStatus, String username, String password) {
        authService.authenticate(txID, id, username, password);

        Trainer trainer = trainerRepository.getReferenceById(id);
        trainer.setIsActive(newStatus);
        trainerRepository.save(trainer);

        LOGGER.info("TX ID: {} — Successfully changed status for trainer with username '{}' and id '{}' to '{}'",
                txID, username, id, newStatus);
    }

    public List<Training> getTrainingsWithFiltering(String txID, long id, String username, String password,
                                                    TrainingGetListRequestDTO requestDTO) {
        authService.authenticate(txID, id, username, password);

        List<Training> trainings = trainingRepository.findAll(where(trainerHasId(id))
                .and(traineeHasUsername(requestDTO.getTraineeName()))
                .and(hasPeriodFrom(requestDTO.getPeriodFrom()))
                .and(hasPeriodTo(requestDTO.getPeriodTo())));

        trainings.forEach(training -> training.setTrainer(null));

        return trainings;
    }

    private void checkIfNotRegisteredYet(String txID, Trainer newTrainer) {
        List<Trainer> candidates = trainerRepository.findByUsernameStartingWith(
                newTrainer.getUsername().replaceAll("\\d", ""));

        for (Trainer trainer : candidates) {
            if (newTrainer.getSpecialization().getId() == trainer.getSpecialization().getId()) {
                throw new PersonAlreadyRegisteredException(txID);
            }
        }
    }

    private TrainingType checkIfSpecializationExists(String txID, Trainer trainer) {
        Optional<TrainingType> trainingTypeOpt = trainingTypeRepository.findById(
                trainer.getSpecialization().getId());

        if (trainingTypeOpt.isEmpty()) {
            throw new NoSuchTrainingTypeException(txID);
        } else {
            return trainingTypeOpt.get();
        }
    }

    private void updateFields(Trainer existingTrainer, Trainer updatedTrainer) {
        existingTrainer.setFirstName(updatedTrainer.getFirstName());
        existingTrainer.setLastName(updatedTrainer.getLastName());
        existingTrainer.setSpecialization(updatedTrainer.getSpecialization());
        existingTrainer.setIsActive(updatedTrainer.getIsActive());
    }
}
