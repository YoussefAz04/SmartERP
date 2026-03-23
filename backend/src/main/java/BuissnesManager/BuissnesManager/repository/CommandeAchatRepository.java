package BuissnesManager.BuissnesManager.repository;

import BuissnesManager.BuissnesManager.entity.CommandeAchat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface CommandeAchatRepository extends JpaRepository<CommandeAchat, Long> {
    List<CommandeAchat> findByEntrepriseId(Long entrepriseId);
    List<CommandeAchat> findByStatut(String statut);
    List<CommandeAchat> findByEntrepriseIdAndStatut(Long entrepriseId, String statut);
    List<CommandeAchat> findByFournisseurId(Long fournisseurId);

    @Query("SELECT c FROM CommandeAchat c WHERE c.entreprise.id = :entrepriseId AND c.dateCommande BETWEEN :debut AND :fin")
    List<CommandeAchat> findByEntrepriseIdAndDateRange(@Param("entrepriseId") Long entrepriseId, @Param("debut") LocalDate debut, @Param("fin") LocalDate fin);
}
