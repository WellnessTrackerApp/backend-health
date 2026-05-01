package pl.edu.healthapp.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.edu.healthapp.model.Event;
import pl.edu.healthapp.model.EventType;

import java.util.List;
import java.util.Optional;
import java.util.UUID;


@Repository
public interface EventRepository extends JpaRepository<Event, Long> {
    Optional<Event> findTopByUserIdOrderByCreatedAtDesc(UUID userId);
    Optional<Event> findTopByEventTypeAndUserIdOrderByCreatedAtDesc(EventType eventType, UUID userId);
    List<Event> findByEventTypeAndUserIdOrderByCreatedAtAsc(EventType eventType, UUID userId);
}
