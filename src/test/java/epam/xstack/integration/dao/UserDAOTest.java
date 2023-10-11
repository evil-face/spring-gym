package epam.xstack.integration.dao;

import epam.xstack.dao.UserDAO;
import epam.xstack.model.Trainee;
import epam.xstack.model.User;
import io.zonky.test.db.AutoConfigureEmbeddedDatabase;
import org.hibernate.Session;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;
import static org.assertj.core.api.Assertions.assertThat;

import javax.persistence.EntityManager;
import java.util.List;
import java.util.Optional;

@SpringBootTest
@TestPropertySource(properties = {"spring.sql.init.mode=always"})
@AutoConfigureEmbeddedDatabase(refresh = AutoConfigureEmbeddedDatabase.RefreshMode.AFTER_CLASS)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
final class UserDAOTest {
    @Autowired
    UserDAO userDAO;

    @Autowired
    private EntityManager entityManager;

    private Session getCurrentSession()  {
        return entityManager.unwrap(Session.class);
    }

    private static final String TX_ID = "12345";

    @Test
    void testFindByUsernameSuccess() {
        Optional<User> actual = userDAO.findByUsername(TX_ID, "alice");

        assertThat(actual).isPresent();
        assertThat(actual.get().getUsername()).isEqualTo("alice");
        assertThat(actual.get().getFirstName()).isEqualTo("Alice");
    }

    @Test
    void testFindByUsernameNotExist() {
        Optional<User> actual = userDAO.findByUsername(TX_ID, "no such person");

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
    @Transactional
    void testUpdatePasswordSuccess() {
        String expected = "new_password";

        boolean result = userDAO.updatePassword(TX_ID, "bob", expected);

        Session session = getCurrentSession();
        Trainee actual = session.get(Trainee.class, 2L);

        assertThat(result).isTrue();
        assertThat(actual.getPassword()).isEqualTo(expected);
    }

    @Test
    @Transactional
    void testUpdatePasswordNoSuchUser() {
        String expected = "new_password";

        boolean result = userDAO.updatePassword(TX_ID, "no such person", expected);

        assertThat(result).isFalse();
    }

}
