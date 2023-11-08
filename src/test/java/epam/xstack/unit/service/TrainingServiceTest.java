package epam.xstack.unit.service;

import epam.xstack.dto.training.TrainingCreateRequestDTO;
import epam.xstack.exception.NoSuchTraineeExistException;
import epam.xstack.exception.NoSuchTrainerExistException;
import epam.xstack.model.Trainee;
import epam.xstack.model.Trainer;
import epam.xstack.model.Training;
import epam.xstack.model.TrainingType;
import epam.xstack.repository.TrainingRepository;
import epam.xstack.service.TraineeService;
import epam.xstack.service.TrainerService;
import epam.xstack.service.TrainingService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TrainingServiceTest {
    @InjectMocks
    TrainingService trainingService;
    @Mock
    TraineeService traineeService;
    @Mock
    TrainerService trainerService;
    @Mock
    TrainingRepository trainingRepository;

    private static final String TX_ID = "12345";

    @Test
    void testCreateTrainingSuccess() {
        TrainingCreateRequestDTO createRequest = getCreateRequest();
        Trainee trainee = getTestTrainee();
        Trainer trainer = getTestTrainer();

        when(traineeService.findByUsername(TX_ID, createRequest.getTraineeUsername()))
                .thenReturn(Optional.of(trainee));
        when(trainerService.findByUsername(TX_ID, createRequest.getTrainerUsername()))
                .thenReturn(Optional.of(trainer));

        Training actual = trainingService.createTraining(TX_ID, createRequest);

        assertThat(actual.getTrainee()).isEqualTo(trainee);
        assertThat(actual.getTrainer()).isEqualTo(trainer);
        assertThat(actual.getTrainingName()).isEqualTo(createRequest.getTrainingName());
        assertThat(actual.getTrainingDate()).isEqualTo(createRequest.getTrainingDate());
        assertThat(actual.getTrainingDuration()).isEqualTo(createRequest.getTrainingDuration());

        verify(trainingRepository, atLeastOnce()).save(any(Training.class));
        verify(traineeService, atLeastOnce()).update(anyString(), any(Trainee.class));
        verify(trainerService, atLeastOnce()).update(anyString(), any(Trainer.class));
    }
    @Test
    void testCreateTrainingNoSuchTrainee() {
        TrainingCreateRequestDTO createRequest = getCreateRequest();

        when(traineeService.findByUsername(TX_ID, createRequest.getTraineeUsername())).thenReturn(Optional.empty());

        Assertions.assertThrows(NoSuchTraineeExistException.class,
                () -> trainingService.createTraining(TX_ID, createRequest));
    }

    @Test
    void testCreateTrainingNoSuchTrainer() {
        TrainingCreateRequestDTO createRequest = getCreateRequest();
        Trainee trainee = getTestTrainee();

        when(trainerService.findByUsername(TX_ID, createRequest.getTrainerUsername())).thenReturn(Optional.empty());
        when(traineeService.findByUsername(TX_ID, createRequest.getTraineeUsername()))
                .thenReturn(Optional.of(trainee));

        Assertions.assertThrows(NoSuchTrainerExistException.class,
                () -> trainingService.createTraining(TX_ID, createRequest));
    }

    private Trainee getTestTrainee() {
        Trainee trainee = new Trainee("Weak", "Dude", "weak.dude",
                "weakpassword", true, LocalDate.now(), "Weak city");
        trainee.setTrainers(new HashSet<>());

        return trainee;
    }

    private Trainer getTestTrainer() {
        Trainer trainer =  new Trainer("Miguel", "Rodriguez", "miguel.rodriguez",
                "qwerty", true, getTestTrainingType());
        trainer.setTrainees(new HashSet<>());

        return trainer;
    }

    private TrainingType getTestTrainingType() {
        return new TrainingType(1, "Lifting");
    }

    private TrainingCreateRequestDTO getCreateRequest() {
        TrainingCreateRequestDTO request = new TrainingCreateRequestDTO();
        request.setTraineeUsername("Trainee");
        request.setTrainerUsername("Trainer");
        request.setTrainingName("Test training");
        request.setTrainingDate(LocalDate.of(2023, 10, 2));
        request.setTrainingDuration(50);

        return request;
    }
}
