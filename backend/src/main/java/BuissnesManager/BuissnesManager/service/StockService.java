package BuissnesManager.BuissnesManager.service;

import BuissnesManager.BuissnesManager.dto.StockDto;
import BuissnesManager.BuissnesManager.entity.*;
import BuissnesManager.BuissnesManager.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class StockService {

    @Autowired
    private StockRepository stockRepository;
    @Autowired
    private ArticleRepository articleRepository;
    @Autowired
    private EntrepriseRepo entrepriseRepo;

    public Stock createStock(StockDto dto) {
        Stock stock = new Stock();
        stock.setQuantiteDisponible(dto.getQuantiteDisponible());
        stock.setQuantiteMinimale(dto.getQuantiteMinimale());
        stock.setQuantiteMaximale(dto.getQuantiteMaximale());
        stock.setEmplacement(dto.getEmplacement());
        if (dto.getArticleId() != null) {
            Article article = articleRepository.findById(dto.getArticleId())
                    .orElseThrow(() -> new RuntimeException("Article non trouvé: " + dto.getArticleId()));
            stock.setArticle(article);
        }
        if (dto.getEntrepriseId() != null) {
            Entreprise entreprise = entrepriseRepo.findById(dto.getEntrepriseId())
                    .orElseThrow(() -> new RuntimeException("Entreprise non trouvée: " + dto.getEntrepriseId()));
            stock.setEntreprise(entreprise);
        }
        return stockRepository.save(stock);
    }

    public List<Stock> getAllStocks() {
        return stockRepository.findAll();
    }

    public List<Stock> getStocksByEntreprise(Long entrepriseId) {
        return stockRepository.findByEntrepriseId(entrepriseId);
    }

    public List<Stock> getLowStockAlerts(Long entrepriseId) {
        return stockRepository.findLowStockByEntreprise(entrepriseId);
    }

    public Optional<Stock> getStockById(Long id) {
        return stockRepository.findById(id);
    }

    public Stock updateStock(Long id, StockDto dto) {
        Stock stock = stockRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Stock non trouvé avec id: " + id));
        if (dto.getQuantiteDisponible() != null) stock.setQuantiteDisponible(dto.getQuantiteDisponible());
        if (dto.getQuantiteMinimale() != null) stock.setQuantiteMinimale(dto.getQuantiteMinimale());
        if (dto.getQuantiteMaximale() != null) stock.setQuantiteMaximale(dto.getQuantiteMaximale());
        if (dto.getEmplacement() != null) stock.setEmplacement(dto.getEmplacement());
        if (dto.getArticleId() != null) {
            Article article = articleRepository.findById(dto.getArticleId())
                    .orElseThrow(() -> new RuntimeException("Article non trouvé"));
            stock.setArticle(article);
        }
        return stockRepository.save(stock);
    }

    public void deleteStock(Long id) {
        if (!stockRepository.existsById(id)) {
            throw new RuntimeException("Stock non trouvé avec id: " + id);
        }
        stockRepository.deleteById(id);
    }
}
