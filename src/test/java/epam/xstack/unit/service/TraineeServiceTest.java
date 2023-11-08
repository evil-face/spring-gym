package epam.xstack.unit.service;

import epam.xstack.dto.auth.AuthDTO;
import epam.xstack.dto.trainee.TraineeRequestDTO;
import epam.xstack.dto.trainee.TraineeResponseDTO;
import epam.xstack.dto.trainer.TrainerResponseDTO;
import epam.xstack.exception.NoSuchTraineeExistException;
import epam.xstack.exception.PersonAlreadyRegisteredException;
import epam.xstack.model.Trainee;
import epam.xstack.model.Trainer;
import epam.xstack.model.TrainingType;
import epam.xstack.repository.TraineeRepository;
import epam.xstack.service.TraineeService;
import epam.xstack.service.TrainerService;
import epam.xstack.service.UserService;
import io.micrometer.core.instrument.MeterRegistry;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TraineeServiceTest {
    @InjectMocks
    TraineeService traineeService;

    @Mock
    TraineeRepository traineeRepository;

    @Mock
    TrainerService trainerService;

    @Mock
    UserService userService;

    @Spy
    ModelMapper modelMapper;

    @Mock
    MeterRegistry meterRegistry;

    @Mock
    PasswordEncoder passwordEncoder;

    private static final String TX_ID = "12345";

    @Test
    void testCreateTraineeSuccess() {
        TraineeRequestDTO createRequestDTO = getCreateRequestDTO();
        Trainee expected = getTestTrainee();

        when(userService.generateUsername(createRequestDTO.getFirstName(), createRequestDTO.getLastName()))
                .thenReturn(expected.getUsername());
        when(userService.generatePassword()).thenReturn(expected.getPassword());
        when(traineeRepository.findByUsernameStartingWith(expected.getUsername())).thenReturn(Collections.emptyList());

        AuthDTO actual = traineeService.createTrainee(TX_ID, createRequestDTO);

        assertThat(actual).usingRecursiveComparison().isEqualTo(expected);
        verify(traineeRepository, atLeastOnce()).save(any(Trainee.class));
    }

    @Test
    void testCreateTraineeAlreadyRegistered() {
        TraineeRequestDTO createRequestDTO = getCreateRequestDTO();
        Trainee expected = getTestTrainee();

        when(userService.generateUsername(createRequestDTO.getFirstName(), createRequestDTO.getLastName()))
                .thenReturn(expected.getUsername());
        when(userService.generatePassword()).thenReturn(expected.getPassword());
        when(traineeRepository.findByUsernameStartingWith(expected.getUsername())).thenReturn(List.of(expected));

        Assertions.assertThrows(PersonAlreadyRegisteredException.class,
                () -> traineeService.createTrainee(TX_ID, createRequestDTO));
        verifyNoMoreInteractions(traineeRepository);
    }

    @Test
    void testFindByIdSuccess() {
        Trainee expected = getTestTrainee();

        when(traineeRepository.findById(anyLong())).thenReturn(Optional.of(expected));

        Optional<TraineeResponseDTO> actual = traineeService.findById(1);

        assertThat(actual).isPresent();
        assertThat(actual.get().getUsername()).isNull();
        assertThat(actual.get()).usingRecursiveComparison().ignoringFields("username")
                .isEqualTo(expected);
    }

    @Test
    void testUpdateSuccess() {
        Trainee initial = getTestTrainee();
        TraineeRequestDTO updateRequestDTO = getUpdateRequestDTO();

        when(traineeRepository.findById(anyLong())).thenReturn(Optional.of(initial));

        Optional<TraineeResponseDTO> actual = traineeService.update(TX_ID, 1, updateRequestDTO);

        assertThat(actual).isPresent();
        assertThat(actual.get())
                .usingRecursiveComparison()
                .ignoringFields("username")
                .isEqualTo(updateRequestDTO);

        verify(traineeRepository, atLeastOnce()).save(initial);
    }

    @Test
    void testGetPotentialTrainersForTraineeSuccess() {
        List<Trainer> allTrainers = getAllTestTrainers();
        int initialSize = allTrainers.size();

        Trainee trainee = getTestTrainee();
        Trainer initialTrainer = allTrainers.get(0);
        trainee.setTrainers(Set.of(initialTrainer));

        when(traineeRepository.findById(anyLong())).thenReturn(Optional.of(trainee));
        when(trainerService.findAll()).thenReturn(allTrainers);

        List<TrainerResponseDTO> actualList = traineeService.getPotentialTrainersForTrainee(TX_ID, 1);

        assertThat(actualList).hasSizeLessThan(initialSize);
        actualList.forEach(trainer -> assertThat(trainer.getUsername()).isNotEqualTo(initialTrainer.getUsername()));
    }

    @Test
    void testGetPotentialTrainersForTraineeNoSuchTrainee() {
        when(traineeRepository.findById(anyLong())).thenReturn(Optional.empty());

        Assertions.assertThrows(NoSuchTraineeExistException.class,
                () -> traineeService.getPotentialTrainersForTrainee(TX_ID, 1));
    }

    @Test
    void testUpdateTrainerListSuccess() {
        Trainee trainee = getTestTrainee();
        List<Trainer> allTrainers = getAllTestTrainers();
        List<Trainer> updatedTrainersList = List.of(allTrainers.get(0), allTrainers.get(1));
        List<TrainerResponseDTO> expectedList = updatedTrainersList.stream()
                .map(trainer -> modelMapper.map(trainer, TrainerResponseDTO.class))
                .toList();
        expectedList.forEach(trainer -> {
            trainer.setTrainees(null);
            trainer.setActive(null);
        });

        TraineeRequestDTO requestDTO = new TraineeRequestDTO();
        requestDTO.setTrainers(List.of(allTrainers.get(0).getUsername(), allTrainers.get(1).getUsername()));

        when(trainerService.getVerifiedTrainersByUsernameList(TX_ID, requestDTO.getTrainers()))
                .thenReturn(updatedTrainersList);
        when(traineeRepository.findById(anyLong())).thenReturn(Optional.of(trainee));

        List<TrainerResponseDTO> actualList = traineeService.updateTrainerList(TX_ID, 1, requestDTO);

        assertThat(actualList)
                .hasSameSizeAs(updatedTrainersList)
                .containsExactlyInAnyOrderElementsOf(expectedList);
        verify(traineeRepository, atLeastOnce()).save(trainee);
    }

    @Test
    void testUpdateTrainerListNoSuchTrainee() {
        List<Trainer> allTrainers = getAllTestTrainers();
        List<Trainer> updatedTrainersList = List.of(allTrainers.get(0), allTrainers.get(1));

        TraineeRequestDTO requestDTO = new TraineeRequestDTO();
        requestDTO.setTrainers(List.of(allTrainers.get(0).getUsername(), allTrainers.get(1).getUsername()));

        when(trainerService.getVerifiedTrainersByUsernameList(TX_ID, requestDTO.getTrainers()))
                .thenReturn(updatedTrainersList);
        when(traineeRepository.findById(anyLong())).thenReturn(Optional.empty());

        Assertions.assertThrows(NoSuchTraineeExistException.class,
                () -> traineeService.updateTrainerList(TX_ID, 1, requestDTO));

        verifyNoMoreInteractions(traineeRepository);
    }

    private Trainee getTestTrainee() {
        return new Trainee("Miguel", "Rodriguez",
                "miguel.rodriguez", "qwerty", true,
                LocalDate.of(1990, 10, 20), "Mexico");
    }

    private Trainee getUpdatedTrainee() {
        return new Trainee("MiguelUPD", "RodriguezUPD",
                "miguel.rodriguez", "qwerty", false,
                LocalDate.of(1999, 11, 22), "MexicoUPD");
    }

    private Trainer getTestTrainer1() {
        return new Trainer("Michael", "Swat",
                "michael.swat", "12345", true,
                getTestTrainingType());
    }

    private Trainer getTestTrainer2() {
        return new Trainer("Robert", "Green",
                "robert.green", "12345", true,
                getTestTrainingType());
    }

    private Trainer getTestTrainer3() {
        return new Trainer("Sergey", "Kozinsky",
                "sergey.kozinskiy", "12345", true,
                getTestTrainingType());
    }

    private Trainer getTestTrainer4() {
        return new Trainer("Lloyd", "Brawl",
                "lloyd.brawl", "12345", true,
                getTestTrainingType());
    }

    private TrainingType getTestTrainingType() {
        return new TrainingType(1, "Lifting");
    }

    private List<Trainer> getAllTestTrainers() {
        return new ArrayList<>(List.of(
                getTestTrainer1(),
                getTestTrainer2(),
                getTestTrainer3(),
                getTestTrainer4()
        ));
    }

    private TraineeRequestDTO getCreateRequestDTO() {
        Trainee trainee = getTestTrainee();

        TraineeRequestDTO requestDTO = new TraineeRequestDTO();

        requestDTO.setFirstName(trainee.getFirstName());
        requestDTO.setLastName(trainee.getLastName());
        requestDTO.setDateOfBirth(trainee.getDateOfBirth());
        requestDTO.setAddress(trainee.getAddress());

        return requestDTO;
    }

    private TraineeRequestDTO getUpdateRequestDTO() {
        Trainee updatedTrainee = getUpdatedTrainee();

        TraineeRequestDTO requestDTO = new TraineeRequestDTO();

        requestDTO.setFirstName(updatedTrainee.getFirstName());
        requestDTO.setLastName(updatedTrainee.getLastName());
        requestDTO.setDateOfBirth(updatedTrainee.getDateOfBirth());
        requestDTO.setActive(updatedTrainee.getActive());
        requestDTO.setAddress(updatedTrainee.getAddress());

        return requestDTO;
    }
}
