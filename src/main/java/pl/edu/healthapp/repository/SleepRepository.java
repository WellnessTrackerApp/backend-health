package pl.edu.healthapp.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.edu.healthapp.model.SleepEntry;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface SleepRepository extends JpaRepository<SleepEntry, Long> {
    boolean existsByUserIdAndSleepStartGreaterThanEqualAndSleepEndLessThanEqual(Long userId, OffsetDateTime start, OffsetDateTime end);
    boolean existsByUserIdAndSleepStartLessThanEqualAndSleepEndGreaterThanEqual(Long userId, OffsetDateTime start, OffsetDateTime end);
    List<SleepEntry> findAllByUserIdAndSleepEndBetween(Long userId, OffsetDateTime start, OffsetDateTime end);
    Optional<SleepEntry> findByIdAndUserId(Long id, Long userId);
}
