package epam.xstack.dto.training;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import epam.xstack.model.Trainee;
import epam.xstack.model.Trainer;
import epam.xstack.model.TrainingType;

import java.time.LocalDate;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class TrainingResponseDTO {
    private String trainingName;
    private LocalDate trainingDate;
    private TrainingType trainingType;
    private Integer trainingDuration;
    @JsonIgnoreProperties({"id", "username", "password", "trainingList", "trainees", "isActive", "specialization"})
    private Trainer trainer;

    @JsonIgnoreProperties({"id", "username", "password", "trainingList", "dateOfBirth", "address",
            "trainers", "isActive"})
    private Trainee trainee;

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

    public Integer getTrainingDuration() {
        return trainingDuration;
    }

    public void setTrainingDuration(Integer trainingDuration) {
        this.trainingDuration = trainingDuration;
    }

    public Trainer getTrainer() {
        return trainer;
    }

    public void setTrainer(Trainer trainer) {
        this.trainer = trainer;
    }

    public Trainee getTrainee() {
        return trainee;
    }

    public void setTrainee(Trainee trainee) {
        this.trainee = trainee;
    }
}
