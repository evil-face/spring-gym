package epam.xstack.unit.service;

import epam.xstack.dto.auth.AuthDTO;
import epam.xstack.dto.trainer.TrainerRequestDTO;
import epam.xstack.dto.trainer.TrainerResponseDTO;
import epam.xstack.exception.NoSuchTrainerExistException;
import epam.xstack.exception.NoSuchTrainingTypeException;
import epam.xstack.exception.PersonAlreadyRegisteredException;
import epam.xstack.model.Trainer;
import epam.xstack.model.TrainingType;

import epam.xstack.repository.TrainerRepository;
import epam.xstack.repository.TrainingTypeRepository;
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

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.atLeastOnce;

@ExtendWith(MockitoExtension.class)
class TrainerServiceTest {
    @InjectMocks
    TrainerService trainerService;

    @Mock
    TrainerRepository trainerRepository;

    @Mock
    TrainingTypeRepository trainingTypeRepository;

    @Mock
    UserService userService;

    @Spy
    ModelMapper modelMapper;

    @Mock
    MeterRegistry meterRegistry;

    private static final String TX_ID = "12345";

    @Test
    void testCreateTrainerSuccess() {
        TrainerRequestDTO createRequestDTO = getCreateRequestDTO();
        Trainer expected = getTestTrainer();

        when(userService.generateUsername(createRequestDTO.getFirstName(), createRequestDTO.getLastName()))
                .thenReturn(expected.getUsername());
        when(userService.generatePassword()).thenReturn(expected.getPassword());
        when(trainingTypeRepository.findById(createRequestDTO.getSpecialization()))
                .thenReturn(Optional.of(expected.getSpecialization()));
        when(trainerRepository.findByUsernameStartingWith(expected.getUsername())).thenReturn(Collections.emptyList());

        AuthDTO actual = trainerService.createTrainer(TX_ID, createRequestDTO);

        assertThat(actual).usingRecursiveComparison().isEqualTo(expected);
        verify(trainerRepository, atLeastOnce()).save(any(Trainer.class));
    }

    @Test
    void testCreateTrainerAlreadyRegistered() {
        TrainerRequestDTO createRequestDTO = getCreateRequestDTO();
        Trainer expected = getTestTrainer();

        when(userService.generateUsername(createRequestDTO.getFirstName(), createRequestDTO.getLastName()))
                .thenReturn(expected.getUsername());
        when(userService.generatePassword()).thenReturn(expected.getPassword());
        when(trainingTypeRepository.findById(createRequestDTO.getSpecialization()))
                .thenReturn(Optional.of(expected.getSpecialization()));
        when(trainerRepository.findByUsernameStartingWith(expected.getUsername())).thenReturn(List.of(expected));

        Assertions.assertThrows(PersonAlreadyRegisteredException.class,
                () -> trainerService.createTrainer(TX_ID, createRequestDTO));
        verifyNoMoreInteractions(trainerRepository);
    }

    @Test
    void testCreateTrainerNoSuchSpecialization() {
        TrainerRequestDTO createRequestDTO = getCreateRequestDTO();
        Trainer expected = getTestTrainer();

        when(userService.generateUsername(createRequestDTO.getFirstName(), createRequestDTO.getLastName()))
                .thenReturn(expected.getUsername());
        when(userService.generatePassword()).thenReturn(expected.getPassword());
        when(trainingTypeRepository.findById(createRequestDTO.getSpecialization()))
                .thenReturn(Optional.empty());

        Assertions.assertThrows(NoSuchTrainingTypeException.class,
                () -> trainerService.createTrainer(TX_ID, createRequestDTO));
    }

    @Test
    void testFindByIdSuccess() {
        Trainer expected = getTestTrainer();

        when(trainerRepository.findById(anyLong())).thenReturn(Optional.of(expected));

        Optional<TrainerResponseDTO> actual = trainerService.findById(1);

        assertThat(actual).isPresent();
        assertThat(actual.get().getUsername()).isNull();
        assertThat(actual.get()).usingRecursiveComparison().isEqualTo(expected);
    }

