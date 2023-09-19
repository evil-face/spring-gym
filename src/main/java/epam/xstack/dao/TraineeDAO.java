package epam.xstack.dao;

import epam.xstack.model.Trainee;
import epam.xstack.model.Training;
import org.hibernate.Hibernate;
import org.hibernate.NonUniqueResultException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.NoResultException;
import java.util.ArrayList;
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

    public Optional<Trainee> findByUsername(String username) {
        Session session = sessionFactory.getCurrentSession();
        Trainee trainee = null;

        try {
            trainee = session.createQuery(
                            "SELECT t FROM Trainee t WHERE t.username = :username", Trainee.class)
                    .setParameter("username", username)
                    .getSingleResult();
            Hibernate.initialize(trainee.getTrainers());
        } catch (NonUniqueResultException | NoResultException e) {
            LOGGER.warn("Either no trainees or several trainees were found for username {}", username);
        }

        if (trainee == null) {
            LOGGER.warn("No records found for username {}", username);
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
    public void delete(String usernameToDelete) {
        Session session = sessionFactory.getCurrentSession();

        try {
            Trainee trainee = session.createQuery(
                            "SELECT t FROM Trainee t WHERE username = :username", Trainee.class)
                    .setParameter("username", usernameToDelete)
                    .getSingleResult();

            if (trainee != null) {
                session.remove(trainee);
                LOGGER.info("Deleted trainee with username {} from the DB", usernameToDelete);
            }
        } catch (NonUniqueResultException | NoResultException e) {
            LOGGER.warn("Either no trainee or several trainees were found for username {}", usernameToDelete);
        }
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

    public List<Training> getTrainingsByTraineeUsername(String traineeUsername) {
        Session session = sessionFactory.getCurrentSession();
        Trainee trainee = null;

        try {
            trainee = session.createQuery(
                            "SELECT t FROM Trainee t WHERE username = :username", Trainee.class)
                    .setParameter("username", traineeUsername)
                    .getSingleResult();

            if (trainee != null) {
                Hibernate.initialize(trainee.getTrainingList());
                return trainee.getTrainingList();
            }
        } catch (NonUniqueResultException | NoResultException e) {
            LOGGER.warn("Either no trainees or several trainees were found for username {}", traineeUsername);
        }

        return new ArrayList<>();
    }
}
