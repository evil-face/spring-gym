package epam.xstack.dao;

import epam.xstack.dto.trainee.req.TraineeGetTrainingListRequestDTO;
import epam.xstack.exception.EntityNotFoundException;
import epam.xstack.exception.ForbiddenException;
import epam.xstack.model.Trainee;
import epam.xstack.model.Trainer;
import epam.xstack.model.Training;
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
import java.util.HashSet;
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
    public void save(String txID, Trainee trainee) {
        Session session = sessionFactory.getCurrentSession();
        session.persist(trainee);
        LOGGER.info("TX ID: {} — Successfully saved new trainee with username '{}' and id '{}'",
                txID, trainee.getUsername(), trainee.getId());
    }

//    public List<Trainee> findAll() {
//        Session session = sessionFactory.getCurrentSession();
//
//        return session.createQuery("from Trainee t", Trainee.class)
//                .getResultList();
//    }

    public Optional<Trainee> findById(String txID, long id) {
        Session session = sessionFactory.getCurrentSession();

        Trainee trainee = session.get(Trainee.class, id);

        if (trainee == null) {
            LOGGER.warn("TX ID: {} — No records were found for id '{}'", txID, id);
        } else {
            Hibernate.initialize(trainee.getTrainers());
        }

        return Optional.ofNullable(trainee);
    }

    public Optional<Trainee> findByUsername(String txID, String username) {
        Session session = sessionFactory.getCurrentSession();
        Trainee trainee = null;

        try {
            trainee = session.createQuery(
                            "SELECT t FROM Trainee t WHERE t.username = :username", Trainee.class)
                    .setParameter("username", username)
                    .getSingleResult();
            Hibernate.initialize(trainee.getTrainers());
        } catch (NonUniqueResultException | NoResultException e) {
            LOGGER.warn("TX ID: {} — Either no trainees or several trainees were found for username {}",
                    txID, username);
        }

        if (trainee == null) {
            LOGGER.warn("TX ID: {} — No trainee records found for username {}", txID, username);
        }

        return Optional.ofNullable(trainee);
    }

    @Transactional
    public Optional<Trainee> update(String txID, Trainee updatedTrainee) {
        Session session = sessionFactory.getCurrentSession();
        Trainee existingTrainee = session.get(Trainee.class, updatedTrainee.getId());

        if (existingTrainee == null) {
            LOGGER.warn("TX ID: {} — Could not update trainee with ID '{}' and username '{}' because it wasn't found",
                    txID, updatedTrainee.getId(), updatedTrainee.getUsername());
        } else {
            Hibernate.initialize(existingTrainee.getTrainers());
            updateFields(existingTrainee, updatedTrainee);
            session.merge(existingTrainee);

            LOGGER.info("TX ID: {} — Successfully updated trainee with username '{}' and id '{}'",
                    txID, existingTrainee.getUsername(), existingTrainee.getId());
        }

        return Optional.ofNullable(existingTrainee);
    }

    @Transactional
    public void delete(String txID, Trainee traineeToDelete) {
        Session session = sessionFactory.getCurrentSession();
        Trainee existingTrainee = session.get(Trainee.class, traineeToDelete.getId());

        if (existingTrainee == null) {
            LOGGER.warn("TX ID: {} — Could not delete trainee with ID '{}' because it wasn't found",
                    txID, traineeToDelete.getId());

            throw new EntityNotFoundException(txID);
        }

        session.remove(existingTrainee);

        LOGGER.info("TX ID: {} — Successfully deleted trainee with username '{}' and id '{}'",
                    txID, traineeToDelete.getUsername(), traineeToDelete.getId());
    }

    @Transactional
    public void changeActivationStatus(String txID, long id, Boolean newStatus, String username) {
        Session session = sessionFactory.getCurrentSession();
        Trainee trainee = session.get(Trainee.class, id);

        if (trainee == null) {
            LOGGER.warn("TX ID: {} — Could not change status of trainee with ID '{}' because it wasn't found",
                    txID, id);

            throw new EntityNotFoundException(txID);
        }

        trainee.setIsActive(newStatus);

        LOGGER.info("TX ID: {} — Successfully changed status for trainee with username '{}' and id '{}' to '{}'",
                txID, username, id, newStatus);
    }

    public List<Training> getTrainingsWithFiltering(String txID, long id, TraineeGetTrainingListRequestDTO criteria) {
        Session session = sessionFactory.getCurrentSession();

        Trainee trainee = session.get(Trainee.class, id);

        if (trainee == null) {
            throw new EntityNotFoundException(txID);
        }

        CriteriaQuery<Training> cr = buildCriteriaQuery(session, txID, id, criteria);
        Query<Training> query = session.createQuery(cr);

        return query.getResultList();
    }

