package BuissnesManager.BuissnesManager.repository;

import BuissnesManager.BuissnesManager.entity.ArticleAchat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ArticleAchatRepo extends JpaRepository<ArticleAchat, Long> {
    List<ArticleAchat> findByFournisseurId(Long fournisseurId);
    List<ArticleAchat> findByCategorieFournisseurId(Long categorieId);
}
