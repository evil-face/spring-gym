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
public final class TrainerRequestDTO {
    @NotBlank(message = "First name cannot be empty", groups = {TrainerCreateGroup.class, TrainerUpdateGroup.class})
    private String firstName;

    @NotBlank(message = "Last name cannot be empty", groups = {TrainerCreateGroup.class, TrainerUpdateGroup.class})
    private String lastName;

    @Min(value = 1, groups = {TrainerCreateGroup.class, TrainerUpdateGroup.class})
    private Long specialization;

    @NotNull(groups = {TrainerUpdateGroup.class, TrainerActivateGroup.class})
    private Boolean active;

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

    public Long getSpecialization() {
        return specialization;
    }

    public void setSpecialization(Long specialization) {
        this.specialization = specialization;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }
}
