package epam.xstack.service;

import epam.xstack.dao.TraineeDAO;
import epam.xstack.model.Trainee;
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
class TraineeServiceTest {
    private static final long TEST_DATE = 1694429213148L;

    @InjectMocks
    TraineeService traineeService;
    @Mock
    TraineeDAO traineeDAO;
    @Mock
    UserService userService;

    @Test
    void testCreateTrainee() {
        Trainee expected = getTestTrainee();

        when(userService.generateId()).thenReturn(expected.getId());
        when(userService.generateUsername(expected.getFirstName(), expected.getLastName()))
                .thenReturn(expected.getUsername());
        when(userService.generatePassword()).thenReturn(expected.getPassword());

        Trainee actual = traineeService.createTrainee(expected.getFirstName(), expected.getLastName(), true,
                new Date(TEST_DATE), expected.getAddress());

        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void testFindAll() {
        List<Trainee> expected = getTestTrainees();

        when(traineeDAO.findAll()).thenReturn(getTestTrainees());

        List<Trainee> actual = traineeService.findAll();

        assertThat(actual).hasSize(3).containsAll(expected);
    }

    @Test
    void testFindById() {
        Trainee expected = getTestTrainee();

        when(traineeDAO.findById("1")).thenReturn(Optional.of(getTestTrainee()));

        Optional<Trainee> actual = traineeService.findById("1");

        assertThat(actual).contains(expected);
    }

    private Trainee getTestTrainee() {
        return new Trainee("1", "Miguel", "Rodriguez",
                "miguel.rodriguez", "qwerty", true,
                new Date(TEST_DATE), "Mexico");
    }

    private List<Trainee> getTestTrainees() {
        return List.of(
                new Trainee("1", "Miguel", "Rodriguez",
                        "miguel.rodriguez", "qwerty", true,
                        new Date(TEST_DATE), "Mexico"),
                new Trainee("2", "Michael", "Shawn",
                        "michael.shawn", "wweerr", true,
                        new Date(TEST_DATE), "London"),
                new Trainee("3", "Ivan", "Popkov",
                        "ivan.popkov", "pass", true,
                        new Date(TEST_DATE), "Ivangorod")
        );
    }
}
