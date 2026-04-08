package BuissnesManager.BuissnesManager.repository;

import BuissnesManager.BuissnesManager.entity.Article;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ArticleRepository extends JpaRepository<Article, Long> {
    List<Article> findByEntrepriseId(Long entrepriseId);
    List<Article> findByCategorieArticleId(Long categorieId);
    List<Article> findByActifTrue();
    List<Article> findByEntrepriseIdAndActifTrue(Long entrepriseId);

    @Query("SELECT a FROM Article a WHERE a.entreprise.id = :entrepriseId AND (LOWER(a.nom) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(a.reference) LIKE LOWER(CONCAT('%', :search, '%')))")
    List<Article> searchByNomOrReference(@Param("entrepriseId") Long entrepriseId, @Param("search") String search);

    @Query("SELECT a FROM Article a WHERE a.entreprise.id = :entrepriseId AND a.categorieArticle.id = :categorieId")
    List<Article> findByEntrepriseIdAndCategorieId(@Param("entrepriseId") Long entrepriseId, @Param("categorieId") Long categorieId);

    @Query("SELECT DISTINCT l.article FROM LigneCommandeAchat l WHERE l.commandeAchat.fournisseur.id = :fournisseurId")
    List<Article> findArticlesOrderedFromFournisseur(@Param("fournisseurId") Long fournisseurId);
}
