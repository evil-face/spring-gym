package epam.xstack.service;

import epam.xstack.dao.TrainingDAO;
import epam.xstack.model.Trainee;
import epam.xstack.model.Trainer;
import epam.xstack.model.Training;
import epam.xstack.model.TrainingType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TrainingServiceTest {
    private static final long TEST_DATE = 1694429213148L;

    @InjectMocks
    TrainingService trainingService;
    @Mock
    TrainingDAO trainingDAO;
    @Mock
    UserService userService;

    @Test
    void testCreateTraining() {
        Training expected = getTestTraining();

        when(userService.generateId()).thenReturn("1");

        Training actual = trainingService.createTraining(expected.getTrainee(), expected.getTrainer(),
                expected.getTrainingName(), expected.getTrainingType(),
                new Date(TEST_DATE), expected.getTrainingDuration());

        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void testFindAll() {
        List<Training> expected = getTestTrainings();

        when(trainingDAO.findAll()).thenReturn(getTestTrainings());

        List<Training> actual = trainingService.findAll();

        assertThat(actual).hasSize(3);
        assertThat(actual).containsAll(expected);
    }

    @Test
    void testFindById() {
        Training expected = getTestTraining();

        when(trainingDAO.findById("1")).thenReturn(Optional.of(getTestTraining()));

        Optional<Training> actual = trainingService.findById("1");

        assertThat(actual).isPresent();
        assertThat(actual.get()).isEqualTo(expected);
    }

    private Trainee getTestTrainee() {
        return new Trainee("1", "Miguel", "Rodriguez",
                "miguel.rodriguez", "qwerty", true,
                new Date(TEST_DATE), "Mexico");
    }

    private Trainer getTestTrainer() {
        return new Trainer("1", "Miguel", "Rodriguez",
                "miguel.rodriguez", "qwerty", true,
                getTestTrainingType());
    }

    private Training getTestTraining() {
        return new Training("1", getTestTrainee(), getTestTrainer(), "First visit",
                getTestTrainingType(), new Date(TEST_DATE), 60);
    }

    private TrainingType getTestTrainingType() {
        return new TrainingType("1", "Lifting");
    }

    private List<Training> getTestTrainings() {
        return List.of(
                new Training("1", getTestTrainee(), getTestTrainer(), "First visit",
                        getTestTrainingType(), new Date(TEST_DATE), 60),
                new Training("2", getTestTrainee(), getTestTrainer(), "Second visit",
                        getTestTrainingType(), new Date(TEST_DATE),80),
                new Training("3", getTestTrainee(), getTestTrainer(), "Third visit",
                        getTestTrainingType(), new Date(TEST_DATE), 100)
        );
    }
}
