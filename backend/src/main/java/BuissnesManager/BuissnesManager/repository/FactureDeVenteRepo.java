package BuissnesManager.BuissnesManager.repository;

import BuissnesManager.BuissnesManager.entity.FactureDeVente;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FactureDeVenteRepo extends JpaRepository<FactureDeVente,Long> {
    List<FactureDeVente> findByEntrepriseId(Long entrepriseId);
    Optional<FactureDeVente> findByCommandeVenteId(Long commandeVenteId);
    List<FactureDeVente> findByClientId(Long clientId);
}
