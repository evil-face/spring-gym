package epam.xstack.dao;

import epam.xstack.dto.trainer.req.TrainerGetTrainingListRequestDTO;
import epam.xstack.exception.EntityNotFoundException;
import epam.xstack.model.Trainer;
import epam.xstack.model.Training;
import epam.xstack.model.TrainingType;
import org.hibernate.Hibernate;
import org.hibernate.NonUniqueResultException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.NoResultException;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.time.LocalDate;
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

    public Optional<Trainer> findByUsername(String txID, String username) {
        Session session = sessionFactory.getCurrentSession();
        Trainer trainer = null;

        try {
            trainer = session.createQuery(
                            "SELECT t FROM Trainer t WHERE t.username = :username", Trainer.class)
                    .setParameter("username", username)
                    .getSingleResult();
            Hibernate.initialize(trainer.getTrainees());
        } catch (NonUniqueResultException | NoResultException e) {
            LOGGER.warn("TX ID: {} — Either no trainers or several trainers were found for username {}",
                    txID, username);
        }

        if (trainer == null) {
            LOGGER.warn("TX ID: {} — No trainer records found for username {}", txID, username);
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

    public List<Training> getTrainingsWithFiltering(String txID, long id, TrainerGetTrainingListRequestDTO criteria) {
        Session session = sessionFactory.getCurrentSession();

        Trainer trainer = session.get(Trainer.class, id);

        if (trainer == null) {
            throw new EntityNotFoundException(txID);
        }

        CriteriaQuery<Training> cr = buildCriteriaQuery(session, txID, id, criteria);
        Query<Training> query = session.createQuery(cr);

        return query.getResultList();
    }

//    public List<Training> getTrainingsByTrainerUsername(String trainerUsername) {
//        Session session = sessionFactory.getCurrentSession();
//        Trainer trainer = null;
//
//        try {
//            trainer = session.createQuery(
//                            "SELECT t FROM Trainer t WHERE username = :username", Trainer.class)
//                    .setParameter("username", trainerUsername)
//                    .getSingleResult();
//
//            if (trainer != null) {
//                Hibernate.initialize(trainer.getTrainingList());
//                return trainer.getTrainingList();
//            }
//        } catch (NonUniqueResultException | NoResultException e) {
//            LOGGER.warn("Either no trainers or several trainers were found for username {}", trainerUsername);
//        }
//
//        return new ArrayList<>();
//    }

    public List<Trainer> findAllByUsernamePartialMatch(String username) {
        Session session = sessionFactory.getCurrentSession();

        return session.createQuery(
                        "SELECT t FROM Trainer t WHERE username LIKE :username", Trainer.class)
                .setParameter("username", username.replaceAll("\\d", "") + "%")
                .getResultList();
    }

    public Optional<TrainingType> trainingTypeExistsById(long id) {
        Session session = sessionFactory.getCurrentSession();

        return Optional.ofNullable(session.get(TrainingType.class, id));
    }

    private void updateFields(Trainer existingTrainer, Trainer updatedTrainer) {
        existingTrainer.setFirstName(updatedTrainer.getFirstName());
        existingTrainer.setLastName(updatedTrainer.getLastName());
        existingTrainer.setSpecialization(updatedTrainer.getSpecialization());
        existingTrainer.setIsActive(updatedTrainer.getIsActive());
    }

    private CriteriaQuery<Training> buildCriteriaQuery(Session session, String txID,
                                                       long id, TrainerGetTrainingListRequestDTO criteria) {
        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaQuery<Training> cr = cb.createQuery(Training.class);
        Root<Training> root = cr.from(Training.class);

        root.fetch("trainee", JoinType.LEFT);
        root.fetch("trainer", JoinType.LEFT);
        root.fetch("trainingType", JoinType.LEFT);

        List<Predicate> predicates = new ArrayList<>();
        predicates.add(cb.equal(root.get("trainer").get("id"), id));

        String traineeName = criteria.getTraineeName();
        if (traineeName != null && !traineeName.isBlank()) {
            predicates.add(cb.equal(root.get("trainee").get("username"), traineeName));
        }

        LocalDate periodFrom = criteria.getPeriodFrom();
        if (periodFrom != null) {
            predicates.add(cb.greaterThanOrEqualTo(root.get("trainingDate"), periodFrom));
        }

        LocalDate periodTo = criteria.getPeriodTo();
        if (periodTo != null) {
            predicates.add(cb.lessThanOrEqualTo(root.get("trainingDate"), periodTo));
        }

        Predicate finalPredicate = cb.and(predicates.toArray(new Predicate[0]));
        cr.select(root).where(finalPredicate);

        LOGGER.info("TX ID: {} — Built filtering query with '{}' parameters for id '{}'",
                txID, predicates.size() - 1, id);

        return cr;
    }
}
