package epam.xstack.service;

import epam.xstack.dao.TraineeDAO;
import epam.xstack.model.Trainee;
import epam.xstack.model.Trainer;
import epam.xstack.model.Training;
import epam.xstack.model.TrainingType;
import epam.xstack.validator.GymValidator;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.naming.AuthenticationException;
import javax.validation.ValidationException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
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
    @Mock
    TrainerService trainerService;
    @Mock
    GymValidator<Trainee> validator;
    @Mock
    AuthenticationService authService;

    @Test
    void testCreateTrainee() {
        Trainee trainee = getTestTrainee();

        when(userService.generateUsername(trainee.getFirstName(), trainee.getLastName()))
                .thenReturn(trainee.getUsername());
        when(userService.generatePassword()).thenReturn(trainee.getPassword());
        when(validator.validate(any(Trainee.class))).thenReturn(Collections.emptySet());

        Assertions.assertDoesNotThrow(
                () -> traineeService.createTrainee(
                        trainee.getFirstName(),
                        trainee.getLastName(),
                        true,
                        new Date(TEST_DATE),
                        trainee.getAddress()));
    }

    @Test
    void testCreateTraineeBad() {
        Trainee badTrainee = getBadTrainee();

        when(userService.generateUsername(badTrainee.getFirstName(), badTrainee.getLastName()))
                .thenReturn(badTrainee.getUsername());
        when(userService.generatePassword()).thenReturn(badTrainee.getPassword());
        when(validator.validate(any(Trainee.class))).thenReturn(Set.of("violation"));

        Assertions.assertThrows(ValidationException.class,
                () -> traineeService.createTrainee(
                        badTrainee.getFirstName(),
                        badTrainee.getLastName(),
                        true,
                        new Date(TEST_DATE),
                        badTrainee.getAddress()));
    }

    @Test
    void testFindAll() throws AuthenticationException {
        List<Trainee> expected = getTestTrainees();
        String login = "test";
        String password = "test";

        when(traineeDAO.findAll()).thenReturn(getTestTrainees());
        when(authService.authenticate(login, password)).thenReturn(true);

        List<Trainee> actual = traineeService.findAll(login, password);

        assertThat(actual).hasSize(3).containsAll(expected);
    }

    @Test
    void testFindAllBadCredentials() {
        String login = "test";
        String password = "test";

        when(authService.authenticate(login, password)).thenReturn(false);

        Assertions.assertThrows(AuthenticationException.class, () -> traineeService.findAll(login, password));
    }

    @Test
    void testFindById() throws AuthenticationException {
        Trainee expected = getTestTrainee();
        String login = "test";
        String password = "test";

        when(traineeDAO.findById(1)).thenReturn(Optional.of(getTestTrainee()));
        when(authService.authenticate(login, password)).thenReturn(true);

        Optional<Trainee> actual = traineeService.findById(1, login, password);

        assertThat(actual).contains(expected);
    }

    @Test
    void testFindByIdBadCredentials() {
        String login = "test";
        String password = "test";

        when(authService.authenticate(login, password)).thenReturn(false);

        Assertions.assertThrows(AuthenticationException.class, () -> traineeService.findById(1, login, password));
    }

    @Test
    void testFindByUsername() throws AuthenticationException {
        Trainee expected = getTestTrainee();
        String login = "test";
        String password = "test";

        when(authService.authenticate(login, password)).thenReturn(true);
        when(userService.findByUsername(expected.getUsername())).thenReturn(Optional.of(expected));

        Optional<Trainee> actual = traineeService.findByUsername(expected.getUsername(), login, password);
        assertThat(actual).isPresent().contains(expected);
    }

    @Test
    void testFindByUsernameNotExist() throws AuthenticationException {
        String login = "test";
        String password = "test";

        when(authService.authenticate(login, password)).thenReturn(true);
        when(userService.findByUsername("does not exist")).thenReturn(Optional.empty());

        Optional<Trainee> actual = traineeService.findByUsername("does not exist", login, password);
        assertThat(actual).isEmpty();
    }

    @Test
    void testFindByUsernameBadCredentials() {
        String login = "test";
        String password = "test";

        when(authService.authenticate(login, password)).thenReturn(false);

        Assertions.assertThrows(AuthenticationException.class,
                () -> traineeService.findByUsername("test", login, password));
    }

    @Test
    void testUpdateTrainee() {
        Trainee trainee = getTestTrainee();
        String login = "test";
        String password = "test";

        when(authService.authenticate(login, password)).thenReturn(true);
        when(validator.validate(any(Trainee.class))).thenReturn(Collections.emptySet());

        Assertions.assertDoesNotThrow(() -> traineeService.update(trainee, login, password));
    }

    @Test
    void testUpdateTraineeBad() {
        Trainee trainee = getTestTrainee();
        String login = "test";
        String password = "test";

        when(authService.authenticate(login, password)).thenReturn(true);
        when(validator.validate(any(Trainee.class))).thenReturn(Set.of("violation"));

        Assertions.assertThrows(ValidationException.class, () -> traineeService.update(trainee, login, password));
    }

    @Test
    void testUpdateTraineeBadCredentials() {
        Trainee trainee = getTestTrainee();
        String login = "test";
        String password = "test";

        when(authService.authenticate(login, password)).thenReturn(false);

        Assertions.assertThrows(AuthenticationException.class, () -> traineeService.update(trainee, login, password));
    }

    @Test
    void testDeleteTrainee() {
        Trainee trainee = getTestTrainee();
        String login = "test";
        String password = "test";

        when(authService.authenticate(login, password)).thenReturn(true);

        Assertions.assertDoesNotThrow(() -> traineeService.delete(trainee, login, password));
    }

    @Test
    void testDeleteTraineeBadCredentials() {
        Trainee trainee = getTestTrainee();
        String login = "test";
        String password = "test";

        when(authService.authenticate(login, password)).thenReturn(false);

        Assertions.assertThrows(AuthenticationException.class, () -> traineeService.delete(trainee, login, password));
    }

    @Test
    void testDeleteTraineeByUsername() {
        String candidate = "deleteme";
        String login = "test";
        String password = "test";

        when(authService.authenticate(login, password)).thenReturn(true);

        Assertions.assertDoesNotThrow(() -> traineeService.deleteByUsername(candidate, login, password));
    }

    @Test
    void testDeleteTraineeByUsernameBadCredentials() {
        String candidate = "deleteme";
        String login = "test";
        String password = "test";

        when(authService.authenticate(login, password)).thenReturn(false);

        Assertions.assertThrows(AuthenticationException.class,
                () -> traineeService.deleteByUsername(candidate, login, password));
    }

    @Test
    void testUpdatePassword() {
        long id = 1;
        String newPassword = "test1";
        String login = "test";
        String password = "test";

        when(authService.authenticate(login, password)).thenReturn(true);

        Assertions.assertDoesNotThrow(() -> traineeService.updatePassword(id, newPassword, login, password));
    }

    @Test
    void testUpdatePasswordBadCredentials() {
        long id = 1;
        String newPassword = "test1";
        String login = "test";
        String password = "test";

        when(authService.authenticate(login, password)).thenReturn(false);

        Assertions.assertThrows(AuthenticationException.class,
                () -> traineeService.updatePassword(id, newPassword, login, password));
    }

    @Test
    void testChangeActivationStatus() {
        long id = 1;
        String login = "test";
        String password = "test";

        when(authService.authenticate(login, password)).thenReturn(true);

        Assertions.assertDoesNotThrow(() -> traineeService.changeActivationStatus(id, login, password));
    }

    @Test
    void testChangeActivationStatusBadCredentials() {
        long id = 1;
        String login = "test";
        String password = "test";

        when(authService.authenticate(login, password)).thenReturn(false);

        Assertions.assertThrows(AuthenticationException.class,
                () -> traineeService.changeActivationStatus(id, login, password));
    }

    @Test
    void testGetTrainingsByTraineeUsername() throws AuthenticationException {
        String login = "test";
        String password = "test";
        String query = "miguel.rodriguez";
        List<Training> expected = getTestTrainings();

        when(authService.authenticate(login, password)).thenReturn(true);
        when(traineeDAO.getTrainingsByTraineeUsername(query)).thenReturn(expected);

        List<Training> actual = traineeService.getTrainingsByTraineeUsername(query, login, password);

        assertThat(actual).containsAll(expected);
    }

    @Test
    void testGetTrainingsByTraineeBadCredentials() {
        String login = "test";
        String password = "test";
        String query = "miguel.rodriguez";

        when(authService.authenticate(login, password)).thenReturn(false);

        Assertions.assertThrows(AuthenticationException.class,
                () -> traineeService.getTrainingsByTraineeUsername(query, login, password));
    }

    @Test
    void testGetTrainingsByTraineeUsernameAndTrainerUsername() throws AuthenticationException {
        String login = "test";
        String password = "test";
        String trainee = "miguel.rodriguez";
        String trainer = "michael.swat";

        when(authService.authenticate(login, password)).thenReturn(true);
        when(traineeDAO.getTrainingsByTraineeUsername(trainee)).thenReturn(getTestTrainings());

        List<Training> actual = traineeService.getTrainingsByTraineeUsernameAndTrainerUsername(
                trainee, trainer, login, password);

        assertThat(actual).hasSize(2);
        actual.forEach(training -> {
                    assertThat(training.getTrainer().getUsername()).isEqualTo(trainer);
                    assertThat(training.getTrainer().getUsername()).isNotEqualTo("robert.green");
                });
    }

    @Test
    void testGetTrainingsByTraineeUsernameAndTrainerUsernameBadCredentials() {
        String login = "test";
        String password = "test";
        String trainee = "miguel.rodriguez";
        String trainer = "michael.swat";

        when(authService.authenticate(login, password)).thenReturn(false);

        Assertions.assertThrows(AuthenticationException.class,
                () -> traineeService.getTrainingsByTraineeUsernameAndTrainerUsername(
                        trainee, trainer, login, password));
    }

    @Test
    void testGetPotentialTrainersForTrainee() throws AuthenticationException {
        String login = "test";
        String password = "test";
        List<Trainer> allTestTrainers = getAllTestTrainers();

        when(trainerService.findAll(login, password)).thenReturn(allTestTrainers);
        when(authService.authenticate(login, password)).thenReturn(true);
        when(traineeDAO.getTrainingsByTraineeUsername(getTestTrainee().getUsername())).thenReturn(getTestTrainings());

        List<Trainer> actual = traineeService.getPotentialTrainersForTrainee(
                getTestTrainee().getUsername(), login, password);

        assertThat(actual).hasSize(2);
        assertThat(actual).contains(getTestTrainer3()).contains(getTestTrainer4());
    }

    private Trainee getTestTrainee() {
        return new Trainee("Miguel", "Rodriguez",
                "miguel.rodriguez", "qwerty", true,
                new Date(TEST_DATE), "Mexico");
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

    private Trainee getBadTrainee() {
        return new Trainee("M", "",
                null, "qwerty", true,
                new Date(), "Mexico");
    }

    private List<Trainee> getTestTrainees() {
        return List.of(
                new Trainee("Miguel", "Rodriguez",
                        "miguel.rodriguez", "qwerty", true,
                        new Date(TEST_DATE), "Mexico"),
                new Trainee("Michael", "Shawn",
                        "michael.shawn", "wweerr", true,
                        new Date(TEST_DATE), "London"),
                new Trainee("Ivan", "Popkov",
                        "ivan.popkov", "pass", true,
                        new Date(TEST_DATE), "Ivangorod")
        );
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
                        getTestTrainingType(), new Date(), 60),
                new Training(getTestTrainee(), getTestTrainer1(), "Second visit",
                        getTestTrainingType(), new Date(), 90),
                new Training(getTestTrainee(), getTestTrainer2(), "Third visit",
                        getTestTrainingType(), new Date(), 120)
        );
    }
}
