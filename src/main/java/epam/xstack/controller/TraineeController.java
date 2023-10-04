package epam.xstack.controller;

import epam.xstack.dto.trainee.TraineeResponseDTO;
import epam.xstack.dto.trainee.validationgroup.TraineeActivateGroup;
import epam.xstack.dto.trainee.validationgroup.TraineeCreateGroup;
import epam.xstack.dto.trainee.TraineeRequestDTO;
import epam.xstack.dto.trainee.validationgroup.TraineeUpdateGroup;
import epam.xstack.dto.trainee.validationgroup.TraineeUpdateTrainerListGroup;
import epam.xstack.dto.auth.AuthDTO;
import epam.xstack.dto.trainer.TrainerResponseDTO;
import epam.xstack.dto.training.TrainingGetListRequestDTO;
import epam.xstack.dto.training.TrainingResponseDTO;
import epam.xstack.model.Trainee;
import epam.xstack.model.Trainer;
import epam.xstack.model.Training;
import epam.xstack.service.TraineeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.helpers.FormattingTuple;
import org.slf4j.helpers.MessageFormatter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
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
import java.util.Set;
import java.util.stream.Collectors;

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
    @Operation(summary = "Create new trainee",
        responses = {
            @ApiResponse(responseCode = "201", description = "Trainee created successfully"),
            @ApiResponse(responseCode = "422", description = "Bad input, check body for error messages")})
    public ResponseEntity<?> handleCreateTrainee(
            @RequestBody @Validated(TraineeCreateGroup.class) TraineeRequestDTO traineeDTO,
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

        AuthDTO responseDTO = modelMapper.map(createdTrainee, AuthDTO.class);
        URI location = uriComponentsBuilder
                .path("/api/v1/trainees/{traineeId}")
                .build(createdTrainee.getId());

        LOGGER.info(LOG_MESSAGE, txID, HttpStatus.OK);
        return ResponseEntity.created(location).body(responseDTO);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get trainee",
        responses = {
            @ApiResponse(responseCode = "200", description = "Trainee found"),
            @ApiResponse(responseCode = "401", description = "Bad credentials"),
            @ApiResponse(responseCode = "403", description = "Access denied (wrong ID?)"),
            @ApiResponse(responseCode = "404", description = "Trainee not found"),
            @ApiResponse(responseCode = "422", description = "Username or password is null")})
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
        Optional<TraineeResponseDTO> traineeResponseDTO = traineeOpt.map(
                value -> modelMapper.map(value, TraineeResponseDTO.class));

        FormattingTuple logMessage = traineeResponseDTO.isPresent()
                ? MessageFormatter.format(LOG_MESSAGE, txID, HttpStatus.OK)
                : MessageFormatter.format(LOG_MESSAGE, txID, HttpStatus.NOT_FOUND);
        LOGGER.info(logMessage.getMessage());

        return ResponseEntity.of(traineeResponseDTO);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update trainee",
        responses = {
            @ApiResponse(responseCode = "200", description = "Trainee updated successfully"),
            @ApiResponse(responseCode = "401", description = "Bad credentials"),
            @ApiResponse(responseCode = "403", description = "Access denied (wrong ID?)"),
            @ApiResponse(responseCode = "404", description = "Trainee not found"),
            @ApiResponse(responseCode = "422", description = "Bad input, check body for error messages")})
    public ResponseEntity<?> handleUpdateTrainee(@PathVariable("id") long id,
                                        @RequestBody @Validated(TraineeUpdateGroup.class) TraineeRequestDTO traineeDTO,
                                        BindingResult bindingResult,
                                        HttpServletRequest httpServletRequest) {
        String txID = (String) httpServletRequest.getAttribute("txID");

        if (bindingResult.hasErrors()) {
            Map<String, String> errors = buildErrorMessage(bindingResult);
            LOGGER.warn(LOG_MESSAGE_WITH_ERRORS, txID, HttpStatus.UNPROCESSABLE_ENTITY, errors);

            return ResponseEntity.unprocessableEntity().body(errors);
        }

        Trainee traineeToUpdate = modelMapper.map(traineeDTO, Trainee.class);
        traineeToUpdate.setId(id);

        Optional<Trainee> updatedTraineeOpt = traineeService.update(txID, traineeToUpdate,
                traineeDTO.getUsername(), traineeDTO.getPassword());

        Optional<TraineeResponseDTO> updatedTraineeResponseDTO = updatedTraineeOpt.map(
                value -> modelMapper.map(value, TraineeResponseDTO.class));

        FormattingTuple logMessage = updatedTraineeResponseDTO.isPresent()
                ? MessageFormatter.format(LOG_MESSAGE, txID, HttpStatus.OK)
                : MessageFormatter.format(LOG_MESSAGE, txID, HttpStatus.NOT_FOUND);
        LOGGER.info(logMessage.getMessage());

        return ResponseEntity.of(updatedTraineeResponseDTO);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete trainee",
        responses = {
            @ApiResponse(responseCode = "204", description = "Trainee deleted successfully"),
            @ApiResponse(responseCode = "401", description = "Bad credentials"),
            @ApiResponse(responseCode = "403", description = "Access denied (wrong ID?)"),
            @ApiResponse(responseCode = "404", description = "Trainee not found"),
            @ApiResponse(responseCode = "422", description = "Username or password is null")})
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
    @Operation(summary = "Activate and deactivate trainee",
        responses = {
            @ApiResponse(responseCode = "200", description = "Changed activation status successfully"),
            @ApiResponse(responseCode = "401", description = "Bad credentials"),
            @ApiResponse(responseCode = "403", description = "Access denied (wrong ID?)"),
            @ApiResponse(responseCode = "404", description = "Trainee not found"),
            @ApiResponse(responseCode = "422", description = "Bad input, check body for error messages")})
    public ResponseEntity<?> handleChangeActivationStatus(@PathVariable("id") long id,
                                      @RequestBody @Validated(TraineeActivateGroup.class) TraineeRequestDTO traineeDTO,
                                      BindingResult bindingResult,
                                      HttpServletRequest httpServletRequest) {
        String txID = (String) httpServletRequest.getAttribute("txID");

        if (bindingResult.hasErrors()) {
            Map<String, String> errors = buildErrorMessage(bindingResult);
            LOGGER.warn(LOG_MESSAGE_WITH_ERRORS, txID, HttpStatus.UNPROCESSABLE_ENTITY, errors);

            return ResponseEntity.unprocessableEntity().body(errors);
        }

        traineeService.changeActivationStatus(txID, id, traineeDTO.getIsActive(),
                traineeDTO.getUsername(), traineeDTO.getPassword());

        LOGGER.info(LOG_MESSAGE, txID, HttpStatus.OK);

        return ResponseEntity.ok().build();
    }

    @GetMapping("/{id}/unassigned-trainers")
    @Operation(summary = "Get unassigned trainers for trainee",
        responses = {
            @ApiResponse(responseCode = "200", description = "List of potential trainers retrieved"),
            @ApiResponse(responseCode = "401", description = "Bad credentials"),
            @ApiResponse(responseCode = "403", description = "Access denied (wrong ID?)"),
            @ApiResponse(responseCode = "404", description = "Trainee not found"),
            @ApiResponse(responseCode = "422", description = "Username or password is null")})
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

        List<TrainerResponseDTO> responseDTO = trainers.stream()
                .map(e -> modelMapper.map(e, TrainerResponseDTO.class))
                .toList();

        LOGGER.info(LOG_MESSAGE, txID, HttpStatus.OK);

        return ResponseEntity.ok().body(responseDTO);
    }

    @GetMapping("/{id}/trainings")
    @Operation(summary = "Get all trainings for trainee with filtering",
        responses = {
            @ApiResponse(responseCode = "200", description = "List of trainings retrieved"),
            @ApiResponse(responseCode = "401", description = "Bad credentials"),
            @ApiResponse(responseCode = "403", description = "Access denied (wrong ID?)"),
            @ApiResponse(responseCode = "404", description = "Trainee not found"),
            @ApiResponse(responseCode = "422", description = "Username or password is null")})
    public ResponseEntity<?> handleGetTrainingsWithFiltering(@PathVariable("id") long id,
                                                             @RequestBody @Valid TrainingGetListRequestDTO requestDTO,
                                                             BindingResult bindingResult,
                                                             HttpServletRequest httpServletRequest) {
        String txID = (String) httpServletRequest.getAttribute("txID");

        if (bindingResult.hasErrors()) {
            Map<String, String> errors = buildErrorMessage(bindingResult);
            LOGGER.warn(LOG_MESSAGE_WITH_ERRORS, txID, HttpStatus.UNPROCESSABLE_ENTITY, errors);

            return ResponseEntity.unprocessableEntity().body(errors);
        }

        List<Training> trainings = traineeService.getTrainingsWithFiltering(txID, id, requestDTO.getUsername(),
                requestDTO.getPassword(), requestDTO);

        List<TrainingResponseDTO> responseDTO = trainings.stream()
                .map(e -> modelMapper.map(e, TrainingResponseDTO.class))
                .toList();

        LOGGER.info(LOG_MESSAGE, txID, HttpStatus.OK);

        return ResponseEntity.ok().body(responseDTO);
    }

    @PutMapping("/{id}/trainers")
    @Operation(summary = "Update trainee's list of assigned trainers",
        responses = {
            @ApiResponse(responseCode = "200", description = "List of trainers successfully updated"),
            @ApiResponse(responseCode = "404", description = "Trainee not found"),
            @ApiResponse(responseCode = "422", description = "Bad input, check body for error messages")})
    public ResponseEntity<?> handleUpdateTraineeTrainerList(@PathVariable("id") long id,
                             @RequestBody @Validated(TraineeUpdateTrainerListGroup.class) TraineeRequestDTO traineeDTO,
                             BindingResult bindingResult,
                             HttpServletRequest httpServletRequest) {
        String txID = (String) httpServletRequest.getAttribute("txID");

        if (bindingResult.hasErrors()) {
            Map<String, String> errors = buildErrorMessage(bindingResult);
            LOGGER.warn(LOG_MESSAGE_WITH_ERRORS, txID, HttpStatus.UNPROCESSABLE_ENTITY, errors);

            return ResponseEntity.unprocessableEntity().body(errors);
        }

        List<Trainer> updatedTrainersList = traineeService.updateTrainerList(txID, id, traineeDTO);

        //todo refactor mapping to service layer
        Set<TrainerResponseDTO> responseDTO = updatedTrainersList.stream()
                .map(trainer -> modelMapper.map(trainer, TrainerResponseDTO.class))
                .collect(Collectors.toSet());

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
