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
