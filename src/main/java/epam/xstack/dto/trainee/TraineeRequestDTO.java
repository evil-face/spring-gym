package epam.xstack.dto.trainee;

import epam.xstack.dto.trainee.validationgroup.TraineeActivateGroup;
import epam.xstack.dto.trainee.validationgroup.TraineeCreateGroup;
import epam.xstack.dto.trainee.validationgroup.TraineeUpdateGroup;
import epam.xstack.dto.trainee.validationgroup.TraineeUpdateTrainerListGroup;

import javax.validation.GroupSequence;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Past;
import java.time.LocalDate;
import java.util.List;

@GroupSequence({TraineeRequestDTO.class, TraineeCreateGroup.class, TraineeUpdateGroup.class,
    TraineeActivateGroup.class, TraineeUpdateTrainerListGroup.class})
public final class TraineeRequestDTO {
    @NotBlank(message = "First name cannot be empty", groups = {TraineeCreateGroup.class, TraineeUpdateGroup.class})
    private String firstName;

    @NotBlank(message = "Last name cannot be empty", groups = {TraineeCreateGroup.class, TraineeUpdateGroup.class})
    private String lastName;

    @Past(message = "Date of birth must be in the past", groups = {TraineeCreateGroup.class, TraineeUpdateGroup.class})
    private LocalDate dateOfBirth;

    private String address;

    @NotNull(groups = {TraineeUpdateGroup.class, TraineeActivateGroup.class})
    private Boolean active;

    @NotEmpty(groups = {TraineeUpdateTrainerListGroup.class})
    private List<String> trainers;

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

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public List<String> getTrainers() {
        return trainers;
    }

    public void setTrainers(List<String> trainers) {
        this.trainers = trainers;
    }
}
