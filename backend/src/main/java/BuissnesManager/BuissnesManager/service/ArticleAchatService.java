package BuissnesManager.BuissnesManager.service;

import BuissnesManager.BuissnesManager.dto.ArticleAchatResponseDto;
import BuissnesManager.BuissnesManager.entity.*;
import BuissnesManager.BuissnesManager.repository.*;
import BuissnesManager.BuissnesManager.security.EnterpriseSecurityUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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

    @Autowired
    private FournisseurRepo fournisseurRepo;

    @Autowired
    private LigneCommandeAchatRepository ligneCommandeAchatRepository;

    @Autowired
    private ArticleVenteRepo articleVenteRepo;

    @Autowired
    private StockRepository stockRepository;

    @Autowired
    private EnterpriseSecurityUtil enterpriseSecurityUtil;

    public List<ArticleAchatResponseDto> getAllArticlesAchat() {
        Long entrepriseId = enterpriseSecurityUtil.getContextEnterpriseId();
        if (entrepriseId == null) {
            return articleRepository.findAllWithRelations().stream()
                    .map(this::toResponseDto)
                    .collect(Collectors.toList());
        }
        return articleRepository.findAllWithRelationsByEntrepriseId(entrepriseId).stream()
                .map(this::toResponseDto)
                .collect(Collectors.toList());
    }

    public Optional<ArticleAchatResponseDto> getArticleAchatById(Long id) {
        return articleRepository.findByIdWithRelations(id)
                .map(this::toResponseDto);
    }

    public List<ArticleAchatResponseDto> getArticleAchatByFournisseur(Long fournisseurId) {
        return articleRepository.findArticlesOrderedFromFournisseur(fournisseurId).stream()
                .map(this::toResponseDto)
                .collect(Collectors.toList());
    }

    public List<ArticleAchatResponseDto> getArticleAchatByCategorie(Long categorieId) {
        return articleRepository.findByCategorieArticleId(categorieId).stream()
                .map(this::toResponseDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public Article addArticleAchat(ArticleAchat input) {
        Article article = mapToArticle(input, new Article());
        if (article.getEntreprise() == null) {
            Long entrepriseId = enterpriseSecurityUtil.getContextEnterpriseId();
            if (entrepriseId != null) {
                Entreprise entreprise = entrepriseRepo.findById(entrepriseId)
                        .orElseThrow(() -> new RuntimeException("Entreprise non trouvée: " + entrepriseId));
                article.setEntreprise(entreprise);
            } else {
                throw new RuntimeException("Enterprise context is required to create an article.");
            }
        }
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
        if (source.getQuantite() != null) target.setQuantite(source.getQuantite());
        if (source.getSku() != null) target.setSku(source.getSku());
        if (source.getUnite() != null) target.setUnite(source.getUnite());
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
        if (source.getFournisseurId() != null) {
            Fournisseur fournisseur = fournisseurRepo.findById(source.getFournisseurId())
                    .orElseThrow(() -> new RuntimeException("Fournisseur non trouvé: " + source.getFournisseurId()));
            target.setFournisseur(fournisseur);
        }
        return target;
    }

    private ArticleAchatResponseDto toResponseDto(Article article) {
        ArticleAchatResponseDto dto = new ArticleAchatResponseDto();
        dto.setId(article.getId());
        dto.setReference(article.getReference());
        dto.setNom(article.getNom());
        dto.setDescription(article.getDescription());
        dto.setPrixUnitaire(article.getPrixAchat());
        dto.setQuantite(article.getQuantite());
        dto.setSku(article.getSku());
        dto.setUnite(article.getUnite());
        dto.setActif(article.isActif());
        dto.setCreatedAt(article.getCreatedAt());
        dto.setUpdatedAt(article.getUpdatedAt());

        if (article.getFournisseur() != null) {
            dto.setFournisseur(new ArticleAchatResponseDto.FournisseurInfo(
                    article.getFournisseur().getId(),
                    article.getFournisseur().getNom()
            ));
        }

        if (article.getCategorieArticle() != null) {
            String nom = article.getCategorieArticle().getLibelle() != null
                    ? article.getCategorieArticle().getLibelle()
                    : (article.getCategorieArticle().getCode() != null
                            ? article.getCategorieArticle().getCode()
                            : "");
            dto.setCategorieArticle(new ArticleAchatResponseDto.CategorieArticleInfo(
                    article.getCategorieArticle().getId(),
                    nom
            ));
        }

        return dto;
    }

    @Transactional
    public void deleteArticleAchat(Long id) {
        if (!articleRepository.existsById(id)) {
            throw new RuntimeException("ArticleAchat non trouvé avec id: " + id);
        }

        List<String> blockers = new ArrayList<>();

        long lcaCount = ligneCommandeAchatRepository.countByArticleId(id);
        if (lcaCount > 0) {
            blockers.add(lcaCount + " ligne(s) de commande d'achat");
        }

        long avCount = articleVenteRepo.countByArticleAchatId(id);
        if (avCount > 0) {
            blockers.add(avCount + " article(s) de vente");
        }

        long stockCount = stockRepository.countByArticleAchatId(id);
        if (stockCount > 0) {
            blockers.add(stockCount + " entrée(s) de stock");
        }

        if (!blockers.isEmpty()) {
            throw new IllegalStateException(
                "Impossible de supprimer cet article car il est référencé par : " + String.join(", ", blockers) + ". Veuillez supprimer ces enregistrements d'abord."
            );
        }

        articleRepository.deleteById(id);
    }
}