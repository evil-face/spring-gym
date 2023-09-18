package epam.xstack.dao;

import epam.xstack.config.TestConfig;
import epam.xstack.model.Trainer;
import epam.xstack.model.Training;
import epam.xstack.model.TrainingType;
import io.zonky.test.db.AutoConfigureEmbeddedDatabase;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringJUnitConfig(TestConfig.class)
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

    @BeforeAll
    void initDatabase() {
        ResourceDatabasePopulator populator = new ResourceDatabasePopulator();
        populator.addScript(new ClassPathResource("init.sql"));
        populator.execute(dataSource);
    }

    @Test
    void testSave() {
        Trainer expected = getTestTrainer();
        trainerDAO.save(expected);

        Session session = sessionFactory.getCurrentSession();
        Trainer actual = session.get(Trainer.class, expected.getId());

        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void testFindAll() {
        List<Trainer> actual = trainerDAO.findAll();

        assertThat(actual).hasSizeGreaterThanOrEqualTo(5);
    }

    @Test
    void testFindById() {
        Optional<Trainer> actual = trainerDAO.findById(6);

        assertThat(actual).isPresent();
        assertThat(actual.get().getFirstName()).isEqualTo("Trainer1");
    }

    @Test
    void testFindByIdNotExist() {
        Optional<Trainer> actual = trainerDAO.findById(100);

        assertThat(actual).isEmpty();
    }

    @Test
    void testUpdate() {
        Session session = sessionFactory.getCurrentSession();
        Trainer beforeUpdate = getTrainerForUpdate();
        beforeUpdate.setFirstName("John");
        beforeUpdate.setUsername("john.smith");

        trainerDAO.update(beforeUpdate);

        Trainer afterUpdate = session.get(Trainer.class, beforeUpdate.getId());

        assertThat(afterUpdate.getUsername()).isEqualTo("john.smith");
        assertThat(afterUpdate.getFirstName()).isEqualTo("John");
    }

    @Test
    void testUpdatePassword(){
        Session session = sessionFactory.getCurrentSession();
        trainerDAO.updatePassword(8, "password8updated");

        Trainer afterUpdate = session.get(Trainer.class, 8L);

        assertThat(afterUpdate.getPassword()).isEqualTo("password8updated");
    }

    @Test
    void testTrainingsByTrainerUsername() {
        List<Training> actual = trainerDAO.getTrainingsByTrainerUsername("trainer5");

        assertThat(actual).hasSize(4);
        actual.stream()
                .map(Training::getTrainingName)
                .forEach(training -> assertThat(training).contains("CrossFit"));
    }

    @Test
    void testTrainingsByTrainerUsernameNotExist() {
        List<Training> actual = trainerDAO.getTrainingsByTrainerUsername("no such person");

        assertThat(actual).isEmpty();
    }

    private Trainer getTestTrainer() {
        return new Trainer("testname", "testsurname", "testname.testsurname", "testpassword",
                true, new TrainingType(3, "Yoga"));
    }

    private Trainer getTrainerForUpdate() {
        Session session = sessionFactory.getCurrentSession();
        return session.get(Trainer.class, 7L);
    }
}
