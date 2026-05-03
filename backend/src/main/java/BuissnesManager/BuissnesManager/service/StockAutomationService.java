package BuissnesManager.BuissnesManager.service;

import BuissnesManager.BuissnesManager.dto.StockAlertDto;
import BuissnesManager.BuissnesManager.entity.*;
import BuissnesManager.BuissnesManager.repository.*;
import BuissnesManager.BuissnesManager.security.EnterpriseSecurityUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class StockAutomationService {

    private static final Logger logger = LoggerFactory.getLogger(StockAutomationService.class);

    @Autowired
    private StockRepository stockRepository;

    @Autowired
    private StockMouvementRepo stockMouvementRepo;

    @Autowired
    private LigneCommandeAchatRepository ligneCommandeAchatRepository;

    @Autowired
    private LigneCommandeVenteRepository ligneCommandeVenteRepository;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private EnterpriseSecurityUtil enterpriseSecurityUtil;

    /**
     * Increments stock for all articles in a purchase order (CommandeAchat).
     * Called when the order is marked as received (RECUE).
     */
    @Transactional
    public void incrementStockFromReception(Long commandeAchatId, String referenceCommande) {
        List<LigneCommandeAchat> lignes = ligneCommandeAchatRepository.findByCommandeAchatId(commandeAchatId);
        User currentUser = enterpriseSecurityUtil.getCurrentUser().orElse(null);

        for (LigneCommandeAchat ligne : lignes) {
            Article article = ligne.getArticle();
            if (article == null) continue;

            BigDecimal quantite = ligne.getQuantite() != null ? ligne.getQuantite() : BigDecimal.ZERO;
            if (quantite.compareTo(BigDecimal.ZERO) <= 0) continue;

            Stock stock = findOrCreateStock(article);

            BigDecimal ancienneQte = stock.getQuantiteDisponible() != null
                    ? stock.getQuantiteDisponible() : BigDecimal.ZERO;
            BigDecimal ancienneEntree = stock.getQuantiteEntree() != null
                    ? stock.getQuantiteEntree() : BigDecimal.ZERO;

            stock.setQuantiteDisponible(ancienneQte.add(quantite));
            stock.setQuantiteEntree(ancienneEntree.add(quantite));
            stockRepository.save(stock);

            Entreprise entreprise = stock.getEntreprise() != null ? stock.getEntreprise() : article.getEntreprise();
            if (entreprise != null) {
                notificationService.createStockAddedNotification(stock, article, entreprise, quantite);
            }

            StockMouvement mouvement = new StockMouvement();
            mouvement.setTypeMouvement(TypeMouvement.ENTREE);
            mouvement.setQuantite((int) Math.round(quantite.doubleValue()));
            mouvement.setReferenceCommande(referenceCommande);
            mouvement.setMotif("Réception commande achat " + referenceCommande);
            mouvement.setDateMouvement(LocalDateTime.now());
            mouvement.setStock(stock);
            if (currentUser != null) mouvement.setUtilisateur(currentUser);
            stockMouvementRepo.save(mouvement);

            logger.info("✅ Stock incremented for article {} by {}", article.getId(), quantite);
        }
    }

    /**
     * Decrements stock for all articles in a sales order (CommandeVente).
     * Called when the order is marked as delivered (LIVREE).
     *
     * @return the list of stock alerts (rupture / faible stock) raised while
     *         decrementing — used by the controller to inform the frontend
     *         immediately after the sale.
     */
    @Transactional
    public List<StockAlertDto> decrementStockFromDelivery(Long commandeVenteId, String referenceCommande) {
        List<StockAlertDto> alerts = new ArrayList<>();
        List<LigneCommandeVente> lignes = ligneCommandeVenteRepository.findByCommandeVenteId(commandeVenteId);
        User currentUser = enterpriseSecurityUtil.getCurrentUser().orElse(null);

        for (LigneCommandeVente ligne : lignes) {
            ArticleVente articleVente = ligne.getArticleVente();
            if (articleVente == null) continue;

            Article article = articleVente.getArticleAchat();
            if (article == null) continue;

            BigDecimal quantite = ligne.getQuantite() != null ? ligne.getQuantite() : BigDecimal.ZERO;
            if (quantite.compareTo(BigDecimal.ZERO) <= 0) continue;

            List<Stock> stocks = stockRepository.findByArticleAchatId(article.getId());
            if (stocks.isEmpty()) {
                logger.warn("⚠️ No stock record found for article {}, skipping decrement", article.getId());
                continue;
            }

            Stock stock = stocks.get(0);
            BigDecimal disponible = stock.getQuantiteDisponible() != null
                    ? stock.getQuantiteDisponible() : BigDecimal.ZERO;
            BigDecimal reserve = stock.getQuantiteReserve() != null
                    ? stock.getQuantiteReserve() : BigDecimal.ZERO;
            BigDecimal vendue = stock.getQuantiteVendue() != null
                    ? stock.getQuantiteVendue() : BigDecimal.ZERO;

            // Prefer releasing from reservation first. If reservation doesn't fully
            // cover the delivery, consume all of it and take the remainder from the
            // available stock — otherwise reserved quantities can stay orphaned in
            // `quantiteReserve` after the sale is delivered.
            BigDecimal totalAccessible = reserve.add(disponible);
            if (totalAccessible.compareTo(quantite) < 0) {
                throw new RuntimeException(
                        String.format("Stock insuffisant pour l'article '%s' (id: %d, disponible: %s, réservé: %s, demandé: %s)",
                                article.getNom(), article.getId(), disponible, reserve, quantite));
            }
            if (reserve.compareTo(quantite) >= 0) {
                stock.setQuantiteReserve(reserve.subtract(quantite));
            } else {
                BigDecimal remainder = quantite.subtract(reserve);
                stock.setQuantiteReserve(BigDecimal.ZERO);
                stock.setQuantiteDisponible(disponible.subtract(remainder));
            }
            stock.setQuantiteVendue(vendue.add(quantite));
            stockRepository.save(stock);

            Entreprise entrepriseForNotif = stock.getEntreprise() != null ? stock.getEntreprise() : article.getEntreprise();
            if (entrepriseForNotif != null) {
                notificationService.createStockSoldNotification(stock, article, entrepriseForNotif, quantite);
            }

            StockMouvement mouvement = new StockMouvement();
            mouvement.setTypeMouvement(TypeMouvement.SORTIE);
            mouvement.setQuantite((int) Math.round(quantite.doubleValue()));
            mouvement.setReferenceCommande(referenceCommande);
            mouvement.setMotif("Livraison commande vente " + referenceCommande);
            mouvement.setDateMouvement(LocalDateTime.now());
            mouvement.setStock(stock);
            if (currentUser != null) mouvement.setUtilisateur(currentUser);
            stockMouvementRepo.save(mouvement);

            if (stock.getSeuilMinimum() != null && stock.getQuantiteDisponible().compareTo(stock.getSeuilMinimum()) < 0) {
                notificationService.createLowStockNotification(stock, article, article.getEntreprise(), stock.getQuantiteDisponible());
            }

            // Collect a transient alert for the controller response so the
            // frontend can show an immediate red toast when stock falls to
            // 0 (rupture) or below the configured seuilMinimum.
            BigDecimal remaining = stock.getQuantiteDisponible() != null
                    ? stock.getQuantiteDisponible() : BigDecimal.ZERO;
            String articleName = article.getNom() != null ? article.getNom() : ("Article #" + article.getId());
            if (remaining.compareTo(BigDecimal.ZERO) <= 0) {
                alerts.add(new StockAlertDto(
                        "RUPTURE",
                        article.getId(),
                        articleName,
                        remaining,
                        stock.getSeuilMinimum(),
                        String.format("RUPTURE DE STOCK : '%s' est épuisé !", articleName)));
            } else if (stock.getSeuilMinimum() != null
                    && remaining.compareTo(stock.getSeuilMinimum()) < 0) {
                alerts.add(new StockAlertDto(
                        "FAIBLE_STOCK",
                        article.getId(),
                        articleName,
                        remaining,
                        stock.getSeuilMinimum(),
                        String.format("STOCK FAIBLE : '%s' en dessous du seuil (reste : %s, seuil : %s).",
                                articleName,
                                remaining.stripTrailingZeros().toPlainString(),
                                stock.getSeuilMinimum().stripTrailingZeros().toPlainString())));
            }

            logger.info("✅ Stock decremented for article {} by {}", article.getId(), quantite);
        }
        return alerts;
    }

    /**
     * Reserves stock for all articles in a sales order (CommandeVente).
     * Called when a new order is created to prevent overselling.
     */
    @Transactional
    public void reserverStockPourCommande(Long commandeVenteId, String referenceCommande) {
        List<LigneCommandeVente> lignes = ligneCommandeVenteRepository.findByCommandeVenteId(commandeVenteId);
        User currentUser = enterpriseSecurityUtil.getCurrentUser().orElse(null);

        for (LigneCommandeVente ligne : lignes) {
            ArticleVente articleVente = ligne.getArticleVente();
            if (articleVente == null) continue;

            Article article = articleVente.getArticleAchat();
            if (article == null) continue;

            BigDecimal quantite = ligne.getQuantite() != null ? ligne.getQuantite() : BigDecimal.ZERO;
            if (quantite.compareTo(BigDecimal.ZERO) <= 0) continue;

            Stock stock = findOrCreateStock(article);
            BigDecimal disponible = stock.getQuantiteDisponible() != null
                    ? stock.getQuantiteDisponible() : BigDecimal.ZERO;
            BigDecimal reserve = stock.getQuantiteReserve() != null
                    ? stock.getQuantiteReserve() : BigDecimal.ZERO;

            if (disponible.compareTo(quantite) < 0) {
                throw new RuntimeException(
                        String.format("Stock insuffisant pour l'article '%s' (id: %d, disponible: %s, demandé: %s)",
                                article.getNom(), article.getId(), disponible, quantite));
            }

            stock.setQuantiteDisponible(disponible.subtract(quantite));
            stock.setQuantiteReserve(reserve.add(quantite));
            stockRepository.save(stock);

            StockMouvement mouvement = new StockMouvement();
            mouvement.setTypeMouvement(TypeMouvement.RESERVATION);
            mouvement.setQuantite((int) Math.round(quantite.doubleValue()));
            mouvement.setReferenceCommande(referenceCommande);
            mouvement.setMotif("Réservation commande vente " + referenceCommande);
            mouvement.setDateMouvement(LocalDateTime.now());
            mouvement.setStock(stock);
            if (currentUser != null) mouvement.setUtilisateur(currentUser);
            stockMouvementRepo.save(mouvement);

            logger.info("✅ Stock reserved for article {} by {}", article.getId(), quantite);
        }
    }

    /**
     * Reverses stock increments for all articles in a cancelled purchase order.
     * Called when a CommandeAchat in RECUE status is cancelled.
     */
    @Transactional
    public void annulerStockDepuisReception(Long commandeAchatId, String referenceCommande) {
        List<LigneCommandeAchat> lignes = ligneCommandeAchatRepository.findByCommandeAchatId(commandeAchatId);
        User currentUser = enterpriseSecurityUtil.getCurrentUser().orElse(null);

        for (LigneCommandeAchat ligne : lignes) {
            Article article = ligne.getArticle();
            if (article == null) continue;

            BigDecimal quantite = ligne.getQuantite() != null ? ligne.getQuantite() : BigDecimal.ZERO;
            if (quantite.compareTo(BigDecimal.ZERO) <= 0) continue;

            List<Stock> stocks = stockRepository.findByArticleAchatId(article.getId());
            if (stocks.isEmpty()) {
                logger.warn("⚠️ No stock record found for article {}, skipping reversal", article.getId());
                continue;
            }

            Stock stock = stocks.get(0);
            BigDecimal disponible = stock.getQuantiteDisponible() != null
                    ? stock.getQuantiteDisponible() : BigDecimal.ZERO;
            BigDecimal entree = stock.getQuantiteEntree() != null
                    ? stock.getQuantiteEntree() : BigDecimal.ZERO;

            BigDecimal toReverse = disponible.min(quantite);
            stock.setQuantiteDisponible(disponible.subtract(toReverse));
            stock.setQuantiteEntree(entree.subtract(entree.min(quantite)));
            stockRepository.save(stock);

            StockMouvement mouvement = new StockMouvement();
            mouvement.setTypeMouvement(TypeMouvement.SORTIE);
            mouvement.setQuantite(-(int) Math.round(toReverse.doubleValue()));
            mouvement.setReferenceCommande(referenceCommande);
            mouvement.setMotif("Annulation réception commande achat " + referenceCommande);
            mouvement.setDateMouvement(LocalDateTime.now());
            mouvement.setStock(stock);
            if (currentUser != null) mouvement.setUtilisateur(currentUser);
            stockMouvementRepo.save(mouvement);

            logger.info("✅ Stock reversed for article {} by {} (cancellation of reception)", article.getId(), toReverse);
        }
    }

    /**
     * Releases stock reservation for all articles in a cancelled sales order.
     * Called when a CommandeVente is cancelled.
     */
    @Transactional
    public void annulerReservationStock(Long commandeVenteId, String referenceCommande) {
        List<LigneCommandeVente> lignes = ligneCommandeVenteRepository.findByCommandeVenteId(commandeVenteId);
        User currentUser = enterpriseSecurityUtil.getCurrentUser().orElse(null);

        for (LigneCommandeVente ligne : lignes) {
            ArticleVente articleVente = ligne.getArticleVente();
            if (articleVente == null) continue;

            Article article = articleVente.getArticleAchat();
            if (article == null) continue;

            BigDecimal quantite = ligne.getQuantite() != null ? ligne.getQuantite() : BigDecimal.ZERO;
            if (quantite.compareTo(BigDecimal.ZERO) <= 0) continue;

            List<Stock> stocks = stockRepository.findByArticleAchatId(article.getId());
            if (stocks.isEmpty()) continue;

            Stock stock = stocks.get(0);
            BigDecimal disponible = stock.getQuantiteDisponible() != null
                    ? stock.getQuantiteDisponible() : BigDecimal.ZERO;
            BigDecimal reserve = stock.getQuantiteReserve() != null
                    ? stock.getQuantiteReserve() : BigDecimal.ZERO;

            BigDecimal toRelease = reserve.min(quantite);
            stock.setQuantiteReserve(reserve.subtract(toRelease));
            stock.setQuantiteDisponible(disponible.add(toRelease));
            stockRepository.save(stock);

            StockMouvement mouvement = new StockMouvement();
            mouvement.setTypeMouvement(TypeMouvement.RESERVATION);
            mouvement.setQuantite(-(int) Math.round(toRelease.doubleValue()));
            mouvement.setReferenceCommande(referenceCommande);
            mouvement.setMotif("Annulation réservation commande vente " + referenceCommande);
            mouvement.setDateMouvement(LocalDateTime.now());
            mouvement.setStock(stock);
            if (currentUser != null) mouvement.setUtilisateur(currentUser);
            stockMouvementRepo.save(mouvement);

            logger.info("✅ Stock reservation released for article {} by {}", article.getId(), toRelease);
        }
    }

    public Stock findOrCreateStock(Article article) {
        List<Stock> stocks = stockRepository.findByArticleAchatId(article.getId());
        if (!stocks.isEmpty()) {
            return stocks.get(0);
        }

        Stock newStock = new Stock();
        newStock.setArticleAchat(article);
        newStock.setEntreprise(article.getEntreprise());
        newStock.setQuantiteDisponible(BigDecimal.ZERO);
        newStock.setQuantiteEntree(BigDecimal.ZERO);
        newStock.setQuantiteReserve(BigDecimal.ZERO);
        newStock.setQuantiteVendue(BigDecimal.ZERO);
        return stockRepository.save(newStock);
    }
}
