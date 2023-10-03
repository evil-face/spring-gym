package epam.xstack.integration.dao;

import epam.xstack.config.TestConfig;
import epam.xstack.dao.TrainerDAO;
import epam.xstack.dto.training.TrainingGetListRequestDTO;
import epam.xstack.exception.EntityNotFoundException;
import epam.xstack.model.Trainee;
import epam.xstack.model.Trainer;
import epam.xstack.model.Training;
import epam.xstack.model.TrainingType;
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

@SpringJUnitConfig(TestConfig.class)
@WebAppConfiguration
@AutoConfigureEmbeddedDatabase(refresh = AutoConfigureEmbeddedDatabase.RefreshMode.AFTER_CLASS)
@Transactional
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TrainerDAOTest {
    @Autowired
    TrainerDAO trainerDAO;
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
        Trainer expected = getTestTrainer();
        trainerDAO.save(TX_ID, expected);

        Session session = sessionFactory.getCurrentSession();
        Trainer actual = session.get(Trainer.class, expected.getId());

        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void testFindAll() {
        List<Trainer> actualList = trainerDAO.findAll();

        assertThat(actualList).hasSize(5);
    }

    @Test
    void testFindByIdSuccess() {
        Session session = sessionFactory.getCurrentSession();
        Trainee actualTrainee = session.get(Trainee.class, 1L);

        Optional<Trainer> actual = trainerDAO.findById(TX_ID, 6);

        assertThat(actual).isPresent();
        assertThat(actual.get().getFirstName()).isEqualTo("Trainer1");
        assertThat(actual.get().getTrainees()).isNotEmpty();
        assertThat(actual.get().getTrainees()).contains(actualTrainee);
    }

    @Test
    void testFindByIdNotExist() {
        Optional<Trainer> actual = trainerDAO.findById(TX_ID, 1000);

        assertThat(actual).isEmpty();
    }

    @Test
    void testFindByUsernameSuccess() {
        Session session = sessionFactory.getCurrentSession();
        Trainee actualTrainee = session.get(Trainee.class, 1L);

        Optional<Trainer> actual = trainerDAO.findByUsername(TX_ID, "trainer1");

        assertThat(actual).isPresent();
        assertThat(actual.get().getFirstName()).isEqualTo("Trainer1");
        assertThat(actual.get().getTrainees()).isNotEmpty();
        assertThat(actual.get().getTrainees()).contains(actualTrainee);
    }

    @Test
    void testFindByUsernameNotExist() {
        Optional<Trainer> actual = trainerDAO.findByUsername(TX_ID, "no such person");

        assertThat(actual).isEmpty();
    }


    @Test
    void testUpdateSuccess() {
        Session session = sessionFactory.getCurrentSession();
        Trainer beforeUpdate = getTestTrainerForUpdate();
        beforeUpdate.setUsername("updated.username");
        beforeUpdate.setSpecialization(new TrainingType(3, "Yoga"));

        Optional<Trainer> afterUpdateOpt = trainerDAO.update(TX_ID, beforeUpdate);
        Trainer afterUpdate = session.get(Trainer.class, beforeUpdate.getId());

        assertThat(afterUpdateOpt).isPresent();
        assertThat(afterUpdate.getUsername()).isEqualTo("updated.username");
        assertThat(afterUpdate.getSpecialization().getId()).isEqualTo(3);
        assertThat(afterUpdate.getTrainees()).isNotEmpty();
    }

    @Test
    void testUpdateNotExist() {
        Trainer beforeUpdate = getTestTrainerForUpdate();
        beforeUpdate.setId(100);

        Optional<Trainer> afterUpdateOpt = trainerDAO.update(TX_ID, beforeUpdate);

        assertThat(afterUpdateOpt).isEmpty();
    }

    @Test
    void testChangeActivationStatusSuccess() {
        Session session = sessionFactory.getCurrentSession();
        Trainer beforeUpdate = getTestTrainerForUpdate();

        trainerDAO.changeActivationStatus(TX_ID, beforeUpdate.getId(), false, beforeUpdate.getUsername());
        Trainer afterUpdate = session.get(Trainer.class, beforeUpdate.getId());

        assertThat(afterUpdate.getIsActive()).isFalse();
    }

    @Test
    void testChangeActivationStatusNotExist() {
        Assertions.assertThrows(EntityNotFoundException.class,
                () -> trainerDAO.changeActivationStatus(TX_ID, 100, false, "no such person"));
    }

    @Test
    void testGetTrainingsWithFilteringNoFilters() {
        Trainer trainer = getTestTrainerForUpdate();
        TrainingGetListRequestDTO request = new TrainingGetListRequestDTO();
        request.setUsername(trainer.getUsername());

        List<Training> actualList = trainerDAO.getTrainingsWithFiltering(TX_ID, trainer.getId(), request);

        assertThat(actualList).hasSameSizeAs(trainer.getTrainingList());
        assertThat(actualList).hasSameElementsAs(trainer.getTrainingList());
    }

    @Test
    void testGetTrainingsWithFilteringFullResponse() {
        Trainer trainer = getTestTrainerForUpdate();
        TrainingGetListRequestDTO request = new TrainingGetListRequestDTO();
        request.setUsername(trainer.getUsername());
        request.setPeriodFrom(LocalDate.of(2020, 1, 1));
        request.setPeriodTo(LocalDate.of(2024, 1, 1));

        List<Training> actualList = trainerDAO.getTrainingsWithFiltering(TX_ID, trainer.getId(), request);

        assertThat(actualList).hasSameSizeAs(trainer.getTrainingList());
        assertThat(actualList).hasSameElementsAs(trainer.getTrainingList());
    }

    @Test
    void testGetTrainingsWithFilteringDateFiltered() {
        Trainer trainer = getTestTrainerForUpdate();
        TrainingGetListRequestDTO request = new TrainingGetListRequestDTO();
        request.setUsername(trainer.getUsername());
        request.setPeriodFrom(LocalDate.of(2023, 10, 1));
        request.setPeriodTo(LocalDate.of(2023, 10, 11));

        List<Training> actualList = trainerDAO.getTrainingsWithFiltering(TX_ID, trainer.getId(), request);

        assertThat(actualList).hasSize(3);
    }

    @Test
    void testGetTrainingsWithFilteringTraineeFiltered() {
        Trainer trainer = getTestTrainerForUpdate();
        TrainingGetListRequestDTO request = new TrainingGetListRequestDTO();
        request.setUsername(trainer.getUsername());
        request.setPassword(trainer.getPassword());
        request.setTraineeName("charlie");

        List<Training> actualList = trainerDAO.getTrainingsWithFiltering(TX_ID, trainer.getId(), request);

        assertThat(actualList).hasSize(2);
        actualList.forEach(training -> assertThat(training.getTrainee().getUsername()).isEqualTo("charlie"));
    }

    @Test
    void testGetTrainingsWithFilteringNoOutput() {
        Trainer trainer = getTestTrainerForUpdate();
        TrainingGetListRequestDTO request = new TrainingGetListRequestDTO();
        request.setUsername(trainer.getUsername());
        request.setPassword(trainer.getPassword());
        request.setTraineeName("no such person");

        List<Training> actualList = trainerDAO.getTrainingsWithFiltering(TX_ID, trainer.getId(), request);

        assertThat(actualList).isEmpty();
    }

    @Test
    void testGetTrainingsWithFilteringTraineeNotExist() {
        Assertions.assertThrows(EntityNotFoundException.class,
                () -> trainerDAO.getTrainingsWithFiltering(TX_ID, 1000, new TrainingGetListRequestDTO()));
    }

    @Test
    void testFindAllByUsernamePartialMatch() {
        String username = "trainer100";

        List<Trainer> actualList = trainerDAO.findAllByUsernamePartialMatch(username);

        assertThat(actualList).hasSize(5);
    }

    @Test
    void testTrainingTypeExistsById() {
        TrainingType expected = new TrainingType(3, "Yoga");

        Optional<TrainingType> actual = trainerDAO.trainingTypeExistsById(3);

        assertThat(actual).isPresent();
        assertThat(actual.get()).isEqualTo(expected);
    }

    private Trainer getTestTrainer() {
        return new Trainer("testname", "testsurname", "testname.testsurname",
                "testpassword",true, new TrainingType(3, "Yoga"));
    }

    private Trainer getTestTrainerForUpdate() {
        Session session = sessionFactory.getCurrentSession();
        return session.get(Trainer.class, 7L);
    }
}
