package pl.edu.healthapp.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.edu.healthapp.model.DietEntry;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DietRepository extends JpaRepository<DietEntry, Long> {
    Optional<DietEntry> findByIdAndUserId(Long id, UUID userId);
    List<DietEntry> findAllByUserIdAndEatenAtBetween(UUID userId, OffsetDateTime start, OffsetDateTime end);
    List<DietEntry> findAllByUserIdAndEatenAtBetweenOrderByEatenAt(UUID userId, OffsetDateTime start, OffsetDateTime end);
}
