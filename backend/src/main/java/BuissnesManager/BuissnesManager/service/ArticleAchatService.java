package BuissnesManager.BuissnesManager.service;

import BuissnesManager.BuissnesManager.entity.Article;
import BuissnesManager.BuissnesManager.entity.ArticleAchat;
import BuissnesManager.BuissnesManager.repository.ArticleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class ArticleAchatService {

    @Autowired
    private ArticleRepository articleRepository;

    public List<Article> getAllArticlesAchat() {
        return articleRepository.findAll();
    }

    public Optional<Article> getArticleAchatById(Long id) {
        return articleRepository.findById(id);
    }

    @Transactional
    public Article addArticleAchat(ArticleAchat input) {
        Article article = new Article();
        article.setNom(input.getNom());
        article.setDescription(input.getDescription());
        article.setReference(input.getReference());
        article.setPrixAchat(input.getPrixUnitaire());
        article.setPrixVente(input.getPrixVente());
        article.setActif(input.getActif() != null ? input.getActif() : true);
        return articleRepository.save(article);
    }

    @Transactional
    public Article updateArticleAchat(Long id, ArticleAchat updated) {
        Article existing = articleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("ArticleAchat non trouvé avec id: " + id));

        if (updated.getNom() != null) existing.setNom(updated.getNom());
        if (updated.getDescription() != null) existing.setDescription(updated.getDescription());
        if (updated.getPrixUnitaire() != null) existing.setPrixAchat(updated.getPrixUnitaire());
        if (updated.getPrixVente() != null) existing.setPrixVente(updated.getPrixVente());
        if (updated.getReference() != null) existing.setReference(updated.getReference());
        if (updated.getActif() != null) existing.setActif(updated.getActif());

        return articleRepository.save(existing);
    }

    @Transactional
    public void deleteArticleAchat(Long id) {
        if (!articleRepository.existsById(id)) {
            throw new RuntimeException("ArticleAchat non trouvé avec id: " + id);
        }
        articleRepository.deleteById(id);
    }
}