package epam.xstack.service;

import epam.xstack.dao.TraineeDAO;
import epam.xstack.model.Trainee;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public class TraineeService {
    private final TraineeDAO traineeDAO;
    private final UserService userService;

    @Autowired
    public TraineeService(TraineeDAO traineeDAO, UserService userService) {
        this.traineeDAO = traineeDAO;
        this.userService = userService;
    }

    public Trainee createTrainee(String firstName, String lastName,
                                 boolean isActive, Date dateOfBirth, String address) {
        long id = userService.generateId();
        String username = userService.generateUsername(firstName, lastName);
        String password = userService.generatePassword();

        Trainee trainee = new Trainee(id, firstName, lastName,
                username, password, true,
                dateOfBirth, address);

        traineeDAO.save(trainee);
        return trainee;
    }

    public List<Trainee> findAll() {
        return traineeDAO.findAll();
    }

    public Optional<Trainee> findById(long id) {
        return traineeDAO.findById(id);
    }

    public void update(Trainee trainee) {
        traineeDAO.update(trainee);
    }

    public void delete(Trainee trainee) {
        traineeDAO.delete(trainee);
    }
}
