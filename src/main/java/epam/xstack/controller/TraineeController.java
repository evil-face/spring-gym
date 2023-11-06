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
import epam.xstack.exception.ValidationException;
import epam.xstack.service.TraineeService;
import epam.xstack.service.TrainingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping(value = "/api/v1/trainees", produces = {"application/JSON"})
public final class TraineeController {
    private final TraineeService traineeService;
    private final TrainingService trainingService;
    private static final Logger LOGGER = LoggerFactory.getLogger(TraineeController.class);
    private static final String LOG_MESSAGE = "TX ID: {} — {}";

    @Autowired
    public TraineeController(TraineeService traineeService, TrainingService trainingService) {
        this.traineeService = traineeService;
        this.trainingService = trainingService;
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
        validatePayload(txID, bindingResult);

        AuthDTO responseDTO = traineeService.createTrainee(txID, traineeDTO);
        URI location = uriComponentsBuilder
                .path("/api/v1/trainees/{traineeId}")
                .build(responseDTO.getId());

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
    public ResponseEntity<?> handleGetTrainee(@PathVariable("id") long id, HttpServletRequest httpServletRequest) {
        String txID = (String) httpServletRequest.getAttribute("txID");

        Optional<TraineeResponseDTO> traineeResponseDTOOpt = traineeService.findById(id);

        LOGGER.info(getLogMessage(txID, traineeResponseDTOOpt));
        return ResponseEntity.of(traineeResponseDTOOpt);
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
        validatePayload(txID, bindingResult);

        Optional<TraineeResponseDTO> updatedTraineeResponseDTOOpt = traineeService.update(txID, id, traineeDTO);

        LOGGER.info(getLogMessage(txID, updatedTraineeResponseDTOOpt));
        return ResponseEntity.of(updatedTraineeResponseDTOOpt);
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
                                                 HttpServletRequest httpServletRequest) {
        String txID = (String) httpServletRequest.getAttribute("txID");

        traineeService.delete(txID, id);

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
        validatePayload(txID, bindingResult);

        traineeService.changeActivationStatus(txID, id, traineeDTO.getActive());

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
                                                                   HttpServletRequest httpServletRequest) {
        String txID = (String) httpServletRequest.getAttribute("txID");

        List<TrainerResponseDTO> responseDTO = traineeService.getPotentialTrainersForTrainee(txID, id);

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
        validatePayload(txID, bindingResult);

        List<TrainingResponseDTO> responseDTO = trainingService.getTraineeTrainingsWithFiltering(id, requestDTO);

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
        validatePayload(txID, bindingResult);

        List<TrainerResponseDTO> responseDTO = traineeService.updateTrainerList(txID, id, traineeDTO);

        LOGGER.info(LOG_MESSAGE, txID, HttpStatus.OK);
        return ResponseEntity.ok(responseDTO);
    }

    private void validatePayload(String txID, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            List<String> errors = bindingResult.getFieldErrors().stream()
                    .map(error -> error.getField() + " - " + error.getDefaultMessage()).toList();

            throw new ValidationException(txID, errors.toString());
        }
    }

    private String getLogMessage(String txID, Optional<?> optional) {
        return optional.isPresent()
                ? MessageFormatter.format(LOG_MESSAGE, txID, HttpStatus.OK).getMessage()
                : MessageFormatter.format(LOG_MESSAGE, txID, HttpStatus.NOT_FOUND).getMessage();
    }
}
