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
    private ArticleAchatRepo articleAchatRepo;
    @Autowired
    private ArticleVenteRepo articleVenteRepo;

    public Stock createStock(StockDto dto) {
        Stock stock = new Stock();
        stock.setQuantiteEntree(dto.getQuantiteEntree());
        stock.setQuantiteDisponible(dto.getQuantiteDisponible());
        stock.setQuantiteReserve(dto.getQuantiteReserve());
        stock.setQuantiteVendue(dto.getQuantiteVendue());
        stock.setLocalisation(dto.getLocalisation());
        stock.setDateDernierInventaire(dto.getDateDernierInventaire());
        if (dto.getArticleAchatId() != null) {
            ArticleAchat articleAchat = articleAchatRepo.findById(dto.getArticleAchatId())
                    .orElseThrow(() -> new RuntimeException("ArticleAchat non trouvé: " + dto.getArticleAchatId()));
            stock.setArticleAchat(articleAchat);
        }
        if (dto.getArticleVenteId() != null) {
            ArticleVente articleVente = articleVenteRepo.findById(dto.getArticleVenteId())
                    .orElseThrow(() -> new RuntimeException("ArticleVente non trouvé: " + dto.getArticleVenteId()));
            stock.setArticleVente(articleVente);
        }
        return stockRepository.save(stock);
    }

    public List<Stock> getAllStocks() {
        return stockRepository.findAll();
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
        if (dto.getQuantiteEntree() != null) stock.setQuantiteEntree(dto.getQuantiteEntree());
        if (dto.getQuantiteDisponible() != null) stock.setQuantiteDisponible(dto.getQuantiteDisponible());
        if (dto.getQuantiteReserve() != null) stock.setQuantiteReserve(dto.getQuantiteReserve());
        if (dto.getQuantiteVendue() != null) stock.setQuantiteVendue(dto.getQuantiteVendue());
        if (dto.getLocalisation() != null) stock.setLocalisation(dto.getLocalisation());
        if (dto.getDateDernierInventaire() != null) stock.setDateDernierInventaire(dto.getDateDernierInventaire());
        if (dto.getArticleAchatId() != null) {
            ArticleAchat articleAchat = articleAchatRepo.findById(dto.getArticleAchatId())
                    .orElseThrow(() -> new RuntimeException("ArticleAchat non trouvé"));
            stock.setArticleAchat(articleAchat);
        }
        if (dto.getArticleVenteId() != null) {
            ArticleVente articleVente = articleVenteRepo.findById(dto.getArticleVenteId())
                    .orElseThrow(() -> new RuntimeException("ArticleVente non trouvé"));
            stock.setArticleVente(articleVente);
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
