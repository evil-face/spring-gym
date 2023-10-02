package epam.xstack.controller;

import epam.xstack.dto.auth.AuthDTO;
import epam.xstack.dto.trainer.TrainerRequestDTO;
import epam.xstack.dto.trainer.TrainerResponseDTO;
import epam.xstack.dto.trainer.validationgroup.TrainerActivateGroup;
import epam.xstack.dto.trainer.validationgroup.TrainerCreateGroup;
import epam.xstack.dto.trainer.validationgroup.TrainerUpdateGroup;
import epam.xstack.dto.training.TrainingGetListRequestDTO;
import epam.xstack.dto.training.TrainingResponseDTO;
import epam.xstack.model.Trainer;
import epam.xstack.model.Training;
import epam.xstack.model.TrainingType;
import epam.xstack.service.TrainerService;
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
@RequestMapping(value = "/api/v1/trainers", consumes = {"application/JSON"}, produces = {"application/JSON"})
public final class TrainerController {
    private final TrainerService trainerService;
    private final ModelMapper modelMapper;
    private static final Logger LOGGER = LoggerFactory.getLogger(TrainerController.class);
    private static final String LOG_MESSAGE_WITH_ERRORS = "TX ID: {} — {} — {}";
    private static final String LOG_MESSAGE = "TX ID: {} — {}";

    @Autowired
    public TrainerController(TrainerService trainerService, ModelMapper modelMapper) {
        this.trainerService = trainerService;
        this.modelMapper = modelMapper;
    }

