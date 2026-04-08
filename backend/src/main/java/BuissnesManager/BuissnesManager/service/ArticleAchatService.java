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

    public List<Article> getArticleAchatByFournisseur(Long fournisseurId) {
        return articleRepository.findArticlesOrderedFromFournisseur(fournisseurId);
    }

    public List<Article> getArticleAchatByCategorie(Long categorieId) {
        return articleRepository.findByCategorieArticleId(categorieId);
    }

    @Transactional
    public Article addArticleAchat(ArticleAchat input) {
        Article article = mapToArticle(input, new Article());
        return articleRepository.save(article);
    }

    @Transactional
    public Article updateArticleAchat(Long id, ArticleAchat updated) {
        Article existing = articleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("ArticleAchat non trouvé avec id: " + id));

        mapToArticle(updated, existing);

        return articleRepository.save(existing);
    }

    private Article mapToArticle(ArticleAchat source, Article target) {
        if (source.getNom() != null) target.setNom(source.getNom());
        if (source.getDescription() != null) target.setDescription(source.getDescription());
        if (source.getReference() != null) target.setReference(source.getReference());
        if (source.getPrixUnitaire() != null) target.setPrixAchat(source.getPrixUnitaire());
        if (source.getPrixVente() != null) target.setPrixVente(source.getPrixVente());
        if (source.getActif() != null) target.setActif(source.getActif());
        return target;
    }

    @Transactional
    public void deleteArticleAchat(Long id) {
        if (!articleRepository.existsById(id)) {
            throw new RuntimeException("ArticleAchat non trouvé avec id: " + id);
        }
        articleRepository.deleteById(id);
    }
}