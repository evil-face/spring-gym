package epam.xstack.dto.trainee.req;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;

import java.util.List;

public class TraineeUpdateTrainerListRequestDTO {
    @NotBlank(message = "Username cannot be empty")
    private String username;
    @NotEmpty
    private List<String> trainers;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public List<String> getTrainers() {
        return trainers;
    }

    public void setTrainers(List<String> trainers) {
        this.trainers = trainers;
    }
}
