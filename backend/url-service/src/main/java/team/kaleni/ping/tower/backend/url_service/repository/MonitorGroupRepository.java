package team.kaleni.ping.tower.backend.url_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import team.kaleni.ping.tower.backend.url_service.entity.MonitorGroup;

import java.util.List;
import java.util.Optional;

@Repository
public interface MonitorGroupRepository extends JpaRepository<MonitorGroup, Long> {

    // Find by owner
    List<MonitorGroup> findByOwner(Integer owner);

    // Find by owner and name (for unique validation within user)
    Optional<MonitorGroup> findByOwnerAndName(Integer owner, String name);

    // Find by owner and id (for security - ensure user owns the group)
    Optional<MonitorGroup> findByIdAndOwner(Long id, Integer owner);

    // Find groups with their monitors (for dashboard)
    @Query("SELECT g FROM MonitorGroup g LEFT JOIN FETCH g.monitors WHERE g.owner = :owner")
    List<MonitorGroup> findByOwnerWithMonitors(@Param("owner") Integer owner);

    // Count groups by owner
    long countByOwner(Integer owner);
}
