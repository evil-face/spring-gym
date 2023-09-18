package epam.xstack.dao;

import epam.xstack.config.TestConfig;
import epam.xstack.model.User;
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
import static org.assertj.core.api.Assertions.assertThat;

import javax.sql.DataSource;
import java.util.List;
import java.util.Optional;

@SpringJUnitConfig(TestConfig.class)
@AutoConfigureEmbeddedDatabase(refresh = AutoConfigureEmbeddedDatabase.RefreshMode.AFTER_CLASS)
@Transactional
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class UserDAOTest {
    @Autowired
    UserDAO userDAO;
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
    void testFindByUsername() {
        Optional<User> actual = userDAO.findByUsername("alice");

        assertThat(actual).isPresent();
        assertThat(actual.get().getUsername()).isEqualTo("alice");
        assertThat(actual.get().getFirstName()).isEqualTo("Alice");
    }

    @Test
    void testFindByUsernameNotExist() {
        Optional<User> actual = userDAO.findByUsername("no such person");

        assertThat(actual).isEmpty();
    }

    @Test
    void testFindUsernameOccurencies() {
        List<String> actual = userDAO.findUsernameOccurencies("alice");

        assertThat(actual).hasSize(1).contains("alice");
    }

    @Test
    void testFindUsernameOccurenciesNotExist() {
        List<String> actual = userDAO.findUsernameOccurencies("no such person");

        assertThat(actual).isEmpty();
    }

    @Test
    void testChangeActivationStatus() {
        Session session = sessionFactory.getCurrentSession();
        User expected = session.get(User.class, 1L);
        assertThat(expected.isActive()).isTrue();

        userDAO.changeActivationStatus(1);
        assertThat(expected.isActive()).isFalse();

        userDAO.changeActivationStatus(1);
        assertThat(expected.isActive()).isTrue();
    }
}
