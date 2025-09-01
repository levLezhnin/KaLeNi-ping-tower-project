package team.kaleni.pingtowerbackend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import team.kaleni.pingtowerbackend.entity.User;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    List<User> findAllByUsernameStartsWithIgnoreCase(String prefix);
}
