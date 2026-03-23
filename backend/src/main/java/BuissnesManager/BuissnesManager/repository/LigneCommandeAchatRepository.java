package BuissnesManager.BuissnesManager.repository;

import BuissnesManager.BuissnesManager.entity.LigneCommandeAchat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface LigneCommandeAchatRepository extends JpaRepository<LigneCommandeAchat, Long> {
    List<LigneCommandeAchat> findByCommandeAchatId(Long commandeAchatId);

    @Transactional
    void deleteByCommandeAchatId(Long commandeAchatId);
}
