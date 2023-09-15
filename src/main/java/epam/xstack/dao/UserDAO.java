package epam.xstack.dao;

import epam.xstack.model.User;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
public class UserDAO {
    private final SessionFactory sessionFactory;
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
}
