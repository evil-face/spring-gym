package epam.xstack.controller;

import epam.xstack.dto.trainee.req.TraineeActivationRequestDTO;
import epam.xstack.dto.trainee.req.TraineeCreateRequestDTO;
import epam.xstack.dto.trainee.resp.TraineeGetResponseDTO;
import epam.xstack.dto.auth.AuthDTO;
import epam.xstack.dto.trainee.req.TraineeUpdateRequestDTO;
import epam.xstack.dto.trainee.resp.TraineeUpdateResponseDTO;
import epam.xstack.dto.trainee.req.TraineeUpdateTrainerListRequestDTO;
import epam.xstack.dto.trainee.resp.UnassignedTrainersResponseDTO;
import epam.xstack.dto.trainee.req.TraineeGetTrainingListRequestDTO;
import epam.xstack.dto.trainee.req.TraineeGetTrainingListResponseDTO;
import epam.xstack.model.Trainee;
import epam.xstack.model.Trainer;
import epam.xstack.model.Training;
import epam.xstack.service.TraineeService;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.helpers.FormattingTuple;
import org.slf4j.helpers.MessageFormatter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
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
import java.util.Optional;

@RestController
@RequestMapping(value = "/api/v1/trainees", consumes = {"application/JSON"}, produces = {"application/JSON"})
public final class TraineeController {
    private final TraineeService traineeService;
    private final ModelMapper modelMapper;
    private static final Logger LOGGER = LoggerFactory.getLogger(TraineeController.class);
    private static final String LOG_MESSAGE_WITH_ERRORS = "TX ID: {} — {} — {}";
    private static final String LOG_MESSAGE = "TX ID: {} — {}";

    @Autowired
    public TraineeController(TraineeService traineeService, ModelMapper modelMapper) {
        this.traineeService = traineeService;
        this.modelMapper = modelMapper;
    }

