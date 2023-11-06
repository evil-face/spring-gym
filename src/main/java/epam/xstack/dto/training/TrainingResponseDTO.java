package epam.xstack.dto.training;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import epam.xstack.model.Trainee;
import epam.xstack.model.Trainer;
import epam.xstack.model.TrainingType;

import java.time.LocalDate;
import java.util.Objects;

@JsonInclude(JsonInclude.Include.NON_NULL)
public final class TrainingResponseDTO {
    private String trainingName;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate trainingDate;

    private TrainingType trainingType;

    private Integer trainingDuration;

    @JsonIgnoreProperties({"id", "username", "password", "trainingList", "trainees", "active", "specialization"})
    private Trainer trainer;

    @JsonIgnoreProperties({"id", "username", "password", "trainingList", "dateOfBirth", "address",
        "trainers", "active"})
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

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        TrainingResponseDTO that = (TrainingResponseDTO) o;
        return Objects.equals(trainingName, that.trainingName)
                && Objects.equals(trainingDate, that.trainingDate)
                && Objects.equals(trainingType, that.trainingType)
                && Objects.equals(trainingDuration, that.trainingDuration)
                && Objects.equals(trainer, that.trainer)
                && Objects.equals(trainee, that.trainee);
    }

    @Override
    public int hashCode() {
        return Objects.hash(trainingName, trainingDate, trainingType, trainingDuration, trainer, trainee);
    }
}
