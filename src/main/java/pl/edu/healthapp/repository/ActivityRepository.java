package pl.edu.healthapp.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.edu.healthapp.model.ActivityEntry;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ActivityRepository extends JpaRepository<ActivityEntry, Long> {
    Optional<ActivityEntry> findByIdAndUserId(Long id, UUID userId);
    List<ActivityEntry> findAllByUserIdAndStartedAtBetween(UUID userId, OffsetDateTime start, OffsetDateTime end);
    List<ActivityEntry> findAllByUserIdAndStartedAtBetweenOrderByStartedAt(UUID userId, OffsetDateTime start, OffsetDateTime end);
    boolean existsByUserIdAndStartedAtBetween(UUID userId, OffsetDateTime start, OffsetDateTime end);
}
