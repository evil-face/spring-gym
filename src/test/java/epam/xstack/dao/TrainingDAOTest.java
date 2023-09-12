package epam.xstack.dao;

import epam.xstack.model.Training;
import epam.xstack.repository.MapRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TrainingDAOTest {
    @InjectMocks
    TrainingDAO trainingDAO;
    @Mock
    MapRepository mapRepository;

    @Test
    void testFindAll() {
        when(mapRepository.findAll(Training.class.getSimpleName())).thenReturn(new ArrayList<>());

        List<Training> actual = trainingDAO.findAll();

        assertThat(actual).isNotNull();
    }

    @Test
    void testFindById() {
        when(mapRepository.findById(Training.class.getSimpleName(), "1")).thenReturn(Optional.empty());

        Optional<Training> actual = trainingDAO.findById("1");

        assertThat(actual).isNotNull();
    }
}
