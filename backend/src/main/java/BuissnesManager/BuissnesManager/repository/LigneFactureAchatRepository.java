package BuissnesManager.BuissnesManager.repository;

import BuissnesManager.BuissnesManager.entity.LigneFactureAchat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface LigneFactureAchatRepository extends JpaRepository<LigneFactureAchat, Long> {
    List<LigneFactureAchat> findByFactureAchatId(Long factureAchatId);

    @Transactional
    void deleteByFactureAchatId(Long factureAchatId);

    @Modifying
    @Transactional
    @Query("DELETE FROM LigneFactureAchat l WHERE l.factureAchat.entreprise.id = :entrepriseId")
    void deleteByFactureAchatEntrepriseId(@Param("entrepriseId") Long entrepriseId);
}
