package epam.xstack.unit.controller;

import epam.xstack.controller.TraineeController;
import epam.xstack.dto.auth.AuthDTO;
import epam.xstack.dto.trainee.TraineeRequestDTO;
import epam.xstack.dto.trainee.TraineeResponseDTO;
import epam.xstack.dto.trainer.TrainerResponseDTO;
import epam.xstack.dto.training.TrainingGetListRequestDTO;
import epam.xstack.dto.training.TrainingResponseDTO;
import epam.xstack.exception.ValidationException;
import epam.xstack.model.Trainee;
import epam.xstack.model.Trainer;
import epam.xstack.model.Training;
import epam.xstack.model.TrainingType;
import epam.xstack.service.TraineeService;
import epam.xstack.service.TrainingService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.web.util.UriComponentsBuilder;

import javax.servlet.http.HttpServletRequest;
import java.net.URI;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.verifyNoMoreInteractions;

@ExtendWith(MockitoExtension.class)
class TraineeControllerTest {
    @InjectMocks
    TraineeController traineeController;

    @Mock
    TraineeService traineeService;

    @Mock
    TrainingService trainingService;

    private static final String TX_ID = "12345";

    @Test
    void testCreateTrainee_ReturnsOkResponseEntity() {
        HttpServletRequest mockRequest = mock(HttpServletRequest.class);
        TraineeRequestDTO requestDTO = new TraineeRequestDTO();
        Trainee createdTrainee = getTestTrainee();

        AuthDTO responseDTO = new AuthDTO();
        responseDTO.setUsername(createdTrainee.getUsername());
        responseDTO.setPassword(createdTrainee.getPassword());
        responseDTO.setId(createdTrainee.getId());

        BindingResult bindingResult = new BeanPropertyBindingResult(requestDTO, "requestDTO");

        when(mockRequest.getAttribute("txID")).thenReturn(TX_ID);
        when(traineeService.createTrainee(anyString(), any(TraineeRequestDTO.class))).thenReturn(responseDTO);

        ResponseEntity<?> response = traineeController.handleCreateTrainee(
                requestDTO, bindingResult, UriComponentsBuilder.fromUriString("http://localhost:8080"), mockRequest);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getHeaders().getLocation())
                .isEqualTo(URI.create("http://localhost:8080/api/v1/trainees/" + createdTrainee.getId()));
        assertThat(response.getBody()).isEqualTo(responseDTO);

