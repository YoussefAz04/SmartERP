package BuissnesManager.BuissnesManager.repository;

import BuissnesManager.BuissnesManager.entity.FactureVente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FactureVenteRepository extends JpaRepository<FactureVente, Long> {
    List<FactureVente> findByEntrepriseId(Long entrepriseId);
    List<FactureVente> findByClientId(Long clientId);
    List<FactureVente> findByStatut(String statut);
    List<FactureVente> findByEntrepriseIdAndStatut(Long entrepriseId, String statut);
}
