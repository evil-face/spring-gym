package epam.xstack.repository;

import epam.xstack.model.GymEntity;
import epam.xstack.model.Trainee;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MapRepositoryImplTest {
    private static final String TEST_TYPE_QUERY = "Trainee";
    @InjectMocks
    MapRepositoryImpl mapRepository;
    @Mock
    Map<String, List<GymEntity>> storage;

    @Test
    void testFindAllNoEntities() {
        when(storage.get(TEST_TYPE_QUERY)).thenReturn(null);

        List<GymEntity> actual = mapRepository.findAll(TEST_TYPE_QUERY);

        assertThat(actual).isNotNull();
        assertThat(actual).isEmpty();
    }

    @Test
    void testFindAllEntitiesExist() {
        when(storage.get(TEST_TYPE_QUERY)).thenReturn(getEntitiesList());

        List<GymEntity> actual = mapRepository.findAll(TEST_TYPE_QUERY);

        assertThat(actual).isNotNull();
        assertThat(actual).hasSize(3);
    }

    @Test
    void testFindByIdNotFound() {
        when(storage.get(TEST_TYPE_QUERY)).thenReturn(null);

        Optional<GymEntity> actual = mapRepository.findById(TEST_TYPE_QUERY, "wrong");

        assertThat(actual).isNotPresent();
    }

    @Test
    void testFindByIdExist() {
        when(storage.get(TEST_TYPE_QUERY)).thenReturn(getEntitiesList());

        Optional<GymEntity> actual = mapRepository.findById(TEST_TYPE_QUERY, "1");

        assertThat(actual).isPresent();
    }

    @Test
    void testUpdate() {
        when(storage.get(TEST_TYPE_QUERY)).thenReturn(getEntitiesList());

        mapRepository.update(getUpdatedEntity());
    }

    @Test
    void testExistsByUsernameFound() {
        when(storage.values()).thenReturn(Map.of("Trainee", getEntitiesList()).values());

        boolean actual = mapRepository.existsByUsername("michael.shawn");

        assertThat(actual).isTrue();
    }

    @Test
    void testExistsByUsernameNotFound() {
        when(storage.values()).thenReturn(Map.of("Trainee", getEntitiesList()).values());

        boolean actual = mapRepository.existsByUsername("notfound");

        assertThat(actual).isFalse();
    }

    @Test
    void testSave() {
        when(storage.computeIfAbsent(any(), any())).thenReturn(new ArrayList<>());

        mapRepository.save(getUpdatedEntity());
    }

    @Test
    void testDelete() {
        when(storage.get(getUpdatedEntity().getClass().getSimpleName())).thenReturn(new ArrayList<>());

        mapRepository.delete(getUpdatedEntity());
    }

    private Trainee getUpdatedEntity() {
        return new Trainee("1", "Miguel", "Rodriguez",
                "miguel.rodriguez", "1234", true,
                new Date(), "Texas");
    }

    private List<GymEntity> getEntitiesList() {
        return List.of(
                new Trainee("1", "Miguel", "Rodriguez",
                        "miguel.rodriguez", "qwerty", true,
                        new Date(), "Mexico"),
                new Trainee("2", "Michael", "Shawn",
                        "michael.shawn", "wweerr", true,
                        new Date(), "London"),
                new Trainee("3", "Ivan", "Popkov",
                        "ivan.popkov", "pass", true,
                        new Date(), "Ivangorod")
        );
    }
}
