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

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public final class TrainingService {
    private final TrainingDAO trainingDAO;
    private final UserService userService;
    private static final Logger LOGGER = LoggerFactory.getLogger(TrainingService.class);

    @Autowired
    public TrainingService(TrainingDAO trainingDAO, UserService userService) {
        this.trainingDAO = trainingDAO;
        this.userService = userService;
    }

    public Training createTraining(Trainee trainee, Trainer trainer, String name,
                                   TrainingType type, Date date, int duration) {
        String id = userService.generateId();

        Training training = new Training(id, trainee, trainer, name, type, date, duration);

        trainingDAO.save(training);
        LOGGER.info("Saved new training with id {} to the DB", training.getId());


        return training;
    }

    public List<Training> findAll() {
        return trainingDAO.findAll();
    }

    public Optional<Training> findById(String id) {
        return trainingDAO.findById(id);
    }
}
