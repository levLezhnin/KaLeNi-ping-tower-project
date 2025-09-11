package team.kaleni.ping.tower.backend.url_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import team.kaleni.ping.tower.backend.url_service.entity.Monitor;
import team.kaleni.ping.tower.backend.url_service.entity.MonitorGroup;
import team.kaleni.ping.tower.backend.url_service.entity.TargetUrl;

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

    // Count monitors by owner (for plan limits)
    long countByOwnerId(Integer ownerId);

    // Count enabled monitors by owner
    long countByOwnerIdAndEnabledTrue(Integer ownerId);
}
