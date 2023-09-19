package epam.xstack.service;

import epam.xstack.dao.TrainerDAO;
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
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
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
    GymValidator<Trainer> validator;
    @Mock
    AuthenticationService authService;

    @Test
    void testCreateTrainer() {
        Trainer trainer = getTestTrainer();

        when(userService.generateUsername(trainer.getFirstName(), trainer.getLastName()))
                .thenReturn(trainer.getUsername());
        when(userService.generatePassword()).thenReturn(trainer.getPassword());
        when(validator.validate(any(Trainer.class))).thenReturn(Collections.emptySet());

        Assertions.assertDoesNotThrow(
                () -> trainerService.createTrainer(
                        trainer.getFirstName(),
                        trainer.getLastName(),
                        true,
                        trainer.getSpecialization())
        );
    }

    @Test
    void testCreateTrainerBad() {
        Trainer badTrainer = getBadTrainer();

        when(userService.generateUsername(badTrainer.getFirstName(), badTrainer.getLastName()))
                .thenReturn(badTrainer.getUsername());
        when(userService.generatePassword()).thenReturn(badTrainer.getPassword());
        when(validator.validate(any(Trainer.class))).thenReturn(Set.of("violation"));

        Assertions.assertThrows(ValidationException.class,
                () -> trainerService.createTrainer(
                        badTrainer.getFirstName(),
                        badTrainer.getLastName(),
                        true,
                        badTrainer.getSpecialization())
        );
    }

    @Test
    void testFindAll() throws AuthenticationException {
        List<Trainer> expected = getTestTrainers();
        String login = "test";
        String password = "test";

        when(trainerDAO.findAll()).thenReturn(getTestTrainers());
        when(authService.authenticate(login, password)).thenReturn(true);

        List<Trainer> actual = trainerService.findAll(login, password);

        assertThat(actual).hasSize(3).containsAll(expected);
    }

    @Test
    void testFindAllBadCredentials() {
        String login = "test";
        String password = "test";

        when(authService.authenticate(login, password)).thenReturn(false);

        Assertions.assertThrows(AuthenticationException.class, () -> trainerService.findAll(login, password));
    }

    @Test
    void testFindById() throws AuthenticationException {
        Trainer expected = getTestTrainer();
        String login = "test";
        String password = "test";

        when(trainerDAO.findById(1)).thenReturn(Optional.of(getTestTrainer()));
        when(authService.authenticate(login, password)).thenReturn(true);

        Optional<Trainer> actual = trainerService.findById(1, login, password);

        assertThat(actual).contains(expected);
    }

    @Test
    void testFindByIdBadCredentials() {
        String login = "test";
        String password = "test";

        when(authService.authenticate(login, password)).thenReturn(false);

        Assertions.assertThrows(AuthenticationException.class, () -> trainerService.findById(1, login, password));
    }

    @Test
    void testFindByUsername() throws AuthenticationException {
        Trainer expected = getTestTrainer();
        String login = "test";
        String password = "test";

        when(authService.authenticate(login, password)).thenReturn(true);
        when(userService.findByUsername(expected.getUsername())).thenReturn(Optional.of(expected));

        Optional<Trainer> actual = trainerService.findByUsername(expected.getUsername(), login, password);
        assertThat(actual).isPresent().contains(expected);
    }

    @Test
    void testFindByUsernameNotExist() throws AuthenticationException {
        String login = "test";
        String password = "test";

        when(authService.authenticate(login, password)).thenReturn(true);
        when(userService.findByUsername("does not exist")).thenReturn(Optional.empty());

        Optional<Trainer> actual = trainerService.findByUsername("does not exist", login, password);
        assertThat(actual).isEmpty();
    }

    @Test
    void testFindByUsernameBadCredentials() {
        String login = "test";
        String password = "test";

        when(authService.authenticate(login, password)).thenReturn(false);

        Assertions.assertThrows(AuthenticationException.class,
                () -> trainerService.findByUsername("test", login, password));
    }

    @Test
    void testUpdateTrainer() {
        Trainer trainer = getTestTrainer();
        String login = "test";
        String password = "test";

        when(authService.authenticate(login, password)).thenReturn(true);
        when(validator.validate(any(Trainer.class))).thenReturn(Collections.emptySet());

        Assertions.assertDoesNotThrow(() -> trainerService.update(trainer, login, password));
    }

    @Test
    void testUpdateTrainerBad() {
        Trainer trainer = getTestTrainer();
        String login = "test";
        String password = "test";

        when(authService.authenticate(login, password)).thenReturn(true);
        when(validator.validate(any(Trainer.class))).thenReturn(Set.of("violation"));

        Assertions.assertThrows(ValidationException.class, () -> trainerService.update(trainer, login, password));
    }

    @Test
    void testUpdateTrainerBadCredentials() {
        Trainer trainer = getTestTrainer();
        String login = "test";
        String password = "test";

        when(authService.authenticate(login, password)).thenReturn(false);

        Assertions.assertThrows(AuthenticationException.class, () -> trainerService.update(trainer, login, password));
    }

    @Test
    void testUpdatePassword() {
        long id = 1;
        String newPassword = "test1";
        String login = "test";
        String password = "test";

        when(authService.authenticate(login, password)).thenReturn(true);

        Assertions.assertDoesNotThrow(() -> trainerService.updatePassword(id, newPassword, login, password));
    }

    @Test
    void testUpdatePasswordBadCredentials() {
        long id = 1;
        String newPassword = "test1";
        String login = "test";
        String password = "test";

        when(authService.authenticate(login, password)).thenReturn(false);

        Assertions.assertThrows(AuthenticationException.class,
                () -> trainerService.updatePassword(id, newPassword, login, password));
    }

    @Test
    void testChangeActivationStatus() {
        long id = 1;
        String login = "test";
        String password = "test";

        when(authService.authenticate(login, password)).thenReturn(true);

        Assertions.assertDoesNotThrow(() -> trainerService.changeActivationStatus(id, login, password));
    }

    @Test
    void testChangeActivationStatusBadCredentials() {
        long id = 1;
        String login = "test";
        String password = "test";

        when(authService.authenticate(login, password)).thenReturn(false);

        Assertions.assertThrows(AuthenticationException.class,
                () -> trainerService.changeActivationStatus(id, login, password));
    }

    @Test
    void testGetTrainingsByTrainerUsername() throws AuthenticationException {
        String login = "test";
        String password = "test";
        String query = getTestTrainer().getUsername();
        List<Training> expected = getTestTrainings();

        when(authService.authenticate(login, password)).thenReturn(true);
        when(trainerDAO.getTrainingsByTrainerUsername(query)).thenReturn(expected);

        List<Training> actual = trainerService.getTrainingsByTrainerUsername(query, login, password);

        assertThat(actual).containsAll(expected);
    }

    @Test
    void testGetTrainingsByTrainerBadCredentials() {
        String login = "test";
        String password = "test";
        String query = getTestTrainer().getUsername();

        when(authService.authenticate(login, password)).thenReturn(false);

        Assertions.assertThrows(AuthenticationException.class,
                () -> trainerService.getTrainingsByTrainerUsername(query, login, password));
    }

    @Test
    void testGetTrainingsByTrainerUsernameAndTraineeUsername() throws AuthenticationException {
        String login = "test";
        String password = "test";
        String trainer = getTestTrainer().getUsername();
        String trainee = getTestTrainee2().getUsername();

        when(authService.authenticate(login, password)).thenReturn(true);
        when(trainerDAO.getTrainingsByTrainerUsername(trainer)).thenReturn(getTestTrainings());

        List<Training> actual = trainerService.getTrainingsByTrainerUsernameAndTraineeUsername(
                trainer, trainee, login, password);

        assertThat(actual).hasSize(2);
        actual.forEach(training -> {
            assertThat(training.getTrainer().getUsername()).isEqualTo(trainer);
            assertThat(training.getTrainee().getUsername()).isEqualTo(trainee);
            assertThat(training.getTrainee().getUsername()).isNotEqualTo(getTestTrainee1().getUsername());
        });
    }

    @Test
    void testGetTrainingsByTrainerUsernameAndTrainerUsernameBadCredentials() {
        String login = "test";
        String password = "test";
        String trainer = getTestTrainer().getUsername();
        String trainee = getTestTrainee2().getUsername();

        when(authService.authenticate(login, password)).thenReturn(false);

        Assertions.assertThrows(AuthenticationException.class,
                () -> trainerService.getTrainingsByTrainerUsernameAndTraineeUsername(
                        trainer, trainer, login, password));
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

    private Trainer getBadTrainer() {
        return new Trainer("M", "", null,
                "qwerty", true, getTestTrainingType());
    }

    private List<Trainer> getTestTrainers() {
        return List.of(
                new Trainer("Miguel", "Rodriguez", "miguel.rodriguez",
                        "qwerty", true, getTestTrainingType()),
                new Trainer("Michael", "Shawn", "michael.shawn",
                        "wweerr", true, getTestTrainingType()),
                new Trainer("Ivan", "Popkov", "ivan.popkov",
                        "pass", true, getTestTrainingType())
        );
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
}
