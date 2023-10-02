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
    @NotBlank(message = "Username cannot be empty",
        groups = {TraineeUpdateGroup.class, TraineeActivateGroup.class, TraineeUpdateTrainerListGroup.class})
    private String username;
    @NotBlank(message = "Password cannot be empty", groups = {TraineeUpdateGroup.class, TraineeActivateGroup.class})
    private String password;
    @NotBlank(message = "First name cannot be empty", groups = {TraineeCreateGroup.class, TraineeUpdateGroup.class})
    private String firstName;
    @NotBlank(message = "Last name cannot be empty", groups = {TraineeCreateGroup.class, TraineeUpdateGroup.class})
    private String lastName;
    @Past(message = "Date of birth must be in the past", groups = {TraineeCreateGroup.class, TraineeUpdateGroup.class})
    private LocalDate dateOfBirth;
    private String address;
    @NotNull(groups = {TraineeUpdateGroup.class, TraineeActivateGroup.class})
    private Boolean isActive;
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

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean active) {
        isActive = active;
    }

    public List<String> getTrainers() {
        return trainers;
    }

    public void setTrainers(List<String> trainers) {
        this.trainers = trainers;
    }
}
