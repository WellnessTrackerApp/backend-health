package pl.edu.healthapp.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;

@Entity
@Table(name = "health_notifications")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
public class Notification {
    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    private Long id;

    @Setter
    @Column(nullable = false)
    private String message;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationType notificationType;

    @Setter
    @Column(nullable = false)
    private OffsetDateTime updatedAt;

    @Setter
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
}
