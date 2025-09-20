package team.kaleni.ping.tower.backend.url_service.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.Map;

@Entity
@Table(name = "monitors",
        uniqueConstraints = @UniqueConstraint(columnNames = {"owner_id", "name"}),
        indexes = {
                @Index(name = "idx_monitor_owner", columnList = "owner_id"),
                @Index(name = "idx_monitor_group_id", columnList = "group_id"),
                @Index(name = "idx_monitor_enabled", columnList = "enabled")
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

    // === HTTP Configuration ===
    @Column(nullable = false, length = 2048)
    private String url;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private HttpMethod method = HttpMethod.GET;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private Map<String, String> headers;

    @Column(length = 4000)
    private String requestBody;

    @Column(length = 100)
    private String contentType;

    // === Monitoring Configuration ===
    @Column(nullable = false)
    @Builder.Default
    private Integer intervalSeconds = 300;

    @Column(nullable = false)
    @Builder.Default
    private Integer timeoutMs = 10000;

    @Column(nullable = false)
    @Builder.Default
    private Boolean enabled = true;

    // === Metadata ===
    @CreationTimestamp
    private Instant createdAt;

    @UpdateTimestamp
    private Instant updatedAt;

    @Column(name = "owner_id", nullable = false)
    private Integer ownerId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id")
    private MonitorGroup group;

}

