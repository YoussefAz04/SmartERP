package BuissnesManager.BuissnesManager.repository;

import BuissnesManager.BuissnesManager.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepo extends JpaRepository<User,Long> {
    Optional<User> findByEmail(String email);
    Optional<User> findByUsername(String username);
    Optional<User> findByResetPasswordToken(String resetPasswordToken);

    // Find all users belonging to a specific enterprise
    List<User> findByEntrepriseId(Long entrepriseId);

    // Find a user by ID and enterprise (security check)
    Optional<User> findByIdAndEntrepriseId(Long id, Long entrepriseId);

    // Check existence by username
    boolean existsByUsername(String username);

    // Check existence by email
    boolean existsByEmail(String email);
}
