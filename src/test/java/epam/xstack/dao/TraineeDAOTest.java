package epam.xstack.dao;

import epam.xstack.model.Trainee;
import epam.xstack.repository.MapRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
class TraineeDAOTest {
    @InjectMocks
    TraineeDAO traineeDAO;
    @Mock
    MapRepository mapRepository;

    @Test
    void testFindAll() {
        when(mapRepository.findAll(Trainee.class.getSimpleName())).thenReturn(new ArrayList<>());

        List<Trainee> actual = traineeDAO.findAll();

        assertThat(actual).isNotNull();
    }

    @Test
    void testFindById() {
        when(mapRepository.findById(Trainee.class.getSimpleName(), "1")).thenReturn(Optional.empty());

        Optional<Trainee> actual = traineeDAO.findById("1");

        assertThat(actual).isNotNull();
    }
}
