package pl.edu.healthapp.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;

@Entity
@Table(name = "activities")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class ActivityEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private OffsetDateTime startedAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ActivityType activityType;

    @Column(nullable = false)
    private int durationInMinutes;

    @Column(nullable = false)
    private double caloriesBurned;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
}
