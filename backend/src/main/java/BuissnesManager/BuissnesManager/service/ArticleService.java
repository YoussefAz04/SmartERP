package BuissnesManager.BuissnesManager.service;

import BuissnesManager.BuissnesManager.dto.ArticleDto;
import BuissnesManager.BuissnesManager.entity.*;
import BuissnesManager.BuissnesManager.repository.*;
import BuissnesManager.BuissnesManager.security.EnterpriseSecurityUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ArticleService {

    @Autowired
    private ArticleRepository articleRepository;
    @Autowired
    private CategorieArticleRepository categorieArticleRepository;
    @Autowired
    private MesureRepository mesureRepository;
    @Autowired
    private EntrepriseRepo entrepriseRepo;
    @Autowired
    private EnterpriseSecurityUtil enterpriseSecurityUtil;

    public Article createArticle(ArticleDto dto) {
        Article article = new Article();
        article.setReference(dto.getReference());
        article.setNom(dto.getNom());
        article.setDescription(dto.getDescription());
        article.setPrixAchat(dto.getPrixAchat());
        article.setPrixVente(dto.getPrixVente());
        article.setActif(dto.isActif());
        if (dto.getCategorieArticleId() != null) {
            CategorieArticle cat = categorieArticleRepository.findById(dto.getCategorieArticleId())
                    .orElseThrow(() -> new RuntimeException("CategorieArticle non trouvée: " + dto.getCategorieArticleId()));
            article.setCategorieArticle(cat);
        }
        if (dto.getMesureId() != null) {
            Mesure mesure = mesureRepository.findById(dto.getMesureId())
                    .orElseThrow(() -> new RuntimeException("Mesure non trouvée: " + dto.getMesureId()));
            article.setMesure(mesure);
        }
        final Long resolvedEntrepriseId = dto.getEntrepriseId() != null
                ? dto.getEntrepriseId()
                : enterpriseSecurityUtil.getContextEnterpriseId();
        if (resolvedEntrepriseId != null) {
            Entreprise entreprise = entrepriseRepo.findById(resolvedEntrepriseId)
                    .orElseThrow(() -> new RuntimeException("Entreprise non trouvée: " + resolvedEntrepriseId));
            article.setEntreprise(entreprise);
        }
        return articleRepository.save(article);
    }

    public List<Article> getAllArticles() {
        Long entrepriseId = enterpriseSecurityUtil.getContextEnterpriseId();
        if (entrepriseId == null) {
            return articleRepository.findAll();
        }
        return articleRepository.findByEntrepriseId(entrepriseId);
    }

    public List<Article> getArticlesByEntreprise(Long entrepriseId) {
        return articleRepository.findByEntrepriseId(entrepriseId);
    }

    public List<Article> getArticlesByCategorie(Long categorieId) {
        return articleRepository.findByCategorieArticleId(categorieId);
    }

    public List<Article> searchArticles(Long entrepriseId, String search) {
        return articleRepository.searchByNomOrReference(entrepriseId, search);
    }

    public Optional<Article> getArticleById(Long id) {
        return articleRepository.findById(id);
    }

    public Article updateArticle(Long id, ArticleDto dto) {
        Article article = articleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Article non trouvé avec id: " + id));
        if (dto.getReference() != null) article.setReference(dto.getReference());
        if (dto.getNom() != null) article.setNom(dto.getNom());
        if (dto.getDescription() != null) article.setDescription(dto.getDescription());
        if (dto.getPrixAchat() != null) article.setPrixAchat(dto.getPrixAchat());
        if (dto.getPrixVente() != null) article.setPrixVente(dto.getPrixVente());
        article.setActif(dto.isActif());
        if (dto.getCategorieArticleId() != null) {
            CategorieArticle cat = categorieArticleRepository.findById(dto.getCategorieArticleId())
                    .orElseThrow(() -> new RuntimeException("CategorieArticle non trouvée"));
            article.setCategorieArticle(cat);
        }
        if (dto.getMesureId() != null) {
            Mesure mesure = mesureRepository.findById(dto.getMesureId())
                    .orElseThrow(() -> new RuntimeException("Mesure non trouvée"));
            article.setMesure(mesure);
        }
        return articleRepository.save(article);
    }

    public List<CategorieArticle> getAllCategories() {
        return categorieArticleRepository.findAll();
    }

    public List<Mesure> getAllMesures() {
        return mesureRepository.findAll();
    }

    public void deleteArticle(Long id) {
        if (!articleRepository.existsById(id)) {
            throw new RuntimeException("Article non trouvé avec id: " + id);
        }
        articleRepository.deleteById(id);
    }
}
