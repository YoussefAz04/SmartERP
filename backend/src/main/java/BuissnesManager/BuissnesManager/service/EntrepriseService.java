package BuissnesManager.BuissnesManager.service;

import BuissnesManager.BuissnesManager.entity.CaracteristiqueFactureEntreprise;
import BuissnesManager.BuissnesManager.entity.Entreprise;
import BuissnesManager.BuissnesManager.entity.User;
import BuissnesManager.BuissnesManager.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class EntrepriseService {
    @Autowired
    private EntrepriseRepo entrepriseRepository;
    @Autowired
    private UserRepo userRepo;
    @Autowired
    private CaracteristiqueFactureEntrepriseRepo caracteristiqueFactureEntrepriseRepo;
    @Autowired
    private PredictionLogRepository predictionLogRepository;
    @Autowired
    private NotificationRepository notificationRepository;
    @Autowired
    private StockMouvementRepo stockMouvementRepo;
    @Autowired
    private LigneFactureAchatRepository ligneFactureAchatRepository;
    @Autowired
    private LigneFactureVenteRepository ligneFactureVenteRepository;
    @Autowired
    private DetaisFactureAchatRepo detaisFactureAchatRepo;
    @Autowired
    private DetaisFactureDeVenteRepo detaisFactureDeVenteRepo;
    @Autowired
    private FactureAchatRepo factureAchatRepo;
    @Autowired
    private FactureDeVenteRepo factureDeVenteRepo;
    @Autowired
    private FactureVenteRepository factureVenteRepository;
    @Autowired
    private BonReceptionRepository bonReceptionRepository;
    @Autowired
    private BonLivraisonRepository bonLivraisonRepository;
    @Autowired
    private LigneCommandeAchatRepository ligneCommandeAchatRepository;
    @Autowired
    private LigneCommandeVenteRepository ligneCommandeVenteRepository;
    @Autowired
    private CommandeAchatRepository commandeAchatRepository;
    @Autowired
    private CommandeVenteRepository commandeVenteRepository;
    @Autowired
    private ArticleVenteRepo articleVenteRepo;
    @Autowired
    private StockRepository stockRepository;
    @Autowired
    private ArticleRepository articleRepository;
    @Autowired
    private PermissionRepo permissionRepo;
    @Autowired
    private RoleRepo roleRepo;
    @Autowired
    private FournisseurRepo fournisseurRepo;
    @Autowired
    private ClientRepo clientRepo;
    @Autowired
    private CategorieArticleRepository categorieArticleRepository;
    @Autowired
    private MesureRepository mesureRepository;
    @Autowired
    private AuditLogService auditLogService;


    public List<Entreprise> getAllEntreprise(){
        return entrepriseRepository.findByActiveTrue();
    }

    public Entreprise ajouterEntreprise(Entreprise entreprise) {
        Entreprise saved = entrepriseRepository.save(entreprise);
        auditLogService.log("ENTREPRISE_CREEE",
                "Entreprise créée: " + saved.getNom() + " (id=" + saved.getId() + ")");
        return saved;
    }

    public List<Entreprise> getAllEntreprises() {
        return entrepriseRepository.findAll();
    }


    public Optional<Entreprise> getEntrepriseById(Long id) {
        return entrepriseRepository.findById(id);
    }
    public Entreprise updateEntreprise(Long id, Entreprise entrepriseDetails) {
        Optional<Entreprise> optionalEntreprise = entrepriseRepository.findById(id);
        if (optionalEntreprise.isPresent()) {
            Entreprise entreprise = optionalEntreprise.get();
            entreprise.setNom(entrepriseDetails.getNom());
            entreprise.setRaisonSociale(entrepriseDetails.getRaisonSociale());
            entreprise.setDescription(entrepriseDetails.getDescription());
            entreprise.setSecteurActivite(entrepriseDetails.getSecteurActivite());
            entreprise.setStatutJuridique(entrepriseDetails.getStatutJuridique());
            entreprise.setNumeroIdentificationFiscale(entrepriseDetails.getNumeroIdentificationFiscale());
            entreprise.setRegistreCommerce(entrepriseDetails.getRegistreCommerce());
            entreprise.setSiteWeb(entrepriseDetails.getSiteWeb());
            entreprise.setDateCreation(entrepriseDetails.getDateCreation());
            entreprise.setEmail(entrepriseDetails.getEmail());
            entreprise.setTelephone(entrepriseDetails.getTelephone());
            entreprise.setAdresse(entrepriseDetails.getAdresse());
            Entreprise saved = entrepriseRepository.save(entreprise);
            auditLogService.log("ENTREPRISE_MODIFIEE",
                    "Entreprise modifiée: " + saved.getNom() + " (id=" + id + ")");
            return saved;
        } else {
            throw new RuntimeException("Entreprise non trouvée avec ID : " + id);
        }
    }

    @Transactional
    public void disableEntreprise(Long id) {
        Entreprise entreprise = entrepriseRepository.findById(id)
                .orElseThrow(() -> new EmptyResultDataAccessException(
                        "Entreprise non trouvée avec ID : " + id, 1));
        entreprise.setActive(false);
        entrepriseRepository.save(entreprise);

        List<User> users = userRepo.findByEntrepriseId(id);
        users.forEach(user -> user.setEnabled(false));
        userRepo.saveAll(users);
        auditLogService.log("ENTREPRISE_DESACTIVEE",
                "Entreprise désactivée: " + entreprise.getNom() + " (id=" + id + ")");
    }

    @Transactional
    public void enableEntreprise(Long id) {
        Entreprise entreprise = entrepriseRepository.findById(id)
                .orElseThrow(() -> new EmptyResultDataAccessException(
                        "Entreprise non trouvée avec ID : " + id, 1));
        entreprise.setActive(true);
        entrepriseRepository.save(entreprise);

        List<User> users = userRepo.findByEntrepriseId(id);
        users.forEach(user -> user.setEnabled(true));
        userRepo.saveAll(users);
        auditLogService.log("ENTREPRISE_ACTIVEE",
                "Entreprise activée: " + entreprise.getNom() + " (id=" + id + ")");
    }

    public void assignerUserToEntreprise(String username,String nom){
        Entreprise entreprise = entrepriseRepository.findByNom(nom).orElse(null);
        User user =userRepo.findByUsername(username).orElse(null);
        user.setEntreprise(entreprise);
        userRepo.save(user);
    }
    public Entreprise getById(Long idEntreprise){
        return entrepriseRepository.findById(idEntreprise)
                .orElseThrow(() -> new RuntimeException("Entreprise non trouvée avec l'id : " + idEntreprise));
    }


    public CaracteristiqueFactureEntreprise addCaracteristiqueFactureEntreprise(CaracteristiqueFactureEntreprise caracteristiqueFactureEntreprise){
        return caracteristiqueFactureEntrepriseRepo.save(caracteristiqueFactureEntreprise);
    }

}
