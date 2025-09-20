package team.kaleni.ping.tower.backend.url_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import team.kaleni.ping.tower.backend.url_service.entity.Monitor;
import team.kaleni.ping.tower.backend.url_service.entity.MonitorGroup;

import java.util.List;
import java.util.Optional;

@Repository
public interface MonitorRepository extends JpaRepository<Monitor, Long> {

    // ✅ Find by owner (most common query)
    List<Monitor> findByOwnerId(Integer ownerId);

    // ✅ Find by owner and enabled status
    List<Monitor> findByOwnerIdAndEnabled(Integer ownerId, Boolean enabled);

    // ✅ Find enabled monitors (for ping service - но теперь не используется, всё в Redis)
    List<Monitor> findByEnabledTrue();

    // ✅ Find by group
    List<Monitor> findByGroup(MonitorGroup group);

    // ✅ Find by owner and group
    List<Monitor> findByOwnerIdAndGroup(Integer ownerId, MonitorGroup group);

    // ✅ Find by name and owner (for unique constraint validation)
    Optional<Monitor> findByOwnerIdAndName(Integer ownerId, String name);

    // ✅ Find monitors with group information (исправлен метод)
    @Query("SELECT m FROM Monitor m LEFT JOIN FETCH m.group WHERE m.ownerId = :ownerId ORDER BY m.id ASC")
    List<Monitor> findByOwnerIdWithGroup(@Param("ownerId") Integer ownerId);

    // ✅ Find by ID and owner (for security)
    Optional<Monitor> findByIdAndOwnerId(Long id, Integer ownerId);

    // ✅ Find by URL (для проверки дубликатов)
    List<Monitor> findByUrl(String url);

    // ✅ Find by owner and URL
    Optional<Monitor> findByOwnerIdAndUrl(Integer ownerId, String url);

    // ✅ Count monitors by group
    long countByGroup(MonitorGroup group);

    // ✅ Count monitors by owner (for plan limits)
    long countByOwnerId(Integer ownerId);

    // ✅ Count enabled monitors by owner
    long countByOwnerIdAndEnabledTrue(Integer ownerId);

    // ✅ Count monitors by URL (global duplicate check)
    long countByUrl(String url);

    // ✅ Find all monitors by IDs (для batch операций)
    List<Monitor> findByIdIn(List<Long> ids);
}
