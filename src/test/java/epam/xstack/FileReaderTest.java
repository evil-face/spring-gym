package epam.xstack;

import epam.xstack.model.GymEntity;
import epam.xstack.model.Trainee;
import epam.xstack.model.Trainer;
import epam.xstack.model.Training;
import epam.xstack.model.TrainingType;
import epam.xstack.util.FileReader;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import static org.assertj.core.api.Assertions.assertThat;

class FileReaderTest {
    private static final String TEST_FIXTURE_PATH = "src/test/resources/filereader_write_fixture.txt";
    private static final String TRAINEE_MAP_KEY = "trainee";
    private static final String TRAINER_MAP_KEY = "trainer";
    private static final String TRAINING_MAP_KEY = "training";
    private static final long TEST_DATE = 1694429213148L;


    @Test
    void testWriting() throws IOException {
        Map<String, List<GymEntity>> testStorage = new HashMap<>();
        testStorage.put(TRAINEE_MAP_KEY, new ArrayList<>(Arrays.asList(getMockTrainee())));
        testStorage.put(TRAINER_MAP_KEY, new ArrayList<>(Arrays.asList(getMockTrainer())));
        testStorage.put(TRAINING_MAP_KEY, new ArrayList<>(Arrays.asList(getMockTraining())));

        FileReader.writeStorage(testStorage, TEST_FIXTURE_PATH);

        String fileContent = Files.readString(Path.of(TEST_FIXTURE_PATH));

        assertThat(fileContent)
                .contains("trainee")
                .contains("trainer")
                .contains("training")
                .contains(getMockTrainee().getUsername())
                .contains(getMockTrainer().getUsername())
                .contains(getMockTraining().getTrainingName());
    }

    @Test
    void testReading() {
        Map<String, List<GymEntity>> storage = FileReader.readStorage(TEST_FIXTURE_PATH);

        assertThat(storage.get(TRAINEE_MAP_KEY)).contains(getMockTrainee());
        assertThat(storage.get(TRAINER_MAP_KEY)).contains(getMockTrainer());
        assertThat(storage.get(TRAINING_MAP_KEY)).contains(getMockTraining());

    }


    private Trainee getMockTrainee() {
        return new Trainee("1", "traineename", "surname",
                "traineename.surname", "1234", true, new Date(TEST_DATE), "test adrress");
    }

    private Trainer getMockTrainer() {
        return new Trainer("2", "trainername", "surname",
                "trainername.surname", "5678", true, getMockTrainingType());
    }

    private Training getMockTraining() {
        return new Training("4", getMockTrainee(), getMockTrainer(),
                "test training", getMockTrainingType(), new Date(TEST_DATE), 60);
    }

    private TrainingType getMockTrainingType() {
        return new TrainingType("3", "Lifting");
    }
}
