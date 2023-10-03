package epam.xstack.dto.trainee;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import epam.xstack.model.Trainer;

import java.time.LocalDate;
import java.util.Set;

@JsonInclude(JsonInclude.Include.NON_NULL)
public final class TraineeResponseDTO {
    private String username;
    private String firstName;
    private String lastName;
    private LocalDate dateOfBirth;
    private String address;
    private Boolean isActive;
    @JsonIgnoreProperties({"id", "password", "trainingList", "trainees", "isActive"})
    private Set<Trainer> trainers;

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

    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(LocalDate dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean active) {
        isActive = active;
    }

    public Set<Trainer> getTrainers() {
        return trainers;
    }

    public void setTrainers(Set<Trainer> trainers) {
        this.trainers = trainers;
    }
}