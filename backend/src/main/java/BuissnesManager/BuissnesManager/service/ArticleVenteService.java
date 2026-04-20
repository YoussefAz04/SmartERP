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

    public List<ArticleVente> getAllArticlesVente() {
        Long entrepriseId = enterpriseSecurityUtil.getContextEnterpriseId();
        if (entrepriseId == null) {
            return articleVenteRepo.findAll();
        }
        return articleVenteRepo.findByEntrepriseId(entrepriseId);
    }

    public Optional<ArticleVente> getArticleVenteById(Long id) {
        return articleVenteRepo.findById(id);
    }

    public List<ArticleVente> getArticleVenteByClient(Long clientId) {
        return articleVenteRepo.findByClientId(clientId);
    }

    public List<ArticleVente> getArticleVenteByArticleAchat(Long articleAchatId) {
        return articleVenteRepo.findByArticleAchatId(articleAchatId);
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
        if (updated.getArticleAchat() != null) existing.setArticleAchat(updated.getArticleAchat());
        if (updated.getClient() != null) existing.setClient(updated.getClient());
        return articleVenteRepo.save(existing);
    }

    @Transactional
    public void deleteArticleVente(Long id) {
        if (!articleVenteRepo.existsById(id)) {
            throw new RuntimeException("ArticleVente non trouvé avec id: " + id);
        }
        articleVenteRepo.deleteById(id);
    }
}
