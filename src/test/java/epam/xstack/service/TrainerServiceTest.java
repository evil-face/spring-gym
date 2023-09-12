package epam.xstack.service;

import epam.xstack.dao.TrainerDAO;
import epam.xstack.model.Trainer;
import epam.xstack.model.TrainingType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TrainerServiceTest {
    @InjectMocks
    TrainerService trainerService;
    @Mock
    TrainerDAO trainerDAO;
    @Mock
    UserService userService;

    @Test
    void testCreateTrainer() {
        Trainer expected = getTestTrainer();

        when(userService.generateId()).thenReturn(expected.getId());
        when(userService.generateUsername(expected.getFirstName(), expected.getLastName()))
                .thenReturn(expected.getUsername());
        when(userService.generatePassword()).thenReturn(expected.getPassword());

        Trainer actual = trainerService.createTrainer(expected.getFirstName(), expected.getLastName(),
                true, getTestTrainingType());

        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void testFindAll() {
        List<Trainer> expected = getTestTrainers();

        when(trainerDAO.findAll()).thenReturn(getTestTrainers());

        List<Trainer> actual = trainerService.findAll();

        assertThat(actual).hasSize(3).containsAll(expected);
    }

    @Test
    void testFindById() {
        Trainer expected = getTestTrainer();

        when(trainerDAO.findById("1")).thenReturn(Optional.of(getTestTrainer()));

        Optional<Trainer> actual = trainerService.findById("1");

        assertThat(actual).contains(expected);
    }

    private Trainer getTestTrainer() {
        return new Trainer("1", "Miguel", "Rodriguez",
                "miguel.rodriguez", "qwerty", true,
                getTestTrainingType());
    }

    private TrainingType getTestTrainingType() {
        return new TrainingType("1", "Lifting");
    }

    private List<Trainer> getTestTrainers() {
        return List.of(
                new Trainer("1", "Miguel", "Rodriguez",
                        "miguel.rodriguez", "qwerty",
                        true, getTestTrainingType()),
                new Trainer("2", "Michael", "Shawn",
                        "michael.shawn", "wweerr", true,
                        getTestTrainingType()),
                new Trainer("3", "Ivan", "Popkov",
                        "ivan.popkov", "pass", true,
                        getTestTrainingType())
        );
    }
}
