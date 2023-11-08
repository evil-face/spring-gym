package epam.xstack.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;

import java.util.List;
import java.util.Objects;

@Entity
public final class TrainingType {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    @Column(nullable = false)
    private String trainingTypeName;
    @OneToMany(mappedBy = "specialization")
    private List<Trainer> trainerList;
    @OneToMany(mappedBy = "trainingType")
    private List<Training> trainingList;

    public TrainingType(long id, String trainingTypeName) {
        this.id = id;
        this.trainingTypeName = trainingTypeName;
    }

    public TrainingType() {
    }

    public long getId() {
        return this.id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getTrainingTypeName() {
        return trainingTypeName;
    }

    public void setTrainingTypeName(String trainingTypeName) {
        this.trainingTypeName = trainingTypeName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        TrainingType that = (TrainingType) o;
        return Objects.equals(id, that.id) && Objects.equals(trainingTypeName, that.trainingTypeName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, trainingTypeName);
    }
}
