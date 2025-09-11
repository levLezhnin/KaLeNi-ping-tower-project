package team.kaleni.ping.tower.backend.url_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import team.kaleni.ping.tower.backend.url_service.entity.PingStatus;
import team.kaleni.ping.tower.backend.url_service.entity.TargetUrl;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface TargetUrlRepository extends JpaRepository<TargetUrl, Long> {

    // Find by normalized URL (most important for avoiding duplicates)
    Optional<TargetUrl> findByUrl(String normalizedUrl);

    // Find by status (for dashboard queries)
    List<TargetUrl> findByLastStatus(PingStatus status);

    // Find targets that haven't been checked recently (for monitoring health)
    @Query("SELECT t FROM TargetUrl t WHERE t.lastCheckedAt < :threshold OR t.lastCheckedAt IS NULL")
    List<TargetUrl> findStaleTargets(@Param("threshold") Instant threshold);

    // Count by status (for statistics)
    long countByLastStatus(PingStatus status);
}
