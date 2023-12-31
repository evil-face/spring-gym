package epam.xstack.controller;

import epam.xstack.dto.training.TrainingCreateRequestDTO;
import epam.xstack.exception.ValidationException;
import epam.xstack.model.Training;
import epam.xstack.model.TrainingType;
import epam.xstack.service.TrainingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.net.URI;
import java.util.List;

@RestController
@RequestMapping(value = "api/v1/trainings", produces = {"application/JSON"})
public final class TrainingController {
    private final TrainingService trainingService;
    private static final Logger LOGGER = LoggerFactory.getLogger(TrainingController.class);
    private static final String LOG_MESSAGE = "TX ID: {} — {}";

    @Autowired
    public TrainingController(TrainingService trainingService) {
        this.trainingService = trainingService;
    }

    @GetMapping("/types")
    @Operation(summary = "Get all existing training types",
        responses = {
            @ApiResponse(responseCode = "200", description = "Training types list retrieved successfully")})
    public List<TrainingType> handleGetAllTrainingTypes(HttpServletRequest httpServletRequest) {
        String txID = (String) httpServletRequest.getAttribute("txID");

        List<TrainingType> response = trainingService.findAllTrainingTypes();

        LOGGER.info(LOG_MESSAGE, txID, HttpStatus.OK);
        return response;
    }

    @PostMapping(consumes = {"application/JSON"})
    @Operation(summary = "Create new training",
        responses = {
            @ApiResponse(responseCode = "201", description = "Training created successfully"),
            @ApiResponse(responseCode = "422", description = "Bad input, check body for error messages")})
    public ResponseEntity<?> handleCreateTraining(@RequestBody @Valid TrainingCreateRequestDTO trainingDTO,
                                                  BindingResult bindingResult,
                                                  UriComponentsBuilder uriComponentsBuilder,
                                                  HttpServletRequest httpServletRequest) {
        String txID = (String) httpServletRequest.getAttribute("txID");
        validatePayload(txID, bindingResult);

        Training createdTraining = trainingService.createTraining(txID, trainingDTO);

        URI location = uriComponentsBuilder
                .path("/api/v1/trainings/{trainingId}")
                .build(createdTraining.getId());

        // Task required 200 OK
        LOGGER.info(LOG_MESSAGE, txID, HttpStatus.CREATED);
        return ResponseEntity.created(location).build();
    }

    private void validatePayload(String txID, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            List<String> errors = bindingResult.getFieldErrors().stream()
                    .map(error -> error.getField() + " - " + error.getDefaultMessage()).toList();

            throw new ValidationException(txID, errors.toString());
        }
    }
}
