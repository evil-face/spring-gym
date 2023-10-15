package epam.xstack.unit.service;

import epam.xstack.dto.training.TrainingGetListRequestDTO;
import epam.xstack.exception.NoSuchTrainingTypeException;
import epam.xstack.exception.PersonAlreadyRegisteredException;
import epam.xstack.exception.UnauthorizedException;
import epam.xstack.model.Trainee;
import epam.xstack.model.Trainer;
import epam.xstack.model.Training;
import epam.xstack.model.TrainingType;

import epam.xstack.repository.TrainerRepository;
import epam.xstack.repository.TrainingRepository;
import epam.xstack.repository.TrainingTypeRepository;
import epam.xstack.service.AuthenticationService;
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
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.atLeastOnce;

@ExtendWith(MockitoExtension.class)
class TrainerServiceTest {
    @InjectMocks
    TrainerService trainerService;
    @Mock
    TrainerRepository trainerRepository;
    @Mock
    TrainingRepository trainingRepository;
    @Mock
    TrainingTypeRepository trainingTypeRepository;
    @Mock
    UserService userService;
    @Mock
    AuthenticationService authService;
    @Mock
    MeterRegistry meterRegistry;

    private static final String TX_ID = "12345";

    @Test
    void testCreateTrainerSuccess() {
        Trainer createRequest = getCreateRequest();
        Trainer expected = getTestTrainer();

        when(userService.generateUsername(createRequest.getFirstName(), createRequest.getLastName()))
                .thenReturn(expected.getUsername());
        when(userService.generatePassword()).thenReturn(expected.getPassword());
        when(trainingTypeRepository.findById(createRequest.getSpecialization().getId()))
                .thenReturn(Optional.of(createRequest.getSpecialization()));
        when(trainerRepository.findByUsernameStartingWith(expected.getUsername())).thenReturn(Collections.emptyList());

        Trainer actual = trainerService.createTrainer(TX_ID, createRequest);

        assertThat(actual).isEqualTo(expected);
        verify(trainerRepository, atLeastOnce()).save(createRequest);
    }

    @Test
    void testCreateTrainerAlreadyRegistered() {
        Trainer createRequest = getCreateRequest();
        Trainer expected = getTestTrainer();

        when(userService.generateUsername(createRequest.getFirstName(), createRequest.getLastName()))
                .thenReturn(expected.getUsername());
        when(userService.generatePassword()).thenReturn(expected.getPassword());
        when(trainingTypeRepository.findById(createRequest.getSpecialization().getId()))
                .thenReturn(Optional.of(createRequest.getSpecialization()));
        when(trainerRepository.findByUsernameStartingWith(expected.getUsername())).thenReturn(List.of(expected));

        Assertions.assertThrows(PersonAlreadyRegisteredException.class,
                () -> trainerService.createTrainer(TX_ID, createRequest));
        verifyNoMoreInteractions(trainerRepository);
    }

    @Test
    void testCreateTrainerNoSuchSpecialization() {
        Trainer createRequest = getCreateRequest();
        Trainer expected = getTestTrainer();

        when(userService.generateUsername(createRequest.getFirstName(), createRequest.getLastName()))
                .thenReturn(expected.getUsername());
        when(userService.generatePassword()).thenReturn(expected.getPassword());
        when(trainingTypeRepository.findById(createRequest.getSpecialization().getId()))
                .thenReturn(Optional.empty());

        Assertions.assertThrows(NoSuchTrainingTypeException.class,
                () -> trainerService.createTrainer(TX_ID, createRequest));
    }

    @Test
    void testFindByIdSuccess() {
        Trainer expected = getTestTrainer();

        when(authService.authenticate(anyString(), anyLong(), anyString(), anyString())).thenReturn(true);
        when(trainerRepository.findById(anyLong())).thenReturn(Optional.of(expected));

        Optional<Trainer> actual = trainerService.findById(TX_ID, 1, "test", "test");

        assertThat(actual).isPresent().contains(expected);
        assertThat(actual.get().getUsername()).isNull();
    }

    @Test
    void testFindByIdBadCredentials() {
        when(authService.authenticate(anyString(), anyLong(), anyString(), anyString()))
                .thenThrow(UnauthorizedException.class);

        Assertions.assertThrows(UnauthorizedException.class,
                () -> trainerService.findById(TX_ID, 1, "test", "test"));

        verifyNoMoreInteractions(trainerRepository);
    }

