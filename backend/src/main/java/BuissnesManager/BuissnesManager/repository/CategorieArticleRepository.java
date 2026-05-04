package BuissnesManager.BuissnesManager.repository;

import BuissnesManager.BuissnesManager.entity.CategorieArticle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface CategorieArticleRepository extends JpaRepository<CategorieArticle, Long> {
    List<CategorieArticle> findByEntrepriseId(Long entrepriseId);

    @Modifying
    @Transactional
    @Query("DELETE FROM CategorieArticle c WHERE c.entreprise.id = :entrepriseId")
    void deleteByEntrepriseId(@Param("entrepriseId") Long entrepriseId);
}
