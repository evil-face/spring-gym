package epam.xstack.service;

import epam.xstack.dao.TrainerDAO;
import epam.xstack.dao.TrainingDAO;
import epam.xstack.model.Trainee;
import epam.xstack.model.Trainer;
import epam.xstack.model.Training;
import epam.xstack.model.TrainingType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public final class TrainingService {
    private final TrainingDAO trainingDAO;
    @Autowired
    public TrainingService(TrainingDAO trainingDAO) {
        this.trainingDAO = trainingDAO;
    }

    public Training createTraining(Trainee trainee, Trainer trainer, String name,
                                   TrainingType type, Date date, int duration) {
        String id = UUID.randomUUID().toString();

        Training training = new Training(id, trainee, trainer, name, type, date, duration);

        trainingDAO.save(training);
        return training;
    }

    public List<Training> findAll() {
        return trainingDAO.findAll();
    }

    public Optional<Training> findById(String id) {
        return trainingDAO.findById(id);
    }
}
