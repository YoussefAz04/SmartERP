package BuissnesManager.BuissnesManager.repository;

import BuissnesManager.BuissnesManager.entity.Entreprise;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EntrepriseRepo extends JpaRepository<Entreprise,Long> {
    Optional<Entreprise> findByNom(String nom);
}
