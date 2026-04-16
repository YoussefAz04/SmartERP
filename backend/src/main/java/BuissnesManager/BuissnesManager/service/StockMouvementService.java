package BuissnesManager.BuissnesManager.service;

import BuissnesManager.BuissnesManager.dto.StockMouvementDto;
import BuissnesManager.BuissnesManager.entity.*;
import BuissnesManager.BuissnesManager.repository.*;
import BuissnesManager.BuissnesManager.security.EnterpriseSecurityUtil;
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

    @Autowired
    private EnterpriseSecurityUtil enterpriseSecurityUtil;

    public List<StockMouvement> getAllMouvements() {
        Long entrepriseId = enterpriseSecurityUtil.getContextEnterpriseId();
        if (entrepriseId == null) {
            return stockMouvementRepo.findAll();
        }
        return stockMouvementRepo.findByEntrepriseId(entrepriseId);
    }

    public Optional<StockMouvement> getMouvementById(Long id) {
        return stockMouvementRepo.findById(id);
    }

    public List<StockMouvement> getMouvementsByStock(Long stockId) {
        Long entrepriseId = enterpriseSecurityUtil.getContextEnterpriseId();
        if (entrepriseId == null) {
            return stockMouvementRepo.findByStockId(stockId);
        }
        return stockMouvementRepo.findByStockIdAndEntrepriseId(stockId, entrepriseId);
    }

    public List<StockMouvement> getMouvementsByType(TypeMouvement type) {
        Long entrepriseId = enterpriseSecurityUtil.getContextEnterpriseId();
        if (entrepriseId == null) {
            return stockMouvementRepo.findByTypeMouvement(type);
        }
        return stockMouvementRepo.findByTypeMouvementAndEntrepriseId(type, entrepriseId);
    }

    public List<StockMouvement> getMouvementsByUser(Long utilisateurId) {
        Long entrepriseId = enterpriseSecurityUtil.getContextEnterpriseId();
        if (entrepriseId == null) {
            return stockMouvementRepo.findByUtilisateurId(utilisateurId);
        }
        return stockMouvementRepo.findByUtilisateurIdAndEntrepriseId(utilisateurId, entrepriseId);
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
