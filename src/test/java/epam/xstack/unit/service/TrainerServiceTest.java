package epam.xstack.unit.service;

import epam.xstack.dao.TrainerDAO;
import epam.xstack.dto.training.TrainingGetListRequestDTO;
import epam.xstack.exception.NoSuchTrainingTypeException;
import epam.xstack.exception.PersonAlreadyRegisteredException;
import epam.xstack.model.Trainee;
import epam.xstack.model.Trainer;
import epam.xstack.model.Training;
import epam.xstack.model.TrainingType;

import epam.xstack.service.AuthenticationService;
import epam.xstack.service.TrainerService;
import epam.xstack.service.UserService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TrainerServiceTest {
    @InjectMocks
    TrainerService trainerService;
    @Mock
    TrainerDAO trainerDAO;
    @Mock
    UserService userService;
    @Mock
    AuthenticationService authService;

    private static final String TX_ID = "12345";

    @Test
    void testCreateTrainerSuccess() {
        Trainer createRequest = getCreateRequest();
        Trainer expected = getTestTrainer();

        when(userService.generateUsername(createRequest.getFirstName(), createRequest.getLastName()))
                .thenReturn(expected.getUsername());
        when(userService.generatePassword()).thenReturn(expected.getPassword());
        when(trainerDAO.trainingTypeExistsById(createRequest.getSpecialization().getId()))
                .thenReturn(Optional.of(createRequest.getSpecialization()));
        when(trainerDAO.findAllByUsernamePartialMatch(expected.getUsername())).thenReturn(Collections.emptyList());
        doNothing().when(trainerDAO).save(anyString(), any(Trainer.class));

        Trainer actual = trainerService.createTrainer(TX_ID, createRequest);

        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void testCreateTrainerAlreadyRegistered() {
        Trainer createRequest = getCreateRequest();
        Trainer expected = getTestTrainer();

        when(userService.generateUsername(createRequest.getFirstName(), createRequest.getLastName()))
                .thenReturn(expected.getUsername());
        when(userService.generatePassword()).thenReturn(expected.getPassword());
        when(trainerDAO.trainingTypeExistsById(createRequest.getSpecialization().getId()))
                .thenReturn(Optional.of(createRequest.getSpecialization()));
        when(trainerDAO.findAllByUsernamePartialMatch(expected.getUsername())).thenReturn(List.of(expected));

        Assertions.assertThrows(PersonAlreadyRegisteredException.class,
                () -> trainerService.createTrainer(TX_ID, createRequest));
    }

    @Test
    void testCreateTrainerNoSuchSpecialization() {
        Trainer createRequest = getCreateRequest();
        Trainer expected = getTestTrainer();

        when(userService.generateUsername(createRequest.getFirstName(), createRequest.getLastName()))
                .thenReturn(expected.getUsername());
        when(userService.generatePassword()).thenReturn(expected.getPassword());
        when(trainerDAO.trainingTypeExistsById(createRequest.getSpecialization().getId()))
                .thenReturn(Optional.empty());

        Assertions.assertThrows(NoSuchTrainingTypeException.class,
                () -> trainerService.createTrainer(TX_ID, createRequest));
    }

    @Test
    void testFindByIdSuccess() {
        Trainer expected = getTestTrainer();

        when(authService.authenticate(anyString(), anyLong(), anyString(), anyString())).thenReturn(true);
        when(trainerDAO.findById(anyString(), anyLong())).thenReturn(Optional.of(expected));

        Optional<Trainer> actual = trainerService.findById(TX_ID, 1, "test", "test");

        assertThat(actual).isPresent();
        assertThat(actual).contains(expected);
        assertThat(actual.get().getUsername()).isNull();
    }

    @Test
    void testFindByIdBadCredentials() {
        when(authService.authenticate(anyString(), anyLong(), anyString(), anyString())).thenReturn(false);

        Optional<Trainer> actual = trainerService.findById(TX_ID, 1, "test", "test");

        assertThat(actual).isEmpty();
    }

    @Test
    void testUpdateSuccess() {
        Trainer expected = getTestTrainer();

        when(authService.authenticate(anyString(), anyLong(), anyString(), anyString())).thenReturn(true);
        when(trainerDAO.trainingTypeExistsById(expected.getSpecialization().getId()))
                .thenReturn(Optional.of(expected.getSpecialization()));
        when(trainerDAO.update(anyString(), any(Trainer.class))).thenReturn(Optional.of(expected));

        Optional<Trainer> actual = trainerService.update(TX_ID, expected, "test", "test");

        assertThat(actual).isPresent();
        assertThat(actual).contains(expected);
        assertThat(actual.get().getUsername()).isNotNull();
    }

    @Test
    void testUpdateNoSuchSpecialization() {
        Trainer expected = getTestTrainer();

        when(authService.authenticate(anyString(), anyLong(), anyString(), anyString())).thenReturn(true);
        when(trainerDAO.trainingTypeExistsById(expected.getSpecialization().getId()))
                .thenReturn(Optional.empty());

        Assertions.assertThrows(NoSuchTrainingTypeException.class,
                () -> trainerService.update(TX_ID, expected, "test", "test"));
    }

    @Test
    void testUpdateBadCredentials() {
        Trainer expected = getTestTrainer();

        when(authService.authenticate(anyString(), anyLong(), anyString(), anyString())).thenReturn(false);

        Optional<Trainer> actual = trainerService.update(TX_ID, expected, "test", "test");
        assertThat(actual).isEmpty();
    }


    @Test
    void testGetTrainingsWithFilteringNoFilters() {
        TrainingGetListRequestDTO request = getEmptyFiltersRequest();
        List<Training> expectedList = getTestTrainings();

        when(authService.authenticate(anyString(), anyLong(), anyString(), anyString())).thenReturn(true);
        when(trainerDAO.getTrainingsWithFiltering(TX_ID, 1, request)).thenReturn(expectedList);

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

        when(authService.authenticate(anyString(), anyLong(), anyString(), anyString())).thenReturn(false);

        List<Training> actualList = trainerService.getTrainingsWithFiltering(
                TX_ID, 1, "test", "test", request);

        assertThat(actualList).isEmpty();
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
