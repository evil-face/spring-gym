package epam.xstack.integration.dao;

import epam.xstack.config.TestConfig;
import epam.xstack.dao.TraineeDAO;
import epam.xstack.dto.training.TrainingGetListRequestDTO;
import epam.xstack.exception.EntityNotFoundException;
import epam.xstack.exception.ForbiddenException;
import epam.xstack.model.Trainee;
import epam.xstack.model.Trainer;
import epam.xstack.model.Training;
import io.zonky.test.db.AutoConfigureEmbeddedDatabase;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@SpringJUnitConfig(TestConfig.class)
@WebAppConfiguration
@AutoConfigureEmbeddedDatabase(refresh = AutoConfigureEmbeddedDatabase.RefreshMode.AFTER_CLASS)
@Transactional
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TraineeDAOTest {
    @Autowired
    TraineeDAO traineeDAO;
    @Autowired
    private DataSource dataSource;
    @Autowired
    private SessionFactory sessionFactory;
    private static final String TX_ID = "12345";

    @BeforeAll
    void initDatabase() {
        ResourceDatabasePopulator populator = new ResourceDatabasePopulator();
        populator.addScript(new ClassPathResource("init.sql"));
        populator.execute(dataSource);
    }

    @Test
    void testSave() {
        Trainee expected = getTestTrainee();
        traineeDAO.save(TX_ID, expected);

        Session session = sessionFactory.getCurrentSession();
        Trainee actual = session.get(Trainee.class, expected.getId());

        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void testFindByIdSuccess() {
        Session session = sessionFactory.getCurrentSession();
        Trainer actualTrainer = session.get(Trainer.class, 7L);

        Optional<Trainee> actual = traineeDAO.findById(TX_ID,2);

        assertThat(actual).isPresent();
        assertThat(actual.get().getFirstName()).isEqualTo("Bob");
        assertThat(actual.get().getTrainers()).isNotEmpty();
        assertThat(actual.get().getTrainers()).contains(actualTrainer);
    }

    @Test
    void testFindByIdNotExist() {
        Optional<Trainee> actual = traineeDAO.findById(TX_ID, 1000);

        assertThat(actual).isEmpty();
    }

    @Test
    void testFindByUsernameSuccess() {
        Session session = sessionFactory.getCurrentSession();
        Trainer actualTrainer = session.get(Trainer.class, 7L);

        Optional<Trainee> actual = traineeDAO.findByUsername(TX_ID,"bob");

        assertThat(actual).isPresent();
        assertThat(actual.get().getFirstName()).isEqualTo("Bob");
        assertThat(actual.get().getTrainers()).isNotEmpty();
        assertThat(actual.get().getTrainers()).contains(actualTrainer);
    }

    @Test
    void testFindByUsernameNotExist() {
        Optional<Trainee> actual = traineeDAO.findByUsername(TX_ID, "no such person");

        assertThat(actual).isEmpty();
    }


    @Test
    void testUpdateSuccess() {
        Session session = sessionFactory.getCurrentSession();
        Trainee beforeUpdate = getTestTraineeForUpdate();
        beforeUpdate.setUsername("charlie.brown");
        beforeUpdate.setAddress("999 Oak St");

        Optional<Trainee> afterUpdateOpt = traineeDAO.update(TX_ID, beforeUpdate);
        Trainee afterUpdate = session.get(Trainee.class, beforeUpdate.getId());

        assertThat(afterUpdateOpt).isPresent();
        assertThat(afterUpdate.getUsername()).isEqualTo("charlie.brown");
        assertThat(afterUpdate.getAddress()).isEqualTo("999 Oak St");
        assertThat(afterUpdate.getTrainers()).isNotEmpty();
    }

    @Test
    void testUpdateNotExist() {
        Trainee beforeUpdate = getTestTraineeForUpdate();
        beforeUpdate.setId(100);

        Optional<Trainee> afterUpdateOpt = traineeDAO.update(TX_ID, beforeUpdate);

        assertThat(afterUpdateOpt).isEmpty();
    }

    @Test
    void testDeleteSuccess() {
        Session session = sessionFactory.getCurrentSession();
        Trainee trainee = getTestTraineeForDeletion();

        traineeDAO.save(TX_ID, trainee);
        Trainee beforeDeletion = session.get(Trainee.class, trainee.getId());
        assertThat(beforeDeletion).isNotNull();

        traineeDAO.delete(TX_ID, beforeDeletion);
        Trainee afterDeletion = session.get(Trainee.class, trainee.getId());
        assertThat(afterDeletion).isNull();
    }

    @Test
    void testDeleteNotExist() {
        Session session = sessionFactory.getCurrentSession();
        Trainee trainee = getTestTraineeForDeletion();
        trainee.setId(100);

        Assertions.assertThrows(EntityNotFoundException.class,
                () -> traineeDAO.delete(TX_ID, trainee));
    }

    @Test
    void testChangeActivationStatusSuccess() {
        Session session = sessionFactory.getCurrentSession();
        Trainee beforeUpdate = getTestTraineeForUpdate();

        traineeDAO.changeActivationStatus(TX_ID, beforeUpdate.getId(), false, beforeUpdate.getUsername());
        Trainee afterUpdate = session.get(Trainee.class, beforeUpdate.getId());

        assertThat(afterUpdate.getIsActive()).isFalse();
    }

    @Test
    void testChangeActivationStatusNotExist() {
        Assertions.assertThrows(EntityNotFoundException.class,
                () -> traineeDAO.changeActivationStatus(TX_ID, 100, false, "no such person"));
    }

    @Test
    void testGetTrainingsWithFilteringNoFilters() {
        Trainee trainee = getTestTraineeForUpdate();
        TrainingGetListRequestDTO request = new TrainingGetListRequestDTO();
        request.setUsername(trainee.getUsername());

        List<Training> actualList = traineeDAO.getTrainingsWithFiltering(TX_ID, trainee.getId(), request);

        assertThat(actualList)
                .hasSameSizeAs(trainee.getTrainingList())
                .hasSameElementsAs(trainee.getTrainingList());
    }

    @Test
    void testGetTrainingsWithFilteringFullResponse() {
        Trainee trainee = getTestTraineeForUpdate();
        TrainingGetListRequestDTO request = new TrainingGetListRequestDTO();
        request.setUsername(trainee.getUsername());
        request.setPeriodFrom(LocalDate.of(2020, 1, 1));
        request.setPeriodTo(LocalDate.of(2024, 1, 1));

        List<Training> actualList = traineeDAO.getTrainingsWithFiltering(TX_ID, trainee.getId(), request);

        assertThat(actualList)
                .hasSameSizeAs(trainee.getTrainingList())
                .hasSameElementsAs(trainee.getTrainingList());
    }

    @Test
    void testGetTrainingsWithFilteringDateFiltered() {
        Trainee trainee = getTestTraineeForUpdate();
        TrainingGetListRequestDTO request = new TrainingGetListRequestDTO();
        request.setUsername(trainee.getUsername());
        request.setPeriodFrom(LocalDate.of(2023, 10, 2));
        request.setPeriodTo(LocalDate.of(2023, 10, 11));

        List<Training> actualList = traineeDAO.getTrainingsWithFiltering(TX_ID, trainee.getId(), request);

        assertThat(actualList).hasSize(3);
    }

    @Test
    void testGetTrainingsWithFilteringTrainerFiltered() {
        Trainee trainee = getTestTraineeForUpdate();
        TrainingGetListRequestDTO request = new TrainingGetListRequestDTO();
        request.setUsername(trainee.getUsername());
        request.setTrainerName("trainer2");

        List<Training> actualList = traineeDAO.getTrainingsWithFiltering(TX_ID, trainee.getId(), request);

        assertThat(actualList).hasSize(2);
        actualList.forEach(training -> assertThat(training.getTrainer().getUsername()).isEqualTo("trainer2"));
    }

    @Test
    void testGetTrainingsWithFilteringTrainingTypeFiltered() {
        Trainee trainee = getTestTraineeForUpdate();
        TrainingGetListRequestDTO request = new TrainingGetListRequestDTO();
        request.setUsername(trainee.getUsername());
        request.setTrainingType(3L);

        List<Training> actualList = traineeDAO.getTrainingsWithFiltering(TX_ID, trainee.getId(), request);

        assertThat(actualList).hasSize(4);
        actualList.forEach(training -> assertThat(training.getTrainingType().getId()).isEqualTo(3L));
    }

    @Test
    void testGetTrainingsWithFilteringNoOutput() {
        Trainee trainee = getTestTraineeForUpdate();
        TrainingGetListRequestDTO request = new TrainingGetListRequestDTO();
        request.setUsername(trainee.getUsername());
        request.setTrainerName("no such person");

        List<Training> actualList = traineeDAO.getTrainingsWithFiltering(TX_ID, trainee.getId(), request);

        assertThat(actualList).isEmpty();
    }

    @Test
    void testGetTrainingsWithFilteringTraineeNotExist() {
        Assertions.assertThrows(EntityNotFoundException.class,
                () -> traineeDAO.getTrainingsWithFiltering(TX_ID, 1000, new TrainingGetListRequestDTO()));
    }

    @Test
    void testFindAllByUsernamePartialMatch() {
        String username = "eve11";

        List<Trainee> actualList = traineeDAO.findAllByUsernamePartialMatch(username);

        assertThat(actualList).hasSize(1);
        assertThat(actualList.get(0).getLastName()).isEqualTo("Davis");
    }

    @Test
    void testUpdateTrainerListSuccess() {
        Session session = sessionFactory.getCurrentSession();
        Trainee trainee = session.get(Trainee.class, 2L);
        Set<Trainer> trainerListBeforeUpdate = trainee.getTrainers();
        List<Trainer> trainerListForUpdate = getTrainerListForUpdate();

        List<Trainer> actualList = traineeDAO.updateTrainerList(TX_ID, trainee.getId(),
                trainee.getUsername(), trainerListForUpdate);

        assertThat(actualList).isNotEmpty();
        assertThat(trainee.getTrainers()).hasSameElementsAs(trainerListForUpdate);
        assertThat(trainee.getTrainers()).doesNotContainAnyElementsOf(trainerListBeforeUpdate);
    }

    @Test
    void testUpdateTrainerListTraineeNotExist() {
        Assertions.assertThrows(EntityNotFoundException.class,
                () -> traineeDAO.updateTrainerList(TX_ID, 100L, null, null));
    }

    @Test
    void testUpdateTrainerListForbiddenAccess() {
        Assertions.assertThrows(ForbiddenException.class,
                () -> traineeDAO.updateTrainerList(TX_ID, 2L, "wrong username", null));
    }

    private Trainee getTestTrainee() {
        return new Trainee("testname", "testsurname",
                "testname.testsurname", "testpassword", true,
                LocalDate.now().minusDays(1), "testcity");
    }

    private Trainee getTestTraineeForDeletion() {
        return new Trainee("deleteme", "deleteme", "deleteme.deleteme", "deleteme",
                true, LocalDate.now().minusDays(1), "deleteme");
    }

    private Trainee getTestTraineeForUpdate() {
        Session session = sessionFactory.getCurrentSession();
        return session.get(Trainee.class, 3L);
    }

    private List<Trainer> getTrainerListForUpdate() {
        Session session = sessionFactory.getCurrentSession();
        Trainer trainer1 = session.get(Trainer.class, 9L);
        Trainer trainer2 = session.get(Trainer.class, 10L);

        return List.of(trainer1, trainer2);
    }
}
