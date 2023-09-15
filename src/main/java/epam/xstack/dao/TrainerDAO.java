package epam.xstack.dao;

import epam.xstack.model.Trainer;
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
}
