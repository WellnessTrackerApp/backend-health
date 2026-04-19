package pl.edu.healthapp.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;

@Entity
@Table(name = "goals")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class HealthGoal {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private HealthGoalType healthGoalType;

    @Column(nullable = false)
    private double target;

    @Column(nullable = false)
    private OffsetDateTime createdAt;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
}
