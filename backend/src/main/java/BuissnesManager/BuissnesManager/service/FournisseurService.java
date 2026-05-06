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
    private CommandeAchatRepository commandeAchatRepository;

    @Autowired
    private ArticleRepository articleRepository;

    @Autowired
    private FactureAchatRepo factureAchatRepo;

    // ✅ ADD FOURNISSEUR
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
        return saved;
    }

    // ── Delivery-stat helpers ─────────────────────────────────────────────────

    /** Returns delivery rate as a percentage (0–100, 1 decimal) or null when no non-cancelled orders exist. */
    private Double computeTauxLivraison(long total, long received) {
        if (total == 0) return null;
        return Math.round((received * 100.0 / total) * 10.0) / 10.0;
    }

    /** Maps a delivery rate to a 1–5 star notation. Returns null when taux is null. */
    private Double computeNotationFromTaux(Double taux) {
        if (taux == null) return null;
        if (taux >= 90.0) return 5.0;
        if (taux >= 70.0) return 4.0;
        if (taux >= 50.0) return 3.0;
        if (taux >= 30.0) return 2.0;
        return 1.0;
    }

    /**
     * Enriches a list of fournisseurs with computed tauxLivraison and notation
     * derived from their CommandeAchat history. Uses two bulk queries to avoid N+1.
     * When a fournisseur has no non-cancelled orders, tauxLivraison is set to null
     * and the stored (manual) notation is left unchanged.
     */
    private void enrichFournisseurs(List<Fournisseur> fournisseurs, Long entrepriseId) {
        List<Object[]> totalRows = commandeAchatRepository.countNonCancelledByFournisseur(entrepriseId);
        List<Object[]> receivedRows = commandeAchatRepository.countReceivedByFournisseur(entrepriseId);

        Map<Long, Long> totalMap = new HashMap<>();
        for (Object[] row : totalRows) {
            totalMap.put(((Number) row[0]).longValue(), ((Number) row[1]).longValue());
        }
        Map<Long, Long> receivedMap = new HashMap<>();
        for (Object[] row : receivedRows) {
            receivedMap.put(((Number) row[0]).longValue(), ((Number) row[1]).longValue());
        }

        for (Fournisseur f : fournisseurs) {
            long total = totalMap.getOrDefault(f.getId(), 0L);
            long received = receivedMap.getOrDefault(f.getId(), 0L);
            Double taux = computeTauxLivraison(total, received);
            f.setTauxLivraison(taux);
            if (taux != null) {
                f.setNotation(computeNotationFromTaux(taux));
            }
            // When taux is null (no orders yet), keep the stored manual notation as fallback
        }
    }

    /**
     * Enriches a single fournisseur with computed tauxLivraison and notation.
     */
    private void enrichFournisseur(Fournisseur f) {
        long total = commandeAchatRepository.countNonCancelledForFournisseur(f.getId());
        long received = commandeAchatRepository.countReceivedForFournisseur(f.getId());
        Double taux = computeTauxLivraison(total, received);
        f.setTauxLivraison(taux);
        if (taux != null) {
            f.setNotation(computeNotationFromTaux(taux));
        }
    }

    // ✅ GET ALL FOURNISSEURS (enterprise-scoped)
    public List<Fournisseur> getAllFournisseurs() {
        Long entrepriseId = enterpriseSecurityUtil.getContextEnterpriseId();
        if (entrepriseId == null) {
            throw new RuntimeException("Enterprise context required");
        }
        List<Fournisseur> fournisseurs = fournisseurRepo.findByEntrepriseId(entrepriseId);
        enrichFournisseurs(fournisseurs, entrepriseId);
        return fournisseurs;
    }

    // ✅ GET FOURNISSEURS BY ENTERPRISE
    public List<Fournisseur> getFournisseursByEntreprise(Long entrepriseId) {
        List<Fournisseur> fournisseurs = fournisseurRepo.findByEntrepriseId(entrepriseId);
        enrichFournisseurs(fournisseurs, entrepriseId);
        return fournisseurs;
    }

    // ✅ GET FOURNISSEUR BY ID
    public Fournisseur getFournisseurById(Long id) {
        Fournisseur f = fournisseurRepo.findById(id)
            .orElseThrow(() -> new RuntimeException("Fournisseur not found with id: " + id));
        enrichFournisseur(f);
        return f;
    }

    // ✅ DELETE FOURNISSEUR
    @Transactional
    public void deleteFournisseur(Long id) {
        if (!fournisseurRepo.existsById(id)) {
            throw new RuntimeException("Fournisseur not found with id: " + id);
        }
        // Nullify fournisseur FK in commande_achat, article, and facture_achat in a single query each
        commandeAchatRepository.detachFournisseur(id);
        articleRepository.detachFournisseur(id);
        factureAchatRepo.detachFournisseur(id);
        fournisseurRepo.deleteById(id);
    }

    // ✅ UPDATE FOURNISSEUR
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
        // tauxLivraison is computed from commande history — never persisted from form input
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
        return fournisseurRepo.save(existing);
    }

    // ✅ ADD CATEGORIE FOURNISSEUR
    @Transactional
    public CategorieFournisseur addCategorieFournisseur(CategorieFournisseur categorie) {
        return categorieFournisseurRepo.save(categorie);
    }

    // ✅ GET ALL CATEGORIES FOURNISSEUR
    public List<CategorieFournisseur> getAllCategorieFournisseurs() {
        return categorieFournisseurRepo.findAll();
    }

    // ✅ GET CATEGORIE FOURNISSEUR BY ID
    public CategorieFournisseur getCategorieFournisseurById(Long id) {
        return categorieFournisseurRepo.findById(id)
            .orElseThrow(() -> new RuntimeException("Categorie not found with id: " + id));
    }

    // ✅ DELETE CATEGORIE FOURNISSEUR
    @Transactional
    public void deleteCategorieFournisseur(Long id) {
        if (!categorieFournisseurRepo.existsById(id)) {
            throw new RuntimeException("Categorie not found with id: " + id);
        }
        categorieFournisseurRepo.deleteById(id);
    }

    // ✅ UPDATE CATEGORIE FOURNISSEUR
    @Transactional
    public CategorieFournisseur updateCategorieFournisseur(Long id, CategorieFournisseur categorie) {
        if (!categorieFournisseurRepo.existsById(id)) {
            throw new RuntimeException("Categorie not found with id: " + id);
        }
        categorie.setId(id);
        return categorieFournisseurRepo.save(categorie);
    }
}
