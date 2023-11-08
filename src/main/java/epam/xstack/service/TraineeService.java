package epam.xstack.service;

import epam.xstack.dto.trainee.TraineeRequestDTO;
import epam.xstack.dto.training.TrainingGetListRequestDTO;
import epam.xstack.exception.EntityNotFoundException;
import epam.xstack.exception.ForbiddenException;
import epam.xstack.exception.NoSuchTrainerExistException;
import epam.xstack.exception.PersonAlreadyRegisteredException;
import epam.xstack.model.Trainee;
import epam.xstack.model.Trainer;
import epam.xstack.model.Training;
import epam.xstack.repository.TraineeRepository;
import epam.xstack.repository.TrainerRepository;
import epam.xstack.repository.TrainingRepository;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.jpa.JpaObjectRetrievalFailureException;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static epam.xstack.repository.TrainingSpecs.*;
import static org.springframework.data.jpa.domain.Specification.where;

@Service
public final class TraineeService {
    private final TraineeRepository traineeRepository;
    private final TrainerRepository trainerRepository;
    private final TrainingRepository trainingRepository;

    private final UserService userService;
    private final AuthenticationService authService;

    private final MeterRegistry meterRegistry;

    private static final Logger LOGGER = LoggerFactory.getLogger(TraineeService.class);


    @Autowired
    public TraineeService(UserService userService, TraineeRepository traineeRepository,
                          AuthenticationService authService, TrainingRepository trainingRepository,
                          TrainerRepository trainerRepository, MeterRegistry meterRegistry) {
        this.userService = userService;
        this.traineeRepository = traineeRepository;
        this.authService = authService;
        this.trainingRepository = trainingRepository;
        this.trainerRepository = trainerRepository;
        this.meterRegistry = meterRegistry;

        Gauge.builder("custom.trainee.count", traineeRepository, TraineeRepository::count)
                .register(meterRegistry);
    }

    public Trainee createTrainee(String txID, Trainee newTrainee) {
        String username = userService.generateUsername(newTrainee.getFirstName(), newTrainee.getLastName());
        String password = userService.generatePassword();

        newTrainee.setUsername(username);
        newTrainee.setPassword(password);
        newTrainee.setIsActive(true);

        checkIfNotRegisteredYet(txID, newTrainee);

        traineeRepository.save(newTrainee);

        LOGGER.info("TX ID: {} — Successfully saved new trainee with username '{}' and id '{}'",
                txID, newTrainee.getUsername(), newTrainee.getId());
        return newTrainee;
    }

    public Optional<Trainee> findById(String txID, long id, String username, String password) {
        authService.authenticate(txID, id, username, password);

        Optional<Trainee> traineeOpt = traineeRepository.findById(id);
        traineeOpt.ifPresent(value -> value.setUsername(null));

        return traineeOpt;
    }

    public Optional<Trainee> findByUsername(String txID, String username) {
        Optional<Trainee> traineeOpt = traineeRepository.findByUsername(username);

        if (traineeOpt.isEmpty()) {
            LOGGER.warn("TX ID: {} — No trainee records found for username {}", txID, username);
        }

        return traineeOpt;
    }

    public Optional<Trainee> update(String txID, Trainee updatedTrainee) {
        authService.authenticate(txID, updatedTrainee.getId(),
                updatedTrainee.getUsername(), updatedTrainee.getPassword());

        return traineeRepository.findById(updatedTrainee.getId())
                .map(trainee -> {
                    updateFields(trainee, updatedTrainee);
                    traineeRepository.save(trainee);

                    LOGGER.info("TX ID: {} — Successfully updated trainee with username '{}' and id '{}'",
                            txID, trainee.getUsername(), trainee.getId());

                    return trainee;
                });
    }

    public void delete(String txID, Trainee traineeToDelete) {
        authService.authenticate(txID, traineeToDelete.getId(),
                traineeToDelete.getUsername(), traineeToDelete.getPassword());

        traineeRepository.delete(traineeToDelete);

        LOGGER.info("TX ID: {} — Successfully deleted trainee with username '{}' and id '{}'",
                txID, traineeToDelete.getUsername(), traineeToDelete.getId());
    }

