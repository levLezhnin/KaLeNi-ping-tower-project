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
                @Index(name = "idx_monitor_next_ping", columnList = "next_ping_at"),
                @Index(name = "idx_monitor_url", columnList = "url") // For quick URL lookups
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

    // === HTTP Request Configuration ===
    @Column(nullable = false, length = 2048)
    private String url;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private HttpMethod method = HttpMethod.GET;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private Map<String, String> headers; // Custom headers

    @Column(length = 4000) // For request body
    private String requestBody;

    @Column(length = 100)
    @Builder.Default
    private String contentType = "application/json"; // Content-Type for POST requests

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

    // === Current Status (cached) ===
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private PingStatus lastStatus = PingStatus.UNKNOWN;

    private Instant lastCheckedAt;
    private Integer lastResponseTimeMs;
    private Integer lastResponseCode;

    @Column(length = 2000)
    private String lastErrorMessage;

    // === Scheduling ===
    @Column(name = "next_ping_at")
    private Instant nextPingAt;

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

    // === Utility Methods ===

    /**
     * Generates unique signature for caching (30-second rule)
     * Same URL+Method+Headers = same cache key
     */
    public String getCacheKey() {
        StringBuilder sb = new StringBuilder();
        sb.append(method.name()).append(":").append(url);
        if (headers != null && !headers.isEmpty()) {
            headers.entrySet().stream()
                    .sorted(Map.Entry.comparingByKey())
                    .forEach(e -> sb.append("|").append(e.getKey()).append("=").append(e.getValue()));
        }
        if (requestBody != null && !requestBody.trim().isEmpty()) {
            sb.append("|BODY:").append(requestBody.hashCode());
        }
        return sb.toString();
    }
}
