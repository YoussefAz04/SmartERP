package BuissnesManager.BuissnesManager.repository;

import BuissnesManager.BuissnesManager.entity.StockMouvement;
import BuissnesManager.BuissnesManager.entity.TypeMouvement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface StockMouvementRepo extends JpaRepository<StockMouvement, Long> {
    List<StockMouvement> findByStockId(Long stockId);
    List<StockMouvement> findByTypeMouvement(TypeMouvement typeMouvement);
    List<StockMouvement> findByUtilisateurId(Long utilisateurId);

    @Query("SELECT sm FROM StockMouvement sm WHERE sm.stock.entreprise.id = :entrepriseId")
    List<StockMouvement> findByEntrepriseId(@Param("entrepriseId") Long entrepriseId);

    @Query("SELECT sm FROM StockMouvement sm WHERE sm.stock.id = :stockId AND sm.stock.entreprise.id = :entrepriseId")
    List<StockMouvement> findByStockIdAndEntrepriseId(@Param("stockId") Long stockId, @Param("entrepriseId") Long entrepriseId);

    @Query("SELECT sm FROM StockMouvement sm WHERE sm.typeMouvement = :type AND sm.stock.entreprise.id = :entrepriseId")
    List<StockMouvement> findByTypeMouvementAndEntrepriseId(@Param("type") TypeMouvement type, @Param("entrepriseId") Long entrepriseId);

    @Query("SELECT sm FROM StockMouvement sm WHERE sm.utilisateur.id = :utilisateurId AND sm.stock.entreprise.id = :entrepriseId")
    List<StockMouvement> findByUtilisateurIdAndEntrepriseId(@Param("utilisateurId") Long utilisateurId, @Param("entrepriseId") Long entrepriseId);

    @Modifying
    @Transactional
    @Query("DELETE FROM StockMouvement sm WHERE sm.stock.entreprise.id = :entrepriseId")
    void deleteByEntrepriseId(@Param("entrepriseId") Long entrepriseId);
}
