package BuissnesManager.BuissnesManager.repository;

import BuissnesManager.BuissnesManager.entity.ArticleVente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ArticleVenteRepo extends JpaRepository<ArticleVente, Long> {
    List<ArticleVente> findByClientId(Long clientId);
    List<ArticleVente> findByArticleAchatId(Long articleAchatId);

    @Query("SELECT av FROM ArticleVente av WHERE av.articleAchat.entreprise.id = :entrepriseId")
    List<ArticleVente> findByEntrepriseId(@Param("entrepriseId") Long entrepriseId);
}
