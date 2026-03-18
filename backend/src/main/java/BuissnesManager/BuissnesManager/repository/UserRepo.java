package BuissnesManager.BuissnesManager.repository;

import BuissnesManager.BuissnesManager.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface UserRepo extends JpaRepository<User,Long> {
    public Optional<User> findByEmail(String email);
    public Optional<User> findByUsername(String email);

//    @Query("select u from User u where u.membre = Null")
//    Optional<User> getAllUserNotAffectedToMembre();


}