    @PostMapping
    public ResponseEntity<?> handleCreateTrainer(
            @RequestBody @Validated(TrainerCreateGroup.class) TrainerRequestDTO trainerDTO,
            BindingResult bindingResult,
            UriComponentsBuilder uriComponentsBuilder,
            HttpServletRequest httpServletRequest) {
        String txID = (String) httpServletRequest.getAttribute("txID");

        if (bindingResult.hasErrors()) {
            Map<String, String> errors = buildErrorMessage(bindingResult);
            LOGGER.warn(LOG_MESSAGE_WITH_ERRORS, txID, HttpStatus.UNPROCESSABLE_ENTITY, errors);

            return ResponseEntity.unprocessableEntity().body(errors);
        }

        Trainer newTrainer = modelMapper.map(trainerDTO, Trainer.class);
        newTrainer.setSpecialization(new TrainingType(trainerDTO.getSpecialization(), ""));
        Trainer createdTrainer = trainerService.createTrainer(txID, newTrainer);

        AuthDTO response = modelMapper.map(createdTrainer, AuthDTO.class);
        URI location = uriComponentsBuilder
                .path("/api/v1/trainers/{trainerId}")
                .build(createdTrainer.getId());

        LOGGER.info(LOG_MESSAGE, txID, HttpStatus.OK);
        return ResponseEntity.created(location).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> handleGetTrainer(@PathVariable("id") long id,
                                              @RequestBody @Valid AuthDTO authDTO,
                                              BindingResult bindingResult,
                                              HttpServletRequest httpServletRequest) {
        String txID = (String) httpServletRequest.getAttribute("txID");

        if (bindingResult.hasErrors()) {
            Map<String, String> errors = buildErrorMessage(bindingResult);
            LOGGER.warn(LOG_MESSAGE_WITH_ERRORS, txID, HttpStatus.UNPROCESSABLE_ENTITY, errors);

            return ResponseEntity.unprocessableEntity().body(errors);
        }

        Optional<Trainer> trainerOpt = trainerService.findById(txID, id, authDTO.getUsername(), authDTO.getPassword());
        Optional<TrainerResponseDTO> trainer = trainerOpt.map(
                value -> modelMapper.map(value, TrainerResponseDTO.class));

        FormattingTuple logMessage = trainer.isPresent()
                ? MessageFormatter.format(LOG_MESSAGE, txID, HttpStatus.OK)
                : MessageFormatter.format(LOG_MESSAGE, txID, HttpStatus.NOT_FOUND);
        LOGGER.info(logMessage.getMessage());

        return ResponseEntity.of(trainer);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> handleUpdateTrainer(@PathVariable("id") long id,
                                         @RequestBody @Validated(TrainerUpdateGroup.class) TrainerRequestDTO trainerDTO,
                                         BindingResult bindingResult,
                                         HttpServletRequest httpServletRequest) {
        String txID = (String) httpServletRequest.getAttribute("txID");

        if (bindingResult.hasErrors()) {
            Map<String, String> errors = buildErrorMessage(bindingResult);
            LOGGER.warn(LOG_MESSAGE_WITH_ERRORS, txID, HttpStatus.UNPROCESSABLE_ENTITY, errors);

            return ResponseEntity.unprocessableEntity().body(errors);
        }

        Trainer trainerToUpdate = modelMapper.map(trainerDTO, Trainer.class);
        trainerToUpdate.setId(id);
        trainerToUpdate.setSpecialization(
                new TrainingType(trainerDTO.getSpecialization(), "")
        );

        Optional<Trainer> updatedTrainerOpt = trainerService.update(txID, trainerToUpdate,
                trainerDTO.getUsername(), trainerDTO.getPassword());

        Optional<TrainerResponseDTO> updatedTrainerResponseDTO = updatedTrainerOpt.map(
                value -> modelMapper.map(value, TrainerResponseDTO.class));

        FormattingTuple logMessage = updatedTrainerResponseDTO.isPresent()
                ? MessageFormatter.format(LOG_MESSAGE, txID, HttpStatus.OK)
                : MessageFormatter.format(LOG_MESSAGE, txID, HttpStatus.NOT_FOUND);
        LOGGER.info(logMessage.getMessage());

        return ResponseEntity.of(updatedTrainerResponseDTO);
    }

    @PatchMapping("/{id}/activate")
    public ResponseEntity<?> handleChangeActivationStatus(@PathVariable("id") long id,
                                      @RequestBody @Validated(TrainerActivateGroup.class) TrainerRequestDTO trainerDTO,
                                      BindingResult bindingResult,
                                      HttpServletRequest httpServletRequest) {
        String txID = (String) httpServletRequest.getAttribute("txID");

        if (bindingResult.hasErrors()) {
            Map<String, String> errors = buildErrorMessage(bindingResult);
            LOGGER.warn(LOG_MESSAGE_WITH_ERRORS, txID, HttpStatus.UNPROCESSABLE_ENTITY, errors);

            return ResponseEntity.unprocessableEntity().body(errors);
        }

        trainerService.changeActivationStatus(txID, id, trainerDTO.getIsActive(),
                trainerDTO.getUsername(), trainerDTO.getPassword());

        LOGGER.info(LOG_MESSAGE, txID, HttpStatus.OK);

        return ResponseEntity.ok().build();
    }

    @GetMapping("/{id}/trainings")
    public ResponseEntity<?> handleGetTrainingsWithFiltering(@PathVariable("id") long id,
                                                     @RequestBody @Valid TrainingGetListRequestDTO requestDTO,
                                                     BindingResult bindingResult,
                                                     HttpServletRequest httpServletRequest) {
        String txID = (String) httpServletRequest.getAttribute("txID");

        List<Training> trainings = trainerService.getTrainingsWithFiltering(txID, id, requestDTO.getUsername(),
                requestDTO.getPassword(), requestDTO);

        List<TrainingResponseDTO> response = trainings.stream()
                .map(e -> modelMapper.map(e, TrainingResponseDTO.class))
                .toList();

        LOGGER.info(LOG_MESSAGE, txID, HttpStatus.OK);

        return ResponseEntity.ok().body(response);
    }

    private Map<String, String> buildErrorMessage(BindingResult bindingResult) {
        Map<String, String> errors = new HashMap<>();

        bindingResult.getFieldErrors().forEach(error ->
                errors.put(error.getField(), error.getDefaultMessage()));

        return errors;
    }
}
