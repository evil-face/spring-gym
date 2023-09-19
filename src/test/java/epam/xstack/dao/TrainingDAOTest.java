package epam.xstack.dao;

import epam.xstack.config.TestConfig;
import epam.xstack.model.Trainee;
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
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringJUnitConfig(TestConfig.class)
@AutoConfigureEmbeddedDatabase(refresh = AutoConfigureEmbeddedDatabase.RefreshMode.AFTER_CLASS)
@Transactional
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TrainingDAOTest {
    @Autowired
    TrainingDAO trainingDAO;
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
        Training expected = getTestTraining();
        trainingDAO.save(expected);

        Session session = sessionFactory.getCurrentSession();
        Training actual = session.get(Training.class, expected.getId());

        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void testFindAll() {
        List<Training> actual = trainingDAO.findAll();

        assertThat(actual).hasSizeGreaterThanOrEqualTo(20);
    }

    @Test
    void testFindById() {
        Optional<Training> actual = trainingDAO.findById(7);

        assertThat(actual).isPresent();
        assertThat(actual.get().getTrainingName()).isEqualTo("Cardio Workout 2");
    }

    @Test
    void testFindByIdNotExist() {
        Optional<Training> actual = trainingDAO.findById(100);

        assertThat(actual).isEmpty();
    }

    private Training getTestTraining() {
        Session session = sessionFactory.getCurrentSession();
        Trainee trainee = session.get(Trainee.class, 5L);
        Trainer trainer = session.get(Trainer.class, 9L);

        return new Training(trainee, trainer, "test training", new TrainingType(3, "Yoga"),
                LocalDate.now(), 60);
    }
}
