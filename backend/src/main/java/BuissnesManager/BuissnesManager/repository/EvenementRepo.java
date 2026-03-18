package BuissnesManager.BuissnesManager.repository;

import BuissnesManager.BuissnesManager.entity.Evenement;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EvenementRepo extends JpaRepository<Evenement,Long> {
}
