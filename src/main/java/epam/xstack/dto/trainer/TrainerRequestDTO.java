package epam.xstack.dto.trainer;

import epam.xstack.dto.trainer.validationgroup.TrainerActivateGroup;
import epam.xstack.dto.trainer.validationgroup.TrainerCreateGroup;
import epam.xstack.dto.trainer.validationgroup.TrainerUpdateGroup;

import javax.validation.GroupSequence;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@GroupSequence({TrainerRequestDTO.class, TrainerCreateGroup.class, TrainerUpdateGroup.class,
        TrainerActivateGroup.class})
public class TrainerRequestDTO {
    @NotBlank(message = "First name cannot be empty", groups = {TrainerCreateGroup.class, TrainerUpdateGroup.class})
    private String firstName;
    @NotBlank(message = "Last name cannot be empty", groups = {TrainerCreateGroup.class, TrainerUpdateGroup.class})
    private String lastName;
    @Min(value = 1, groups = {TrainerCreateGroup.class, TrainerUpdateGroup.class})
    private long specialization;
    @NotBlank(message = "Username cannot be empty", groups = {TrainerUpdateGroup.class, TrainerActivateGroup.class})
    private String username;
    @NotBlank(message = "Password cannot be empty", groups = {TrainerUpdateGroup.class, TrainerActivateGroup.class})
    private String password;
    @NotNull(groups = {TrainerUpdateGroup.class, TrainerActivateGroup.class})
    private Boolean isActive;

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

    public long getSpecialization() {
        return specialization;
    }

    public void setSpecialization(long specialization) {
        this.specialization = specialization;
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
}
