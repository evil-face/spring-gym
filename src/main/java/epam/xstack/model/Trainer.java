package epam.xstack.model;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import java.util.List;
import java.util.Objects;
import java.util.Set;

@Entity
@Table(name = "trainer")
public class Trainer extends User {
    @ManyToOne
    @JoinColumn(name = "specialization", referencedColumnName = "id")
    private TrainingType specialization;

    @OneToMany(mappedBy = "trainer")
    private List<Training> trainingList;

    @ManyToMany(mappedBy = "trainers")
    private Set<Trainee> trainees;

    public Trainer(String firstName, String lastName,
                   String username, String password, boolean isActive, TrainingType specialization) {
        super(firstName, lastName, username, password, isActive);
        this.specialization = specialization;
    }

    public Trainer() {
    }

    public TrainingType getSpecialization() {
        return specialization;
    }

    public void setSpecialization(TrainingType specialization) {
        this.specialization = specialization;
    }

    public List<Training> getTrainingList() {
        return trainingList;
    }

    public void setTrainingList(List<Training> trainingList) {
        this.trainingList = trainingList;
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
        if (!super.equals(o)) {
            return false;
        }
        Trainer trainer = (Trainer) o;
        return Objects.equals(specialization, trainer.specialization);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), specialization);
    }
}
