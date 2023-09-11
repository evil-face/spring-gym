package epam.xstack.dao;

import epam.xstack.model.Trainee;
import epam.xstack.repository.MapRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public final class TraineeDAO {
    private final MapRepository mapRepository;
    private static final Logger LOGGER = LoggerFactory.getLogger(TraineeDAO.class);


    @Autowired
    public TraineeDAO(MapRepository mapRepository) {
        this.mapRepository = mapRepository;
    }

    public void save(Trainee trainee) {
        mapRepository.save(trainee);
    }

    public List<Trainee> findAll() {
        return mapRepository.findAll(Trainee.class.getSimpleName()).stream()
                .map(Trainee.class::cast)
                .toList();
    }

    public Optional<Trainee> findById(String id) {
        Optional<Trainee> trainee = mapRepository.findById(Trainee.class.getSimpleName(), id)
                .map(Trainee.class::cast);

        if (trainee.isEmpty()) {
            LOGGER.warn("No records found for id " + id);
        }

        return trainee;
    }

    public void update(Trainee trainee) {
        mapRepository.update(trainee);
    }

    public void delete(Trainee trainee) {
        mapRepository.delete(trainee);
    }
}
