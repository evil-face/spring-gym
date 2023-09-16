package epam.xstack.service;

import epam.xstack.dao.TrainerDAO;
import epam.xstack.model.Trainer;
import epam.xstack.model.TrainingType;
import epam.xstack.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.naming.AuthenticationException;
import java.util.List;
import java.util.Optional;

@Service
public final class TrainerService {
    private final TrainerDAO trainerDAO;
    private final UserService userService;
    private final AuthenticationService authService;
    private static final Logger LOGGER = LoggerFactory.getLogger(TrainerService.class);

    @Autowired
    public TrainerService(TrainerDAO trainerDAO, UserService userService, AuthenticationService authService) {
        this.trainerDAO = trainerDAO;
        this.userService = userService;
        this.authService = authService;
    }

    public void createTrainer(String firstName, String lastName,
                                 boolean isActive, TrainingType specialization) {

        String username = userService.generateUsername(firstName, lastName);
        String password = userService.generatePassword();

        Trainer trainer = new Trainer(firstName, lastName,
                username, password, isActive, specialization);

        trainerDAO.save(trainer);
        LOGGER.info("Saved new trainer with id {} to the DB", trainer.getId());
    }

    public List<Trainer> findAll(String username, String password) throws AuthenticationException {
        if (authService.authenticate(username, password)) {
            return trainerDAO.findAll();
        } else {
            LOGGER.info("Failed attempt to find all trainers with credentials {}:{}", username, password);
            throw new AuthenticationException("Authentication failed");
        }
    }

    public Optional<Trainer> findById(long id, String username, String password) throws AuthenticationException {
        if (authService.authenticate(username, password)) {
            return trainerDAO.findById(id);
        } else {
            LOGGER.info("Failed attempt to find trainer by id with credentials {}:{}", username, password);
            throw new AuthenticationException("Authentication failed");
        }
    }

    public Optional<Trainer> findByUsername(String query, String username, String password) throws AuthenticationException {
        if (authService.authenticate(username, password)) {
            Optional<User> user = userService.findByUsername(query);

            return user.isPresent() && user.get() instanceof Trainer ?
                    Optional.of((Trainer) user.get()) : Optional.empty();
        } else {
            LOGGER.info("Failed attempt to find trainer by username with credentials {}:{}", username, password);
            throw new AuthenticationException("Authentication failed");
        }
    }

    public void update(Trainer trainer, String username, String password) throws AuthenticationException {
        if (authService.authenticate(username, password)) {
            trainerDAO.update(trainer);
            LOGGER.info("Updated trainer with id {} in the DB", trainer.getId());
        } else {
            LOGGER.info("Failed attempt update trainer with credentials {}:{}", username, password);
            throw new AuthenticationException("Authentication failed");
        }
    }
    }
}
