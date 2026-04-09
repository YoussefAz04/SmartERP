package BuissnesManager.BuissnesManager.repository;

import BuissnesManager.BuissnesManager.entity.Client;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ClientRepo extends JpaRepository<Client,Long> {
    List<Client> findByEntrepriseId(Long entrepriseId);
    Optional<Client> findByIdAndEntrepriseId(Long id, Long entrepriseId);
}
