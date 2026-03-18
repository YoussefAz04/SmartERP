package BuissnesManager.BuissnesManager.repository;

import BuissnesManager.BuissnesManager.entity.Client;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClientRepo extends JpaRepository<Client,Long> {
}