    @PostMapping
    public ResponseEntity<?> handleCreateTrainee(@RequestBody @Valid TraineeCreateRequestDTO traineeDTO,
                                                 BindingResult bindingResult,
                                                 UriComponentsBuilder uriComponentsBuilder,
                                                 HttpServletRequest httpServletRequest) {
        String txID = (String) httpServletRequest.getAttribute("txID");

        if (bindingResult.hasErrors()) {
            Map<String, String> errors = buildErrorMessage(bindingResult);
            LOGGER.warn(LOG_MESSAGE_WITH_ERRORS, txID, HttpStatus.UNPROCESSABLE_ENTITY, errors);

            return ResponseEntity.unprocessableEntity().body(errors);
        }

        Trainee newTrainee = modelMapper.map(traineeDTO, Trainee.class);
        Trainee createdTrainee = traineeService.createTrainee(txID, newTrainee);

        AuthDTO response = modelMapper.map(createdTrainee, AuthDTO.class);
        URI location = uriComponentsBuilder
                .path("/api/v1/trainees/{traineeId}")
                .build(createdTrainee.getId());

        LOGGER.info(LOG_MESSAGE, txID, HttpStatus.OK);
        return ResponseEntity.created(location).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> handleGetTrainee(@PathVariable("id") long id,
                                              @RequestBody @Valid AuthDTO authDTO,
                                              BindingResult bindingResult,
                                              HttpServletRequest httpServletRequest) {
        String txID = (String) httpServletRequest.getAttribute("txID");

        if (bindingResult.hasErrors()) {
            Map<String, String> errors = buildErrorMessage(bindingResult);
            LOGGER.warn(LOG_MESSAGE_WITH_ERRORS, txID, HttpStatus.UNPROCESSABLE_ENTITY, errors);

            return ResponseEntity.unprocessableEntity().body(errors);
        }

        Optional<Trainee> traineeOpt = traineeService.findById(txID, id, authDTO.getUsername(), authDTO.getPassword());
        Optional<TraineeGetResponseDTO> trainee = traineeOpt.map(
                value -> modelMapper.map(value, TraineeGetResponseDTO.class));

        FormattingTuple logMessage = trainee.isPresent()
                ? MessageFormatter.format(LOG_MESSAGE, txID, HttpStatus.OK)
                : MessageFormatter.format(LOG_MESSAGE, txID, HttpStatus.NOT_FOUND);
        LOGGER.info(logMessage.getMessage());

        return ResponseEntity.of(trainee);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> handleUpdateTrainee(@PathVariable("id") long id,
                                                 @RequestBody @Valid TraineeUpdateRequestDTO traineeUpdateRequestDTO,
                                                 BindingResult bindingResult,
                                                 HttpServletRequest httpServletRequest) {
        String txID = (String) httpServletRequest.getAttribute("txID");

        if (bindingResult.hasErrors()) {
            Map<String, String> errors = buildErrorMessage(bindingResult);
            LOGGER.warn(LOG_MESSAGE_WITH_ERRORS, txID, HttpStatus.UNPROCESSABLE_ENTITY, errors);

            return ResponseEntity.unprocessableEntity().body(errors);
        }

        Trainee traineeToUpdate = modelMapper.map(traineeUpdateRequestDTO, Trainee.class);
        traineeToUpdate.setId(id);

        Optional<Trainee> updatedTraineeOpt = traineeService.update(txID, traineeToUpdate,
                traineeUpdateRequestDTO.getUsername(), traineeUpdateRequestDTO.getPassword());

        Optional<TraineeUpdateResponseDTO> updatedTraineeResponseDTO = updatedTraineeOpt.map(
                value -> modelMapper.map(value, TraineeUpdateResponseDTO.class));

        FormattingTuple logMessage = updatedTraineeResponseDTO.isPresent()
                ? MessageFormatter.format(LOG_MESSAGE, txID, HttpStatus.OK)
                : MessageFormatter.format(LOG_MESSAGE, txID, HttpStatus.NOT_FOUND);
        LOGGER.info(logMessage.getMessage());

        return ResponseEntity.of(updatedTraineeResponseDTO);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> handleDeleteTrainee(@PathVariable("id") long id,
                                                 @RequestBody @Valid AuthDTO authDTO,
                                                 BindingResult bindingResult,
                                                 HttpServletRequest httpServletRequest) {
        String txID = (String) httpServletRequest.getAttribute("txID");

        if (bindingResult.hasErrors()) {
            Map<String, String> errors = buildErrorMessage(bindingResult);
            LOGGER.warn(LOG_MESSAGE_WITH_ERRORS, txID, HttpStatus.UNPROCESSABLE_ENTITY, errors);

            return ResponseEntity.unprocessableEntity().body(errors);
        }

        Trainee traineeToDelete = modelMapper.map(authDTO, Trainee.class);
        traineeToDelete.setId(id);

        traineeService.delete(txID, traineeToDelete);

        LOGGER.info(LOG_MESSAGE, txID, HttpStatus.NO_CONTENT);

        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/activate")
    public ResponseEntity<?> handleChangeActivationStatus(@PathVariable("id") long id,
                                                          @RequestBody @Valid TraineeActivationRequestDTO requestDTO,
                                                          BindingResult bindingResult,
                                                          HttpServletRequest httpServletRequest) {
        String txID = (String) httpServletRequest.getAttribute("txID");

        if (bindingResult.hasErrors()) {
            Map<String, String> errors = buildErrorMessage(bindingResult);
            LOGGER.warn(LOG_MESSAGE_WITH_ERRORS, txID, HttpStatus.UNPROCESSABLE_ENTITY, errors);

            return ResponseEntity.unprocessableEntity().body(errors);
        }

        traineeService.changeActivationStatus(txID, id, requestDTO.getIsActive(),
                requestDTO.getUsername(), requestDTO.getPassword());

        LOGGER.info(LOG_MESSAGE, txID, HttpStatus.OK);

        return ResponseEntity.ok().build();
    }

    @GetMapping("/{id}/unassigned-trainers")
    public ResponseEntity<?> handleGetUnassignedTrainersForTrainee(@PathVariable("id") long id,
                                                                   @RequestBody @Valid AuthDTO authDTO,
                                                                   BindingResult bindingResult,
                                                                   HttpServletRequest httpServletRequest) {
        String txID = (String) httpServletRequest.getAttribute("txID");

        if (bindingResult.hasErrors()) {
            Map<String, String> errors = buildErrorMessage(bindingResult);
            LOGGER.warn(LOG_MESSAGE_WITH_ERRORS, txID, HttpStatus.UNPROCESSABLE_ENTITY, errors);

            return ResponseEntity.unprocessableEntity().body(errors);
        }

        List<Trainer> trainers = traineeService.getPotentialTrainersForTrainee(
                txID, id, authDTO.getUsername(), authDTO.getPassword());

        List<UnassignedTrainersResponseDTO> response = trainers.stream()
                .map(e -> modelMapper.map(e, UnassignedTrainersResponseDTO.class))
                .toList();

        LOGGER.info(LOG_MESSAGE, txID, HttpStatus.OK);

        return ResponseEntity.ok().body(response);
    }

    @GetMapping("/{id}/trainings")
    public ResponseEntity<?> handleGetTrainingsWithFiltering(@PathVariable("id") long id,
                                                     @RequestBody @Valid TraineeGetTrainingListRequestDTO requestDTO,
                                                     BindingResult bindingResult,
                                                     HttpServletRequest httpServletRequest) {
        String txID = (String) httpServletRequest.getAttribute("txID");

        List<Training> trainings = traineeService.getTrainingsWithFiltering(txID, id, requestDTO.getUsername(),
                requestDTO.getPassword(), requestDTO);

        List<TraineeGetTrainingListResponseDTO> response = trainings.stream()
                .map(e -> modelMapper.map(e, TraineeGetTrainingListResponseDTO.class))
                .toList();

        LOGGER.info(LOG_MESSAGE, txID, HttpStatus.OK);

        return ResponseEntity.ok().body(response);
    }

    @PutMapping("/{id}/trainers")
    public ResponseEntity<?> handleUpdateTraineeTrainerList(@PathVariable("id") long id,
                                                 @RequestBody @Valid TraineeUpdateTrainerListRequestDTO requestDTO,
                                                 BindingResult bindingResult,
                                                 HttpServletRequest httpServletRequest) {
        String txID = (String) httpServletRequest.getAttribute("txID");

        if (bindingResult.hasErrors()) {
            Map<String, String> errors = buildErrorMessage(bindingResult);
            LOGGER.warn(LOG_MESSAGE_WITH_ERRORS, txID, HttpStatus.UNPROCESSABLE_ENTITY, errors);

            return ResponseEntity.unprocessableEntity().body(errors);
        }

        List<Trainer> updatedTrainersList = traineeService.updateTrainerList(txID, id, requestDTO);

        List<UnassignedTrainersResponseDTO> responseDTO = updatedTrainersList.stream()
                .map(trainer -> modelMapper.map(trainer, UnassignedTrainersResponseDTO.class))
                .toList();

        LOGGER.info(LOG_MESSAGE, txID, HttpStatus.OK);

        return ResponseEntity.ok(responseDTO);
    }

    private Map<String, String> buildErrorMessage(BindingResult bindingResult) {
        Map<String, String> errors = new HashMap<>();

        bindingResult.getFieldErrors().forEach(error ->
                errors.put(error.getField(), error.getDefaultMessage()));

        return errors;
    }
}