        verify(traineeService).createTrainee(anyString(), any(TraineeRequestDTO.class));
        verifyNoMoreInteractions(traineeService);
    }

    @Test
    void testCreateTrainee_ThrowsValidationException() {
        HttpServletRequest mockRequest = mock(HttpServletRequest.class);
        TraineeRequestDTO requestDTO = new TraineeRequestDTO();

        BindingResult bindingResult = new BeanPropertyBindingResult(requestDTO, "requestDTO");
        bindingResult.rejectValue("lastName", "Error message");

        when(mockRequest.getAttribute("txID")).thenReturn(TX_ID);

        ValidationException thrownException = Assertions.assertThrows(ValidationException.class,
                () -> traineeController.handleCreateTrainee(requestDTO, bindingResult,
                        UriComponentsBuilder.fromUriString("http://localhost:8080"), mockRequest));

        assertThat(thrownException.getErrors()).contains("lastName").contains("null");
        assertThat(thrownException.getMessage()).isEqualTo(TX_ID);

        verifyNoMoreInteractions(traineeService);
    }

    @Test
    void testGetTrainee_ReturnsOkResponseEntity() {
        HttpServletRequest mockRequest = mock(HttpServletRequest.class);
        Trainee foundTrainee = getTestTrainee();
        TraineeResponseDTO expectedDTO = getTestTraineeResponseDTO(foundTrainee);

        when(mockRequest.getAttribute("txID")).thenReturn(TX_ID);
        when(traineeService.findById(foundTrainee.getId())).thenReturn(Optional.of(expectedDTO));

        ResponseEntity<?> response = traineeController.handleGetTrainee(foundTrainee.getId(), mockRequest);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(expectedDTO);

        verify(traineeService).findById(foundTrainee.getId());
        verifyNoMoreInteractions(traineeService);
    }

    @Test
    void testGetTrainee_ReturnsNotFoundEntity() {
        HttpServletRequest mockRequest = mock(HttpServletRequest.class);

        when(mockRequest.getAttribute("txID")).thenReturn(TX_ID);
        when(traineeService.findById(anyLong())).thenReturn(Optional.empty());

        ResponseEntity<?> response = traineeController.handleGetTrainee(1, mockRequest);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNull();

        verify(traineeService).findById(anyLong());
        verifyNoMoreInteractions(traineeService);
    }

    @Test
    void testUpdateTrainee_ReturnsOkResponseEntity() {
        HttpServletRequest mockRequest = mock(HttpServletRequest.class);
        TraineeRequestDTO requestDTO = new TraineeRequestDTO();
        Trainee updatedTrainee = getTestTrainee();
        BindingResult bindingResult = new BeanPropertyBindingResult(requestDTO, "requestDTO");
        TraineeResponseDTO expectedDTO = getTestTraineeResponseDTO(updatedTrainee);

        when(mockRequest.getAttribute("txID")).thenReturn(TX_ID);
        when(traineeService.update(anyString(), anyLong(), any(TraineeRequestDTO.class)))
                .thenReturn(Optional.of(expectedDTO));

        ResponseEntity<?> response = traineeController.handleUpdateTrainee(
                1, requestDTO, bindingResult, mockRequest);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(expectedDTO);

        verify(traineeService).update(anyString(), anyLong(), any(TraineeRequestDTO.class));
        verifyNoMoreInteractions(traineeService);
    }

    @Test
    void testUpdateTrainee_ReturnsNotFoundEntity() {
        HttpServletRequest mockRequest = mock(HttpServletRequest.class);
        TraineeRequestDTO requestDTO = new TraineeRequestDTO();
        BindingResult bindingResult = new BeanPropertyBindingResult(requestDTO, "requestDTO");

        when(mockRequest.getAttribute("txID")).thenReturn(TX_ID);
        when(traineeService.update(anyString(), anyLong(), any(TraineeRequestDTO.class)))
                .thenReturn(Optional.empty());

        ResponseEntity<?> response = traineeController.handleUpdateTrainee(
                1, requestDTO, bindingResult, mockRequest);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNull();

        verify(traineeService).update(anyString(), anyLong(), any(TraineeRequestDTO.class));
        verifyNoMoreInteractions(traineeService);
    }

    @Test
    void testUpdateTrainee_ThrowsValidationException() {
        HttpServletRequest mockRequest = mock(HttpServletRequest.class);
        TraineeRequestDTO requestDTO = new TraineeRequestDTO();
        BindingResult bindingResult = new BeanPropertyBindingResult(requestDTO, "requestDTO");
        bindingResult.rejectValue("firstName", "Error message");

        when(mockRequest.getAttribute("txID")).thenReturn(TX_ID);

        ValidationException thrownException = Assertions.assertThrows(ValidationException.class,
                () -> traineeController.handleUpdateTrainee(
                        1, requestDTO, bindingResult, mockRequest));

        assertThat(thrownException.getErrors()).contains("firstName").contains("null");
        assertThat(thrownException.getMessage()).isEqualTo(TX_ID);

        verifyNoMoreInteractions(traineeService);
    }

    @Test
    void testDeleteTrainee_ReturnsNoContentEntity() {
        HttpServletRequest mockRequest = mock(HttpServletRequest.class);
        Trainee traineeToDelete = getTestTrainee();

        when(mockRequest.getAttribute("txID")).thenReturn(TX_ID);
        doNothing().when(traineeService).delete(TX_ID, 1);

        ResponseEntity<?> response = traineeController.handleDeleteTrainee(1, mockRequest);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(response.getBody()).isNull();

        verify(traineeService).delete(TX_ID, 1);
        verifyNoMoreInteractions(traineeService);
    }

    @Test
    void testChangeActivationStatus_ReturnsOkEntity() {
        HttpServletRequest mockRequest = mock(HttpServletRequest.class);
        TraineeRequestDTO requestDTO = new TraineeRequestDTO();
        requestDTO.setActive(true);
        BindingResult bindingResult = new BeanPropertyBindingResult(requestDTO, "requestDTO");

        when(mockRequest.getAttribute("txID")).thenReturn(TX_ID);
        doNothing().when(traineeService).changeActivationStatus(anyString(), anyLong(), anyBoolean());

        ResponseEntity<?> response = traineeController.handleChangeActivationStatus(
                1, requestDTO, bindingResult, mockRequest);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNull();

        verify(traineeService).changeActivationStatus(anyString(), anyLong(), anyBoolean());
        verifyNoMoreInteractions(traineeService);
    }

    @Test
    void testChangeActivationStatus_ThrowsValidationException() {
        HttpServletRequest mockRequest = mock(HttpServletRequest.class);
        TraineeRequestDTO requestDTO = new TraineeRequestDTO();
        BindingResult bindingResult = new BeanPropertyBindingResult(requestDTO, "requestDTO");
        bindingResult.rejectValue("active", "Error message");

        when(mockRequest.getAttribute("txID")).thenReturn(TX_ID);

        ValidationException thrownException = Assertions.assertThrows(ValidationException.class,
                () -> traineeController.handleChangeActivationStatus(
                        1, requestDTO, bindingResult, mockRequest));

        assertThat(thrownException.getErrors()).contains("active").contains("null");
        assertThat(thrownException.getMessage()).isEqualTo(TX_ID);

        verifyNoMoreInteractions(traineeService);
    }

    @Test
    void testGetUnassignedTrainersForTrainee_ReturnsOkEntity() {
        HttpServletRequest mockRequest = mock(HttpServletRequest.class);
        List<TrainerResponseDTO> expectedList = new ArrayList<>();

        when(mockRequest.getAttribute("txID")).thenReturn(TX_ID);
        when(traineeService.getPotentialTrainersForTrainee(anyString(), anyLong())).thenReturn(expectedList);

        ResponseEntity<?> response = traineeController.handleGetUnassignedTrainersForTrainee(1, mockRequest);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(expectedList);

        verify(traineeService).getPotentialTrainersForTrainee(anyString(), anyLong());
        verifyNoMoreInteractions(traineeService);
    }

    @Test
    void testGetTrainingsWithFiltering_ReturnsOkEntity() {
        HttpServletRequest mockRequest = mock(HttpServletRequest.class);
        TrainingGetListRequestDTO requestDTO = new TrainingGetListRequestDTO();
        BindingResult bindingResult = new BeanPropertyBindingResult(requestDTO, "requestDTO");
        List<TrainingResponseDTO> expectedList = new ArrayList<>();

        when(mockRequest.getAttribute("txID")).thenReturn(TX_ID);
        when(trainingService.getTraineeTrainingsWithFiltering(anyLong(), any(TrainingGetListRequestDTO.class)))
                .thenReturn(expectedList);

        ResponseEntity<?> response = traineeController.handleGetTrainingsWithFiltering(
                1, requestDTO, bindingResult, mockRequest);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(expectedList);

        verify(trainingService).getTraineeTrainingsWithFiltering(anyLong(), any(TrainingGetListRequestDTO.class));
        verifyNoMoreInteractions(traineeService);
    }


    @Test
    void UpdateTraineeTrainerList_ReturnsOkEntity() {
        HttpServletRequest mockRequest = mock(HttpServletRequest.class);
        TraineeRequestDTO requestDTO = new TraineeRequestDTO();
        BindingResult bindingResult = new BeanPropertyBindingResult(requestDTO, "requestDTO");
        List<TrainerResponseDTO> expectedSet = new ArrayList<>();

        when(mockRequest.getAttribute("txID")).thenReturn(TX_ID);
        when(traineeService.updateTrainerList(anyString(), anyLong(), any(TraineeRequestDTO.class)))
                .thenReturn(expectedSet);

//        Answer<TrainerResponseDTO> answer = invocation -> {
//            Trainer value = invocation.getArgument(0);
//            TrainerResponseDTO result = getTestTrainerResponseDTO(value);
//            expectedSet.add(result);
//            return result;
//        };
//
//        when(modelMapper.map(any(Trainer.class), eq(TrainerResponseDTO.class)))
//                .thenAnswer(answer)
//                .thenAnswer(answer)
//                .thenAnswer(answer);

        ResponseEntity<?> response = traineeController.handleUpdateTraineeTrainerList(
                1, requestDTO, bindingResult, mockRequest);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(expectedSet);

        verify(traineeService).updateTrainerList(
                anyString(), anyLong(), any(TraineeRequestDTO.class));
//        verify(modelMapper, times(3)).map(any(Trainer.class), eq(TrainerResponseDTO.class));
        verifyNoMoreInteractions(traineeService);
    }

    @Test
    void UpdateTraineeTrainerList_ThrowsValidationException() {
        HttpServletRequest mockRequest = mock(HttpServletRequest.class);
        TraineeRequestDTO requestDTO = new TraineeRequestDTO();
        BindingResult bindingResult = new BeanPropertyBindingResult(requestDTO, "requestDTO");
        bindingResult.rejectValue("trainers", "Error message");

        when(mockRequest.getAttribute("txID")).thenReturn(TX_ID);

        ValidationException thrownException = Assertions.assertThrows(ValidationException.class,
                () -> traineeController.handleUpdateTraineeTrainerList(
                        1, requestDTO, bindingResult, mockRequest));

        assertThat(thrownException.getErrors()).contains("trainers").contains("null");
        assertThat(thrownException.getMessage()).isEqualTo(TX_ID);

        verifyNoMoreInteractions(traineeService);
    }

    private Trainee getTestTrainee() {
        Trainee trainee = new Trainee("trainee", "trainee",
                "trainee.trainee", "qwerty", true,
                LocalDate.of(1980, 2, 16), "traineeCity");

        trainee.setId(1);

        trainee.setTrainers(Set.of(new Trainer("trainer", "trainer", "trainer.trainer",
                "123", true, new TrainingType(1, "Lifting"))));

        return trainee;
    }

    private List<Trainer> getTestTrainers() {
        return List.of(
                new Trainer("Michael", "Swat",
                        "michael.swat", "12345", true,
                        new TrainingType(1, "Lifting")),
                new Trainer("Robert", "Green",
                        "robert.green", "12345", true,
                        new TrainingType(2, "Crossfit")),
                new Trainer("Sergey", "Kozinsky",
                        "sergey.kozinskiy", "12345", true,
                        new TrainingType(2, "Cardio"))
        );
    }

    private List<Training> getTestTrainings() {
        TrainingType trainingType = new TrainingType(1, "Lifting");

        Trainee trainee = new Trainee("trainee", "trainee",
                "trainee.trainee", "qwerty", true,
                LocalDate.of(1980, 2, 16), "traineeCity");

        Trainer trainer = new Trainer("Michael", "Swat",
                "michael.swat", "12345", true, trainingType);


        return List.of(
                new Training(trainee, trainer, "First training",
                        trainingType, LocalDate.of(2023, 10, 3), 100),
                new Training(trainee, trainer, "Second training",
                        trainingType, LocalDate.of(2023, 10, 5), 110),
                new Training(trainee, trainer, "Third training",
                        trainingType, LocalDate.of(2023, 10, 7), 120)
        );
    }

    private TraineeResponseDTO getTestTraineeResponseDTO(Trainee trainee) {
        ModelMapper oneTimeMapper = new ModelMapper();
        return oneTimeMapper.map(trainee, TraineeResponseDTO.class);
    }

    private TrainerResponseDTO getTestTrainerResponseDTO(Trainer trainer) {
        ModelMapper oneTimeMapper = new ModelMapper();
        return oneTimeMapper.map(trainer, TrainerResponseDTO.class);
    }

    private TrainingResponseDTO getTestTrainingResponseDTO(Training training) {
        ModelMapper oneTimeMapper = new ModelMapper();
        return oneTimeMapper.map(training, TrainingResponseDTO.class);
    }
}
