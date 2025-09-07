package team.kaleni.ping.tower.backend.url_service.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import team.kaleni.ping.tower.backend.url_service.service.URLNormalizer;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "monitor_targets", indexes = {
        @Index(name = "idx_target_url_url", columnList = "url", unique = true),
        @Index(name = "idx_target_url_last_status", columnList = "last_status", unique = true)
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TargetUrl {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique=true, length = 256)
    private String url; // normalized version

    // Кэшированный последний результат для быстрого доступа
    @Enumerated(EnumType.STRING)
    private PingStatus lastStatus = PingStatus.UNKNOWN;

    private Instant lastCheckedAt;
    private Integer lastResponseTimeMs;

    @CreationTimestamp
    private Instant createdAt;

    @UpdateTimestamp
    private Instant updatedAt;

    // Связи
    @OneToMany(mappedBy = "target", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Monitor> monitors = new HashSet<>();

    @PrePersist
    @PreUpdate
    private void normalizeUrl() {
        this.url = URLNormalizer.normalize(this.url);
    }
}

