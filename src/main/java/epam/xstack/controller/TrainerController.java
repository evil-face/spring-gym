package epam.xstack.controller;

import epam.xstack.dto.auth.AuthDTO;
import epam.xstack.dto.trainer.req.TrainerActivationRequestDTO;
import epam.xstack.dto.trainer.req.TrainerCreateRequestDTO;
import epam.xstack.dto.trainer.req.TrainerUpdateRequestDTO;
import epam.xstack.dto.trainer.resp.TrainerGetResponseDTO;
import epam.xstack.dto.trainer.resp.TrainerUpdateResponseDTO;
import epam.xstack.model.Trainer;
import epam.xstack.model.TrainingType;
import epam.xstack.service.TrainerService;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
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
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping(value = "/api/v1/trainers", consumes = {"application/JSON"}, produces = {"application/JSON"})
public class TrainerController {
    private final TrainerService trainerService;
    private final ModelMapper modelMapper;
    private static final Logger LOGGER = LoggerFactory.getLogger(TraineeController.class);

    @Autowired
    public TrainerController(TrainerService trainerService, ModelMapper modelMapper) {
        this.trainerService = trainerService;
        this.modelMapper = modelMapper;
    }

    @PostMapping
    public ResponseEntity<?> handleCreateTrainer(@RequestBody @Valid TrainerCreateRequestDTO trainerDTO,
                                                 BindingResult bindingResult,
                                                 UriComponentsBuilder uriComponentsBuilder,
                                                 HttpServletRequest httpServletRequest) {
        String txID = (String) httpServletRequest.getAttribute("txID");

        if (bindingResult.hasErrors()) {
            Map<String, String> errors = buildErrorMessage(bindingResult);
            LOGGER.warn("TX ID: {} — " + HttpStatus.UNPROCESSABLE_ENTITY + " — " + errors, txID);

            return ResponseEntity.unprocessableEntity().body(errors);
        }

        Trainer newTrainer = modelMapper.map(trainerDTO, Trainer.class);
        newTrainer.setSpecialization(new TrainingType(trainerDTO.getSpecialization(), ""));
        Trainer createdTrainer = trainerService.createTrainer(txID, newTrainer);

        AuthDTO response = modelMapper.map(createdTrainer, AuthDTO.class);
        URI location = uriComponentsBuilder
                .path("/api/v1/trainers/{trainerId}")
                .build(createdTrainer.getId());

        LOGGER.info("TX ID: {} — " + HttpStatus.OK, txID);
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
            LOGGER.warn("TX ID: {} — " + HttpStatus.UNPROCESSABLE_ENTITY + " — " + errors, txID);

            return ResponseEntity.unprocessableEntity().body(errors);
        }

        Optional<Trainer> trainerOpt = trainerService.findById(txID, id, authDTO.getUsername(), authDTO.getPassword());
        Optional<TrainerGetResponseDTO> trainer = trainerOpt.map(
                value -> modelMapper.map(value, TrainerGetResponseDTO.class));

        String logMessage = trainer.isPresent() ?
                "TX ID: {} — " + HttpStatus.OK :
                "TX ID: {} — " + HttpStatus.NOT_FOUND;
        LOGGER.info(logMessage, txID);

        return ResponseEntity.of(trainer);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> handleUpdateTrainer(@PathVariable("id") long id,
                                                 @RequestBody @Valid TrainerUpdateRequestDTO trainerUpdateRequestDTO,
                                                 BindingResult bindingResult,
                                                 HttpServletRequest httpServletRequest) {
        String txID = (String) httpServletRequest.getAttribute("txID");

        if (bindingResult.hasErrors()) {
            Map<String, String> errors = buildErrorMessage(bindingResult);
            LOGGER.warn("TX ID: {} — " + HttpStatus.UNPROCESSABLE_ENTITY + " — " + errors, txID);

            return ResponseEntity.unprocessableEntity().body(errors);
        }

        Trainer trainerToUpdate = modelMapper.map(trainerUpdateRequestDTO, Trainer.class);
        trainerToUpdate.setId(id);
        trainerToUpdate.setSpecialization(
                new TrainingType(trainerUpdateRequestDTO.getSpecialization(), "")
        );

        Optional<Trainer> updatedTrainerOpt = trainerService.update(txID, trainerToUpdate,
                trainerUpdateRequestDTO.getUsername(), trainerUpdateRequestDTO.getPassword());

        Optional<TrainerUpdateResponseDTO> updatedTrainerResponseDTO = updatedTrainerOpt.map(
                value -> modelMapper.map(value, TrainerUpdateResponseDTO.class));

        String logMessage = updatedTrainerResponseDTO.isPresent() ?
                "TX ID: {} — " + HttpStatus.OK :
                "TX ID: {} — " + HttpStatus.NOT_FOUND;
        LOGGER.info(logMessage, txID);

        return ResponseEntity.of(updatedTrainerResponseDTO);
    }

    @PatchMapping("/{id}/activate")
    public ResponseEntity<?> handleChangeActivationStatus(@PathVariable("id") long id,
                                                          @RequestBody @Valid TrainerActivationRequestDTO requestDTO,
                                                          BindingResult bindingResult,
                                                          HttpServletRequest httpServletRequest) {
        String txID = (String) httpServletRequest.getAttribute("txID");

        if (bindingResult.hasErrors()) {
            Map<String, String> errors = buildErrorMessage(bindingResult);
            LOGGER.warn("TX ID: {} — " + HttpStatus.UNPROCESSABLE_ENTITY + " — " + errors, txID);

            return ResponseEntity.unprocessableEntity().body(errors);
        }

        trainerService.changeActivationStatus(txID, id, requestDTO.getIsActive(),
                requestDTO.getUsername(), requestDTO.getPassword());

        LOGGER.info("TX ID: {} — " + HttpStatus.OK, txID);

        return ResponseEntity.ok().build();
    }

    private Map<String, String> buildErrorMessage(BindingResult bindingResult) {
        Map<String, String> errors = new HashMap<>();

        bindingResult.getFieldErrors().forEach(error ->
                errors.put(error.getField(), error.getDefaultMessage()));

        return errors;
    }
}
