package epam.xstack.unit.controller;

import epam.xstack.controller.TrainingController;
import epam.xstack.dto.training.TrainingCreateRequestDTO;
import epam.xstack.exception.ValidationException;
import epam.xstack.model.Trainee;
import epam.xstack.model.Trainer;
import epam.xstack.model.Training;
import epam.xstack.model.TrainingType;
import epam.xstack.service.TrainingService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.web.util.UriComponentsBuilder;

import javax.servlet.http.HttpServletRequest;

import java.net.URI;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class TrainingControllerTest {
    @InjectMocks
    TrainingController trainingController;

    @Mock
    TrainingService trainingService;

    private static final String TX_ID = "12345";

    @Test
    void testGetAll_ReturnsOkResponseEntity() {
        HttpServletRequest mockRequest = mock(HttpServletRequest.class);
        List<TrainingType> trainingTypeList = List.of(
                new TrainingType(1, "Lifting"),
                new TrainingType(2, "Cardio"),
                new TrainingType(1, "Crossfit")
        );

        when(mockRequest.getAttribute("txID")).thenReturn(TX_ID);
        when(trainingService.findAllTrainingTypes()).thenReturn(trainingTypeList);

        List<TrainingType> response = trainingController.handleGetAllTrainingTypes(mockRequest);

        assertThat(response).isEqualTo(trainingTypeList);
        verify(trainingService).findAllTrainingTypes();
    }

    @Test
    void testCreateTraining_ReturnsOkResponseEntity() {
        HttpServletRequest mockRequest = mock(HttpServletRequest.class);
        TrainingCreateRequestDTO requestDTO = new TrainingCreateRequestDTO();
        Training createdTraining = getTestTraining();
        BindingResult bindingResult = new BeanPropertyBindingResult(requestDTO, "requestDTO");

        when(mockRequest.getAttribute("txID")).thenReturn(TX_ID);
        when(trainingService.createTraining(TX_ID, requestDTO)).thenReturn(createdTraining);

        ResponseEntity<?> response = trainingController.handleCreateTraining(
                requestDTO, bindingResult, UriComponentsBuilder.fromUriString("http://localhost:8080"), mockRequest);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getHeaders().getLocation())
                .isEqualTo(URI.create("http://localhost:8080/api/v1/trainings/" + createdTraining.getId()));
        verify(trainingService).createTraining(anyString(), eq(requestDTO));
        verifyNoMoreInteractions(trainingService);
    }

    @Test
    void testCreateTraining_ThrowsValidationException() {
        HttpServletRequest mockRequest = mock(HttpServletRequest.class);
        TrainingCreateRequestDTO requestDTO = new TrainingCreateRequestDTO();
        BindingResult bindingResult = new BeanPropertyBindingResult(requestDTO, "requestDTO");
        bindingResult.rejectValue("trainingName", "Error message");

        when(mockRequest.getAttribute("txID")).thenReturn(TX_ID);

        ValidationException thrownException = Assertions.assertThrows(ValidationException.class,
                () -> trainingController.handleCreateTraining(requestDTO, bindingResult,
                        UriComponentsBuilder.fromUriString("http://localhost:8080"), mockRequest));

        assertThat(thrownException.getErrors()).contains("trainingName").contains("null");
        assertThat(thrownException.getMessage()).isEqualTo(TX_ID);

        verifyNoMoreInteractions(trainingService);
    }

    private Training getTestTraining() {
        Trainee trainee = new Trainee("trainee", "trainee", "trainee.trainee",
                "qwerty", true, LocalDate.now().minusYears(1), "traineeCity");
        Trainer trainer = new Trainer("trainer", "trainer", "trainer.trainer",
                "123", true, new TrainingType(1, "Lifting"));
        Training training = new Training(trainee, trainer, "test training", trainer.getSpecialization(),
                LocalDate.now(), 100);
        training.setId(1);

        return training;
    }
}
