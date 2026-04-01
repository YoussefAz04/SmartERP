package BuissnesManager.BuissnesManager.repository;

import BuissnesManager.BuissnesManager.entity.StockMouvement;
import BuissnesManager.BuissnesManager.entity.TypeMouvement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StockMouvementRepo extends JpaRepository<StockMouvement, Long> {
    List<StockMouvement> findByStockId(Long stockId);
    List<StockMouvement> findByTypeMouvement(TypeMouvement typeMouvement);
    List<StockMouvement> findByUtilisateurId(Long utilisateurId);
}
