package epam.xstack.dto.trainer;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import epam.xstack.model.Trainee;
import epam.xstack.model.TrainingType;

import java.util.Set;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class TrainerResponseDTO {
    private String username;
    private String firstName;
    private String lastName;
    private TrainingType specialization;
    private Boolean isActive;
    @JsonIgnoreProperties({"id", "password", "trainingList", "trainers", "isActive", "dateOfBirth", "address"})
    private Set<Trainee> trainees;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public TrainingType getSpecialization() {
        return specialization;
    }

    public void setSpecialization(TrainingType specialization) {
        this.specialization = specialization;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean active) {
        isActive = active;
    }

    public Set<Trainee> getTrainees() {
        return trainees;
    }

    public void setTrainees(Set<Trainee> trainees) {
        this.trainees = trainees;
    }
}
