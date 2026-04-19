package pl.edu.healthapp.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.edu.healthapp.model.ActivityEntry;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ActivityRepository extends JpaRepository<ActivityEntry, Long> {
    Optional<ActivityEntry> findByIdAndUserId(Long id, Long userId);
    List<ActivityEntry> findAllByUserIdAndStartedAtBetween(Long userId, OffsetDateTime start, OffsetDateTime end);
    List<ActivityEntry> findAllByUserIdAndStartedAtBetweenOrderByStartedAt(Long userId, OffsetDateTime start, OffsetDateTime end);
    boolean existsByUserIdAndStartedAtBetween(Long userId, OffsetDateTime start, OffsetDateTime end);
}
