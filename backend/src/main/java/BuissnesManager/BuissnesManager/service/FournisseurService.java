package BuissnesManager.BuissnesManager.service;

import BuissnesManager.BuissnesManager.entity.CategorieFournisseur;
import BuissnesManager.BuissnesManager.entity.Entreprise;
import BuissnesManager.BuissnesManager.entity.Fournisseur;
import BuissnesManager.BuissnesManager.repository.ArticleRepository;
import BuissnesManager.BuissnesManager.repository.CategorieFournisseurRepo;
import BuissnesManager.BuissnesManager.repository.CommandeAchatRepository;
import BuissnesManager.BuissnesManager.repository.EntrepriseRepo;
import BuissnesManager.BuissnesManager.repository.FactureAchatRepo;
import BuissnesManager.BuissnesManager.repository.FournisseurRepo;
import BuissnesManager.BuissnesManager.security.EnterpriseSecurityUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class FournisseurService {

    @Autowired
    private FournisseurRepo fournisseurRepo;

    @Autowired
    private CategorieFournisseurRepo categorieFournisseurRepo;

    @Autowired
    private EnterpriseSecurityUtil enterpriseSecurityUtil;

    @Autowired
    private EntrepriseRepo entrepriseRepo;

    @Autowired
    private NotificationService notificationService;
    @Autowired
    private AuditLogService auditLogService;

    @Autowired
    private CommandeAchatRepository commandeAchatRepository;

    @Autowired
    private ArticleRepository articleRepository;

    @Autowired
    private FactureAchatRepo factureAchatRepo;

    
    @Transactional
    public Fournisseur addFournisseur(Fournisseur fournisseur, Long idCategorie) {
        CategorieFournisseur categorie = categorieFournisseurRepo.findById(idCategorie)
            .orElseThrow(() -> new RuntimeException("Categorie not found with id: " + idCategorie));
        fournisseur.setCategorieFournisseur(categorie);
        Long entrepriseId = enterpriseSecurityUtil.getContextEnterpriseId();
        if (entrepriseId != null && fournisseur.getEntreprise() == null) {
            Entreprise entreprise = entrepriseRepo.findById(entrepriseId)
                .orElseThrow(() -> new RuntimeException("Entreprise not found with id: " + entrepriseId));
            fournisseur.setEntreprise(entreprise);
        }
        Fournisseur saved = fournisseurRepo.save(fournisseur);
        if (saved.getEntreprise() != null) {
            notificationService.createFournisseurAddedNotification(saved, saved.getEntreprise());
        }
        auditLogService.log("FOURNISSEUR_CREATED",
                "Fournisseur créé: " + saved.getNom() + " " + saved.getPrenom() + " (id=" + saved.getId() + ")");
        return saved;
    }

    

    









    private Double computeTauxLivraison(long deliveredWithPlanned, long onTime) {
        if (deliveredWithPlanned == 0) return null;
        return Math.round((onTime * 100.0 / deliveredWithPlanned) * 10.0) / 10.0;
    }

    
    private Double computeNotationFromTaux(Double taux) {
        if (taux == null) return null;
        if (taux >= 90.0) return 5.0;
        if (taux >= 70.0) return 4.0;
        if (taux >= 50.0) return 3.0;
        if (taux >= 30.0) return 2.0;
        return 1.0;
    }

    






    private void enrichFournisseurs(List<Fournisseur> fournisseurs, Long entrepriseId) {
        List<Object[]> totalRows = commandeAchatRepository.countDeliveredWithPlannedDateByFournisseur(entrepriseId);
        List<Object[]> onTimeRows = commandeAchatRepository.countOnTimeDeliveredByFournisseur(entrepriseId);

        Map<Long, Long> totalMap = new HashMap<>();
        for (Object[] row : totalRows) {
            totalMap.put(((Number) row[0]).longValue(), ((Number) row[1]).longValue());
        }
        Map<Long, Long> onTimeMap = new HashMap<>();
        for (Object[] row : onTimeRows) {
            onTimeMap.put(((Number) row[0]).longValue(), ((Number) row[1]).longValue());
        }

        for (Fournisseur f : fournisseurs) {
            long total = totalMap.getOrDefault(f.getId(), 0L);
            long onTime = onTimeMap.getOrDefault(f.getId(), 0L);
            Double taux = computeTauxLivraison(total, onTime);
            f.setTauxLivraison(taux);
            if (taux != null) {
                f.setNotation(computeNotationFromTaux(taux));
            }
            
        }
    }

    


    private void enrichFournisseur(Fournisseur f) {
        long total = commandeAchatRepository.countDeliveredWithPlannedDateForFournisseur(f.getId());
        long onTime = commandeAchatRepository.countOnTimeDeliveredForFournisseur(f.getId());
        Double taux = computeTauxLivraison(total, onTime);
        f.setTauxLivraison(taux);
        if (taux != null) {
            f.setNotation(computeNotationFromTaux(taux));
        }
    }

    
    public List<Fournisseur> getAllFournisseurs() {
        Long entrepriseId = enterpriseSecurityUtil.getContextEnterpriseId();
        if (entrepriseId == null) {
            throw new RuntimeException("Enterprise context required");
        }
        List<Fournisseur> fournisseurs = fournisseurRepo.findByEntrepriseId(entrepriseId);
        enrichFournisseurs(fournisseurs, entrepriseId);
        return fournisseurs;
    }

    
    public List<Fournisseur> getFournisseursByEntreprise(Long entrepriseId) {
        List<Fournisseur> fournisseurs = fournisseurRepo.findByEntrepriseId(entrepriseId);
        enrichFournisseurs(fournisseurs, entrepriseId);
        return fournisseurs;
    }

    
    public Fournisseur getFournisseurById(Long id) {
        Fournisseur f = fournisseurRepo.findById(id)
            .orElseThrow(() -> new RuntimeException("Fournisseur not found with id: " + id));
        enrichFournisseur(f);
        return f;
    }

    
    @Transactional
    public void deleteFournisseur(Long id) {
        Fournisseur fournisseur = fournisseurRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Fournisseur not found with id: " + id));
        
        commandeAchatRepository.detachFournisseur(id);
        articleRepository.detachFournisseur(id);
        factureAchatRepo.detachFournisseur(id);
        fournisseurRepo.deleteById(id);
        auditLogService.log("FOURNISSEUR_DELETED",
                "Supplier deleted: " + fournisseur.getNom() + " " + fournisseur.getPrenom() + " (id=" + id + ")");
    }

    
    @Transactional
    public Fournisseur updateFournisseur(Long id, Fournisseur fournisseur) {
        Fournisseur existing = fournisseurRepo.findById(id)
            .orElseThrow(() -> new RuntimeException("Fournisseur not found with id: " + id));
        existing.setNom(fournisseur.getNom());
        existing.setPrenom(fournisseur.getPrenom());
        existing.setEmail(fournisseur.getEmail());
        existing.setTelephone(fournisseur.getTelephone());
        existing.setAdresse(fournisseur.getAdresse());
        existing.setSecteurActivite(fournisseur.getSecteurActivite());
        
        existing.setNotation(fournisseur.getNotation());
        existing.setVille(fournisseur.getVille());
        existing.setPays(fournisseur.getPays());
        existing.setCodePostal(fournisseur.getCodePostal());
        existing.setNumeroIdentificationFiscale(fournisseur.getNumeroIdentificationFiscale());
        existing.setRegistreCommerce(fournisseur.getRegistreCommerce());
        if (fournisseur.getCategorieFournisseur() != null && fournisseur.getCategorieFournisseur().getId() != null) {
            CategorieFournisseur categorie = categorieFournisseurRepo
                .findById(fournisseur.getCategorieFournisseur().getId())
                .orElseThrow(() -> new RuntimeException("Categorie not found with id: " + fournisseur.getCategorieFournisseur().getId()));
            existing.setCategorieFournisseur(categorie);
        }
        Fournisseur updated = fournisseurRepo.save(existing);
        auditLogService.log("FOURNISSEUR_UPDATED",
                "Fournisseur mis à jour: " + updated.getNom() + " " + updated.getPrenom() + " (id=" + updated.getId() + ")");
        return updated;
    }

    
    @Transactional
    public CategorieFournisseur addCategorieFournisseur(CategorieFournisseur categorie) {
        return categorieFournisseurRepo.save(categorie);
    }

    
    public List<CategorieFournisseur> getAllCategorieFournisseurs() {
        return categorieFournisseurRepo.findAll();
    }

    
    public CategorieFournisseur getCategorieFournisseurById(Long id) {
        return categorieFournisseurRepo.findById(id)
            .orElseThrow(() -> new RuntimeException("Categorie not found with id: " + id));
    }

    
    @Transactional
    public void deleteCategorieFournisseur(Long id) {
        if (!categorieFournisseurRepo.existsById(id)) {
            throw new RuntimeException("Categorie not found with id: " + id);
        }
        categorieFournisseurRepo.deleteById(id);
    }

    
    @Transactional
    public CategorieFournisseur updateCategorieFournisseur(Long id, CategorieFournisseur categorie) {
        if (!categorieFournisseurRepo.existsById(id)) {
            throw new RuntimeException("Categorie not found with id: " + id);
        }
        categorie.setId(id);
        return categorieFournisseurRepo.save(categorie);
    }
}
