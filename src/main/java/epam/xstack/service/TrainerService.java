package epam.xstack.service;

import epam.xstack.dao.TrainerDAO;
import epam.xstack.model.Trainer;
import epam.xstack.model.TrainingType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public final class TrainerService {
    private final TrainerDAO trainerDAO;
    private final UserService userService;
    private static final Logger LOGGER = LoggerFactory.getLogger(TrainerService.class);

    @Autowired
    public TrainerService(TrainerDAO trainerDAO, UserService userService) {
        this.trainerDAO = trainerDAO;
        this.userService = userService;
    }

    public Trainer createTrainer(String firstName, String lastName,
                                 boolean isActive, TrainingType specialization) {
        String id = userService.generateId();
        String username = userService.generateUsername(firstName, lastName);
        String password = userService.generatePassword();

        Trainer trainer = new Trainer(id, firstName, lastName,
                username, password, isActive, specialization);

        trainerDAO.save(trainer);
        LOGGER.info("Saved new trainer with id {} to the DB", trainer.getId());


        return trainer;
    }

    public List<Trainer> findAll() {
        return trainerDAO.findAll();
    }

    public Optional<Trainer> findById(String id) {
        return trainerDAO.findById(id);
    }

    public void update(Trainer trainer) {
        trainerDAO.update(trainer);
        LOGGER.info("Updated trainer with id {} in the DB", trainer.getId());
    }
}
