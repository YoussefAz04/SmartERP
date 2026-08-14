package BuissnesManager.BuissnesManager.service;

import BuissnesManager.BuissnesManager.entity.Article;
import BuissnesManager.BuissnesManager.entity.ArticleVente;
import BuissnesManager.BuissnesManager.entity.Client;
import BuissnesManager.BuissnesManager.repository.ArticleRepository;
import BuissnesManager.BuissnesManager.repository.ArticleVenteRepo;
import BuissnesManager.BuissnesManager.repository.ClientRepo;
import BuissnesManager.BuissnesManager.security.EnterpriseSecurityUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class ArticleVenteService {

    @Autowired
    private ArticleVenteRepo articleVenteRepo;

    @Autowired
    private ArticleRepository articleRepository;

    @Autowired
    private ClientRepo clientRepo;

    @Autowired
    private StockAutomationService stockAutomationService;

    @Autowired
    private EnterpriseSecurityUtil enterpriseSecurityUtil;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private AuditLogService auditLogService;

    public List<ArticleVente> getAllArticlesVente() {
        Long entrepriseId = enterpriseSecurityUtil.getContextEnterpriseId();
        if (entrepriseId == null) {
            throw new RuntimeException("Enterprise context required");
        }
        return articleVenteRepo.findByEntrepriseId(entrepriseId);
    }

    public Optional<ArticleVente> getArticleVenteById(Long id) {
        return articleVenteRepo.findById(id);
    }

    public List<ArticleVente> getArticleVenteByClient(Long clientId) {
        return articleVenteRepo.findByClientId(clientId);
    }

    @Transactional
    public ArticleVente addArticleVente(ArticleVente articleVente, Long articleAchatId, Long clientId) {
        if (articleAchatId == null) {
            throw new IllegalArgumentException("articleAchatId est requis pour créer un ArticleVente");
        }
        Article articleAchat = articleRepository.findById(articleAchatId)
                .orElseThrow(() -> new RuntimeException("Article non trouvé avec id: " + articleAchatId + ". Veuillez sélectionner un article valide."));
        articleVente.setArticleAchat(articleAchat);

        articleVente.setStock(stockAutomationService.findOrCreateStock(articleAchat));

        if (clientId != null) {
            Client client = clientRepo.findById(clientId)
                    .orElseThrow(() -> new RuntimeException("Client non trouvé avec id: " + clientId));
            articleVente.setClient(client);
        }
        try {
            ArticleVente saved = articleVenteRepo.save(articleVente);
            if (articleAchat.getEntreprise() != null) {
                notificationService.createArticleVenteAddedNotification(saved, articleAchat.getEntreprise());
            }
            auditLogService.log("ARTICLE_VENTE_CREATED",
                    "Article de vente cree: nom=" + saved.getNom()
                            + ", id=" + saved.getId()
                            + ", articleAchatId=" + articleAchatId);
            return saved;
        } catch (DataIntegrityViolationException e) {
            throw new RuntimeException("Impossible de créer l'ArticleVente: contrainte d'intégrité violée. Vérifiez que l'article avec id=" + articleAchatId + " existe.", e);
        }
    }

    @Transactional
    public ArticleVente updateArticleVente(Long id, ArticleVente updated) {
        ArticleVente existing = articleVenteRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("ArticleVente non trouvé avec id: " + id));
        if (updated.getNom() != null) existing.setNom(updated.getNom());
        if (updated.getDescription() != null) existing.setDescription(updated.getDescription());
        if (updated.getPrixUnitaire() != null) existing.setPrixUnitaire(updated.getPrixUnitaire());
        if (updated.getQuantite() != null) existing.setQuantite(updated.getQuantite());
        if (updated.getUnite() != null) existing.setUnite(updated.getUnite());
        if (updated.getSku() != null) existing.setSku(updated.getSku());
        if (updated.getMarkup() != null) existing.setMarkup(updated.getMarkup());
        if (updated.getArticleAchat() != null && updated.getArticleAchat().getId() != null) {
            Long articleAchatId = updated.getArticleAchat().getId();
            Article managedArticleAchat = articleRepository.findById(articleAchatId)
                    .orElseThrow(() -> new RuntimeException("Article achat non trouvé avec id: " + articleAchatId));
            existing.setArticleAchat(managedArticleAchat);
        }
        if (updated.getClient() != null && updated.getClient().getId() != null) {
            Long clientId = updated.getClient().getId();
            Client managedClient = clientRepo.findById(clientId)
                    .orElseThrow(() -> new RuntimeException("Client non trouvé avec id: " + clientId));
            existing.setClient(managedClient);
        }
        ArticleVente saved = articleVenteRepo.save(existing);
        auditLogService.log("ARTICLE_VENTE_UPDATED",
                "Article de vente modifie: nom=" + saved.getNom()
                        + ", id=" + saved.getId());
        return saved;
    }

    @Transactional
    public void deleteArticleVente(Long id) {
        ArticleVente existing = articleVenteRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("ArticleVente non trouvé avec id: " + id));
        articleVenteRepo.deleteById(id);
        auditLogService.log("ARTICLE_VENTE_DELETED",
                "Article de vente supprime: nom=" + existing.getNom() + ", id=" + id);
    }
}
