package team.kaleni.ping.tower.backend.url_service.repository;

import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import team.kaleni.ping.tower.backend.url_service.entity.Monitor;
import team.kaleni.ping.tower.backend.url_service.entity.MonitorGroup;
import team.kaleni.ping.tower.backend.url_service.entity.TargetUrl;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface MonitorRepository extends JpaRepository<Monitor, Long> {

    // Find by owner (most common query)
    List<Monitor> findByOwnerId(Integer ownerId);

    // Find by owner and enabled status
    List<Monitor> findByOwnerIdAndEnabled(Integer ownerId, Boolean enabled);

    // Find enabled monitors (for ping service)
    List<Monitor> findByEnabledTrue();

    // Find by target (to check for duplicates and optimization)
    List<Monitor> findByTarget(TargetUrl target);

    // Find by group
    List<Monitor> findByGroup(MonitorGroup group);

    // Find by owner and group
    List<Monitor> findByOwnerIdAndGroup(Integer ownerId, MonitorGroup group);

    // Find by name and owner (for unique constraint validation)
    Optional<Monitor> findByOwnerIdAndName(Integer ownerId, String name);

    // Find monitors with target information (join fetch for performance)
    @Query("SELECT m FROM Monitor m JOIN FETCH m.target WHERE m.ownerId = :ownerId")
    List<Monitor> findByOwnerIdWithTarget(@Param("ownerId") Integer ownerId);

    // Find monitors with group information
    @Query("SELECT m FROM Monitor m LEFT JOIN FETCH m.group WHERE m.ownerId = :ownerId")
    List<Monitor> findByOwnerIDWithGroup(@Param("ownerId") Integer ownerId);

    // Find by ID and owner (for security)
    @Query("SELECT m FROM Monitor m JOIN FETCH m.target LEFT JOIN FETCH m.group WHERE m.id = :id AND m.ownerId = :ownerId")
    Optional<Monitor> findByIdAndOwnerId(@Param("id") Long id, @Param("ownerId") Integer ownerId);

    // Find monitors ready for ping with pessimistic locking to prevent race conditions
    @Query("SELECT m FROM Monitor m JOIN FETCH m.target WHERE m.enabled = true AND (m.nextPingAt <= :now OR m.nextPingAt IS NULL) ORDER BY m.nextPingAt ASC NULLS FIRST")
    List<Monitor> findMonitorsReadyForPingWithLock(@Param("now") Instant now);

    // Batch update next ping times
    @Modifying
    @Query("UPDATE Monitor m SET m.nextPingAt = :nextPingAt WHERE m.id = :id")
    void updateNextPingAt(@Param("id") Long id, @Param("nextPingAt") Instant nextPingAt);


    // Count monitors using specific target
    long countByTarget(TargetUrl target);

    // Count monitors that are in a specific group
    long countByGroup(MonitorGroup group);

    // Count monitors by owner (for plan limits)
    long countByOwnerId(Integer ownerId);

    // Count enabled monitors by owner
    long countByOwnerIdAndEnabledTrue(Integer ownerId);
}
