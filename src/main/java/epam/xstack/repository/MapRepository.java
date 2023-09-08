package epam.xstack.repository;

import epam.xstack.model.GymEntity;

import java.util.List;
import java.util.Optional;

public interface MapRepository {
    void save(GymEntity entity);
    List<GymEntity> findAll(String entityType);
    Optional<GymEntity> findById(String entityType, long id);
    boolean existsByUsername(String username);
    void update(GymEntity entity);
    void delete(GymEntity entity);
}
