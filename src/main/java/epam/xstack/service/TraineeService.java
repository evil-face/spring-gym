package epam.xstack.service;

import epam.xstack.dao.TraineeDAO;
import epam.xstack.model.Trainee;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public final class TraineeService {
    private final TraineeDAO traineeDAO;
    private final UserService userService;
    private static final Logger LOGGER = LoggerFactory.getLogger(TraineeService.class);

    @Autowired
    public TraineeService(TraineeDAO traineeDAO, UserService userService) {
        this.traineeDAO = traineeDAO;
        this.userService = userService;
    }

    public Trainee createTrainee(String firstName, String lastName,
                                 boolean isActive, Date dateOfBirth, String address) {
        String id = userService.generateId();
        String username = userService.generateUsername(firstName, lastName);
        String password = userService.generatePassword();

        Trainee trainee = new Trainee(id, firstName, lastName,
                username, password, true,
                dateOfBirth, address);

        traineeDAO.save(trainee);
        LOGGER.info("Saved new trainee with id {} to the DB", trainee.getId());

        return trainee;
    }

    public List<Trainee> findAll() {
        return traineeDAO.findAll();
    }

    public Optional<Trainee> findById(String id) {
        return traineeDAO.findById(id);
    }

    public void update(Trainee trainee) {
        traineeDAO.update(trainee);
        LOGGER.info("Updated trainee with id {} in the DB", trainee.getId());
    }

    public void delete(Trainee trainee) {
        traineeDAO.delete(trainee);
        LOGGER.info("Deleted trainee with id {} from the DB", trainee.getId());

    }
}
