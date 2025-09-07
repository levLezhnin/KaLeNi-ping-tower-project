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
        @Index(name = "idx_monitor_owner", columnList = "owner", unique = true),
        @Index(name = "idx_monitor_target_id", columnList = "target_id"),
        @Index(name = "idx_monitor_group_id", columnList = "group_id", unique = false)
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
    private String name; // пользовательское имя монитора

    @Column(length = 1000)
    private String description;

    // Настройки проверки
    @Column(nullable = false)
    private Integer intervalSeconds = 300; // 5 минут по умолчанию

    @Column(nullable = false)
    private Integer timeoutMs = 10000; // 10 секунд

    @Column(nullable = false)
    private Boolean enabled = true; // todo: should check if any of other monitors are also down

//    // HTTP специфичные настройки (хранятся как JSON)
//    @JdbcTypeCode(SqlTypes.JSON)
//    @Column(columnDefinition = "jsonb")
//    private HttpSettings httpSettings;

//    // Настройки уведомлений
//    @Column(nullable = false)
//    private Boolean notificationsEnabled = true;

    // Метаданные
    @CreationTimestamp
    private Instant createdAt;

    @UpdateTimestamp
    private Instant updatedAt;

    // Связи
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    private Integer owner;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "target_id", nullable = false)
    private TargetUrl target;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id")
    private MonitorGroup group;
}

