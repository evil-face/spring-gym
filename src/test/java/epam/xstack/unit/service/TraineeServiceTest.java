package epam.xstack.unit.service;

import epam.xstack.dto.trainee.TraineeRequestDTO;
import epam.xstack.dto.training.TrainingGetListRequestDTO;
import epam.xstack.exception.EntityNotFoundException;
import epam.xstack.exception.NoSuchTrainerExistException;
import epam.xstack.exception.PersonAlreadyRegisteredException;
import epam.xstack.exception.UnauthorizedException;
import epam.xstack.model.Trainee;
import epam.xstack.model.Trainer;
import epam.xstack.model.Training;
import epam.xstack.model.TrainingType;
import epam.xstack.repository.TraineeRepository;
import epam.xstack.repository.TrainerRepository;
import epam.xstack.repository.TrainingRepository;
import epam.xstack.service.AuthenticationService;
import epam.xstack.service.TraineeService;
import epam.xstack.service.TrainerService;
import epam.xstack.service.UserService;
import io.micrometer.core.instrument.MeterRegistry;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TraineeServiceTest {
    @InjectMocks
    TraineeService traineeService;
    @Mock
    TraineeRepository traineeRepository;
    @Mock
    TrainerRepository trainerRepository;
    @Mock
    TrainingRepository trainingRepository;
    @Mock
    UserService userService;
    @Mock
    AuthenticationService authService;
    @Mock
    MeterRegistry meterRegistry;

    private static final String TX_ID = "12345";

    @Test
    void testCreateTraineeSuccess() {
        Trainee createRequest = getCreateRequest();
        Trainee expected = getTestTrainee();

        when(userService.generateUsername(createRequest.getFirstName(), createRequest.getLastName()))
                .thenReturn(expected.getUsername());
        when(userService.generatePassword()).thenReturn(expected.getPassword());
        when(traineeRepository.findByUsernameStartingWith(expected.getUsername())).thenReturn(Collections.emptyList());

        Trainee actual = traineeService.createTrainee(TX_ID, createRequest);

        assertThat(actual).isEqualTo(expected);
        verify(traineeRepository, atLeastOnce()).save(createRequest);
    }

    @Test
    void testCreateTraineeAlreadyRegistered() {
        Trainee createRequest = getCreateRequest();
        Trainee expected = getTestTrainee();

        when(userService.generateUsername(createRequest.getFirstName(), createRequest.getLastName()))
                .thenReturn(expected.getUsername());
        when(userService.generatePassword()).thenReturn(expected.getPassword());
        when(traineeRepository.findByUsernameStartingWith(expected.getUsername())).thenReturn(List.of(expected));

        Assertions.assertThrows(PersonAlreadyRegisteredException.class,
                () -> traineeService.createTrainee(TX_ID, createRequest));
        verifyNoMoreInteractions(traineeRepository);
    }

    @Test
    void testFindByIdSuccess() {
        Trainee expected = getTestTrainee();

        when(authService.authenticate(anyString(), anyLong(), anyString(), anyString())).thenReturn(true);
        when(traineeRepository.findById(anyLong())).thenReturn(Optional.of(expected));

        Optional<Trainee> actual = traineeService.findById(TX_ID, 1, "test", "test");

        assertThat(actual).isPresent().contains(expected);
        assertThat(actual.get().getUsername()).isNull();
    }

    @Test
    void testFindByIdBadCredentials() {
        when(authService.authenticate(anyString(), anyLong(), anyString(), anyString()))
                .thenThrow(UnauthorizedException.class);

        Assertions.assertThrows(UnauthorizedException.class,
                () -> traineeService.findById(TX_ID, 1, "test", "test"));

        verifyNoMoreInteractions(traineeRepository);
    }

    @Test
    void testUpdateSuccess() {
        Trainee initial = getTestTrainee();
        Trainee updated = getUpdatedTrainee();

        when(authService.authenticate(anyString(), anyLong(), any(), any())).thenReturn(true);
        when(traineeRepository.findById(anyLong())).thenReturn(Optional.of(initial));

        Optional<Trainee> actual = traineeService.update(TX_ID, updated);

        assertThat(actual).isPresent().contains(updated);
        assertThat(actual.get().getUsername()).isNotNull();
        verify(traineeRepository, atLeastOnce()).save(updated);
    }

    @Test
    void testUpdateBadCredentials() {
        when(authService.authenticate(anyString(), anyLong(), any(), any())).thenThrow(UnauthorizedException.class);

        Assertions.assertThrows(UnauthorizedException.class,
                () -> traineeService.update(TX_ID, new Trainee()));

        verifyNoMoreInteractions(traineeRepository);
    }

    @Test
    void testGetTrainingsWithFilteringNoFilters() {
        TrainingGetListRequestDTO request = getEmptyFiltersRequest();
        List<Training> expectedList = getTestTrainings();
        expectedList.forEach(training -> training.setTrainee(null));

        when(authService.authenticate(anyString(), anyLong(), anyString(), anyString())).thenReturn(true);
        when(trainingRepository.findAll(any(Specification.class))).thenReturn(expectedList);

        List<Training> actualList = traineeService.getTrainingsWithFiltering(
                TX_ID, 1L, "test", "test", request);

        assertThat(actualList).containsExactlyInAnyOrderElementsOf(expectedList);
        for (Training training : actualList) {
            assertThat(training.getTrainee()).isNull();
        }
    }

    @Test
    void testGetTrainingsWithFilteringNoFiltersBadCredentials() {
        TrainingGetListRequestDTO request = getEmptyFiltersRequest();

        when(authService.authenticate(anyString(), anyLong(), anyString(), anyString()))
                .thenThrow(UnauthorizedException.class);

        Assertions.assertThrows(UnauthorizedException.class,
                () -> traineeService.getTrainingsWithFiltering(
                        TX_ID, 1, "test", "test", request));

        verifyNoMoreInteractions(trainingRepository);
    }

    @Test
    void testGetPotentialTrainersForTraineeSuccess() {
        List<Trainer> allTrainers = getAllTestTrainers();
        int initialSize = allTrainers.size();

        Trainee trainee = getTestTrainee();
        Trainer initialTrainer = allTrainers.get(0);
        trainee.setTrainers(Set.of(initialTrainer));

        when(authService.authenticate(anyString(), anyLong(), anyString(), anyString())).thenReturn(true);
        when(traineeRepository.findByUsername(trainee.getUsername())).thenReturn(Optional.of(trainee));
        when(trainerRepository.findAll()).thenReturn(allTrainers);

        List<Trainer> actualList = traineeService.getPotentialTrainersForTrainee(
                TX_ID, 1, trainee.getUsername(), trainee.getPassword());

        assertThat(actualList)
                .hasSizeLessThan(initialSize)
                .doesNotContain(initialTrainer);
    }

    @Test
    void testGetPotentialTrainersForTraineeNoSuchTrainee() {
        when(authService.authenticate(anyString(), anyLong(), anyString(), anyString())).thenReturn(true);
        when(traineeRepository.findByUsername(anyString())).thenReturn(Optional.empty());

        Assertions.assertThrows(EntityNotFoundException.class,
                () -> traineeService.getPotentialTrainersForTrainee(TX_ID, 1, "test", "test"));
    }

    @Test
    void testGetPotentialTrainersForTraineeBadCredentials() {
        when(authService.authenticate(anyString(), anyLong(), anyString(), anyString()))
                .thenThrow(UnauthorizedException.class);

        Assertions.assertThrows(UnauthorizedException.class, () -> {
            traineeService.getPotentialTrainersForTrainee(TX_ID, 1, "test", "test");
        });
    }

    @Test
    void testUpdateTrainerListSuccess() {
        Trainee trainee = getTestTrainee();
        List<Trainer> allTrainers = getAllTestTrainers();
        List<Trainer> updatedTrainersList = List.of(allTrainers.get(0), allTrainers.get(1));
        List<String> updatedTrainersUsernames = List.of(
                allTrainers.get(0).getUsername(), allTrainers.get(1).getUsername());

        TraineeRequestDTO request = new TraineeRequestDTO();
        request.setUsername("miguel.rodriguez");
        request.setTrainers(updatedTrainersUsernames);

        when(trainerRepository.findAll()).thenReturn(allTrainers);
        when(traineeRepository.getReferenceById(anyLong())).thenReturn(trainee);

        List<Trainer> actualList = traineeService.updateTrainerList(TX_ID, 1, request);

        assertThat(actualList)
                .hasSameSizeAs(updatedTrainersUsernames)
                .hasSameElementsAs(updatedTrainersList);
        verify(traineeRepository, atLeastOnce()).save(trainee);
    }

    @Test
    void testUpdateTrainerListNoSuchTrainer() {
        List<Trainer> allTrainers = getAllTestTrainers();
        TraineeRequestDTO request = new TraineeRequestDTO();
        request.setUsername("test");
        request.setTrainers(List.of("no_such_trainer1", "no_such_trainer2"));

        when(trainerRepository.findAll()).thenReturn(allTrainers);

        Assertions.assertThrows(NoSuchTrainerExistException.class,
                () -> traineeService.updateTrainerList(TX_ID, 1, request));
    }

    private Trainee getTestTrainee() {
        return new Trainee("Miguel", "Rodriguez",
                "miguel.rodriguez", "qwerty", true,
                LocalDate.of(1990, 10, 20), "Mexico");
    }

    private Trainee getUpdatedTrainee() {
        return new Trainee("MiguelUPD", "RodriguezUPD",
                "miguel.rodriguez", "qwerty", false,
                LocalDate.of(1999, 11, 22), "MexicoUPD");
    }

    private Trainer getTestTrainer1() {
        return new Trainer("Michael", "Swat",
                "michael.swat", "12345", true,
                getTestTrainingType());
    }

    private Trainer getTestTrainer2() {
        return new Trainer("Robert", "Green",
                "robert.green", "12345", true,
                getTestTrainingType());
    }

    private Trainer getTestTrainer3() {
        return new Trainer("Sergey", "Kozinsky",
                "sergey.kozinskiy", "12345", true,
                getTestTrainingType());
    }

    private Trainer getTestTrainer4() {
        return new Trainer("Lloyd", "Brawl",
                "lloyd.brawl", "12345", true,
                getTestTrainingType());
    }

    private TrainingType getTestTrainingType() {
        return new TrainingType(1, "Lifting");
    }

    private List<Trainer> getAllTestTrainers() {
        return new ArrayList<>(List.of(
                getTestTrainer1(),
                getTestTrainer2(),
                getTestTrainer3(),
                getTestTrainer4()
        ));
    }

    private List<Training> getTestTrainings() {
        return List.of(
                new Training(getTestTrainee(), getTestTrainer1(), "First visit",
                        getTestTrainingType(), LocalDate.now(), 60),
                new Training(getTestTrainee(), getTestTrainer1(), "Second visit",
                        getTestTrainingType(), LocalDate.now(), 90),
                new Training(getTestTrainee(), getTestTrainer2(), "Third visit",
                        getTestTrainingType(), LocalDate.now(), 120)
        );
    }

    private Trainee getCreateRequest() {
        Trainee request = new Trainee();
        Trainee trainee = getTestTrainee();

        request.setFirstName(trainee.getFirstName());
        request.setLastName(trainee.getLastName());
        request.setDateOfBirth(trainee.getDateOfBirth());
        request.setAddress(trainee.getAddress());

        return request;
    }

    private TrainingGetListRequestDTO getEmptyFiltersRequest() {
        TrainingGetListRequestDTO request = new TrainingGetListRequestDTO();
        request.setUsername("test");
        request.setPassword("test");

        return request;
    }
}
