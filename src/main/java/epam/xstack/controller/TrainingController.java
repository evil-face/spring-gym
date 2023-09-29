package epam.xstack.controller;

import epam.xstack.dto.training.TrainingCreateRequestDTO;
import epam.xstack.model.Training;
import epam.xstack.model.TrainingType;
import epam.xstack.service.TrainingService;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping(value = "api/v1/trainings")
public final class TrainingController {
    private final TrainingService trainingService;
    private static final Logger LOGGER = LoggerFactory.getLogger(TrainingController.class);
    private static final String LOG_MESSAGE_WITH_ERRORS = "TX ID: {} — {} — {}";
    private static final String LOG_MESSAGE = "TX ID: {} — {}";

    @Autowired
    public TrainingController(TrainingService trainingService) {
        this.trainingService = trainingService;
    }

    @GetMapping("/types")
    public ResponseEntity<List<TrainingType>> handleGetAll(HttpServletRequest httpServletRequest) {
        String txID = (String) httpServletRequest.getAttribute("txID");

        List<TrainingType> response = trainingService.findAllTrainingTypes(txID);

        LOGGER.info(LOG_MESSAGE, txID, HttpStatus.OK);
        return ResponseEntity.ok().body(response);
    }

    @PostMapping
    public ResponseEntity<?> handleCreateTraining(@RequestBody @Valid TrainingCreateRequestDTO trainingDTO,
                                                  BindingResult bindingResult,
                                                  UriComponentsBuilder uriComponentsBuilder,
                                                  HttpServletRequest httpServletRequest) {
        String txID = (String) httpServletRequest.getAttribute("txID");

        if (bindingResult.hasErrors()) {
            Map<String, String> errors = buildErrorMessage(bindingResult);
            LOGGER.warn(LOG_MESSAGE_WITH_ERRORS, txID, HttpStatus.UNPROCESSABLE_ENTITY, errors);

            return ResponseEntity.unprocessableEntity().body(errors);
        }

        Training createdTraining = trainingService.createTraining(txID, trainingDTO);

        URI location = uriComponentsBuilder
                .path("/api/v1/trainings/{trainingId}")
                .build(createdTraining.getId());


        // Task required 200 OK
        LOGGER.info(LOG_MESSAGE, txID, HttpStatus.CREATED);
        return ResponseEntity.created(location).build();
    }

    private Map<String, String> buildErrorMessage(BindingResult bindingResult) {
        Map<String, String> errors = new HashMap<>();

        bindingResult.getFieldErrors().forEach(error ->
                errors.put(error.getField(), error.getDefaultMessage()));

        return errors;
    }
}
