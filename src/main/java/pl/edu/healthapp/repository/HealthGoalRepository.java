package pl.edu.healthapp.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.edu.healthapp.model.HealthGoal;
import pl.edu.healthapp.model.HealthGoalType;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface HealthGoalRepository extends JpaRepository<HealthGoal, Long> {
    Optional<HealthGoal> findByUserIdAndHealthGoalType(UUID userId, HealthGoalType healthGoalType);
    Optional<HealthGoal> findByIdAndUserId(UUID id, Long userId);
    List<HealthGoal> findAllByUserId(UUID userId);
}
