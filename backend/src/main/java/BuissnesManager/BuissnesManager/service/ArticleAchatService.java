package BuissnesManager.BuissnesManager.service;

import BuissnesManager.BuissnesManager.entity.ArticleAchat;
import BuissnesManager.BuissnesManager.entity.CategorieFournisseur;
import BuissnesManager.BuissnesManager.entity.Fournisseur;
import BuissnesManager.BuissnesManager.repository.ArticleAchatRepo;
import BuissnesManager.BuissnesManager.repository.CategorieFournisseurRepo;
import BuissnesManager.BuissnesManager.repository.FournisseurRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class ArticleAchatService {

    @Autowired
    private ArticleAchatRepo articleAchatRepo;

    @Autowired
    private FournisseurRepo fournisseurRepo;

    @Autowired
    private CategorieFournisseurRepo categorieFournisseurRepo;

    public List<ArticleAchat> getAllArticlesAchat() {
        return articleAchatRepo.findAll();
    }

    public Optional<ArticleAchat> getArticleAchatById(Long id) {
        return articleAchatRepo.findById(id);
    }

    public List<ArticleAchat> getArticleAchatByFournisseur(Long fournisseurId) {
        return articleAchatRepo.findByFournisseurId(fournisseurId);
    }

    public List<ArticleAchat> getArticleAchatByCategorie(Long categorieId) {
        return articleAchatRepo.findByCategorieFournisseurId(categorieId);
    }

    @Transactional
    public ArticleAchat addArticleAchat(ArticleAchat articleAchat, Long fournisseurId) {
        if (fournisseurId != null) {
            Fournisseur fournisseur = fournisseurRepo.findById(fournisseurId)
                    .orElseThrow(() -> new RuntimeException("Fournisseur non trouvé avec id: " + fournisseurId));
            articleAchat.setFournisseur(fournisseur);
            if (articleAchat.getCategorieFournisseur() == null && fournisseur.getCategorieFournisseur() != null) {
                articleAchat.setCategorieFournisseur(fournisseur.getCategorieFournisseur());
            }
        }
        return articleAchatRepo.save(articleAchat);
    }

    @Transactional
    public ArticleAchat updateArticleAchat(Long id, ArticleAchat updated) {
        ArticleAchat existing = articleAchatRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("ArticleAchat non trouvé avec id: " + id));
        if (updated.getNom() != null) existing.setNom(updated.getNom());
        if (updated.getDescription() != null) existing.setDescription(updated.getDescription());
        if (updated.getPrixUnitaire() != null) existing.setPrixUnitaire(updated.getPrixUnitaire());
        if (updated.getQuantite() != null) existing.setQuantite(updated.getQuantite());
        if (updated.getUnite() != null) existing.setUnite(updated.getUnite());
        if (updated.getSku() != null) existing.setSku(updated.getSku());
        if (updated.getFournisseur() != null) existing.setFournisseur(updated.getFournisseur());
        if (updated.getCategorieFournisseur() != null) existing.setCategorieFournisseur(updated.getCategorieFournisseur());
        return articleAchatRepo.save(existing);
    }

    @Transactional
    public void deleteArticleAchat(Long id) {
        if (!articleAchatRepo.existsById(id)) {
            throw new RuntimeException("ArticleAchat non trouvé avec id: " + id);
        }
        articleAchatRepo.deleteById(id);
    }
}
