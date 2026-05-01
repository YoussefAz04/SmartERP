package BuissnesManager.BuissnesManager.service;

import BuissnesManager.BuissnesManager.entity.Article;
import BuissnesManager.BuissnesManager.entity.CategorieFournisseur;
import BuissnesManager.BuissnesManager.entity.CommandeAchat;
import BuissnesManager.BuissnesManager.entity.Entreprise;
import BuissnesManager.BuissnesManager.entity.FactureAchat;
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

import java.util.List;

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

    // ✅ GET ALL FOURNISSEURS (enterprise-scoped)
    public List<Fournisseur> getAllFournisseurs() {
        Long entrepriseId = enterpriseSecurityUtil.getContextEnterpriseId();
        if (entrepriseId == null) {
            return fournisseurRepo.findAll();
        }
        return fournisseurRepo.findByEntrepriseId(entrepriseId);
    }

    // ✅ GET FOURNISSEURS BY ENTERPRISE
    public List<Fournisseur> getFournisseursByEntreprise(Long entrepriseId) {
        return fournisseurRepo.findByEntrepriseId(entrepriseId);
    }

    // ✅ GET FOURNISSEUR BY ID
    public Fournisseur getFournisseurById(Long id) {
        return fournisseurRepo.findById(id)
            .orElseThrow(() -> new RuntimeException("Fournisseur not found with id: " + id));
    }

    // ✅ DELETE FOURNISSEUR
    @Transactional
    public void deleteFournisseur(Long id) {
        if (!fournisseurRepo.existsById(id)) {
            throw new RuntimeException("Fournisseur not found with id: " + id);
        }
        // Detach commande_achat rows that reference this fournisseur
        List<CommandeAchat> relatedCommandes = commandeAchatRepository.findByFournisseurId(id);
        for (CommandeAchat ca : relatedCommandes) {
            ca.setFournisseur(null);
        }
        commandeAchatRepository.saveAll(relatedCommandes);
        // Detach article rows that reference this fournisseur
        List<Article> relatedArticles = articleRepository.findByFournisseurId(id);
        for (Article a : relatedArticles) {
            a.setFournisseur(null);
        }
        articleRepository.saveAll(relatedArticles);
        // Detach facture_achat rows that reference this fournisseur
        List<FactureAchat> relatedFactures = factureAchatRepo.findByFournisseurId(id);
        for (FactureAchat fa : relatedFactures) {
            fa.setFournisseur(null);
        }
        factureAchatRepo.saveAll(relatedFactures);
        fournisseurRepo.deleteById(id);
    }

    // ✅ UPDATE FOURNISSEUR
    @Transactional
    public Fournisseur updateFournisseur(Long id, Fournisseur fournisseur) {
        if (!fournisseurRepo.existsById(id)) {
            throw new RuntimeException("Fournisseur not found with id: " + id);
        }
        fournisseur.setId(id);
        return fournisseurRepo.save(fournisseur);
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
