package epam.xstack.service;

import epam.xstack.dto.auth.AuthDTO;
import epam.xstack.dto.trainer.TrainerRequestDTO;
import epam.xstack.dto.trainer.TrainerResponseDTO;
import epam.xstack.dto.workload.Action;
import epam.xstack.dto.workload.TrainerWorkloadRequestDTO;
import epam.xstack.exception.NoSuchTrainerExistException;
import epam.xstack.exception.NoSuchTrainingTypeException;
import epam.xstack.exception.PersonAlreadyRegisteredException;
import epam.xstack.model.Trainer;
import epam.xstack.model.Training;
import epam.xstack.model.TrainingType;
import epam.xstack.repository.TrainerRepository;
import epam.xstack.repository.TrainingTypeRepository;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class TrainerService {
    private final TrainerRepository trainerRepository;

    private final TrainingTypeRepository trainingTypeRepository;
    private final UserService userService;

    private final ModelMapper modelMapper;
    private final RestTemplate restTemplate;
    private static final Logger LOGGER = LoggerFactory.getLogger(TrainerService.class);

    @Autowired
    public TrainerService(TrainerRepository trainerRepository, UserService userService,
                          MeterRegistry meterRegistry, TrainingTypeRepository trainingTypeRepository,
                          ModelMapper modelMapper, RestTemplate restTemplate) {
        this.trainerRepository = trainerRepository;
        this.userService = userService;
        this.trainingTypeRepository = trainingTypeRepository;
        this.modelMapper = modelMapper;
        this.restTemplate = restTemplate;

        Gauge.builder("custom.trainer.count", trainerRepository, TrainerRepository::count)
                .register(meterRegistry);
    }

    @Transactional
    public AuthDTO createTrainer(String txID, TrainerRequestDTO trainerDTO) {
        Trainer newTrainer = modelMapper.map(trainerDTO, Trainer.class);

        String username = userService.generateUsername(newTrainer.getFirstName(), newTrainer.getLastName());
        String password = userService.generatePassword();

        newTrainer.setSpecialization(new TrainingType(trainerDTO.getSpecialization(), ""));
        newTrainer.setUsername(username);
        newTrainer.setPassword(password);
        newTrainer.setActive(true);

        checkIfSpecializationExists(txID, trainerDTO);
        checkIfNotRegisteredYet(txID, newTrainer);

        trainerRepository.save(newTrainer);

        LOGGER.info("TX ID: {} — Successfully saved new trainer with username '{}' and id '{}'",
                txID, newTrainer.getUsername(), newTrainer.getId());
        return modelMapper.map(newTrainer, AuthDTO.class);
    }

    @Transactional
    public void save(Trainer trainer) {
        trainerRepository.save(trainer);
    }

    public List<Trainer> findAll() {
        return trainerRepository.findAll();
    }

    public Optional<TrainerResponseDTO> findById(long id) {
        Optional<Trainer> trainerOpt = trainerRepository.findById(id);
        trainerOpt.ifPresent(value -> value.setUsername(null));

        return trainerOpt.map(value -> modelMapper.map(value, TrainerResponseDTO.class));
    }

    public Optional<Trainer> findByUsername(String username) {
        return trainerRepository.findByUsername(username);
    }

    @Transactional
    public Optional<TrainerResponseDTO> update(String txID, long id, TrainerRequestDTO trainerDTO) {
        Trainer updatedTrainer = modelMapper.map(trainerDTO, Trainer.class);
        updatedTrainer.setId(id);
        updatedTrainer.setSpecialization(checkIfSpecializationExists(txID, trainerDTO));

        Optional<Trainer> trainerOpt = trainerRepository.findById(updatedTrainer.getId())
                .map(trainer -> {
                    updateFields(trainer, updatedTrainer);
                    trainerRepository.save(trainer);

                    LOGGER.info("TX ID: {} — Successfully updated trainer with username '{}' and id '{}'",
                            txID, trainer.getUsername(), trainer.getId());

                    return trainer;
                });

        return trainerOpt.map(value -> modelMapper.map(value, TrainerResponseDTO.class));
    }

    public List<Trainer> getVerifiedTrainersByUsernameList(String txID, List<String> trainerUsernames) {
        List<Trainer> verifiedList = findAll().stream()
                .filter(trainer -> trainerUsernames.contains(trainer.getUsername()))
                .toList();

        if (verifiedList.isEmpty()) {
            throw new NoSuchTrainerExistException(txID);
        }

        return verifiedList;
    }

    @Transactional
    public void changeActivationStatus(String txID, long id, Boolean newStatus) {

        Trainer trainer = trainerRepository.getReferenceById(id);
        trainer.setActive(newStatus);
        trainerRepository.save(trainer);

        LOGGER.info("TX ID: {} — Successfully changed status for trainer with id '{}' to '{}'", txID, id, newStatus);
    }

    @CircuitBreaker(name = "trainerservice", fallbackMethod = "updateTrainerWorkloadFallback")
    public void updateTrainerWorkload(String txID, Training training, Action action) {
        Trainer trainer = training.getTrainer();

        TrainerWorkloadRequestDTO requestDTO = new TrainerWorkloadRequestDTO(
                trainer.getUsername(),
                trainer.getFirstName(),
                trainer.getLastName(),
                trainer.getActive(),
                training.getTrainingDate(),
                training.getTrainingDuration(),
                action
        );

        String url = "http://trainer-workload-service/api/v1/trainerworkload";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add("txID", txID);
        headers.setBearerAuth(extractJWT(txID));

        HttpEntity<TrainerWorkloadRequestDTO> request = new HttpEntity<>(requestDTO, headers);
        restTemplate.exchange(url, HttpMethod.POST, request, String.class);

        LOGGER.info("TX ID: {} — Sent a '{}' request to 'trainer-workload' microservice for '{}' trainer",
                txID, action, trainer.getUsername());
    }

    public void updateTrainerWorkloadFallback(String txID, Training training,
                                              Action action, RuntimeException e) {
        Trainer trainer = training.getTrainer();
        LocalDate trainingDate = training.getTrainingDate();

        LOGGER.warn("TX ID: {} — Circuit breaker engaged: Couldn't send a '{}' request to 'trainer-workload' " +
                        "microservice for '{}' trainer. Consider adding workload manually later: {}.{}, {} minutes",
                txID, action, trainer.getUsername(),
                trainingDate.getMonth(), trainingDate.getYear(), training.getTrainingDuration());
    }

    private void checkIfNotRegisteredYet(String txID, Trainer newTrainer) {
        List<Trainer> candidates = trainerRepository.findByUsernameStartingWith(
                newTrainer.getUsername().replaceAll("\\d", ""));

        for (Trainer trainer : candidates) {
            if (newTrainer.getSpecialization().getId().equals(trainer.getSpecialization().getId())) {
                throw new PersonAlreadyRegisteredException(txID);
            }
        }
    }

    private TrainingType checkIfSpecializationExists(String txID, TrainerRequestDTO trainer) {
        Optional<TrainingType> trainingTypeOpt = trainingTypeRepository.findById(
                trainer.getSpecialization());

        if (trainingTypeOpt.isEmpty()) {
            throw new NoSuchTrainingTypeException(txID);
        } else {
            return trainingTypeOpt.get();
        }
    }

    private void updateFields(Trainer existingTrainer, Trainer updatedTrainer) {
        existingTrainer.setFirstName(updatedTrainer.getFirstName());
        existingTrainer.setLastName(updatedTrainer.getLastName());
        existingTrainer.setSpecialization(updatedTrainer.getSpecialization());
        existingTrainer.setActive(updatedTrainer.getActive());
    }

    private String extractJWT(String txID) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication instanceof JwtAuthenticationToken jwtAuth) {
            return jwtAuth.getToken().getTokenValue();
        } else {
            LOGGER.warn("TX ID: {} — No JWT found in authentication", txID);
            return null;
        }
    }
}
