package epam.xstack.unit.controller;

import epam.xstack.controller.TrainerController;
import epam.xstack.dto.auth.AuthDTO;
import epam.xstack.dto.trainer.TrainerRequestDTO;
import epam.xstack.dto.trainer.TrainerResponseDTO;
import epam.xstack.dto.training.TrainingGetListRequestDTO;
import epam.xstack.dto.training.TrainingResponseDTO;
import epam.xstack.exception.ValidationException;
import epam.xstack.model.Trainee;
import epam.xstack.model.Trainer;
import epam.xstack.model.TrainingType;
import epam.xstack.service.TrainerService;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TrainerControllerTest {
    @InjectMocks
    TrainerController trainerController;

    @Mock
    TrainerService trainerService;

    @Mock
    TrainingService trainingService;

    private static final String TX_ID = "12345";

    @Test
    void testCreateTrainer_ReturnsOkResponseEntity() {
        HttpServletRequest mockRequest = mock(HttpServletRequest.class);
        TrainerRequestDTO requestDTO = new TrainerRequestDTO();
        Trainer createdTrainer = getTestTrainer();

        AuthDTO responseDTO = new AuthDTO();
        responseDTO.setUsername(createdTrainer.getUsername());
        responseDTO.setPassword(createdTrainer.getPassword());
        responseDTO.setId(createdTrainer.getId());

        BindingResult bindingResult = new BeanPropertyBindingResult(requestDTO, "requestDTO");

        when(mockRequest.getAttribute("txID")).thenReturn(TX_ID);
        when(trainerService.createTrainer(anyString(), any(TrainerRequestDTO.class))).thenReturn(responseDTO);

        ResponseEntity<?> response = trainerController.handleCreateTrainer(
                requestDTO, bindingResult, UriComponentsBuilder.fromUriString("http://localhost:8080"), mockRequest);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getHeaders().getLocation())
                .isEqualTo(URI.create("http://localhost:8080/api/v1/trainers/" + createdTrainer.getId()));
        assertThat(response.getBody()).isEqualTo(responseDTO);

        verify(trainerService).createTrainer(anyString(), any(TrainerRequestDTO.class));
        verifyNoMoreInteractions(trainerService);
    }

    @Test
    void testCreateTrainer_ThrowsValidationException() {
        HttpServletRequest mockRequest = mock(HttpServletRequest.class);
        TrainerRequestDTO requestDTO = new TrainerRequestDTO();

        BindingResult bindingResult = new BeanPropertyBindingResult(requestDTO, "requestDTO");
        bindingResult.rejectValue("lastName", "Error message");

        when(mockRequest.getAttribute("txID")).thenReturn(TX_ID);

        ValidationException thrownException = Assertions.assertThrows(ValidationException.class,
                () -> trainerController.handleCreateTrainer(requestDTO, bindingResult,
                        UriComponentsBuilder.fromUriString("http://localhost:8080"), mockRequest));

        assertThat(thrownException.getErrors()).contains("lastName").contains("null");
        assertThat(thrownException.getMessage()).isEqualTo(TX_ID);

        verifyNoMoreInteractions(trainerService);
    }

    @Test
    void testGetTrainer_ReturnsOkResponseEntity() {
        HttpServletRequest mockRequest = mock(HttpServletRequest.class);
        Trainer foundTrainer = getTestTrainer();
        TrainerResponseDTO expectedDTO = getTestTrainerResponseDTO(foundTrainer);

        when(mockRequest.getAttribute("txID")).thenReturn(TX_ID);
        when(trainerService.findById(foundTrainer.getId())).thenReturn(Optional.of(expectedDTO));

        ResponseEntity<?> response = trainerController.handleGetTrainer(foundTrainer.getId(), mockRequest);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(expectedDTO);

        verify(trainerService).findById(foundTrainer.getId());
        verifyNoMoreInteractions(trainerService);
    }

    @Test
    void testGetTrainer_ReturnsNotFoundEntity() {
        HttpServletRequest mockRequest = mock(HttpServletRequest.class);

        when(mockRequest.getAttribute("txID")).thenReturn(TX_ID);
        when(trainerService.findById(anyLong())).thenReturn(Optional.empty());

        ResponseEntity<?> response = trainerController.handleGetTrainer(1, mockRequest);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNull();

        verify(trainerService).findById(anyLong());
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
        when(trainerService.update(anyString(), anyLong(), any(TrainerRequestDTO.class)))
                .thenReturn(Optional.of(expectedDTO));

        ResponseEntity<?> response = trainerController.handleUpdateTrainer(
                1, requestDTO, bindingResult, mockRequest);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(expectedDTO);

        verify(trainerService).update(anyString(), anyLong(), any(TrainerRequestDTO.class));
        verifyNoMoreInteractions(trainerService);
    }

    @Test
    void testUpdateTrainer_ReturnsNotFoundEntity() {
        HttpServletRequest mockRequest = mock(HttpServletRequest.class);
        TrainerRequestDTO requestDTO = new TrainerRequestDTO();
        BindingResult bindingResult = new BeanPropertyBindingResult(requestDTO, "requestDTO");

        when(mockRequest.getAttribute("txID")).thenReturn(TX_ID);
        when(trainerService.update(anyString(), anyLong(), any(TrainerRequestDTO.class)))
                .thenReturn(Optional.empty());

        ResponseEntity<?> response = trainerController.handleUpdateTrainer(
                1, requestDTO, bindingResult, mockRequest);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNull();

        verify(trainerService).update(anyString(), anyLong(), any(TrainerRequestDTO.class));
        verifyNoMoreInteractions(trainerService);
    }

    @Test
    void testUpdateTrainer_ThrowsValidationException() {
        HttpServletRequest mockRequest = mock(HttpServletRequest.class);
        TrainerRequestDTO requestDTO = new TrainerRequestDTO();
        BindingResult bindingResult = new BeanPropertyBindingResult(requestDTO, "requestDTO");
        bindingResult.rejectValue("firstName", "Error message");

        when(mockRequest.getAttribute("txID")).thenReturn(TX_ID);

        ValidationException thrownException = Assertions.assertThrows(ValidationException.class,
                () -> trainerController.handleUpdateTrainer(
                        1, requestDTO, bindingResult, mockRequest));

        assertThat(thrownException.getErrors()).contains("firstName").contains("null");
        assertThat(thrownException.getMessage()).isEqualTo(TX_ID);

        verifyNoMoreInteractions(trainerService);
    }

    @Test
    void testChangeActivationStatus_ReturnsOkEntity() {
        HttpServletRequest mockRequest = mock(HttpServletRequest.class);
        TrainerRequestDTO requestDTO = new TrainerRequestDTO();
        requestDTO.setActive(true);
        BindingResult bindingResult = new BeanPropertyBindingResult(requestDTO, "requestDTO");

        when(mockRequest.getAttribute("txID")).thenReturn(TX_ID);
        doNothing().when(trainerService).changeActivationStatus(anyString(), anyLong(), anyBoolean());

        ResponseEntity<?> response = trainerController.handleChangeActivationStatus(
                1, requestDTO, bindingResult, mockRequest);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNull();

        verify(trainerService).changeActivationStatus(anyString(), anyLong(), anyBoolean());
        verifyNoMoreInteractions(trainerService);
    }

    @Test
    void testChangeActivationStatus_ThrowsValidationException() {
        HttpServletRequest mockRequest = mock(HttpServletRequest.class);
        TrainerRequestDTO requestDTO = new TrainerRequestDTO();
        BindingResult bindingResult = new BeanPropertyBindingResult(requestDTO, "requestDTO");
        bindingResult.rejectValue("active", "Error message");

        when(mockRequest.getAttribute("txID")).thenReturn(TX_ID);

        ValidationException thrownException = Assertions.assertThrows(ValidationException.class,
                () -> trainerController.handleChangeActivationStatus(
                        1, requestDTO, bindingResult, mockRequest));

        assertThat(thrownException.getErrors()).contains("active").contains("null");
        assertThat(thrownException.getMessage()).isEqualTo(TX_ID);

        verifyNoMoreInteractions(trainerService);
    }

    @Test
    void testGetTrainingsWithFiltering_ReturnsOkEntity() {
        HttpServletRequest mockRequest = mock(HttpServletRequest.class);
        TrainingGetListRequestDTO requestDTO = new TrainingGetListRequestDTO();
        List<TrainingResponseDTO> expectedList = new ArrayList<>();

        when(mockRequest.getAttribute("txID")).thenReturn(TX_ID);
        when(trainingService.getTrainerTrainingsWithFiltering(anyLong(), any(TrainingGetListRequestDTO.class)))
                .thenReturn(expectedList);

        ResponseEntity<?> response = trainerController.handleGetTrainingsWithFiltering(
                1, requestDTO, mockRequest);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(expectedList);

        verify(trainingService).getTrainerTrainingsWithFiltering(anyLong(), any(TrainingGetListRequestDTO.class));
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

    private TrainerResponseDTO getTestTrainerResponseDTO(Trainer trainer) {
        ModelMapper oneTimeMapper = new ModelMapper();
        return oneTimeMapper.map(trainer, TrainerResponseDTO.class);
    }
}
