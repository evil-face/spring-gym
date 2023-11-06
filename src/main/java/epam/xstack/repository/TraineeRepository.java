package epam.xstack.repository;

import epam.xstack.model.Trainee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TraineeRepository extends JpaRepository<Trainee, Long> {
    Optional<Trainee> findByUsername(String username);
    List<Trainee> findByUsernameStartingWith(String username);
    Long deleteById(long id);
}
