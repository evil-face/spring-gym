package epam.xstack.dto.training;

import javax.validation.constraints.FutureOrPresent;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;

public final class TrainingCreateRequestDTO {
    @NotBlank(message = "Trainee username cannot be empty")
    private String traineeUsername;

    @NotBlank(message = "Trainer username cannot be empty")
    private String trainerUsername;

    @NotBlank(message = "Training name cannot be empty")
    private String trainingName;

    @NotNull
    @FutureOrPresent(message = "Training date must be today or in the future")
    private LocalDate trainingDate;

    @Min(1)
    private int trainingDuration;

    public String getTraineeUsername() {
        return traineeUsername;
    }

    public void setTraineeUsername(String traineeUsername) {
        this.traineeUsername = traineeUsername;
    }

    public String getTrainerUsername() {
        return trainerUsername;
    }

    public void setTrainerUsername(String trainerUsername) {
        this.trainerUsername = trainerUsername;
    }

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

    public int getTrainingDuration() {
        return trainingDuration;
    }

    public void setTrainingDuration(int trainingDuration) {
        this.trainingDuration = trainingDuration;
    }
}
