package epam.xstack.repository;

import epam.xstack.model.Training;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;

public final class TrainingSpecs {
    private TrainingSpecs() {
        throw new IllegalStateException("Utility class");
    }
    public static Specification<Training> traineeHasId(long id) {
        return (root, query, builder) -> builder.equal(root.get("trainee").get("id"), id);
    }

    public static Specification<Training> trainerHasId(long id) {
        return (root, query, builder) -> builder.equal(root.get("trainer").get("id"), id);
    }

    public static Specification<Training> trainerHasUsername(String trainerName) {
        return (root, query, builder) -> {
            if (trainerName == null || trainerName.isBlank()) {
                return null;
            } else {
                return builder.equal(root.get("trainer").get("username"), trainerName);
            }
        };
    }

    public static Specification<Training> traineeHasUsername(String traineeName) {
        return (root, query, builder) -> {
            if (traineeName == null || traineeName.isBlank()) {
                return null;
            } else {
                return builder.equal(root.get("trainee").get("username"), traineeName);
            }
        };
    }

    public static Specification<Training> hasTrainingType(Long trainingType) {
        return (root, query, builder) -> {
            if (trainingType == null) {
                return null;
            } else {
                return builder.equal(root.get("trainingType").get("id"), trainingType);
            }
        };
    }

    public static Specification<Training> hasPeriodFrom(LocalDate periodFrom) {
        return (root, query, builder) -> {
            if (periodFrom == null) {
                return null;
            } else {
                return builder.greaterThanOrEqualTo(root.get("trainingDate"), periodFrom);
            }
        };
    }

    public static Specification<Training> hasPeriodTo(LocalDate periodTo) {
        return (root, query, builder) -> {
            if (periodTo == null) {
                return null;
            } else {
                return builder.lessThanOrEqualTo(root.get("trainingDate"), periodTo);
            }
        };
    }
}
