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

    // ✅ Find by owner
    List<MonitorGroup> findByOwnerId(Integer ownerId);

    // ✅ Find by owner and name (for unique validation within user)
    Optional<MonitorGroup> findByOwnerIdAndName(Integer ownerId, String name);

    // ✅ Find by owner and id (for security - ensure user owns the group)
    Optional<MonitorGroup> findByIdAndOwnerId(Long id, Integer ownerId);

    // 🔥 ИСПРАВЛЕНО: убрали LEFT JOIN FETCH g.monitors - теперь monitors хранят ссылку на group, а не наоборот
    @Query("SELECT g FROM MonitorGroup g WHERE g.ownerId = :ownerId ORDER BY g.name ASC")
    List<MonitorGroup> findByOwnerIdOrderByName(@Param("ownerId") Integer ownerId);

    // ✅ Find by name and owner excluding specific ID (for update validation)
    @Query("SELECT g FROM MonitorGroup g WHERE g.ownerId = :ownerId AND g.name = :name AND g.id != :excludeId")
    Optional<MonitorGroup> findByOwnerIdAndNameExcludingId(@Param("ownerId") Integer ownerId,
                                                           @Param("name") String name,
                                                           @Param("excludeId") Long excludeId);

    // ✅ Count groups by owner
    long countByOwnerId(Integer ownerId);

    // ✅ Find all groups by IDs (для batch операций)
    List<MonitorGroup> findByIdIn(List<Long> ids);
}
