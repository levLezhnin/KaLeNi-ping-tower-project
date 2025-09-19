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

    // Find recent ping records for a monitor
    @Query("SELECT p FROM PingRecord p WHERE p.monitorId = :monitorId ORDER BY p.scheduledAt DESC")
    List<PingRecord> findByMonitorIdOrderByScheduledAtDesc(@Param("monitorId") Long monitorId, Pageable pageable);

    // Count pings by status for statistics
    @Query("SELECT COUNT(p) FROM PingRecord p WHERE p.monitorId = :monitorId AND p.status = :status AND p" +
            ".scheduledAt" + " >= :since")
    Long countByMonitorIdAndStatusAndScheduledAtAfter(
            @Param("monitorId") Long monitorId,
            @Param("status") PingStatus status,
            @Param("since") Instant since);

    // Find ping records in time range for uptime calculation
    @Query("SELECT p FROM PingRecord p WHERE p.monitorId = :monitorId AND p.scheduledAt BETWEEN :start AND :end " +
            "ORDER" + " BY p.scheduledAt")
    List<PingRecord> findByMonitorIdAndScheduledAtBetween(
            @Param("monitorId") Long monitorId,
            @Param("start") Instant start, @Param("end") Instant end);
}
