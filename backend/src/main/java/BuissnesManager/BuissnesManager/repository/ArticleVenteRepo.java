package BuissnesManager.BuissnesManager.repository;

import BuissnesManager.BuissnesManager.entity.ArticleVente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface ArticleVenteRepo extends JpaRepository<ArticleVente, Long> {
    List<ArticleVente> findByClientId(Long clientId);
    long countByArticleAchatId(Long articleAchatId);

    @Query("SELECT av FROM ArticleVente av WHERE av.articleAchat IS NOT NULL AND av.articleAchat.entreprise IS NOT NULL AND av.articleAchat.entreprise.id = :entrepriseId")
    List<ArticleVente> findByEntrepriseId(@Param("entrepriseId") Long entrepriseId);

    @Modifying
    @Transactional
    @Query("DELETE FROM ArticleVente av WHERE av.articleAchat IS NOT NULL AND av.articleAchat.entreprise.id = :entrepriseId")
    void deleteByEntrepriseId(@Param("entrepriseId") Long entrepriseId);
}