//    public List<Training> getTrainingsByTraineeUsername(String traineeUsername) {
//        Session session = sessionFactory.getCurrentSession();
//        Trainee trainee = null;
//
//        try {
//            trainee = session.createQuery(
//                            "SELECT t FROM Trainee t WHERE username = :username", Trainee.class)
//                    .setParameter("username", traineeUsername)
//                    .getSingleResult();
//
//            if (trainee != null) {
//                Hibernate.initialize(trainee.getTrainingList());
//                return trainee.getTrainingList();
//            }
//        } catch (NonUniqueResultException | NoResultException e) {
//            LOGGER.warn("Either no trainees or several trainees were found for username {}", traineeUsername);
//        }
//
//        return new ArrayList<>();
//    }

    public List<Trainee> findAllByUsernamePartialMatch(String username) {
        Session session = sessionFactory.getCurrentSession();

        return session.createQuery(
                        "SELECT t FROM Trainee t WHERE username LIKE :username", Trainee.class)
                .setParameter("username", username.replaceAll("\\d", "") + "%")
                .getResultList();
    }

    @Transactional
    public List<Trainer> updateTrainerList(String txID, long id, String username, List<Trainer> updatedList) {
        Session session = sessionFactory.getCurrentSession();

        Trainee trainee = session.get(Trainee.class, id);

        if (trainee == null) {
            throw new EntityNotFoundException(txID);
        } else if (!trainee.getUsername().equals(username)) {
            throw new ForbiddenException(txID);
        }

        trainee.setTrainers(new HashSet<>(updatedList));

        LOGGER.info("TX ID: {} — Successfully updated trainee's list of trainers "
                        + "for username '{}' with '{}' trainers", txID, username, updatedList.size());

        return updatedList;
    }

    private void updateFields(Trainee existingTrainee, Trainee updatedTrainee) {
        existingTrainee.setFirstName(updatedTrainee.getFirstName());
        existingTrainee.setLastName(updatedTrainee.getLastName());
        existingTrainee.setDateOfBirth(updatedTrainee.getDateOfBirth());
        existingTrainee.setAddress(updatedTrainee.getAddress());
        existingTrainee.setIsActive(updatedTrainee.getIsActive());
    }

    private CriteriaQuery<Training> buildCriteriaQuery(Session session, String txID,
                                                       long id, TraineeGetTrainingListRequestDTO criteria) {
        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaQuery<Training> cr = cb.createQuery(Training.class);
        Root<Training> root = cr.from(Training.class);

        root.fetch("trainee", JoinType.LEFT);
        root.fetch("trainer", JoinType.LEFT);
        root.fetch("trainingType", JoinType.LEFT);

        List<Predicate> predicates = new ArrayList<>();
        predicates.add(cb.equal(root.get("trainee").get("id"), id));

        String trainerName = criteria.getTrainerName();
        if (trainerName != null && !trainerName.isBlank()) {
            predicates.add(cb.equal(root.get("trainer").get("username"), trainerName));
        }

        Long trainingType = criteria.getTrainingType();
        if (trainingType != null && trainingType > 0) {
            predicates.add(cb.equal(root.get("trainingType").get("id"), trainingType));
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
