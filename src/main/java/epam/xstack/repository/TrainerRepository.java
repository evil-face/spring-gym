package epam.xstack.repository;

import epam.xstack.model.Trainer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TrainerRepository extends JpaRepository<Trainer, Long> {
    Optional<Trainer> findByUsername(String username);
    List<Trainer> findByUsernameStartingWith(String username);
}
