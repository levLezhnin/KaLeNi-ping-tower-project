package team.kaleni.ping.tower.backend.url_service.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.time.Instant;

@Entity
@Table(name = "monitors",
        uniqueConstraints = @UniqueConstraint(columnNames = {"owner_id", "name"}),
        indexes = {
                @Index(name = "idx_monitor_owner", columnList = "owner_id"),  // Fixed: was "owner"
                @Index(name = "idx_monitor_target_id", columnList = "target_id"),
                @Index(name = "idx_monitor_group_id", columnList = "group_id")
        })
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Monitor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(length = 1000)
    private String description;

    // Настройки проверки
    @Column(nullable = false)
    @Builder.Default
    private Integer intervalSeconds = 300;

    @Column(nullable = false)
    @Builder.Default
    private Integer timeoutMs = 10000;

    @Column(nullable = false)
    @Builder.Default
    private Boolean enabled = true;

    // Метаданные
    @CreationTimestamp
    private Instant createdAt;

    @UpdateTimestamp
    private Instant updatedAt;

    @Column(name = "owner_id", nullable = false)
    private Integer ownerId;  // user id

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "target_id", nullable = false)
    private TargetUrl target;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id")
    private MonitorGroup group;
}
