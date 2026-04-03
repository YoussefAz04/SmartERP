package BuissnesManager.BuissnesManager.service;

import BuissnesManager.BuissnesManager.entity.ArticleAchat;
import BuissnesManager.BuissnesManager.repository.ArticleAchatRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class ArticleAchatService {

    @Autowired
    private ArticleAchatRepo articleAchatRepo;

    public List<ArticleAchat> getAllArticlesAchat() {
        return articleAchatRepo.findAll();
    }

    public Optional<ArticleAchat> getArticleAchatById(Long id) {
        return articleAchatRepo.findById(id);
    }

    @Transactional
    public ArticleAchat addArticleAchat(ArticleAchat articleAchat) {
        return articleAchatRepo.save(articleAchat);
    }

    @Transactional
    public ArticleAchat updateArticleAchat(Long id, ArticleAchat updated) {
        ArticleAchat existing = articleAchatRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("ArticleAchat non trouvé avec id: " + id));
        
        if (updated.getNom() != null) existing.setNom(updated.getNom());
        if (updated.getDescription() != null) existing.setDescription(updated.getDescription());
        if (updated.getPrixUnitaire() != null) existing.setPrixUnitaire(updated.getPrixUnitaire());
        if (updated.getPrixVente() != null) existing.setPrixVente(updated.getPrixVente());
        if (updated.getReference() != null) existing.setReference(updated.getReference());
        if (updated.getActif() != null) existing.setActif(updated.getActif());
        
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