    @Test
    void testUpdateSuccess() {
        Trainer initial = getTestTrainer();
        TrainerRequestDTO updateRequestDTO = getUpdateRequestDTO();

        when(trainingTypeRepository.findById(initial.getSpecialization().getId()))
                .thenReturn(Optional.of(initial.getSpecialization()));
        when(trainerRepository.findById(anyLong())).thenReturn(Optional.of(initial));

        Optional<TrainerResponseDTO> actual = trainerService.update(TX_ID, 1, updateRequestDTO);

        assertThat(actual).isPresent();
        assertThat(actual.get())
                .usingRecursiveComparison()
                .ignoringFields("username", "trainees", "specialization")
                .isEqualTo(updateRequestDTO);
        assertThat(actual.get().getSpecialization().getId()).isEqualTo(updateRequestDTO.getSpecialization());

        verify(trainerRepository, atLeastOnce()).save(initial);
    }

    @Test
    void testUpdateNoSuchSpecialization() {
        TrainerRequestDTO updateRequestDTO = getUpdateRequestDTO();

        when(trainingTypeRepository.findById(updateRequestDTO.getSpecialization()))
                .thenReturn(Optional.empty());

        Assertions.assertThrows(NoSuchTrainingTypeException.class,
                () -> trainerService.update(TX_ID, 1, updateRequestDTO));
    }

    @Test
    void testGetVerifiedTrainersByUsernameList() {
        List<Trainer> allTrainers = getTestTrainers();
        List<String> trainerUsernames = List.of(getTestTrainer().getUsername(), getTestTrainer1().getUsername());

        when(trainerRepository.findAll()).thenReturn(allTrainers);

        List<Trainer> actualList = trainerService.getVerifiedTrainersByUsernameList(TX_ID, trainerUsernames);

        assertThat(actualList).containsExactlyInAnyOrderElementsOf(allTrainers);
    }

    @Test
    void testGetVerifiedTrainersByUsernameListNoSuchTrainers() {
        List<Trainer> allTrainers = getTestTrainers();
        List<String> trainerUsernames = List.of("not exist", "not exist 1");

        when(trainerRepository.findAll()).thenReturn(allTrainers);

        Assertions.assertThrows(NoSuchTrainerExistException.class,
                () -> trainerService.getVerifiedTrainersByUsernameList(TX_ID,trainerUsernames));
    }

    private TrainerRequestDTO getCreateRequestDTO() {
        Trainer trainer = getTestTrainer();

        TrainerRequestDTO requestDTO = new TrainerRequestDTO();

        requestDTO.setFirstName(trainer.getFirstName());
        requestDTO.setLastName(trainer.getLastName());
        requestDTO.setSpecialization(getTestTrainingType().getId());

        return requestDTO;
    }

    private TrainerRequestDTO getUpdateRequestDTO() {
        Trainer updatedTrainer = getUpdatedTrainer();

        TrainerRequestDTO requestDTO = new TrainerRequestDTO();

        requestDTO.setFirstName(updatedTrainer.getFirstName());
        requestDTO.setLastName(updatedTrainer.getLastName());
        requestDTO.setActive(updatedTrainer.getActive());
        requestDTO.setSpecialization(updatedTrainer.getSpecialization().getId());

        return requestDTO;
    }

    private Trainer getTestTrainer() {
        return new Trainer("Miguel", "Rodriguez",
                "miguel.rodriguez", "qwerty", true,
                getTestTrainingType());
    }

    private Trainer getTestTrainer1() {
        return new Trainer("Michael", "Swat",
                "michael.swat", "12345", true,
                getTestTrainingType());
    }

    private List<Trainer> getTestTrainers() {
        return List.of(getTestTrainer(), getTestTrainer1());
    }

    private Trainer getUpdatedTrainer() {
        return new Trainer("MiguelUPD", "RodriguezUPD",
                "miguel.rodriguez", "qwerty", false,
                getTestTrainingType());
    }

    private TrainingType getTestTrainingType() {
        return new TrainingType(1, "Lifting");
    }
}
