package BuissnesManager.BuissnesManager.repository;

import BuissnesManager.BuissnesManager.entity.BonReception;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BonReceptionRepository extends JpaRepository<BonReception, Long> {
    List<BonReception> findByEntrepriseId(Long entrepriseId);
    List<BonReception> findByCommandeAchatId(Long commandeAchatId);
    List<BonReception> findByStatut(String statut);
}
