package BuissnesManager.BuissnesManager.service;

import BuissnesManager.BuissnesManager.entity.Article;
import BuissnesManager.BuissnesManager.entity.ArticleAchat;
import BuissnesManager.BuissnesManager.entity.CategorieArticle;
import BuissnesManager.BuissnesManager.entity.Entreprise;
import BuissnesManager.BuissnesManager.entity.Mesure;
import BuissnesManager.BuissnesManager.repository.ArticleRepository;
import BuissnesManager.BuissnesManager.repository.CategorieArticleRepository;
import BuissnesManager.BuissnesManager.repository.EntrepriseRepo;
import BuissnesManager.BuissnesManager.repository.MesureRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class ArticleAchatService {

    @Autowired
    private ArticleRepository articleRepository;

    @Autowired
    private EntrepriseRepo entrepriseRepo;

    @Autowired
    private CategorieArticleRepository categorieArticleRepository;

    @Autowired
    private MesureRepository mesureRepository;

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
        if (source.getEntrepriseId() != null) {
            Entreprise entreprise = entrepriseRepo.findById(source.getEntrepriseId())
                    .orElseThrow(() -> new RuntimeException("Entreprise non trouvée: " + source.getEntrepriseId()));
            target.setEntreprise(entreprise);
        }
        if (source.getCategorieArticleId() != null) {
            CategorieArticle cat = categorieArticleRepository.findById(source.getCategorieArticleId())
                    .orElseThrow(() -> new RuntimeException("CategorieArticle non trouvée: " + source.getCategorieArticleId()));
            target.setCategorieArticle(cat);
        }
        if (source.getMesureId() != null) {
            Mesure mesure = mesureRepository.findById(source.getMesureId())
                    .orElseThrow(() -> new RuntimeException("Mesure non trouvée: " + source.getMesureId()));
            target.setMesure(mesure);
        }
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