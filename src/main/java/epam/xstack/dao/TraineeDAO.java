package epam.xstack.dao;

import epam.xstack.model.Trainee;
import epam.xstack.repository.MapRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public final class TraineeDAO {
    private MapRepository mapRepository;

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
        return mapRepository.findById(Trainee.class.getSimpleName(), id)
                .map(Trainee.class::cast);
    }

    public void update(Trainee trainee) {
        mapRepository.update(trainee);
    }

    public void delete(Trainee trainee) {
        mapRepository.delete(trainee);
    }
}
