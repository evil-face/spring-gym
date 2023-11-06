package epam.xstack.unit.service;

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
import epam.xstack.repository.TraineeRepository;
import epam.xstack.repository.TrainerRepository;
import epam.xstack.repository.TrainingRepository;
import epam.xstack.service.TraineeService;
import epam.xstack.service.TrainerService;
import epam.xstack.service.TrainingService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
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
    TraineeRepository traineeRepository;

    @Mock
    TrainerRepository trainerRepository;

    @Mock
    TrainingRepository trainingRepository;

    @Spy
    ModelMapper modelMapper;

    private static final String TX_ID = "12345";

    @Test
    void testCreateTraining_Success() {
        TrainingCreateRequestDTO createRequest = getCreateRequest();
        Trainee trainee = getTestTrainee();
        Trainer trainer = getTestTrainer();

        when(traineeService.findByUsername(createRequest.getTraineeUsername()))
                .thenReturn(Optional.of(trainee));
        when(trainerService.findByUsername(createRequest.getTrainerUsername()))
                .thenReturn(Optional.of(trainer));

        Training actual = trainingService.createTraining(TX_ID, createRequest);

        assertThat(actual.getTrainee()).isEqualTo(trainee);
        assertThat(actual.getTrainer()).isEqualTo(trainer);
        assertThat(actual.getTrainingName()).isEqualTo(createRequest.getTrainingName());
        assertThat(actual.getTrainingDate()).isEqualTo(createRequest.getTrainingDate());
        assertThat(actual.getTrainingDuration()).isEqualTo(createRequest.getTrainingDuration());

        verify(trainingRepository, atLeastOnce()).save(any(Training.class));
        verify(traineeService, atLeastOnce()).save(any(Trainee.class));
        verify(trainerService, atLeastOnce()).save(any(Trainer.class));
        verify(trainerService, atLeastOnce()).updateTrainerWorkload(anyString(), any(Training.class), any(Action.class));
    }
    @Test
    void testCreateTraining_NoSuchTrainee() {
        TrainingCreateRequestDTO createRequest = getCreateRequest();

        when(traineeService.findByUsername(createRequest.getTraineeUsername())).thenReturn(Optional.empty());

        Assertions.assertThrows(NoSuchTraineeExistException.class,
                () -> trainingService.createTraining(TX_ID, createRequest));
    }

    @Test
    void testCreateTraining_NoSuchTrainer() {
        TrainingCreateRequestDTO createRequest = getCreateRequest();
        Trainee trainee = getTestTrainee();

        when(trainerService.findByUsername(createRequest.getTrainerUsername())).thenReturn(Optional.empty());
        when(traineeService.findByUsername(createRequest.getTraineeUsername()))
                .thenReturn(Optional.of(trainee));

        Assertions.assertThrows(NoSuchTrainerExistException.class,
                () -> trainingService.createTraining(TX_ID, createRequest));
    }

    @Test
    void testGetTraineeTrainingsWithFiltering_NoFilters() {
        TrainingGetListRequestDTO requestDTO = new TrainingGetListRequestDTO();
        List<Training> trainings = getTestTrainings();

        List<TrainingResponseDTO> expectedList = trainings.stream()
                .map(training -> {
                    training.setTrainee(null);
                    return modelMapper.map(training, TrainingResponseDTO.class);
                }).toList();

        when(trainingRepository.findAll(any(Specification.class))).thenReturn(trainings);

        List<TrainingResponseDTO> actualList = trainingService.getTraineeTrainingsWithFiltering(1L, requestDTO);

        assertThat(actualList).containsExactlyInAnyOrderElementsOf(expectedList);
    }

    @Test
    void testGetTrainerTrainingsWithFiltering_NoFilters() {
        TrainingGetListRequestDTO requestDTO = new TrainingGetListRequestDTO();
        List<Training> trainings = getTestTrainings();

        List<TrainingResponseDTO> expectedList = trainings.stream()
                .map(training -> {
                    training.setTrainer(null);
                    return modelMapper.map(training, TrainingResponseDTO.class);
                }).toList();

        when(trainingRepository.findAll(any(Specification.class))).thenReturn(trainings);

        List<TrainingResponseDTO> actualList = trainingService.getTrainerTrainingsWithFiltering(1L, requestDTO);

        assertThat(actualList).containsExactlyInAnyOrderElementsOf(expectedList);
    }

    private Trainee getTestTrainee() {
        Trainee trainee = new Trainee("Weak", "Dude", "weak.dude",
                "weakpassword", true, LocalDate.now(), "Weak city");
        trainee.setTrainers(new HashSet<>());

        return trainee;
    }

    private Trainee getTestTrainee2() {
        return new Trainee("Strong", "Dude", "strong.dude",
                "strongpassword", true, LocalDate.now(), "Strong city");
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

    private List<Training> getTestTrainings() {
        return List.of(
                new Training(getTestTrainee(), getTestTrainer(), "First visit",
                        getTestTrainingType(), LocalDate.now(), 60),
                new Training(getTestTrainee(), getTestTrainer(), "Second visit",
                        getTestTrainingType(), LocalDate.now(), 90),
                new Training(getTestTrainee(), getTestTrainer(), "Third visit",
                        getTestTrainingType(), LocalDate.now(), 120),
                new Training(getTestTrainee2(), getTestTrainer(), "First visit",
                        getTestTrainingType(), LocalDate.now(), 200),
                new Training(getTestTrainee2(), getTestTrainer(), "Second visit",
                        getTestTrainingType(), LocalDate.now(), 200)
        );
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
