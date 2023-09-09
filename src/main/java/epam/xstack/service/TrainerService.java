package epam.xstack.service;

import epam.xstack.dao.TrainerDAO;
import epam.xstack.model.Trainer;
import epam.xstack.model.TrainingType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public final class TrainerService {
    private final TrainerDAO trainerDAO;
    private final UserService userService;

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
                username, password, true, specialization);

        trainerDAO.save(trainer);
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
    }
}
