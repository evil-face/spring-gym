package epam.xstack.dao;

import epam.xstack.model.Trainer;
import epam.xstack.repository.MapRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public final class TrainerDAO {
    private final MapRepository mapRepository;

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
        return mapRepository.findById(Trainer.class.getSimpleName(), id)
                .map(Trainer.class::cast);
    }

    public void update(Trainer trainer) {
        mapRepository.update(trainer);
    }
}
