package epam.xstack.service;

import epam.xstack.dao.TrainingDAO;
import epam.xstack.model.Trainee;
import epam.xstack.model.Trainer;
import epam.xstack.model.Training;
import epam.xstack.model.TrainingType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.naming.AuthenticationException;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public final class TrainingService {
    private final TrainingDAO trainingDAO;
    private final AuthenticationService authService;
    private static final Logger LOGGER = LoggerFactory.getLogger(TrainingService.class);

    @Autowired
    public TrainingService(TrainingDAO trainingDAO, AuthenticationService authService) {
        this.trainingDAO = trainingDAO;
        this.authService = authService;
    }

    public void createTraining(Trainee trainee, Trainer trainer, String name,
                                   TrainingType type, Date date, int duration,
                               String username, String password) throws AuthenticationException {
        if (authService.authenticate(username, password)) {
            Training training = new Training(trainee, trainer, name, type, date, duration);
            trainingDAO.save(training);
            LOGGER.info("Saved new training with id {} to the DB", training.getId());
        } else {
            LOGGER.info("Failed attempt to create new training with credentials {}:{}", username, password);
            throw new AuthenticationException("Authentication failed");
        }

    }

    public List<Training> findAll(String username, String password) throws AuthenticationException {
        if (authService.authenticate(username, password)) {
            return trainingDAO.findAll();
        } else {
            LOGGER.info("Failed attempt to find all trainings with credentials {}:{}", username, password);
            throw new AuthenticationException("Authentication failed");
        }
    }

    public Optional<Training> findById(long id, String username, String password) throws AuthenticationException {
        if (authService.authenticate(username, password)) {
            return trainingDAO.findById(id);
        } else {
            LOGGER.info("Failed attempt to find training by id with credentials {}:{}", username, password);
            throw new AuthenticationException("Authentication failed");
        }
    }
}
