package epam.xstack.dto.workload;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;

import java.time.LocalDate;

public final class TrainerWorkloadRequestDTO {
    private String username;
    private String firstName;
    private String lastName;
    private Boolean active;

    @JsonFormat(pattern = "yyyy-MM-dd")
    @JsonSerialize(using = LocalDateSerializer.class)
    private LocalDate trainingDate;

    private int trainingDuration;
    private Action action;

    public TrainerWorkloadRequestDTO(String username, String firstName, String lastName,
                                     Boolean isActive, LocalDate trainingDate, int trainingDuration, Action action) {
        this.username = username;
        this.firstName = firstName;
        this.lastName = lastName;
        this.active = isActive;
        this.trainingDate = trainingDate;
        this.trainingDuration = trainingDuration;
        this.action = action;
    }

    public TrainerWorkloadRequestDTO() {
    }

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

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
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

    public Action getAction() {
        return action;
    }

    public void setAction(Action action) {
        this.action = action;
    }
}
