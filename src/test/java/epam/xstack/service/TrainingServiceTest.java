package epam.xstack.service;

import epam.xstack.dao.TrainingDAO;
import epam.xstack.model.Trainee;
import epam.xstack.model.Trainer;
import epam.xstack.model.Training;
import epam.xstack.model.TrainingType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.naming.AuthenticationException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TrainingServiceTest {
    @InjectMocks
    TrainingService trainingService;
    @Mock
    TrainingDAO trainingDAO;
    @Mock
    AuthenticationService authService;

    @Test
    void testCreateTraining() {
        String login = "test";
        String password = "test";
        Training training = getTestTraining();

        when(authService.authenticate(login, password)).thenReturn(true);

        Assertions.assertDoesNotThrow(
                () -> trainingService.createTraining(
                        training.getTrainee(),
                        training.getTrainer(),
                        training.getTrainingName(),
                        training.getTrainingType(),
                        training.getTrainingDate(),
                        training.getTrainingDuration(),
                        login, password
                ));
    }

    @Test
    void testCreateTrainingBadCredentials() {
        String login = "test";
        String password = "test";
        Training training = getTestTraining();

        when(authService.authenticate(login, password)).thenReturn(false);

        Assertions.assertThrows(AuthenticationException.class,
                () -> trainingService.createTraining(
                        training.getTrainee(),
                        training.getTrainer(),
                        training.getTrainingName(),
                        training.getTrainingType(),
                        training.getTrainingDate(),
                        training.getTrainingDuration(),
                        login, password
                ));
    }

    @Test
    void testFindAll() throws AuthenticationException {
        List<Training> expected = getTestTrainings();
        String login = "test";
        String password = "test";

        when(trainingDAO.findAll()).thenReturn(expected);
        when(authService.authenticate(login, password)).thenReturn(true);

        List<Training> actual = trainingService.findAll(login, password);

        assertThat(actual).hasSize(5).containsAll(expected);
    }

    @Test
    void testFindAllBadCredentials() {
        String login = "test";
        String password = "test";

        when(authService.authenticate(login, password)).thenReturn(false);

        Assertions.assertThrows(AuthenticationException.class, () -> trainingService.findAll(login, password));
    }

    @Test
    void testFindById() throws AuthenticationException {
        Training expected = getTestTraining();
        String login = "test";
        String password = "test";

        when(trainingDAO.findById(1)).thenReturn(Optional.of(expected));
        when(authService.authenticate(login, password)).thenReturn(true);

        Optional<Training> actual = trainingService.findById(1, login, password);

        assertThat(actual).contains(expected);
    }

    @Test
    void testFindByIdBadCredentials() {
        String login = "test";
        String password = "test";

        when(authService.authenticate(login, password)).thenReturn(false);

        Assertions.assertThrows(AuthenticationException.class, () -> trainingService.findById(1, login, password));
    }

    private Training getTestTraining() {
        return new Training(getTestTrainee(), getTestTrainer(),
                "First visit", getTestTrainingType(), LocalDate.now(), 60);
    }

    private Trainee getTestTrainee() {
        return new Trainee("Weak", "Dude", "weak.dude",
                "weakpassword", true, LocalDate.now(), "Weak city");
    }

    private Trainer getTestTrainer() {
        return new Trainer("Miguel", "Rodriguez", "miguel.rodriguez",
                "qwerty", true, getTestTrainingType());
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
                new Training(getTestTrainee(), getTestTrainer(), "First visit",
                        getTestTrainingType(), LocalDate.now(), 200),
                new Training(getTestTrainee(), getTestTrainer(), "Second visit",
                        getTestTrainingType(), LocalDate.now(), 200)
        );
    }
}
