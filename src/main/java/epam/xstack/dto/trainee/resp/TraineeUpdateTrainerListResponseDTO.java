package epam.xstack.dto.trainee.resp;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import epam.xstack.model.Trainer;

import java.util.List;

public class TraineeUpdateTrainerListResponseDTO {
    @JsonIgnoreProperties({"id", "password", "trainingList", "trainees", "isActive"})
    private List<Trainer> trainers;

    public List<Trainer> getTrainers() {
        return trainers;
    }

    public void setTrainers(List<Trainer> trainers) {
        this.trainers = trainers;
    }
}