    public void changeActivationStatus(String txID, long id, Boolean newStatus, String username, String password) {
        authService.authenticate(txID, id, username, password);

        Trainee trainee = traineeRepository.getReferenceById(id);
        trainee.setIsActive(newStatus);
        traineeRepository.save(trainee);

        LOGGER.info("TX ID: {} — Successfully changed status for trainee with username '{}' and id '{}' to '{}'",
                txID, username, id, newStatus);
    }

    public List<Training> getTrainingsWithFiltering(String txID, long id, String username, String password,
                                                    TrainingGetListRequestDTO requestDTO) {
        authService.authenticate(txID, id, username, password);

        List<Training> trainings = trainingRepository.findAll(where(traineeHasId(id))
                .and(trainerHasUsername(requestDTO.getTrainerName()))
                .and(hasTrainingType(requestDTO.getTrainingType()))
                .and(hasPeriodFrom(requestDTO.getPeriodFrom()))
                .and(hasPeriodTo(requestDTO.getPeriodTo())));

        trainings.forEach(training -> training.setTrainee(null));

        return trainings;
    }

    public List<Trainer> getPotentialTrainersForTrainee(String txID, long id, String username, String password) {
        authService.authenticate(txID, id, username, password);

        Optional<Trainee> traineeOpt = traineeRepository.findByUsername(username);

        if (traineeOpt.isEmpty()) {
            throw new EntityNotFoundException(txID);
        }

        List<Trainer> allTrainers = trainerRepository.findAll();
        Set<Trainer> assignedTrainers = traineeOpt.get().getTrainers();
        allTrainers.removeAll(assignedTrainers);

        List<Trainer> filteredActiveUnassignedTrainers = allTrainers.stream()
                .filter(Trainer::getIsActive)
                .toList();

        allTrainers.forEach(trainer -> {
            trainer.setTrainees(null);
            trainer.setIsActive(null);
        });

        return filteredActiveUnassignedTrainers;
    }

    // no auth in task requirements!
    public List<Trainer> updateTrainerList(String txID, long id, TraineeRequestDTO traineeRequestDTO) {
        List<Trainer> allTrainers = trainerRepository.findAll();
        List<Trainer> updatedList = verifyTrainers(txID, traineeRequestDTO, allTrainers);

        try {
            Trainee trainee = traineeRepository.getReferenceById(id);

            if (!trainee.getUsername().equals(traineeRequestDTO.getUsername())) {
                throw new ForbiddenException(txID);
            }

            trainee.setTrainers(new HashSet<>(updatedList));
            traineeRepository.save(trainee);

            LOGGER.info("TX ID: {} — Successfully updated trainee's list of trainers for username '{}' with '{}'"
                    + " trainers", txID, traineeRequestDTO.getUsername(), updatedList.size());
        } catch (JpaObjectRetrievalFailureException e) {
            throw new EntityNotFoundException(txID);
        }

        updatedList.forEach(trainer -> {
            trainer.setTrainees(null);
            trainer.setIsActive(null);
        });

        return updatedList;
    }

    private List<Trainer> verifyTrainers(String txID, TraineeRequestDTO traineeRequestDTO, List<Trainer> allTrainers) {
        List<Trainer> updatedList = allTrainers.stream()
                .filter(trainer -> traineeRequestDTO.getTrainers().contains(trainer.getUsername()))
                .toList();

        if (updatedList.isEmpty()) {
            throw new NoSuchTrainerExistException(txID);
        }

        return updatedList;
    }

    private void checkIfNotRegisteredYet(String txID, Trainee newTrainee) {
        List<Trainee> candidates = traineeRepository.findByUsernameStartingWith(
                newTrainee.getUsername().replaceAll("\\d", ""));

        for (Trainee trainee : candidates) {
            if (newTrainee.getAddress().equals(trainee.getAddress())
                    && newTrainee.getDateOfBirth().equals(trainee.getDateOfBirth())) {
                throw new PersonAlreadyRegisteredException(txID);
            }
        }
    }

    private void updateFields(Trainee existingTrainee, Trainee updatedTrainee) {
        existingTrainee.setFirstName(updatedTrainee.getFirstName());
        existingTrainee.setLastName(updatedTrainee.getLastName());
        existingTrainee.setDateOfBirth(updatedTrainee.getDateOfBirth());
        existingTrainee.setAddress(updatedTrainee.getAddress());
        existingTrainee.setIsActive(updatedTrainee.getIsActive());
    }
}
