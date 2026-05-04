package BuissnesManager.BuissnesManager.repository;

import BuissnesManager.BuissnesManager.entity.FactureAchat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface FactureAchatRepo extends JpaRepository<FactureAchat,Long> {

    List<FactureAchat> findByEntrepriseId(Long entrepriseId);

    List<FactureAchat> findByFournisseurId(Long fournisseurId);

    @Modifying
    @Query("UPDATE FactureAchat f SET f.fournisseur = null WHERE f.fournisseur.id = :fournisseurId")
    void detachFournisseur(@Param("fournisseurId") Long fournisseurId);

    @Query("SELECT COALESCE(SUM(f.montantTTC), 0) FROM FactureAchat f WHERE f.entreprise.id = :entrepriseId AND f.statut = 'PAYEE'")
    Double getTotalExpensesPaid(@Param("entrepriseId") Long entrepriseId);

    @Query("SELECT COALESCE(SUM(f.montantTTC), 0) FROM FactureAchat f WHERE f.entreprise.id = :entrepriseId AND f.statut != 'PAYEE'")
    Double getAccountsPayable(@Param("entrepriseId") Long entrepriseId);

    @Modifying
    @Transactional
    @Query("DELETE FROM FactureAchat f WHERE f.entreprise.id = :entrepriseId")
    void deleteByEntrepriseId(@Param("entrepriseId") Long entrepriseId);
}
