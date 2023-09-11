package epam.xstack.dao;

import epam.xstack.model.Training;
import epam.xstack.repository.MapRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class TrainingDAO {
    private final MapRepository mapRepository;
    private static final Logger LOGGER = LoggerFactory.getLogger(TrainerDAO.class);


    @Autowired
    public TrainingDAO(MapRepository mapRepository) {
        this.mapRepository = mapRepository;
    }

    public void save(Training training) {
        mapRepository.save(training);
    }

    public List<Training> findAll() {
        return mapRepository.findAll(Training.class.getSimpleName()).stream()
                .map(Training.class::cast)
                .toList();
    }

    public Optional<Training> findById(String id) {
        Optional<Training> training = mapRepository.findById(Training.class.getSimpleName(), id)
                .map(Training.class::cast);

        if (training.isEmpty()) {
            LOGGER.warn("No records found for id " + id);
        }

        return training;
    }
}
