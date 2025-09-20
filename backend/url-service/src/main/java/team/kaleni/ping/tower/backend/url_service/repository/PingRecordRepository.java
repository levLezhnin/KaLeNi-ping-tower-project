package team.kaleni.ping.tower.backend.url_service.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import team.kaleni.ping.tower.backend.url_service.entity.PingRecord;
import team.kaleni.ping.tower.backend.url_service.entity.PingStatus;

import java.time.Instant;
import java.util.List;

@Repository
public interface PingRecordRepository extends JpaRepository<PingRecord, Long> {

    // ✅ Find recent ping records for a monitor (для исторической аналитики)
    @Query("SELECT p FROM PingRecord p WHERE p.monitorId = :monitorId ORDER BY p.createdAt DESC")
    List<PingRecord> findByMonitorIdOrderByCreatedAtDesc(@Param("monitorId") Long monitorId, Pageable pageable);

    // ✅ Count pings by status for statistics (исправлен field name)
    @Query("SELECT COUNT(p) FROM PingRecord p WHERE p.monitorId = :monitorId AND p.status = :status AND p.createdAt >= :since")
    Long countByMonitorIdAndStatusAndCreatedAtAfter(
            @Param("monitorId") Long monitorId,
            @Param("status") PingStatus status,
            @Param("since") Instant since);

    // ✅ Find ping records in time range for uptime calculation (исправлен field name)
    @Query("SELECT p FROM PingRecord p WHERE p.monitorId = :monitorId AND p.createdAt BETWEEN :start AND :end ORDER BY p.createdAt")
    List<PingRecord> findByMonitorIdAndCreatedAtBetween(
            @Param("monitorId") Long monitorId,
            @Param("start") Instant start,
            @Param("end") Instant end);

    // ✅ Delete old ping records (cleanup job)
    void deleteByCreatedAtBefore(Instant cutoffDate);

    // ✅ Count total pings for monitor
    long countByMonitorId(Long monitorId);

    // ПРИМЕЧАНИЕ: Этот repository теперь используется только для долгосрочной аналитики
    // Текущие статусы и короткие истории пингов хранятся в Redis
}
