package team.kaleni.ping.tower.backend.url_service.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.Map;

@Entity
@Table(name = "ping_records", indexes = {
        @Index(name = "idx_ping_records_monitor_time", columnList = "monitor_id, scheduled_at"),
        @Index(name = "idx_ping_records_status_time", columnList = "status, scheduled_at")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PingRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "monitor_id", nullable = false)
    private Long monitorId;

    @Column(name = "scheduled_at", nullable = false)
    private Instant scheduledAt;

    @Column(name = "actual_ping_at", nullable = false)
    private Instant actualPingAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PingStatus status;

    @Column(name = "response_code")
    private Integer responseCode;

    @Column(name = "response_time_ms")
    private Integer responseTimeMs;

    @Column(name = "error_message", length = 2000)
    private String errorMessage;

    @Column(name = "used_cached_result", nullable = false)
    @Builder.Default
    private Boolean usedCachedResult = false;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private Map<String, Object> metadata;

    @CreationTimestamp
    @Column(name = "created_at")
    private Instant createdAt;

    // Store request details for debugging
    @Enumerated(EnumType.STRING)
    private HttpMethod requestMethod;

    @Column(name = "request_url", length = 2048)
    private String requestUrl;
}
