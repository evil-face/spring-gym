package epam.xstack.controller;

import epam.xstack.dto.trainee.TraineeActivationRequestDTO;
import epam.xstack.dto.trainee.TraineeCreateRequestDTO;
import epam.xstack.dto.trainee.TraineeGetResponseDTO;
import epam.xstack.dto.auth.AuthDTO;
import epam.xstack.dto.trainee.TraineeUpdateRequestDTO;
import epam.xstack.dto.trainee.TraineeUpdateResponseDTO;
import epam.xstack.dto.trainee.TraineeUpdateTrainerListRequestDTO;
import epam.xstack.dto.trainee.UnassignedTrainersResponseDTO;
import epam.xstack.dto.training.TrainingGetListForTraineeRequestDTO;
import epam.xstack.dto.training.TrainingResponseDTO;
import epam.xstack.model.Trainee;
import epam.xstack.model.Trainer;
import epam.xstack.model.Training;
import epam.xstack.service.TraineeService;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
public class TraineeController {
    private final TraineeService traineeService;
    private final ModelMapper modelMapper;
    private static final Logger LOGGER = LoggerFactory.getLogger(TraineeController.class);

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
            LOGGER.warn("TX ID: {} — " + HttpStatus.UNPROCESSABLE_ENTITY + " — " + errors, txID);

            return ResponseEntity.unprocessableEntity().body(errors);
        }

        Trainee newTrainee = modelMapper.map(traineeDTO, Trainee.class);
        Trainee createdTrainee = traineeService.createTrainee(txID, newTrainee);

        AuthDTO response = modelMapper.map(createdTrainee, AuthDTO.class);
        URI location = uriComponentsBuilder
                .path("/api/v1/trainees/{traineeId}")
                .build(createdTrainee.getId());

        LOGGER.info("TX ID: {} — " + HttpStatus.OK, txID);
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
            LOGGER.warn("TX ID: {} — " + HttpStatus.UNPROCESSABLE_ENTITY + " — " + errors, txID);

            return ResponseEntity.unprocessableEntity().body(errors);
        }

        Optional<Trainee> traineeOpt = traineeService.findById(txID, id, authDTO.getUsername(), authDTO.getPassword());
        Optional<TraineeGetResponseDTO> trainee = traineeOpt.map(
                value -> modelMapper.map(value, TraineeGetResponseDTO.class));

        String logMessage = trainee.isPresent() ?
                "TX ID: {} — " + HttpStatus.OK :
                "TX ID: {} — " + HttpStatus.NOT_FOUND;
        LOGGER.info(logMessage, txID);

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
            LOGGER.warn("TX ID: {} — " + HttpStatus.UNPROCESSABLE_ENTITY + " — " + errors, txID);

            return ResponseEntity.unprocessableEntity().body(errors);
        }

        Trainee traineeToUpdate = modelMapper.map(traineeUpdateRequestDTO, Trainee.class);
        traineeToUpdate.setId(id);

        Optional<Trainee> updatedTraineeOpt = traineeService.update(txID, traineeToUpdate,
                traineeUpdateRequestDTO.getUsername(), traineeUpdateRequestDTO.getPassword());

        Optional<TraineeUpdateResponseDTO> updatedTraineeResponseDTO = updatedTraineeOpt.map(
                value -> modelMapper.map(value, TraineeUpdateResponseDTO.class));

        String logMessage = updatedTraineeResponseDTO.isPresent() ?
                "TX ID: {} — " + HttpStatus.OK :
                "TX ID: {} — " + HttpStatus.NOT_FOUND;
        LOGGER.info(logMessage, txID);

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
            LOGGER.warn("TX ID: {} — " + HttpStatus.UNPROCESSABLE_ENTITY + " — " + errors, txID);

            return ResponseEntity.unprocessableEntity().body(errors);
        }

        Trainee traineeToDelete = modelMapper.map(authDTO, Trainee.class);
        traineeToDelete.setId(id);

        traineeService.delete(txID, traineeToDelete);

        LOGGER.info("TX ID: {} — " + HttpStatus.NO_CONTENT, txID);

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
            LOGGER.warn("TX ID: {} — " + HttpStatus.UNPROCESSABLE_ENTITY + " — " + errors, txID);

            return ResponseEntity.unprocessableEntity().body(errors);
        }

        traineeService.changeActivationStatus(txID, id, requestDTO.getIsActive(),
                requestDTO.getUsername(), requestDTO.getPassword());

        LOGGER.info("TX ID: {} — " + HttpStatus.OK, txID);

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
            LOGGER.warn("TX ID: {} — " + HttpStatus.UNPROCESSABLE_ENTITY + " — " + errors, txID);

            return ResponseEntity.unprocessableEntity().body(errors);
        }

        List<Trainer> trainers = traineeService.getPotentialTrainersForTrainee(
                txID, id, authDTO.getUsername(), authDTO.getPassword());

        List<UnassignedTrainersResponseDTO> response = trainers.stream()
                .map(e -> modelMapper.map(e, UnassignedTrainersResponseDTO.class))
                .toList();

        LOGGER.info("TX ID: {} — " + HttpStatus.OK, txID);

        return ResponseEntity.ok().body(response);
    }

    @GetMapping("/{id}/trainings")
    public ResponseEntity<?> handleGetTrainingsWithFiltering(@PathVariable("id") long id,
                                                             @RequestBody @Valid TrainingGetListForTraineeRequestDTO requestDTO,
                                                             BindingResult bindingResult,
                                                             HttpServletRequest httpServletRequest) {
        String txID = (String) httpServletRequest.getAttribute("txID");

        List<Training> trainings = traineeService.getTrainingsWithFiltering(txID, id, requestDTO.getUsername(),
                requestDTO.getPassword(), requestDTO);

        List<TrainingResponseDTO> response = trainings.stream()
                .map(e -> modelMapper.map(e, TrainingResponseDTO.class))
                .toList();

        LOGGER.info("TX ID: {} — " + HttpStatus.OK, txID);

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
            LOGGER.warn("TX ID: {} — " + HttpStatus.UNPROCESSABLE_ENTITY + " — " + errors, txID);

            return ResponseEntity.unprocessableEntity().body(errors);
        }

        List<Trainer> updatedTrainersList = traineeService.updateTrainerList(txID, id, requestDTO);

        List<UnassignedTrainersResponseDTO> responseDTO = updatedTrainersList.stream()
                .map(trainer -> modelMapper.map(trainer, UnassignedTrainersResponseDTO.class))
                .toList();

        LOGGER.info("TX ID: {} — " + HttpStatus.OK, txID);

        return ResponseEntity.ok(responseDTO);
    }

    private Map<String, String> buildErrorMessage(BindingResult bindingResult) {
        Map<String, String> errors = new HashMap<>();

        bindingResult.getFieldErrors().forEach(error ->
                errors.put(error.getField(), error.getDefaultMessage()));

        return errors;
    }
}