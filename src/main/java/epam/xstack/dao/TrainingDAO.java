package epam.xstack.dao;

import epam.xstack.model.Trainee;
import epam.xstack.model.Trainer;
import epam.xstack.model.Training;
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
public class TrainingDAO {
    private final SessionFactory sessionFactory;
    private static final Logger LOGGER = LoggerFactory.getLogger(TrainingDAO.class);

    @Autowired
    public TrainingDAO(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    @Transactional
    public void save(Training training) {
        Session session = sessionFactory.getCurrentSession();
        Trainee trainee = training.getTrainee();
        Trainer trainer = training.getTrainer();

        trainee.getTrainers().add(trainer);
        trainer.getTrainees().add(trainee);

        session.merge(trainee);
        session.merge(trainer);

        session.persist(training);
    }

    public List<Training> findAll() {
        Session session = sessionFactory.getCurrentSession();

        return session.createQuery("from Training t", Training.class)
                .getResultList();
    }

    public Optional<Training> findById(long id) {
        Session session = sessionFactory.getCurrentSession();

        Training training = session.get(Training.class, id);

        if (training == null) {
            LOGGER.warn("No records found for id {}", id);
        }

        return training == null ? Optional.empty() : Optional.of(training);
    }
}
