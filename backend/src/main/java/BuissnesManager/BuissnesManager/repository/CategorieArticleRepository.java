package BuissnesManager.BuissnesManager.repository;

import BuissnesManager.BuissnesManager.entity.CategorieArticle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CategorieArticleRepository extends JpaRepository<CategorieArticle, Long> {
    List<CategorieArticle> findByEntrepriseId(Long entrepriseId);
}
