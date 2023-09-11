package epam.xstack.dao;

import epam.xstack.model.Trainer;
import epam.xstack.repository.MapRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public final class TrainerDAO {
    private final MapRepository mapRepository;
    private static final Logger LOGGER = LoggerFactory.getLogger(TrainerDAO.class);

    @Autowired
    public TrainerDAO(MapRepository mapRepository) {
        this.mapRepository = mapRepository;
    }

    public void save(Trainer trainer) {
        mapRepository.save(trainer);
    }

    public List<Trainer> findAll() {
        return mapRepository.findAll(Trainer.class.getSimpleName()).stream()
                .map(Trainer.class::cast)
                .toList();
    }

    public Optional<Trainer> findById(String id) {
        Optional<Trainer> trainer = mapRepository.findById(Trainer.class.getSimpleName(), id)
                .map(Trainer.class::cast);

        if (trainer.isEmpty()) {
            LOGGER.warn("No records found for id " + id);
        }

        return trainer;
    }

    public void update(Trainer trainer) {
        mapRepository.update(trainer);
    }
}
