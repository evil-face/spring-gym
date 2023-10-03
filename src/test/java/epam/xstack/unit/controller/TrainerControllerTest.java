package epam.xstack.unit.controller;

import epam.xstack.controller.TrainerController;
import epam.xstack.dto.auth.AuthDTO;
import epam.xstack.dto.trainer.TrainerRequestDTO;
import epam.xstack.dto.trainer.TrainerResponseDTO;
import epam.xstack.dto.training.TrainingGetListRequestDTO;
import epam.xstack.dto.training.TrainingResponseDTO;
import epam.xstack.model.Trainee;
import epam.xstack.model.Trainer;
import epam.xstack.model.Training;
import epam.xstack.model.TrainingType;
import epam.xstack.service.TrainerService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.stubbing.Answer;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TrainerControllerTest {
    @InjectMocks
    TrainerController trainerController;
    @Mock
    TrainerService trainerService;
    @Mock
    ModelMapper modelMapper;
    private static final String TX_ID = "12345";

    @Test
    void testCreateTrainer_ReturnsOkResponseEntity() {
        HttpServletRequest mockRequest = mock(HttpServletRequest.class);
        TrainerRequestDTO requestDTO = new TrainerRequestDTO();
        Trainer createdTrainer = getTestTrainer();

        AuthDTO responseDTO = new AuthDTO();
        responseDTO.setUsername(createdTrainer.getUsername());
        responseDTO.setPassword(createdTrainer.getPassword());

        BindingResult bindingResult = new BeanPropertyBindingResult(requestDTO, "requestDTO");

        when(mockRequest.getAttribute("txID")).thenReturn(TX_ID);
        when(modelMapper.map(requestDTO, Trainer.class)).thenReturn(new Trainer());
        when(trainerService.createTrainer(anyString(), any(Trainer.class))).thenReturn(createdTrainer);
        when(modelMapper.map(createdTrainer, AuthDTO.class)).thenReturn(responseDTO);


        ResponseEntity<?> response = trainerController.handleCreateTrainer(
                requestDTO, bindingResult, UriComponentsBuilder.fromUriString("http://localhost:8080"), mockRequest);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getHeaders().getLocation())
                .isEqualTo(URI.create("http://localhost:8080/api/v1/trainers/" + createdTrainer.getId()));
        assertThat(response.getBody()).isEqualTo(responseDTO);

        verify(trainerService).createTrainer(anyString(), any(Trainer.class));
        verifyNoMoreInteractions(trainerService);
    }

    @Test
    void testCreateTrainer_ReturnsUnprocessableEntity() {
        HttpServletRequest mockRequest = mock(HttpServletRequest.class);
        TrainerRequestDTO requestDTO = new TrainerRequestDTO();
        Trainer createdTrainer = getTestTrainer();

        AuthDTO responseDTO = new AuthDTO();
        responseDTO.setUsername(createdTrainer.getUsername());
        responseDTO.setPassword(createdTrainer.getPassword());

        BindingResult bindingResult = new BeanPropertyBindingResult(requestDTO, "requestDTO");
        bindingResult.rejectValue("lastName", "Error message");

        when(mockRequest.getAttribute("txID")).thenReturn(TX_ID);

        ResponseEntity<?> response = trainerController.handleCreateTrainer(
                requestDTO, bindingResult, UriComponentsBuilder.fromUriString("http://localhost:8080"), mockRequest);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
        assertThat(response.getHeaders().getLocation())
                .isNull();
        assertThat(response.getBody().toString()).contains("lastName").contains("null");

        verifyNoMoreInteractions(trainerService);
    }

    @Test
    void testGetTrainer_ReturnsOkResponseEntity() {
        HttpServletRequest mockRequest = mock(HttpServletRequest.class);
        Trainer foundTrainer = getTestTrainer();
        AuthDTO authDTO = new AuthDTO();
        authDTO.setUsername(foundTrainer.getUsername());
        authDTO.setPassword(foundTrainer.getPassword());
        BindingResult bindingResult = new BeanPropertyBindingResult(authDTO, "authDTO");
        TrainerResponseDTO expectedDTO = getTestTrainerResponseDTO(foundTrainer);

        when(mockRequest.getAttribute("txID")).thenReturn(TX_ID);
        when(trainerService.findById(TX_ID, foundTrainer.getId(), authDTO.getUsername(), authDTO.getPassword()))
                .thenReturn(Optional.of(foundTrainer));
        when(modelMapper.map(any(), any())).thenReturn(expectedDTO);

        ResponseEntity<?> response = trainerController.handleGetTrainer(
                foundTrainer.getId(), authDTO, bindingResult, mockRequest);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(expectedDTO);

        verify(trainerService).findById(TX_ID, foundTrainer.getId(), authDTO.getUsername(), authDTO.getPassword());
        verifyNoMoreInteractions(trainerService);
    }

    @Test
    void testGetTrainer_ReturnsNotFoundEntity() {
        HttpServletRequest mockRequest = mock(HttpServletRequest.class);
        AuthDTO authDTO = new AuthDTO();
        BindingResult bindingResult = new BeanPropertyBindingResult(authDTO, "authDTO");

        when(mockRequest.getAttribute("txID")).thenReturn(TX_ID);
        when(trainerService.findById(anyString(), anyLong(), any(), any()))
                .thenReturn(Optional.empty());

        ResponseEntity<?> response = trainerController.handleGetTrainer(
                1, authDTO, bindingResult, mockRequest);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNull();

        verify(trainerService).findById(anyString(), anyLong(), any(), any());
        verifyNoMoreInteractions(trainerService);
    }

    @Test
    void testGetTrainer_ReturnsUnprocessableEntity() {
        HttpServletRequest mockRequest = mock(HttpServletRequest.class);
        AuthDTO authDTO = new AuthDTO();
        BindingResult bindingResult = new BeanPropertyBindingResult(authDTO, "authDTO");
        bindingResult.rejectValue("password", "Error message");

        when(mockRequest.getAttribute("txID")).thenReturn(TX_ID);

        ResponseEntity<?> response = trainerController.handleGetTrainer(1, authDTO, bindingResult, mockRequest);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
        assertThat(response.getBody().toString()).contains("password").contains("null");

        verifyNoMoreInteractions(trainerService);
    }

    @Test
    void testUpdateTrainer_ReturnsOkResponseEntity() {
        HttpServletRequest mockRequest = mock(HttpServletRequest.class);
        TrainerRequestDTO requestDTO = new TrainerRequestDTO();
        Trainer updatedTrainer = getTestTrainer();
        BindingResult bindingResult = new BeanPropertyBindingResult(requestDTO, "requestDTO");
        TrainerResponseDTO expectedDTO = getTestTrainerResponseDTO(updatedTrainer);

        when(mockRequest.getAttribute("txID")).thenReturn(TX_ID);
        when(modelMapper.map(requestDTO, Trainer.class)).thenReturn(new Trainer());
        when(trainerService.update(anyString(), any(Trainer.class), any(), any()))
                .thenReturn(Optional.of(updatedTrainer));
        when(modelMapper.map(updatedTrainer, TrainerResponseDTO.class)).thenReturn(expectedDTO);

        ResponseEntity<?> response = trainerController.handleUpdateTrainer(
                1, requestDTO, bindingResult, mockRequest);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(expectedDTO);

        verify(trainerService).update(anyString(), any(Trainer.class), any(), any());
        verifyNoMoreInteractions(trainerService);
    }

    @Test
    void testUpdateTrainer_ReturnsNotFoundEntity() {
        HttpServletRequest mockRequest = mock(HttpServletRequest.class);
        TrainerRequestDTO requestDTO = new TrainerRequestDTO();
        BindingResult bindingResult = new BeanPropertyBindingResult(requestDTO, "requestDTO");

        when(mockRequest.getAttribute("txID")).thenReturn(TX_ID);
        when(modelMapper.map(requestDTO, Trainer.class)).thenReturn(new Trainer());
        when(trainerService.update(anyString(), any(Trainer.class), any(), any()))
                .thenReturn(Optional.empty());

        ResponseEntity<?> response = trainerController.handleUpdateTrainer(
                1, requestDTO, bindingResult, mockRequest);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNull();

        verify(trainerService).update(anyString(), any(Trainer.class), any(), any());
        verifyNoMoreInteractions(trainerService);
    }

    @Test
    void testUpdateTrainer_ReturnsUnprocessableEntity() {
        HttpServletRequest mockRequest = mock(HttpServletRequest.class);
        TrainerRequestDTO requestDTO = new TrainerRequestDTO();
        BindingResult bindingResult = new BeanPropertyBindingResult(requestDTO, "requestDTO");
        bindingResult.rejectValue("firstName", "Error message");

        when(mockRequest.getAttribute("txID")).thenReturn(TX_ID);

        ResponseEntity<?> response = trainerController.handleUpdateTrainer(
                1, requestDTO, bindingResult, mockRequest);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
        assertThat(response.getBody().toString()).contains("firstName").contains("null");

        verifyNoMoreInteractions(trainerService);
    }

    @Test
    void testChangeActivationStatus_ReturnsOkEntity() {
        HttpServletRequest mockRequest = mock(HttpServletRequest.class);
        TrainerRequestDTO requestDTO = new TrainerRequestDTO();
        BindingResult bindingResult = new BeanPropertyBindingResult(requestDTO, "requestDTO");

        when(mockRequest.getAttribute("txID")).thenReturn(TX_ID);
        doNothing().when(trainerService).changeActivationStatus(anyString(), anyLong(), any(), any(), any());

        ResponseEntity<?> response = trainerController.handleChangeActivationStatus(
                1, requestDTO, bindingResult, mockRequest);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNull();

        verify(trainerService).changeActivationStatus(anyString(), anyLong(), any(), any(), any());
        verifyNoMoreInteractions(trainerService);
    }

    @Test
    void testChangeActivationStatus_ReturnsUnprocessableEntity() {
        HttpServletRequest mockRequest = mock(HttpServletRequest.class);
        TrainerRequestDTO requestDTO = new TrainerRequestDTO();
        BindingResult bindingResult = new BeanPropertyBindingResult(requestDTO, "requestDTO");
        bindingResult.rejectValue("isActive", "Error message");

        when(mockRequest.getAttribute("txID")).thenReturn(TX_ID);

        ResponseEntity<?> response = trainerController.handleChangeActivationStatus(
                1, requestDTO, bindingResult, mockRequest);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
        assertThat(response.getBody().toString()).contains("isActive").contains("null");

        verifyNoMoreInteractions(trainerService);
    }

    @Test
    void testGetTrainingsWithFiltering_ReturnsOkEntity() {
        HttpServletRequest mockRequest = mock(HttpServletRequest.class);
        TrainingGetListRequestDTO requestDTO = new TrainingGetListRequestDTO();
        BindingResult bindingResult = new BeanPropertyBindingResult(requestDTO, "requestDTO");
        List<Training> testTrainings = getTestTrainings();
        List<TrainingResponseDTO> expectedList = new ArrayList<>();

        when(mockRequest.getAttribute("txID")).thenReturn(TX_ID);
        when(trainerService.getTrainingsWithFiltering(
                anyString(), anyLong(), any(), any(), any(TrainingGetListRequestDTO.class)))
                .thenReturn(testTrainings);

        Answer<TrainingResponseDTO> answer = invocation -> {
            Training value = invocation.getArgument(0);
            TrainingResponseDTO result = getTestTrainingResponseDTO(value);
            expectedList.add(result);
            return result;
        };

        when(modelMapper.map(any(Training.class), eq(TrainingResponseDTO.class)))
                .thenAnswer(answer)
                .thenAnswer(answer)
                .thenAnswer(answer);

        ResponseEntity<?> response = trainerController.handleGetTrainingsWithFiltering(
                1, requestDTO, bindingResult, mockRequest);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(expectedList);

        verify(trainerService).getTrainingsWithFiltering(
                anyString(), anyLong(), any(), any(), any(TrainingGetListRequestDTO.class));
        verify(modelMapper, times(3)).map(any(Training.class), eq(TrainingResponseDTO.class));
        verifyNoMoreInteractions(trainerService);
    }

    @Test
    void testGetTrainingsWithFiltering_ReturnsUnprocessableEntity() {
        HttpServletRequest mockRequest = mock(HttpServletRequest.class);
        TrainingGetListRequestDTO requestDTO = new TrainingGetListRequestDTO();
        BindingResult bindingResult = new BeanPropertyBindingResult(requestDTO, "requestDTO");
        bindingResult.rejectValue("password", "Error message");

        when(mockRequest.getAttribute("txID")).thenReturn(TX_ID);

        ResponseEntity<?> response = trainerController.handleGetTrainingsWithFiltering(
                1, requestDTO, bindingResult, mockRequest);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
        assertThat(response.getBody().toString()).contains("password").contains("null");

        verifyNoMoreInteractions(trainerService);
    }

    private Trainer getTestTrainer() {
        Trainer trainer = new Trainer("Michael", "Swat",
                "michael.swat", "12345", true,
                new TrainingType(1, "Lifting"));

        trainer.setId(1);

        trainer.setTrainees(Set.of(new Trainee("trainee", "trainee",
                "trainee.trainee", "qwerty", true,
                LocalDate.of(1980, 2, 16), "traineeCity")));

        return trainer;
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

    private TrainerResponseDTO getTestTrainerResponseDTO(Trainer trainer) {
        ModelMapper oneTimeMapper = new ModelMapper();
        return oneTimeMapper.map(trainer, TrainerResponseDTO.class);
    }

    private TrainingResponseDTO getTestTrainingResponseDTO(Training training) {
        ModelMapper oneTimeMapper = new ModelMapper();
        return oneTimeMapper.map(training, TrainingResponseDTO.class);
    }
}
