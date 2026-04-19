package pl.edu.healthapp.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;

@Entity
@Table(name = "sleep_entries")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class SleepEntry {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private OffsetDateTime sleepStart;

    @Column(nullable = false)
    private OffsetDateTime sleepEnd;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SleepQuality quality;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    public Long durationInMinutes(){
        return ChronoUnit.MINUTES.between(sleepStart, sleepEnd);
    }

    public double durationInHours(){
        return ChronoUnit.HOURS.between(sleepStart, sleepEnd);
    }
}
