package BuissnesManager.BuissnesManager.service;

import BuissnesManager.BuissnesManager.entity.*;
import BuissnesManager.BuissnesManager.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

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

    /**
     * Increments stock for all articles in a purchase order (CommandeAchat).
     * Called when the order is marked as received (RECUE).
     */
    @Transactional
    public void incrementStockFromReception(Long commandeAchatId, String referenceCommande) {
        List<LigneCommandeAchat> lignes = ligneCommandeAchatRepository.findByCommandeAchatId(commandeAchatId);

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

            StockMouvement mouvement = new StockMouvement();
            mouvement.setTypeMouvement(TypeMouvement.ENTREE);
            mouvement.setQuantite((int) Math.round(quantite.doubleValue()));
            mouvement.setReferenceCommande(referenceCommande);
            mouvement.setMotif("Réception commande achat " + referenceCommande);
            mouvement.setDateMouvement(LocalDateTime.now());
            mouvement.setStock(stock);
            stockMouvementRepo.save(mouvement);

            logger.info("✅ Stock incremented for article {} by {}", article.getId(), quantite);
        }
    }

    /**
     * Decrements stock for all articles in a sales order (CommandeVente).
     * Called when the order is marked as delivered (LIVREE).
     */
    @Transactional
    public void decrementStockFromDelivery(Long commandeVenteId, String referenceCommande) {
        List<LigneCommandeVente> lignes = ligneCommandeVenteRepository.findByCommandeVenteId(commandeVenteId);

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

            // Prefer releasing from reservation if available, otherwise check disponible
            if (reserve.compareTo(quantite) >= 0) {
                stock.setQuantiteReserve(reserve.subtract(quantite));
                stock.setQuantiteVendue(vendue.add(quantite));
            } else if (disponible.compareTo(quantite) >= 0) {
                // Backward-compatible path: no prior reservation, decrement disponible directly
                stock.setQuantiteDisponible(disponible.subtract(quantite));
                stock.setQuantiteVendue(vendue.add(quantite));
            } else {
                throw new RuntimeException(
                        String.format("Stock insuffisant pour l'article '%s' (id: %d, disponible: %s, demandé: %s)",
                                article.getNom(), article.getId(), disponible, quantite));
            }
            stockRepository.save(stock);

            StockMouvement mouvement = new StockMouvement();
            mouvement.setTypeMouvement(TypeMouvement.SORTIE);
            mouvement.setQuantite((int) Math.round(quantite.doubleValue()));
            mouvement.setReferenceCommande(referenceCommande);
            mouvement.setMotif("Livraison commande vente " + referenceCommande);
            mouvement.setDateMouvement(LocalDateTime.now());
            mouvement.setStock(stock);
            stockMouvementRepo.save(mouvement);

            if (stock.getSeuilMinimum() != null && stock.getQuantiteDisponible().compareTo(stock.getSeuilMinimum()) < 0) {
                notificationService.createLowStockNotification(stock, article, article.getEntreprise(), stock.getQuantiteDisponible());
            }

            logger.info("✅ Stock decremented for article {} by {}", article.getId(), quantite);
        }
    }

    /**
     * Reserves stock for all articles in a sales order (CommandeVente).
     * Called when a new order is created to prevent overselling.
     */
    @Transactional
    public void reserverStockPourCommande(Long commandeVenteId, String referenceCommande) {
        List<LigneCommandeVente> lignes = ligneCommandeVenteRepository.findByCommandeVenteId(commandeVenteId);

        for (LigneCommandeVente ligne : lignes) {
            ArticleVente articleVente = ligne.getArticleVente();
            if (articleVente == null) continue;

            Article article = articleVente.getArticleAchat();
            if (article == null) continue;

            BigDecimal quantite = ligne.getQuantite() != null ? ligne.getQuantite() : BigDecimal.ZERO;
            if (quantite.compareTo(BigDecimal.ZERO) <= 0) continue;

            List<Stock> stocks = stockRepository.findByArticleAchatId(article.getId());
            if (stocks.isEmpty()) {
                throw new RuntimeException(
                        "Stock non configuré pour l'article " + article.getNom() + ". Veuillez d'abord configurer le stock.");
            }

            Stock stock = stocks.get(0);
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
            stockMouvementRepo.save(mouvement);

            logger.info("✅ Stock reserved for article {} by {}", article.getId(), quantite);
        }
    }

    /**
     * Releases stock reservation for all articles in a cancelled sales order.
     * Called when a CommandeVente is cancelled.
     */
    @Transactional
    public void annulerReservationStock(Long commandeVenteId, String referenceCommande) {
        List<LigneCommandeVente> lignes = ligneCommandeVenteRepository.findByCommandeVenteId(commandeVenteId);

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
            stockMouvementRepo.save(mouvement);

            logger.info("✅ Stock reservation released for article {} by {}", article.getId(), toRelease);
        }
    }

    private Stock findOrCreateStock(Article article) {
        List<Stock> stocks = stockRepository.findByArticleAchatId(article.getId());
        if (!stocks.isEmpty()) {
            return stocks.get(0);
        }

        Stock newStock = new Stock();
        newStock.setArticleAchat(article);
        newStock.setQuantiteDisponible(BigDecimal.ZERO);
        newStock.setQuantiteEntree(BigDecimal.ZERO);
        newStock.setQuantiteReserve(BigDecimal.ZERO);
        newStock.setQuantiteVendue(BigDecimal.ZERO);
        return stockRepository.save(newStock);
    }
}
