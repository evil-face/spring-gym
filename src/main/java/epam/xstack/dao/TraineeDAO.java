package epam.xstack.dao;

import epam.xstack.model.Trainee;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Component
@Transactional(readOnly = true)
public class TraineeDAO {
    private final SessionFactory sessionFactory;
    private static final Logger LOGGER = LoggerFactory.getLogger(TraineeDAO.class);

    @Autowired
    public TraineeDAO(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    @Transactional
    public void save(Trainee trainee) {
        Session session = sessionFactory.getCurrentSession();
        session.persist(trainee);
    }

    public List<Trainee> findAll() {
        Session session = sessionFactory.getCurrentSession();

        return session.createQuery("from Trainee t", Trainee.class)
                .getResultList();
    }

    public Optional<Trainee> findById(long id) {
        Session session = sessionFactory.getCurrentSession();

        Trainee trainee = session.get(Trainee.class, id);

        if (trainee == null) {
            LOGGER.warn("No records found for id {}", id);
        }

        return trainee == null ? Optional.empty() : Optional.of(trainee);
    }

    @Transactional
    public void update(Trainee updatedTrainee) {
        Session session = sessionFactory.getCurrentSession();
        session.merge(updatedTrainee);
    }

    @Transactional
    public void delete(Trainee trainee) {
        Session session = sessionFactory.getCurrentSession();
        session.remove(trainee);
    }

    @Transactional
    public void updatePassword(long id, String newPassword) {
        Session session = sessionFactory.getCurrentSession();
        Trainee trainee = session.get(Trainee.class, id);

        if (trainee == null) {
            LOGGER.warn("No records found for id {}", id);
        } else {
            trainee.setPassword(newPassword);
        }
    }
}
