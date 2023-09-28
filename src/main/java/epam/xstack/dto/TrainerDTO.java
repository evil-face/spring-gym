package epam.xstack.dto;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;

public class TrainerDTO {
    @NotBlank(message = "First name cannot be empty")
    private String firstName;
    @NotBlank(message = "Last name cannot be empty")
    private String lastName;
    @Min(1)
    private long specialization;

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
}
