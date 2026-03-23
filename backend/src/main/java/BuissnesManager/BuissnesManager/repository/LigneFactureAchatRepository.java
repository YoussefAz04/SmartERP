package BuissnesManager.BuissnesManager.repository;

import BuissnesManager.BuissnesManager.entity.LigneFactureAchat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface LigneFactureAchatRepository extends JpaRepository<LigneFactureAchat, Long> {
    List<LigneFactureAchat> findByFactureAchatId(Long factureAchatId);

    @Transactional
    void deleteByFactureAchatId(Long factureAchatId);
}
