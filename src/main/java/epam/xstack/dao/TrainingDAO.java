package epam.xstack.dao;

import epam.xstack.model.Trainee;
import epam.xstack.model.Trainer;
import epam.xstack.model.Training;
import epam.xstack.model.TrainingType;
import org.hibernate.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;

@Repository
@Transactional(readOnly = true)
public class TrainingDAO {
    @PersistenceContext
    EntityManager entityManager;

    protected Session getCurrentSession()  {
        return entityManager.unwrap(Session.class);
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(TrainingDAO.class);

    @Transactional
    public void save(String txID, Training training) {
        Session session = getCurrentSession();
        Trainee trainee = training.getTrainee();
        Trainer trainer = training.getTrainer();

        trainee.getTrainers().add(trainer);
        trainer.getTrainees().add(trainee);

        session.merge(trainee);
        session.merge(trainer);

        session.persist(training);

        LOGGER.info("TX ID: {} — Successfully saved new training '{}' with '{}' trainee and '{}' trainer",
                txID, training.getTrainingName(), trainee.getUsername(), trainer.getUsername());
    }

    public List<TrainingType> findAllTrainingTypes(String txID) {
        Session session = getCurrentSession();

        List<TrainingType> existingTypes = session.createQuery("from TrainingType t", TrainingType.class)
                .getResultList();

        LOGGER.info("TX ID: {} — Successfully fetched {} training types", txID, existingTypes.size());

        return existingTypes;
    }
}
