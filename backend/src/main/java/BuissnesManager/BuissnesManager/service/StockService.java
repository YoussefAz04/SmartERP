package BuissnesManager.BuissnesManager.service;

import BuissnesManager.BuissnesManager.dto.StockDto;
import BuissnesManager.BuissnesManager.entity.*;
import BuissnesManager.BuissnesManager.repository.*;
import BuissnesManager.BuissnesManager.security.EnterpriseSecurityUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
public class StockService {

    @Autowired
    private StockRepository stockRepository;
    @Autowired
    private ArticleRepository articleRepository;
    @Autowired
    private EnterpriseSecurityUtil enterpriseSecurityUtil;
    @Autowired
    private NotificationService notificationService;

    // ✅ Helper method to convert any number to BigDecimal
    private BigDecimal toBigDecimal(Object value) {
        if (value == null) return BigDecimal.ZERO;
        if (value instanceof BigDecimal) return (BigDecimal) value;
        if (value instanceof Integer) return new BigDecimal((Integer) value);
        if (value instanceof Long) return new BigDecimal((Long) value);
        if (value instanceof Double) return new BigDecimal((Double) value);
        return new BigDecimal(value.toString());
    }

    public Stock createStock(StockDto dto) {
        Stock stock = new Stock();
        stock.setQuantiteEntree(toBigDecimal(dto.getQuantiteEntree()));
        stock.setQuantiteDisponible(toBigDecimal(dto.getQuantiteDisponible()));
        stock.setQuantiteReserve(toBigDecimal(dto.getQuantiteReserve()));
        stock.setQuantiteVendue(toBigDecimal(dto.getQuantiteVendue()));
        stock.setLocalisation(dto.getLocalisation());
        stock.setDateDernierInventaire(dto.getDateDernierInventaire());
        if (dto.getArticleAchatId() != null) {
            Article articleAchat = articleRepository.findById(dto.getArticleAchatId())
                    .orElseThrow(() -> new RuntimeException("ArticleAchat non trouvé: " + dto.getArticleAchatId()));
            if (articleAchat.getEntreprise() == null) {
                throw new RuntimeException("Impossible de créer un stock: l'article n'est lié à aucune entreprise");
            }
            stock.setArticleAchat(articleAchat);
            stock.setEntreprise(articleAchat.getEntreprise());
        }
        Stock saved = stockRepository.save(stock);
        if (saved.getArticleAchat() != null && saved.getEntreprise() != null) {
            notificationService.createStockAddedNotification(
                    saved,
                    saved.getArticleAchat(),
                    saved.getEntreprise(),
                    saved.getQuantiteDisponible()
            );
        }
        return saved;
    }

    public List<Stock> getAllStocks() {
        Long entrepriseId = enterpriseSecurityUtil.getContextEnterpriseId();
        if (entrepriseId == null) {
            return stockRepository.findAll();
        }
        return stockRepository.findByEntrepriseId(entrepriseId);
    }

    public List<Stock> getStocksByArticleAchat(Long articleAchatId) {
        return stockRepository.findByArticleAchatId(articleAchatId);
    }

    public Optional<Stock> getStockById(Long id) {
        return stockRepository.findById(id);
    }

    public Stock updateStock(Long id, StockDto dto) {
        Stock stock = stockRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Stock non trouvé avec id: " + id));
        BigDecimal previousDisponible = stock.getQuantiteDisponible() != null ? stock.getQuantiteDisponible() : BigDecimal.ZERO;
        if (dto.getQuantiteEntree() != null) stock.setQuantiteEntree(toBigDecimal(dto.getQuantiteEntree()));
        if (dto.getQuantiteDisponible() != null) stock.setQuantiteDisponible(toBigDecimal(dto.getQuantiteDisponible()));
        if (dto.getQuantiteReserve() != null) stock.setQuantiteReserve(toBigDecimal(dto.getQuantiteReserve()));
        if (dto.getQuantiteVendue() != null) stock.setQuantiteVendue(toBigDecimal(dto.getQuantiteVendue()));
        if (dto.getLocalisation() != null) stock.setLocalisation(dto.getLocalisation());
        if (dto.getDateDernierInventaire() != null) stock.setDateDernierInventaire(dto.getDateDernierInventaire());
        if (dto.getArticleAchatId() != null) {
            Article articleAchat = articleRepository.findById(dto.getArticleAchatId())
                    .orElseThrow(() -> new RuntimeException("ArticleAchat non trouvé"));
            if (articleAchat.getEntreprise() == null) {
                throw new RuntimeException("Impossible de mettre à jour le stock: l'article n'est lié à aucune entreprise");
            }
            stock.setArticleAchat(articleAchat);
            stock.setEntreprise(articleAchat.getEntreprise());
        }
        Stock saved = stockRepository.save(stock);

        BigDecimal currentDisponible = saved.getQuantiteDisponible() != null ? saved.getQuantiteDisponible() : BigDecimal.ZERO;
        BigDecimal addedQuantity = currentDisponible.subtract(previousDisponible);
        if (addedQuantity.compareTo(BigDecimal.ZERO) > 0 && saved.getArticleAchat() != null && saved.getEntreprise() != null) {
            notificationService.createStockAddedNotification(
                    saved,
                    saved.getArticleAchat(),
                    saved.getEntreprise(),
                    addedQuantity
            );
        }
        return saved;
    }

    public void deleteStock(Long id) {
        if (!stockRepository.existsById(id)) {
            throw new RuntimeException("Stock non trouvé avec id: " + id);
        }
        stockRepository.deleteById(id);
    }
}
