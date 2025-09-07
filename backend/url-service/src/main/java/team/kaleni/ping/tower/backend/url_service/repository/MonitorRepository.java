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
    List<Monitor> findByOwner(Integer owner);

    // Find by owner and enabled status
    List<Monitor> findByOwnerAndEnabled(Integer owner, Boolean enabled);

    // Find enabled monitors (for ping service)
    List<Monitor> findByEnabledTrue();

    // Find by target (to check for duplicates and optimization)
    List<Monitor> findByTarget(TargetUrl target);

    // Find by group
    List<Monitor> findByGroup(MonitorGroup group);

    // Find by owner and group
    List<Monitor> findByOwnerAndGroup(Integer owner, MonitorGroup group);

    // Find by name and owner (for unique constraint validation)
    Optional<Monitor> findByOwnerAndName(Integer owner, String name);

    // Find monitors with target information (join fetch for performance)
    @Query("SELECT m FROM Monitor m JOIN FETCH m.target WHERE m.owner = :owner")
    List<Monitor> findByOwnerWithTarget(@Param("owner") Integer owner);

    // Find monitors with group information
    @Query("SELECT m FROM Monitor m LEFT JOIN FETCH m.group WHERE m.owner = :owner")
    List<Monitor> findByOwnerWithGroup(@Param("owner") Integer owner);

    // Count monitors by owner (for plan limits)
    long countByOwner(Integer owner);

    // Count enabled monitors by owner
    long countByOwnerAndEnabledTrue(Integer owner);
}
