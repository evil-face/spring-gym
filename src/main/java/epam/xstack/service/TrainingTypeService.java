package epam.xstack.service;

import epam.xstack.exception.NoSuchTrainingTypeException;
import epam.xstack.model.TrainingType;
import epam.xstack.repository.TrainingTypeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class TrainingTypeService {
    private final TrainingTypeRepository trainingTypeRepository;

    @Autowired
    public TrainingTypeService(TrainingTypeRepository trainingTypeRepository) {
        this.trainingTypeRepository = trainingTypeRepository;
    }

    public TrainingType getSpecializationIfExists(String txID, long id) {
        Optional<TrainingType> trainingTypeOpt = trainingTypeRepository.findById(id);

        if (trainingTypeOpt.isEmpty()) {
            throw new NoSuchTrainingTypeException(txID);
        }

        return trainingTypeOpt.get();
    }
}
