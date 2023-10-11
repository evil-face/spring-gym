package epam.xstack.integration.dao;

import epam.xstack.dao.TrainingDAO;
import epam.xstack.model.Trainee;
import epam.xstack.model.Trainer;
import epam.xstack.model.Training;
import epam.xstack.model.TrainingType;
import io.zonky.test.db.AutoConfigureEmbeddedDatabase;
import org.hibernate.Session;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@TestPropertySource(properties = {"spring.sql.init.mode=always"})
@AutoConfigureEmbeddedDatabase(refresh = AutoConfigureEmbeddedDatabase.RefreshMode.AFTER_CLASS)
@Transactional
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
final class TrainingDAOTest {
    @Autowired
    TrainingDAO trainingDAO;

    @Autowired
    private EntityManager entityManager;

    private Session getCurrentSession()  {
        return entityManager.unwrap(Session.class);
    }

    private static final String TX_ID = "12345";

    @Test
    void testSave() {
        Training expected = getTestTraining();
        trainingDAO.save(TX_ID, expected);

        Session session = getCurrentSession();
        Training actual = session.get(Training.class, expected.getId());

        assertThat(actual).isEqualTo(expected);
        assertThat(actual.getTrainee().getTrainers()).contains(expected.getTrainer());
        assertThat(actual.getTrainer().getTrainees()).contains(expected.getTrainee());
    }

    @Test
    void testFindAllTrainingTypes() {
        List<TrainingType> actualList = trainingDAO.findAllTrainingTypes(TX_ID);

        assertThat(actualList).hasSize(5);
    }

    private Training getTestTraining() {
        Session session = getCurrentSession();
        Trainee trainee = session.get(Trainee.class, 5L);
        Trainer trainer = session.get(Trainer.class, 9L);

        return new Training(trainee, trainer, "test training",
                new TrainingType(3, "Yoga"), LocalDate.now(), 60);
    }
}
