package BuissnesManager.BuissnesManager.repository;

import BuissnesManager.BuissnesManager.entity.DetailsFactureAchat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface DetaisFactureAchatRepo extends JpaRepository<DetailsFactureAchat,Long> {

    @Modifying
    @Transactional
    @Query("DELETE FROM DetailsFactureAchat d WHERE d.factureAchat.entreprise.id = :entrepriseId")
    void deleteByFactureAchatEntrepriseId(@Param("entrepriseId") Long entrepriseId);
}
