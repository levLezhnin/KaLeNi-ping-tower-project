package team.kaleni.pingtowerbackend.userservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import team.kaleni.pingtowerbackend.userservice.entity.User;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    Optional<User> findByTelegramChatId(Long chatId);
    List<User> findAllByUsernameStartsWithIgnoreCase(String prefix);
}
