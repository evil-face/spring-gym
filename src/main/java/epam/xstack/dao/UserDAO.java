package epam.xstack.dao;

import epam.xstack.model.User;
import org.hibernate.NonUniqueResultException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.NoResultException;
import java.util.List;
import java.util.Optional;

@Component
public class UserDAO {
    private final SessionFactory sessionFactory;
    private static final Logger LOGGER = LoggerFactory.getLogger(TrainingDAO.class);
    @Autowired
    public UserDAO(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    @Transactional(readOnly = true)
    public boolean existsByUsername(String newUsername) {
        Session session = sessionFactory.getCurrentSession();

        List<User> users = session.createQuery(
                        "SELECT u FROM User u WHERE username = :username", User.class)
                .setParameter("username", newUsername)
                .getResultList();

        return !users.isEmpty();
    }

    public Optional<User> findByUsername(String username) {
        Session session = sessionFactory.getCurrentSession();
        User user = null;

        try {
            user = session.createQuery(
                            "SELECT u FROM User u WHERE username = :username", User.class)
                    .setParameter("username", username)
                    .getSingleResult();
        } catch (NonUniqueResultException | NoResultException e) {
            LOGGER.warn("Either no users or several users were found for username {}", username);
        }

        return Optional.ofNullable(user);
    }
}
