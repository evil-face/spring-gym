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
import epam.xstack.model.Trainer;
import epam.xstack.model.Training;
import epam.xstack.model.TrainingType;
import epam.xstack.service.TrainerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.modelmapper.ModelMapper;
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
import javax.validation.Valid;
import java.net.URI;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping(value = "/api/v1/trainers", consumes = {"application/JSON"}, produces = {"application/JSON"})
public final class TrainerController {
    private final TrainerService trainerService;
    private final ModelMapper modelMapper;
    private static final Logger LOGGER = LoggerFactory.getLogger(TrainerController.class);
    private static final String LOG_MESSAGE = "TX ID: {} — {}";

    @Autowired
    public TrainerController(TrainerService trainerService, ModelMapper modelMapper) {
        this.trainerService = trainerService;
        this.modelMapper = modelMapper;
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

        Trainer newTrainer = modelMapper.map(trainerDTO, Trainer.class);
        newTrainer.setSpecialization(new TrainingType(trainerDTO.getSpecialization(), ""));
        Trainer createdTrainer = trainerService.createTrainer(txID, newTrainer);

        AuthDTO responseDTO = modelMapper.map(createdTrainer, AuthDTO.class);
        URI location = uriComponentsBuilder
                .path("/api/v1/trainers/{trainerId}")
                .build(createdTrainer.getId());

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
    public ResponseEntity<?> handleGetTrainer(@PathVariable("id") long id,
                                              @RequestBody @Valid AuthDTO authDTO,
                                              BindingResult bindingResult,
                                              HttpServletRequest httpServletRequest) {
        String txID = (String) httpServletRequest.getAttribute("txID");
        validatePayload(txID, bindingResult);

        Optional<Trainer> trainerOpt = trainerService.findById(txID, id, authDTO.getUsername(), authDTO.getPassword());
        Optional<TrainerResponseDTO> trainerResponseDTOOpt = trainerOpt.map(
                value -> modelMapper.map(value, TrainerResponseDTO.class));

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

        Trainer trainerToUpdate = modelMapper.map(trainerDTO, Trainer.class);
        trainerToUpdate.setId(id);
        trainerToUpdate.setSpecialization(new TrainingType(trainerDTO.getSpecialization(), ""));

        Optional<Trainer> updatedTrainerOpt = trainerService.update(txID, trainerToUpdate);

        Optional<TrainerResponseDTO> updatedTrainerResponseDTOOpt = updatedTrainerOpt.map(
                value -> modelMapper.map(value, TrainerResponseDTO.class));

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

        trainerService.changeActivationStatus(txID, id, trainerDTO.getIsActive(),
                trainerDTO.getUsername(), trainerDTO.getPassword());

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
                                                     @RequestBody @Valid TrainingGetListRequestDTO requestDTO,
                                                     BindingResult bindingResult,
                                                     HttpServletRequest httpServletRequest) {
        String txID = (String) httpServletRequest.getAttribute("txID");
        validatePayload(txID, bindingResult);

        List<Training> trainings = trainerService.getTrainingsWithFiltering(txID, id, requestDTO.getUsername(),
                requestDTO.getPassword(), requestDTO);

        List<TrainingResponseDTO> responseDTO = trainings.stream()
                .map(e -> modelMapper.map(e, TrainingResponseDTO.class))
                .toList();

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
