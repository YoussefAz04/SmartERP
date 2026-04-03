package BuissnesManager.BuissnesManager.repository;

import BuissnesManager.BuissnesManager.entity.Article;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ArticleAchatRepo extends JpaRepository<Article, Long> {
}