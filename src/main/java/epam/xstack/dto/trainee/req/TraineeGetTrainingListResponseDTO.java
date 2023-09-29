package epam.xstack.dto.trainee.req;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import epam.xstack.model.Trainer;
import epam.xstack.model.TrainingType;
import java.time.LocalDate;

public class TraineeGetTrainingListResponseDTO {

    private String trainingName;
    private LocalDate trainingDate;
    private TrainingType trainingType;
    private int trainingDuration;
    @JsonIgnoreProperties({"id", "username", "password", "trainingList", "trainees", "isActive", "specialization"})
    private Trainer trainer;

    public String getTrainingName() {
        return trainingName;
    }

    public void setTrainingName(String trainingName) {
        this.trainingName = trainingName;
    }

    public LocalDate getTrainingDate() {
        return trainingDate;
    }

    public void setTrainingDate(LocalDate trainingDate) {
        this.trainingDate = trainingDate;
    }

    public TrainingType getTrainingType() {
        return trainingType;
    }

    public void setTrainingType(TrainingType trainingType) {
        this.trainingType = trainingType;
    }

    public int getTrainingDuration() {
        return trainingDuration;
    }

    public void setTrainingDuration(int trainingDuration) {
        this.trainingDuration = trainingDuration;
    }

    public Trainer getTrainer() {
        return trainer;
    }

    public void setTrainer(Trainer trainer) {
        this.trainer = trainer;
    }
}
