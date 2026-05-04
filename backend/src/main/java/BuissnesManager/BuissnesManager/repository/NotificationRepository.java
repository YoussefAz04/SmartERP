package BuissnesManager.BuissnesManager.repository;

import BuissnesManager.BuissnesManager.entity.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findByEntrepriseIdAndStatutOrderByCreatedAtDesc(Long entrepriseId, NotificationStatus statut);

    List<Notification> findByEntrepriseIdOrderByCreatedAtDesc(Long entrepriseId);

    List<Notification> findByStockIdAndType(Long stockId, NotificationType type);

    long countByEntrepriseIdAndStatut(Long entrepriseId, NotificationStatus statut);

    @Query("SELECT n FROM Notification n WHERE n.stock.id = :stockId AND n.type = :type AND n.statut = :statut")
    Optional<Notification> findActiveAlert(@Param("stockId") Long stockId, @Param("type") NotificationType type, @Param("statut") NotificationStatus statut);

    @Modifying
    @Transactional
    @Query("DELETE FROM Notification n WHERE n.entreprise.id = :entrepriseId")
    void deleteByEntrepriseId(@Param("entrepriseId") Long entrepriseId);
}
