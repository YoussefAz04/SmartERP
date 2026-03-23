package BuissnesManager.BuissnesManager.repository;

import BuissnesManager.BuissnesManager.entity.CommandeVente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface CommandeVenteRepository extends JpaRepository<CommandeVente, Long> {
    List<CommandeVente> findByEntrepriseId(Long entrepriseId);
    List<CommandeVente> findByStatut(String statut);
    List<CommandeVente> findByEntrepriseIdAndStatut(Long entrepriseId, String statut);
    List<CommandeVente> findByClientId(Long clientId);

    @Query("SELECT c FROM CommandeVente c WHERE c.entreprise.id = :entrepriseId AND c.dateCommande BETWEEN :debut AND :fin")
    List<CommandeVente> findByEntrepriseIdAndDateRange(@Param("entrepriseId") Long entrepriseId, @Param("debut") LocalDate debut, @Param("fin") LocalDate fin);
}
