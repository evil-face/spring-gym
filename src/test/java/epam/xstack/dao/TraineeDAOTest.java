package epam.xstack.dao;

import epam.xstack.config.TestConfig;
import epam.xstack.model.Trainee;
import epam.xstack.model.Training;
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

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@SpringJUnitConfig(TestConfig.class)
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

    @BeforeAll
    void initDatabase() {
        ResourceDatabasePopulator populator = new ResourceDatabasePopulator();
        populator.addScript(new ClassPathResource("init.sql"));
        populator.execute(dataSource);
    }

    @Test
    void testSave() {
        Trainee expected = getTestTrainee();
        traineeDAO.save(expected);

        Session session = sessionFactory.getCurrentSession();
        Trainee actual = session.get(Trainee.class, expected.getId());

        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void testFindAll() {
        List<Trainee> actual = traineeDAO.findAll();

        assertThat(actual).hasSizeGreaterThanOrEqualTo(5);
    }

    @Test
    void testFindById() {
        Optional<Trainee> actual = traineeDAO.findById(2);

        assertThat(actual).isPresent();
        assertThat(actual.get().getFirstName()).isEqualTo("Bob");
    }

    @Test
    void testFindByIdNotExist() {
        Optional<Trainee> actual = traineeDAO.findById(100);

        assertThat(actual).isEmpty();
    }

    @Test
    void testUpdate() {
        Session session = sessionFactory.getCurrentSession();
        Trainee beforeUpdate = getTraineeForUpdate();
        beforeUpdate.setUsername("charlie.brown");
        beforeUpdate.setAddress("999 Oak St");

        traineeDAO.update(beforeUpdate);

        Trainee afterUpdate = session.get(Trainee.class, beforeUpdate.getId());

        assertThat(afterUpdate.getUsername()).isEqualTo("charlie.brown");
        assertThat(afterUpdate.getAddress()).isEqualTo("999 Oak St");
    }

    @Test
    void testDelete() {
        Session session = sessionFactory.getCurrentSession();
        Trainee trainee = getTestTraineeForDeletion();

        traineeDAO.save(trainee);
        Trainee beforeDeletion = session.get(Trainee.class, trainee.getId());
        assertThat(beforeDeletion).isNotNull();

        traineeDAO.delete(beforeDeletion);
        Trainee afterDeletion = session.get(Trainee.class, trainee.getId());
        assertThat(afterDeletion).isNull();
    }

    @Test
    void testDeleteByUsername() {
        Session session = sessionFactory.getCurrentSession();
        Trainee trainee = getTestTraineeForDeletion();

        traineeDAO.save(trainee);
        Trainee beforeDeletion = session.get(Trainee.class, trainee.getId());
        assertThat(beforeDeletion).isNotNull();

        traineeDAO.delete("deleteme.deleteme");
        Trainee afterDeletion = session.get(Trainee.class, trainee.getId());
        assertThat(afterDeletion).isNull();
    }

    @Test
    void testUpdatePassword(){
        Session session = sessionFactory.getCurrentSession();
        traineeDAO.updatePassword(4, "passwordupdated");

        Trainee afterUpdate = session.get(Trainee.class, 4L);

        assertThat(afterUpdate.getPassword()).isEqualTo("passwordupdated");
    }

    @Test
    void testTrainingsByTraineeUsername() {
        List<Training> actual = traineeDAO.getTrainingsByTraineeUsername("alice");

        assertThat(actual).hasSize(4);
        actual.stream()
                .map(Training::getTrainingName)
                .forEach(training -> assertThat(training).contains("Strength"));
    }

    @Test
    void testTrainingsByTraineeUsernameNotExist() {
        List<Training> actual = traineeDAO.getTrainingsByTraineeUsername("no such person");

        assertThat(actual).isEmpty();
    }

    private Trainee getTestTrainee() {
        return new Trainee("testname", "testsurname", "testname.testsurname", "testpassword",
                true, new Date(), "testcity");
    }

    private Trainee getTestTraineeForDeletion() {
        return new Trainee("deleteme", "deleteme", "deleteme.deleteme", "deleteme",
                true, new Date(), "deleteme");
    }

    private Trainee getTraineeForUpdate() {
        Session session = sessionFactory.getCurrentSession();
        return session.get(Trainee.class, 3L);
    }
}
