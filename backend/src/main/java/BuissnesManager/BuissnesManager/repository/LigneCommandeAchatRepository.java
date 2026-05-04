package BuissnesManager.BuissnesManager.repository;

import BuissnesManager.BuissnesManager.entity.LigneCommandeAchat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface LigneCommandeAchatRepository extends JpaRepository<LigneCommandeAchat, Long> {
    List<LigneCommandeAchat> findByCommandeAchatId(Long commandeAchatId);

    long countByArticleId(Long articleId);

    @Transactional
    void deleteByCommandeAchatId(Long commandeAchatId);

    @Modifying
    @Transactional
    @Query("DELETE FROM LigneCommandeAchat l WHERE l.commandeAchat.entreprise.id = :entrepriseId")
    void deleteByCommandeAchatEntrepriseId(@Param("entrepriseId") Long entrepriseId);
}
