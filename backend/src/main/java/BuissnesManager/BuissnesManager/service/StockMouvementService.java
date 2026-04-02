package BuissnesManager.BuissnesManager.service;

import BuissnesManager.BuissnesManager.dto.StockMouvementDto;
import BuissnesManager.BuissnesManager.entity.*;
import BuissnesManager.BuissnesManager.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class StockMouvementService {

    @Autowired
    private StockMouvementRepo stockMouvementRepo;

    @Autowired
    private StockRepository stockRepository;

    @Autowired
    private UserRepo userRepo;

    public List<StockMouvement> getAllMouvements() {
        return stockMouvementRepo.findAll();
    }

    public Optional<StockMouvement> getMouvementById(Long id) {
        return stockMouvementRepo.findById(id);
    }

    public List<StockMouvement> getMouvementsByStock(Long stockId) {
        return stockMouvementRepo.findByStockId(stockId);
    }

    public List<StockMouvement> getMouvementsByType(TypeMouvement type) {
        return stockMouvementRepo.findByTypeMouvement(type);
    }

    public List<StockMouvement> getMouvementsByUser(Long utilisateurId) {
        return stockMouvementRepo.findByUtilisateurId(utilisateurId);
    }

    public StockMouvement createMouvement(StockMouvementDto dto) {
        StockMouvement mouvement = new StockMouvement();
        mouvement.setTypeMouvement(dto.getTypeMouvement());
        mouvement.setQuantite(dto.getQuantite());
        mouvement.setReferenceCommande(dto.getReferenceCommande());
        mouvement.setMotif(dto.getMotif());
        mouvement.setDateMouvement(dto.getDateMouvement() != null ? dto.getDateMouvement() : LocalDateTime.now());

        if (dto.getStockId() != null) {
            Stock stock = stockRepository.findById(dto.getStockId())
                    .orElseThrow(() -> new RuntimeException("Stock non trouvé: " + dto.getStockId()));
            mouvement.setStock(stock);
        }

        if (dto.getUtilisateurId() != null) {
            userRepo.findById(dto.getUtilisateurId()).ifPresent(mouvement::setUtilisateur);
        }

        return stockMouvementRepo.save(mouvement);
    }

    public void deleteMouvement(Long id) {
        if (!stockMouvementRepo.existsById(id)) {
            throw new RuntimeException("Mouvement non trouvé avec id: " + id);
        }
        stockMouvementRepo.deleteById(id);
    }
}
