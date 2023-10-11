package epam.xstack.dao;

import epam.xstack.model.User;
import org.hibernate.NonUniqueResultException;
import org.hibernate.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import java.util.List;
import java.util.Optional;

@Repository
@Transactional(readOnly = true)
public class UserDAO {
    @PersistenceContext
    EntityManager entityManager;

    protected Session getCurrentSession()  {
        return entityManager.unwrap(Session.class);
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(UserDAO.class);

    public List<String> findUsernameOccurencies(String newUsername) {
        Session session = getCurrentSession();

        return session.createQuery(
                "SELECT username FROM User u WHERE username LIKE :username", String.class)
                .setParameter("username", newUsername + "%")
                .getResultList();
    }

    public Optional<User> findByUsername(String txID, String username) {
        Session session = getCurrentSession();
        User user = null;

        try {
            user = session.createQuery(
                            "SELECT u FROM User u WHERE username = :username", User.class)
                    .setParameter("username", username)
                    .getSingleResult();
        } catch (NonUniqueResultException | NoResultException e) {
            LOGGER.warn("TX ID: {} — Either no users or several users were found for username {}", txID, username);
        }

        return Optional.ofNullable(user);
    }

    @Transactional
    public boolean updatePassword(String txID, String username, String newPassword) {
        Session session = getCurrentSession();

        try {
            User user = session.createQuery(
                            "SELECT u FROM User u WHERE username = :username", User.class)
                    .setParameter("username", username)
                    .getSingleResult();
            user.setPassword(newPassword);
            LOGGER.info("TX ID: {} — Successfully updated password of trainee with username {}", txID, username);

            return true;
        } catch (NonUniqueResultException | NoResultException e) {
            LOGGER.warn("TX ID: {} — Either no trainees or several trainees were found for username {}",
                    txID, username);
        }

        return false;
    }
}