    @Test
    void testUpdateSuccess() {
        Trainer initial = getTestTrainer();
        Trainer updated = getUpdatedTrainer();

        when(authService.authenticate(anyString(), anyLong(), any(), any())).thenReturn(true);
        when(trainingTypeRepository.findById(initial.getSpecialization().getId()))
                .thenReturn(Optional.of(initial.getSpecialization()));
        when(trainerRepository.findById(anyLong())).thenReturn(Optional.of(initial));

        Optional<Trainer> actual = trainerService.update(TX_ID, updated);

        assertThat(actual).isPresent().contains(updated);
        assertThat(actual.get().getUsername()).isNotNull();
        verify(trainerRepository, atLeastOnce()).save(updated);
    }

    @Test
    void testUpdateNoSuchSpecialization() {
        Trainer expected = getTestTrainer();

        when(authService.authenticate(anyString(), anyLong(), any(), any())).thenReturn(true);
        when(trainingTypeRepository.findById(expected.getSpecialization().getId()))
                .thenReturn(Optional.empty());

        Assertions.assertThrows(NoSuchTrainingTypeException.class,
                () -> trainerService.update(TX_ID, expected));
    }

    @Test
    void testUpdateBadCredentials() {
        when(authService.authenticate(anyString(), anyLong(), any(), any())).thenThrow(UnauthorizedException.class);

        Assertions.assertThrows(UnauthorizedException.class,
                () -> trainerService.update(TX_ID, new Trainer()));

        verifyNoMoreInteractions(trainerRepository);
    }


    @Test
    void testGetTrainingsWithFilteringNoFilters() {
        TrainingGetListRequestDTO request = getEmptyFiltersRequest();
        List<Training> expectedList = getTestTrainings();
        expectedList.forEach(training -> training.setTrainer(null));

        when(authService.authenticate(anyString(), anyLong(), anyString(), anyString())).thenReturn(true);
        when(trainingRepository.findAll(any(Specification.class))).thenReturn(expectedList);

        List<Training> actualList = trainerService.getTrainingsWithFiltering(
                TX_ID, 1, "test", "test", request);

        assertThat(actualList).containsExactlyInAnyOrderElementsOf(expectedList);
        for (Training training : actualList) {
            assertThat(training.getTrainer()).isNull();
        }
    }

    @Test
    void testGetTrainingsWithFilteringNoFiltersBadCredentials() {
        TrainingGetListRequestDTO request = getEmptyFiltersRequest();

        when(authService.authenticate(anyString(), anyLong(), anyString(), anyString()))
                .thenThrow(UnauthorizedException.class);

        Assertions.assertThrows(UnauthorizedException.class,
                () -> trainerService.getTrainingsWithFiltering(
                        TX_ID, 1, "test", "test", request));

        verifyNoMoreInteractions(trainingRepository);
    }

    private Trainer getCreateRequest() {
        Trainer request = new Trainer();
        Trainer trainer = getTestTrainer();

        request.setFirstName(trainer.getFirstName());
        request.setLastName(trainer.getLastName());
        request.setSpecialization(getTestTrainingType());

        return request;
    }

    private Trainer getTestTrainer() {
        return new Trainer("Miguel", "Rodriguez",
                "miguel.rodriguez", "qwerty", true,
                getTestTrainingType());
    }

    private Trainer getUpdatedTrainer() {
        return new Trainer("MiguelUPD", "RodriguezUPD",
                "miguel.rodriguez", "qwerty", false,
                getTestTrainingType());
    }

    private Trainee getTestTrainee1() {
        return new Trainee("Weak", "Dude", "weak.dude",
                "weakpassword", true, LocalDate.now(), "Weak city");
    }

    private Trainee getTestTrainee2() {
        return new Trainee("Strong", "Dude", "strong.dude",
                "strongpassword", true, LocalDate.now(), "Strong city");
    }

    private TrainingType getTestTrainingType() {
        return new TrainingType(1, "Lifting");
    }

    private List<Training> getTestTrainings() {
        return List.of(
                new Training(getTestTrainee1(), getTestTrainer(), "First visit",
                        getTestTrainingType(), LocalDate.now(), 60),
                new Training(getTestTrainee1(), getTestTrainer(), "Second visit",
                        getTestTrainingType(), LocalDate.now(), 90),
                new Training(getTestTrainee1(), getTestTrainer(), "Third visit",
                        getTestTrainingType(), LocalDate.now(), 120),
                new Training(getTestTrainee2(), getTestTrainer(), "First visit",
                        getTestTrainingType(), LocalDate.now(), 200),
                new Training(getTestTrainee2(), getTestTrainer(), "Second visit",
                        getTestTrainingType(), LocalDate.now(), 200)
        );
    }

    private TrainingGetListRequestDTO getEmptyFiltersRequest() {
        TrainingGetListRequestDTO request = new TrainingGetListRequestDTO();
        request.setUsername("test");
        request.setPassword("test");

        return request;
    }
}
