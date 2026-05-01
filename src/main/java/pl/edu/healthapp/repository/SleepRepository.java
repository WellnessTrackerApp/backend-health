package pl.edu.healthapp.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.edu.healthapp.model.SleepEntry;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SleepRepository extends JpaRepository<SleepEntry, Long> {
    boolean existsByUserIdAndSleepStartGreaterThanEqualAndSleepEndLessThanEqual(UUID userId, OffsetDateTime start, OffsetDateTime end);
    boolean existsByUserIdAndSleepStartLessThanEqualAndSleepEndGreaterThanEqual(UUID userId, OffsetDateTime start, OffsetDateTime end);
    List<SleepEntry> findAllByUserIdAndSleepEndBetween(UUID userId, OffsetDateTime start, OffsetDateTime end);
    Optional<SleepEntry> findByIdAndUserId(Long id, UUID userId);
}
