package epam.xstack.dto.trainer;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import epam.xstack.model.Trainee;
import epam.xstack.model.TrainingType;

import java.util.Objects;
import java.util.Set;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class TrainerResponseDTO {
    private String username;

    private String firstName;

    private String lastName;

    private TrainingType specialization;

    private Boolean active;

    @JsonIgnoreProperties({"id", "password", "trainingList", "trainers", "active", "dateOfBirth", "address"})
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

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public Set<Trainee> getTrainees() {
        return trainees;
    }

    public void setTrainees(Set<Trainee> trainees) {
        this.trainees = trainees;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        TrainerResponseDTO that = (TrainerResponseDTO) o;
        return Objects.equals(username, that.username)
                && Objects.equals(firstName, that.firstName)
                && Objects.equals(lastName, that.lastName)
                && Objects.equals(specialization, that.specialization)
                && Objects.equals(active, that.active)
                && Objects.equals(trainees, that.trainees);
    }

    @Override
    public int hashCode() {
        return Objects.hash(username, firstName, lastName, specialization, active, trainees);
    }
}
