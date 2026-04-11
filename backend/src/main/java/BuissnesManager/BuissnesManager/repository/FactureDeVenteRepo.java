package BuissnesManager.BuissnesManager.repository;

import BuissnesManager.BuissnesManager.entity.FactureDeVente;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FactureDeVenteRepo extends JpaRepository<FactureDeVente,Long> {
    List<FactureDeVente> findByEntrepriseId(Long entrepriseId);
}
