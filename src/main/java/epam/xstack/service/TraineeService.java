package epam.xstack.service;

import epam.xstack.dto.auth.AuthDTO;
import epam.xstack.dto.trainee.TraineeRequestDTO;
import epam.xstack.dto.trainee.TraineeResponseDTO;
import epam.xstack.dto.trainer.TrainerResponseDTO;
import epam.xstack.dto.workload.Action;
import epam.xstack.exception.NoSuchTraineeExistException;
import epam.xstack.exception.PersonAlreadyRegisteredException;
import epam.xstack.model.Trainee;
import epam.xstack.model.Trainer;
import epam.xstack.repository.TraineeRepository;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
public class TraineeService {
    private final TraineeRepository traineeRepository;
    private final TrainerService trainerService;

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final ModelMapper modelMapper;

    private static final Logger LOGGER = LoggerFactory.getLogger(TraineeService.class);


    @Autowired
    public TraineeService(UserService userService, TraineeRepository traineeRepository, TrainerService trainerService,
                          MeterRegistry meterRegistry, PasswordEncoder passwordEncoder,
                          ModelMapper modelMapper) {
        this.userService = userService;
        this.traineeRepository = traineeRepository;
        this.trainerService = trainerService;
        this.passwordEncoder = passwordEncoder;
        this.modelMapper = modelMapper;

        Gauge.builder("custom.trainee.count", traineeRepository, TraineeRepository::count)
                .register(meterRegistry);
    }

    @Transactional
    public AuthDTO createTrainee(String txID, TraineeRequestDTO traineeDTO) {
        Trainee newTrainee = modelMapper.map(traineeDTO, Trainee.class);

        String username = userService.generateUsername(newTrainee.getFirstName(), newTrainee.getLastName());
        String plainPassword = userService.generatePassword();
        String encodedPassword = passwordEncoder.encode(plainPassword);

        newTrainee.setUsername(username);
        newTrainee.setPassword(encodedPassword);
        newTrainee.setActive(true);

        checkIfNotRegisteredYet(txID, newTrainee);

        traineeRepository.save(newTrainee);
        LOGGER.info("TX ID: {} — Successfully saved new trainee with username '{}' and id '{}'",
                txID, newTrainee.getUsername(), newTrainee.getId());

        AuthDTO responseDTO = modelMapper.map(newTrainee, AuthDTO.class);
        responseDTO.setPassword(plainPassword);

        return responseDTO;
    }

    @Transactional
    public void save(Trainee trainee) {
        traineeRepository.save(trainee);
    }

    public Optional<TraineeResponseDTO> findById(long id) {
        Optional<Trainee> traineeOpt = traineeRepository.findById(id);

        traineeOpt.ifPresent(value -> value.setUsername(null));

        return traineeOpt.map(value -> modelMapper.map(value, TraineeResponseDTO.class));
    }

    public Optional<Trainee> findByUsername(String username) {
        return traineeRepository.findByUsername(username);
    }

    @Transactional
    public Optional<TraineeResponseDTO> update(String txID, long id, TraineeRequestDTO traineeDTO) {
        Trainee updatedTrainee = modelMapper.map(traineeDTO, Trainee.class);
        updatedTrainee.setId(id);

        Optional<Trainee> traineeOptional = traineeRepository.findById(id);

        if (traineeOptional.isPresent()) {
            Trainee existingTrainee = traineeOptional.get();
            updateFields(existingTrainee, updatedTrainee);

            traineeRepository.save(existingTrainee);
            LOGGER.info("TX ID: {} — Successfully updated trainee with username '{}' and id '{}'",
                    txID, existingTrainee.getUsername(), existingTrainee.getId());

            return Optional.of(modelMapper.map(existingTrainee, TraineeResponseDTO.class));
        }

        return Optional.empty();
    }

    @Transactional
    public void delete(String txID, long id) {
        traineeRepository.findById(id).ifPresent(trainee ->
                trainee.getTrainingList().forEach(
                        training -> trainerService.updateTrainerWorkload(txID, training, Action.DELETE)));

        traineeRepository.deleteById(id);

        LOGGER.info("TX ID: {} — Successfully deleted trainee with id '{}'", txID, id);
    }

    @Transactional
    public void changeActivationStatus(String txID, long id, Boolean newStatus) {
        Optional<Trainee> traineeOpt = traineeRepository.findById(id);

        if (traineeOpt.isPresent()) {
            Trainee trainee = traineeOpt.get();
            trainee.setActive(newStatus);

            traineeRepository.save(trainee);
            LOGGER.info("TX ID: {} — Successfully changed status for trainee with  id '{}' to '{}'",
                    txID, id, newStatus);
        } else {
            throw new NoSuchTraineeExistException(txID);
        }
    }

    public List<TrainerResponseDTO> getPotentialTrainersForTrainee(String txID, long id) {
        Optional<Trainee> traineeOpt = traineeRepository.findById(id);

        if (traineeOpt.isEmpty()) {
            throw new NoSuchTraineeExistException(txID);
        }

        List<Trainer> allTrainers = trainerService.findAll();
        Set<Trainer> assignedTrainers = traineeOpt.get().getTrainers();

        List<TrainerResponseDTO> responseDTOS = allTrainers.stream()
                .filter(trainer -> !assignedTrainers.contains(trainer))
                .filter(Trainer::getActive)
                .map(trainer -> modelMapper.map(trainer, TrainerResponseDTO.class))
                .toList();

        responseDTOS.forEach(trainer -> {
            trainer.setTrainees(null);
            trainer.setActive(null);
        });

        return responseDTOS;
    }

    @Transactional
    public List<TrainerResponseDTO> updateTrainerList(String txID, long id, TraineeRequestDTO traineeRequestDTO) {
        List<Trainer> updatedList =
                trainerService.getVerifiedTrainersByUsernameList(txID, traineeRequestDTO.getTrainers());

        Optional<Trainee> traineeOpt = traineeRepository.findById(id);

        if (traineeOpt.isPresent()) {
            Trainee trainee = traineeOpt.get();
            trainee.setTrainers(new HashSet<>(updatedList));

            traineeRepository.save(trainee);
            LOGGER.info("TX ID: {} — Successfully updated trainee's list of trainers for id '{}' with '{}'"
                    + " trainers", txID, id, updatedList.size());
        } else {
            throw new NoSuchTraineeExistException(txID);
        }

        List<TrainerResponseDTO> responseDTOS = updatedList.stream()
                .map(trainer -> modelMapper.map(trainer, TrainerResponseDTO.class))
                .toList();

        responseDTOS.forEach(trainer -> {
            trainer.setTrainees(null);
            trainer.setActive(null);
        });

        return responseDTOS;
    }

    private void checkIfNotRegisteredYet(String txID, Trainee newTrainee) {
        List<Trainee> trainees = traineeRepository.findByUsernameStartingWith(
                newTrainee.getUsername().replaceAll("\\d", ""));

        for (Trainee trainee : trainees) {
            if (newTrainee.getAddress().equals(trainee.getAddress())
                    && newTrainee.getDateOfBirth().equals(trainee.getDateOfBirth())) {
                throw new PersonAlreadyRegisteredException(txID);
            }
        }
    }

    private void updateFields(Trainee existingTrainee, Trainee updatedTrainee) {
        existingTrainee.setFirstName(updatedTrainee.getFirstName());
        existingTrainee.setLastName(updatedTrainee.getLastName());
        existingTrainee.setDateOfBirth(updatedTrainee.getDateOfBirth());
        existingTrainee.setAddress(updatedTrainee.getAddress());
        existingTrainee.setActive(updatedTrainee.getActive());
    }
}
