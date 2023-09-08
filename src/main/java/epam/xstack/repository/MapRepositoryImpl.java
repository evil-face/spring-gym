package epam.xstack.repository;

import epam.xstack.model.GymEntity;
import epam.xstack.model.Trainee;
import epam.xstack.model.User;
import epam.xstack.util.FileReader;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
public class MapRepositoryImpl implements MapRepository {
    private Map<String, List<GymEntity>> storage;
    private final String storageFilePath;

    public MapRepositoryImpl(Map<String, List<GymEntity>> storage,
                             @Value("${storagefile.path}") String storageFilePath) {
        this.storage = storage;
        this.storageFilePath = storageFilePath;
    }

    @PostConstruct
    public void init() {
        storage = FileReader.readStorage(storageFilePath);
    }

    @PreDestroy
    public void destroy() {
        FileReader.writeStorage(storage, storageFilePath);
    }

    @Override
    public void save(GymEntity entity) {
        String entityType = entity.getClass().getSimpleName();

        storage.computeIfAbsent(entityType, k -> new ArrayList<>()).add(entity);
    }

    @Override
    public List<GymEntity> findAll(String entityType) {
        List<GymEntity> list = storage.get(entityType);

        return list == null ? new ArrayList<>() : list;
    }

    @Override
    public Optional<GymEntity> findById(String entityType, long id) {
        List<GymEntity> list = storage.get(entityType);

        if (list != null) {
            return list.stream()
                    .filter(entity -> entity.getId() == id)
                    .findFirst();
        }

        return Optional.empty();
    }

    @Override
    public boolean existsByUsername(String username) {
        return storage.values().stream()
                .flatMap(Collection::stream)
                .anyMatch(entity -> entity instanceof User
                        && ((User) entity).getUsername().equals(username));
    }

    @Override
    public void update(GymEntity entity) {
        String entityType = entity.getClass().getSimpleName();

        long id = entity.getId();
        Optional<GymEntity> oldEntity = findById(entityType, id);

        if (oldEntity.isPresent()) {
            switch (entityType) {
                case "Trainee" -> updateTrainee(oldEntity.get(), entity);
            }
        }
    }

    @Override
    public void delete(GymEntity entity) {
        String entityType = entity.getClass().getSimpleName();

        storage.get(entityType).remove(entity);
    }

    private void updateTrainee(GymEntity oldEntity, GymEntity updatedEntity) {
        Trainee oldE = (Trainee) oldEntity;
        Trainee newE = (Trainee) updatedEntity;

        oldE.setFirstName(newE.getFirstName());
        oldE.setLastName(newE.getLastName());
        oldE.setUsername(newE.getUsername());
        oldE.setPassword(newE.getPassword());
        oldE.setActive(newE.isActive());
        oldE.setDateOfBirth(newE.getDateOfBirth());
        oldE.setAddress(newE.getAddress());
    }
}
