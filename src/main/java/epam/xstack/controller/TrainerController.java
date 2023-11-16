package epam.xstack.controller;

import epam.xstack.dto.auth.AuthDTO;
import epam.xstack.dto.trainer.TrainerRequestDTO;
import epam.xstack.dto.trainer.TrainerResponseDTO;
import epam.xstack.dto.trainer.validationgroup.TrainerActivateGroup;
import epam.xstack.dto.trainer.validationgroup.TrainerCreateGroup;
import epam.xstack.dto.trainer.validationgroup.TrainerUpdateGroup;
import epam.xstack.dto.training.TrainingGetListRequestDTO;
import epam.xstack.dto.training.TrainingResponseDTO;
import epam.xstack.exception.ValidationException;
import epam.xstack.service.TrainerService;
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
import java.net.URI;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping(value = "/api/v1/trainers", consumes = {"application/JSON"}, produces = {"application/JSON"})
public final class TrainerController {
    private final TrainerService trainerService;
    private final TrainingService trainingService;
    private static final Logger LOGGER = LoggerFactory.getLogger(TrainerController.class);
    private static final String LOG_MESSAGE = "TX ID: {} — {}";

    @Autowired
    public TrainerController(TrainerService trainerService, TrainingService trainingService) {
        this.trainerService = trainerService;
        this.trainingService = trainingService;
    }

    @PostMapping
    @Operation(summary = "Create new trainer",
        responses = {
            @ApiResponse(responseCode = "201", description = "Trainer created successfully"),
            @ApiResponse(responseCode = "422", description = "Bad input, check body for error messages")})
    public ResponseEntity<?> handleCreateTrainer(
                                        @RequestBody @Validated(TrainerCreateGroup.class) TrainerRequestDTO trainerDTO,
                                        BindingResult bindingResult,
                                        UriComponentsBuilder uriComponentsBuilder,
                                        HttpServletRequest httpServletRequest) {
        String txID = (String) httpServletRequest.getAttribute("txID");
        validatePayload(txID, bindingResult);

        AuthDTO responseDTO = trainerService.createTrainer(txID, trainerDTO);
        URI location = uriComponentsBuilder
                .path("/api/v1/trainers/{trainerId}")
                .build(responseDTO.getId());

        LOGGER.info(LOG_MESSAGE, txID, HttpStatus.OK);
        return ResponseEntity.created(location).body(responseDTO);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get trainer",
        responses = {
            @ApiResponse(responseCode = "200", description = "Trainer found"),
            @ApiResponse(responseCode = "401", description = "Bad credentials"),
            @ApiResponse(responseCode = "403", description = "Access denied (wrong ID?)"),
            @ApiResponse(responseCode = "404", description = "Trainer not found"),
            @ApiResponse(responseCode = "422", description = "Username or password is null")})
    public ResponseEntity<?> handleGetTrainer(@PathVariable("id") long id, HttpServletRequest httpServletRequest) {
        String txID = (String) httpServletRequest.getAttribute("txID");

        Optional<TrainerResponseDTO> trainerResponseDTOOpt = trainerService.findById(id);

        LOGGER.info(getLogMessage(txID, trainerResponseDTOOpt));
        return ResponseEntity.of(trainerResponseDTOOpt);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update trainer",
        responses = {
            @ApiResponse(responseCode = "200", description = "Trainer updated successfully"),
            @ApiResponse(responseCode = "401", description = "Bad credentials"),
            @ApiResponse(responseCode = "403", description = "Access denied (wrong ID?)"),
            @ApiResponse(responseCode = "404", description = "Trainer not found"),
            @ApiResponse(responseCode = "422", description = "Bad input, check body for error messages")})
    public ResponseEntity<?> handleUpdateTrainer(@PathVariable("id") long id,
                                         @RequestBody @Validated(TrainerUpdateGroup.class) TrainerRequestDTO trainerDTO,
                                         BindingResult bindingResult,
                                         HttpServletRequest httpServletRequest) {
        String txID = (String) httpServletRequest.getAttribute("txID");
        validatePayload(txID, bindingResult);

        Optional<TrainerResponseDTO> updatedTrainerResponseDTOOpt = trainerService.update(txID, id, trainerDTO);

        LOGGER.info(getLogMessage(txID, updatedTrainerResponseDTOOpt));
        return ResponseEntity.of(updatedTrainerResponseDTOOpt);
    }

    @PatchMapping("/{id}/activate")
    @Operation(summary = "Activate and deactivate trainer",
        responses = {
            @ApiResponse(responseCode = "200", description = "Changed activation status successfully"),
            @ApiResponse(responseCode = "401", description = "Bad credentials"),
            @ApiResponse(responseCode = "403", description = "Access denied (wrong ID?)"),
            @ApiResponse(responseCode = "404", description = "Trainer not found"),
            @ApiResponse(responseCode = "422", description = "Bad input, check body for error messages")})
    public ResponseEntity<?> handleChangeActivationStatus(@PathVariable("id") long id,
                                      @RequestBody @Validated(TrainerActivateGroup.class) TrainerRequestDTO trainerDTO,
                                      BindingResult bindingResult,
                                      HttpServletRequest httpServletRequest) {
        String txID = (String) httpServletRequest.getAttribute("txID");
        validatePayload(txID, bindingResult);

        trainerService.changeActivationStatus(txID, id, trainerDTO.getActive());

        LOGGER.info(LOG_MESSAGE, txID, HttpStatus.OK);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{id}/trainings")
    @Operation(summary = "Get all trainings for trainer with filtering",
        responses = {
            @ApiResponse(responseCode = "200", description = "List of trainings retrieved"),
            @ApiResponse(responseCode = "401", description = "Bad credentials"),
            @ApiResponse(responseCode = "403", description = "Access denied (wrong ID?)"),
            @ApiResponse(responseCode = "404", description = "Trainer not found"),
            @ApiResponse(responseCode = "422", description = "Username or password is null")})
    public ResponseEntity<?> handleGetTrainingsWithFiltering(@PathVariable("id") long id,
                                                     @RequestBody TrainingGetListRequestDTO requestDTO,
                                                     HttpServletRequest httpServletRequest) {
        String txID = (String) httpServletRequest.getAttribute("txID");

        List<TrainingResponseDTO> responseDTO = trainingService.getTrainerTrainingsWithFiltering(id, requestDTO);

        LOGGER.info(LOG_MESSAGE, txID, HttpStatus.OK);
        return ResponseEntity.ok().body(responseDTO);
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
