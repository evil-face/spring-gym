package epam.xstack.dao;

import epam.xstack.exception.EntityNotFoundException;
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
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Root;
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
    public void save(String txID, Trainer trainer) {
        Session session = sessionFactory.getCurrentSession();
        session.persist(trainer);
        LOGGER.info("TX ID: {} — Successfully saved new trainer with username '{}' and id '{}'",
                txID, trainer.getUsername(), trainer.getId());
    }

    public List<Trainer> findAll() {
        Session session = sessionFactory.getCurrentSession();

        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaQuery<Trainer> cr = cb.createQuery(Trainer.class);
        Root<Trainer> root = cr.from(Trainer.class);
        root.fetch("specialization", JoinType.LEFT);

        return session.createQuery(cr).getResultList();
    }

    public Optional<Trainer> findById(String txID, long id) {
        Session session = sessionFactory.getCurrentSession();

        Trainer trainer = session.get(Trainer.class, id);

        if (trainer == null) {
            LOGGER.warn("TX ID: {} — No records were found for id '{}'", txID, id);
        } else {
            Hibernate.initialize(trainer.getTrainees());
        }

        return Optional.ofNullable(trainer);
    }

    @Transactional
    public Optional<Trainer> update(String txID, Trainer updatedTrainer) {
        Session session = sessionFactory.getCurrentSession();
        Trainer existingTrainer = session.get(Trainer.class, updatedTrainer.getId());

        if (existingTrainer == null) {
            LOGGER.warn("TX ID: {} — Could not update trainer with ID '{}' and username '{}' because it wasn't found",
                    txID, updatedTrainer.getId(), updatedTrainer.getUsername());
        } else {
            Hibernate.initialize(existingTrainer.getTrainees());
            updateFields(existingTrainer, updatedTrainer);
            session.merge(existingTrainer);

            LOGGER.info("TX ID: {} — Successfully updated trainer with username '{}' and id '{}'",
                    txID, existingTrainer.getUsername(), existingTrainer.getId());
        }

        return Optional.ofNullable(existingTrainer);
    }

    @Transactional
    public void changeActivationStatus(String txID, long id, Boolean newStatus, String username) {
        Session session = sessionFactory.getCurrentSession();
        Trainer trainer = session.get(Trainer.class, id);

        if (trainer == null) {
            LOGGER.warn("TX ID: {} — Could not change status of trainer with ID '{}' because it wasn't found",
                    txID, id);

            throw new EntityNotFoundException(txID);
        }

        trainer.setIsActive(newStatus);

        LOGGER.info("TX ID: {} — Successfully changed status for trainer with username '{}' and id '{}' to '{}'",
                txID, username, id, newStatus);
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

    public List<Trainer> findAllByUsernamePartialMatch(String username) {
        Session session = sessionFactory.getCurrentSession();

        return session.createQuery(
                        "SELECT t FROM Trainer t WHERE username LIKE :username", Trainer.class)
                .setParameter("username", username.replaceAll("\\d", "") + "%")
                .getResultList();
    }

    private void updateFields(Trainer existingTrainer, Trainer updatedTrainer) {
        existingTrainer.setFirstName(updatedTrainer.getFirstName());
        existingTrainer.setLastName(updatedTrainer.getLastName());
        existingTrainer.setSpecialization(updatedTrainer.getSpecialization());
        existingTrainer.setIsActive(updatedTrainer.getIsActive());
    }
}
