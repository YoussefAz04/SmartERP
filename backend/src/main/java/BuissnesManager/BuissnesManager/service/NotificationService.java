package BuissnesManager.BuissnesManager.service;

import BuissnesManager.BuissnesManager.entity.*;
import BuissnesManager.BuissnesManager.repository.NotificationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class NotificationService {

    private static final Logger logger = LoggerFactory.getLogger(NotificationService.class);

    @Autowired
    private NotificationRepository notificationRepository;

    @Transactional
    public void createArticleAddedNotification(Article article, Entreprise entreprise) {
        if (article == null || entreprise == null) {
            return;
        }
        String articleName = article.getNom() != null ? article.getNom() : ("Article #" + article.getId());
        String message = String.format("NOUVEL ARTICLE: '%s' a été ajouté au catalogue.", articleName);
        createNotification(NotificationType.ARTICLE_AJOUTE, message, null, null, null, article, entreprise);
    }

    @Transactional
    public void createStockAddedNotification(Stock stock, Article article, Entreprise entreprise, BigDecimal quantiteAjoutee) {
        if (stock == null || article == null || entreprise == null) {
            return;
        }
        BigDecimal addedQty = quantiteAjoutee != null ? quantiteAjoutee : BigDecimal.ZERO;
        if (addedQty.compareTo(BigDecimal.ZERO) <= 0) {
            return;
        }
        String articleName = article.getNom() != null ? article.getNom() : ("Article #" + article.getId());
        String message = String.format("STOCK AJOUTÉ: +%s pour l'article '%s'.", addedQty.stripTrailingZeros().toPlainString(), articleName);
        createNotification(NotificationType.STOCK_AJOUTE, message, addedQty, stock.getSeuilMinimum(), stock, article, entreprise);
    }

    @Transactional
    public void createLowStockNotification(Stock stock, Article article, Entreprise entreprise, BigDecimal quantiteActuelle) {
        NotificationType type;
        String message;

        if (quantiteActuelle.compareTo(BigDecimal.ZERO) <= 0) {
            type = NotificationType.RUPTURE;
            message = String.format("RUPTURE DE STOCK: '%s' est épuisé! Quantité: 0", article.getNom());
        } else if (stock.getSeuilMinimum() != null && quantiteActuelle.compareTo(stock.getSeuilMinimum()) < 0) {
            type = NotificationType.FAIBLE_STOCK;
            message = String.format("STOCK FAIBLE: '%s' en dessous du seuil. Quantité: %s (Seuil: %s)",
                    article.getNom(), quantiteActuelle, stock.getSeuilMinimum());
        } else {
            return;
        }

        var existingAlert = notificationRepository.findActiveAlert(stock.getId(), type, NotificationStatus.NOUVEAU);
        if (existingAlert.isPresent()) {
            logger.info("Alert already exists for stock {}, skipping duplicate", stock.getId());
            return;
        }

        createNotification(type, message, quantiteActuelle, stock.getSeuilMinimum(), stock, article, entreprise);
        logger.info("Low stock notification created: {} - {}", type, article.getNom());
    }

    private void createNotification(
            NotificationType type,
            String message,
            BigDecimal quantiteActuelle,
            BigDecimal seuilMinimum,
            Stock stock,
            Article article,
            Entreprise entreprise
    ) {
        Notification notification = new Notification();
        notification.setType(type);
        notification.setStatut(NotificationStatus.NOUVEAU);
        notification.setMessage(message);
        notification.setQuantiteActuelle(quantiteActuelle);
        notification.setSeuilMinimum(seuilMinimum);
        notification.setStock(stock);
        notification.setArticle(article);
        notification.setEntreprise(entreprise);
        notificationRepository.save(notification);
    }

    @Transactional
    public void createStockSoldNotification(Stock stock, Article article, Entreprise entreprise, BigDecimal quantiteVendue) {
        if (stock == null || article == null || entreprise == null) {
            return;
        }
        BigDecimal soldQty = quantiteVendue != null ? quantiteVendue : BigDecimal.ZERO;
        if (soldQty.compareTo(BigDecimal.ZERO) <= 0) {
            return;
        }
        String articleName = article.getNom() != null ? article.getNom() : ("Article #" + article.getId());
        BigDecimal remaining = stock.getQuantiteDisponible() != null ? stock.getQuantiteDisponible() : BigDecimal.ZERO;
        String message = String.format("STOCK VENDU: -%s pour l'article '%s' (reste: %s).",
                soldQty.stripTrailingZeros().toPlainString(),
                articleName,
                remaining.stripTrailingZeros().toPlainString());
        createNotification(NotificationType.STOCK_VENDU, message, remaining, stock.getSeuilMinimum(), stock, article, entreprise);
    }

    @Transactional
    public void createClientAddedNotification(Client client, Entreprise entreprise) {
        if (client == null || entreprise == null) {
            return;
        }
        String clientName = client.getNom() != null ? client.getNom() : ("Client #" + client.getId());
        String message = String.format("NOUVEAU CLIENT: '%s' a été ajouté.", clientName);
        createNotification(NotificationType.CLIENT_AJOUTE, message, null, null, null, null, entreprise);
    }

    @Transactional
    public void createFournisseurAddedNotification(Fournisseur fournisseur, Entreprise entreprise) {
        if (fournisseur == null || entreprise == null) {
            return;
        }
        String fournisseurName = fournisseur.getNom() != null ? fournisseur.getNom() : ("Fournisseur #" + fournisseur.getId());
        String message = String.format("NOUVEAU FOURNISSEUR: '%s' a été ajouté.", fournisseurName);
        createNotification(NotificationType.FOURNISSEUR_AJOUTE, message, null, null, null, null, entreprise);
    }

    @Transactional
    public void createArticleVenteAddedNotification(ArticleVente articleVente, Entreprise entreprise) {
        if (articleVente == null || entreprise == null) {
            return;
        }
        String name = articleVente.getNom() != null ? articleVente.getNom() : ("ArticleVente #" + articleVente.getId());
        String message = String.format("NOUVEL ARTICLE DE VENTE: '%s' a été ajouté au catalogue.", name);
        Article article = articleVente.getArticleAchat();
        createNotification(NotificationType.ARTICLE_VENTE_AJOUTE, message, null, null, null, article, entreprise);
    }

    @Transactional
    public void createCommandeAchatNotification(NotificationType type, String reference, Entreprise entreprise) {
        if (entreprise == null || type == null) {
            return;
        }
        String ref = reference != null ? reference : "-";
        String message;
        switch (type) {
            case COMMANDE_ACHAT_CREEE:
                message = String.format("COMMANDE D'ACHAT CRÉÉE: '%s'.", ref);
                break;
            case COMMANDE_ACHAT_RECUE:
                message = String.format("COMMANDE D'ACHAT REÇUE: '%s'.", ref);
                break;
            case COMMANDE_ACHAT_ANNULEE:
                message = String.format("COMMANDE D'ACHAT ANNULÉE: '%s'.", ref);
                break;
            default:
                return;
        }
        createNotification(type, message, null, null, null, null, entreprise);
    }

    @Transactional
    public void createCommandeVenteNotification(NotificationType type, String reference, Entreprise entreprise) {
        if (entreprise == null || type == null) {
            return;
        }
        String ref = reference != null ? reference : "-";
        String message;
        switch (type) {
            case COMMANDE_VENTE_CREEE:
                message = String.format("COMMANDE DE VENTE CRÉÉE: '%s'.", ref);
                break;
            case COMMANDE_VENTE_LIVREE:
                message = String.format("COMMANDE DE VENTE LIVRÉE: '%s'.", ref);
                break;
            case COMMANDE_VENTE_ANNULEE:
                message = String.format("COMMANDE DE VENTE ANNULÉE: '%s'.", ref);
                break;
            default:
                return;
        }
        createNotification(type, message, null, null, null, null, entreprise);
    }

    @Transactional
    public Notification markAsViewed(Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Notification not found: " + notificationId));
        notification.setStatut(NotificationStatus.VU);
        return notificationRepository.save(notification);
    }

    @Transactional
    public Notification dismissNotification(Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Notification not found: " + notificationId));
        notification.setStatut(NotificationStatus.IGNORE);
        return notificationRepository.save(notification);
    }

    public List<Notification> getUnreadNotifications(Long entrepriseId) {
        return notificationRepository.findByEntrepriseIdAndStatutOrderByCreatedAtDesc(
                entrepriseId, NotificationStatus.NOUVEAU);
    }

    public List<Notification> getAllNotifications(Long entrepriseId) {
        return notificationRepository.findByEntrepriseIdOrderByCreatedAtDesc(entrepriseId);
    }

    public long getUnreadCount(Long entrepriseId) {
        return notificationRepository.countByEntrepriseIdAndStatut(entrepriseId, NotificationStatus.NOUVEAU);
    }

    @Transactional
    public void markAllAsViewed(Long entrepriseId) {
        List<Notification> unread = notificationRepository.findByEntrepriseIdAndStatutOrderByCreatedAtDesc(
                entrepriseId, NotificationStatus.NOUVEAU);
        unread.forEach(n -> n.setStatut(NotificationStatus.VU));
        notificationRepository.saveAll(unread);
    }
}
