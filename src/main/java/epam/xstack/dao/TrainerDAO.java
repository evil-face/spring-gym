package epam.xstack.dao;

import epam.xstack.model.Trainer;
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
public class TrainerDAO {
    private final SessionFactory sessionFactory;
    private static final Logger LOGGER = LoggerFactory.getLogger(TrainerDAO.class);

    @Autowired
    public TrainerDAO(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    @Transactional
    public void save(Trainer trainer) {
        Session session = sessionFactory.getCurrentSession();
        session.persist(trainer);
    }

    public List<Trainer> findAll() {
        Session session = sessionFactory.getCurrentSession();

        return session.createQuery("from Trainer t", Trainer.class)
                .getResultList();
    }

    public Optional<Trainer> findById(long id) {
        Session session = sessionFactory.getCurrentSession();

        Trainer trainer = session.get(Trainer.class, id);

        if (trainer == null) {
            LOGGER.warn("No records found for id {}", id);
        }

        return trainer == null ? Optional.empty() : Optional.of(trainer);
    }

    @Transactional
    public void update(Trainer updatedTrainer) {
        Session session = sessionFactory.getCurrentSession();
        session.merge(updatedTrainer);
    }

    @Transactional
    public void updatePassword(long id, String newPassword) {
        Session session = sessionFactory.getCurrentSession();
        Trainer trainer = session.get(Trainer.class, id);

        if (trainer == null) {
            LOGGER.warn("No records found for id {}", id);
        } else {
            trainer.setPassword(newPassword);
        }
    }

    public List<Training> getTrainingsByTrainerUsername(String trainerUsername) {
        Session session = sessionFactory.getCurrentSession();
        Trainer trainer = null;

        try {
            trainer = session.createQuery(
                            "SELECT t FROM Trainer t WHERE username = :username", Trainer.class)
                    .setParameter("username", trainerUsername)
                    .getSingleResult();

            if (trainer != null) {
                Hibernate.initialize(trainer.getTrainingList());
                return trainer.getTrainingList();
            }
        } catch (NonUniqueResultException | NoResultException e) {
            LOGGER.warn("Either no trainers or several trainers were found for username {}", trainerUsername);
        }

        return new ArrayList<>();
    }
}
