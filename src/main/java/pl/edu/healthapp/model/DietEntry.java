package pl.edu.healthapp.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;

@Entity
@Table(name = "diet_entries")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class DietEntry {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String mealName;

    @Embedded
    @Column(nullable = false)
    private Macronutrients macronutrients;

    @Column(nullable = false)
    private OffsetDateTime eatenAt;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
}
