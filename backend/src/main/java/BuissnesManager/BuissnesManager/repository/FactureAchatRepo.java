package BuissnesManager.BuissnesManager.repository;

import BuissnesManager.BuissnesManager.dto.FactureAchatListDto;
import BuissnesManager.BuissnesManager.entity.FactureAchat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface FactureAchatRepo extends JpaRepository<FactureAchat,Long> {

    List<FactureAchat> findByEntrepriseId(Long entrepriseId);

    @Query("""
            SELECT new BuissnesManager.BuissnesManager.dto.FactureAchatListDto(
                f.id,
                f.dateAchat,
                f.referance,
                f.montantTotal,
                f.montantHT,
                f.montantTVA,
                f.montantTTC,
                f.remise,
                f.modePaiement,
                f.statut,
                f.description,
                f.cheminFichier,
                fo.id,
                fo.nom,
                fo.prenom,
                fo.email,
                fo.telephone,
                fo.adresse,
                fo.ville,
                fo.pays,
                fo.numeroIdentificationFiscale,
                fo.registreCommerce,
                ca.id,
                ca.reference
            )
            FROM FactureAchat f
            LEFT JOIN f.fournisseur fo
            LEFT JOIN f.commandeAchat ca
            WHERE f.entreprise.id = :entrepriseId
            ORDER BY f.dateAchat DESC, f.id DESC
            """)
    List<FactureAchatListDto> findListDtoByEntrepriseId(@Param("entrepriseId") Long entrepriseId);

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
