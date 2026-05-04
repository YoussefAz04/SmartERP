package BuissnesManager.BuissnesManager.service;

import BuissnesManager.BuissnesManager.entity.CaracteristiqueFactureEntreprise;
import BuissnesManager.BuissnesManager.entity.Entreprise;
import BuissnesManager.BuissnesManager.entity.User;
import BuissnesManager.BuissnesManager.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
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


    public List<Entreprise> getAllEntreprise(){
        return entrepriseRepository.findAll();
    }

    public Entreprise ajouterEntreprise(Entreprise entreprise) {
        return entrepriseRepository.save(entreprise);
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
            return entrepriseRepository.save(entreprise);
        } else {
            throw new RuntimeException("Entreprise non trouvée avec ID : " + id);
        }
    }

    @Transactional
    public void deleteEntreprise(Long id) {
        // 1. Prediction logs (leaf, only references entreprise)
        predictionLogRepository.deleteByEntrepriseId(id);

        // 2. Notifications (references entreprise, article, stock)
        notificationRepository.deleteByEntrepriseId(id);

        // 3. Stock movements (references stock -> article -> entreprise)
        stockMouvementRepo.deleteByEntrepriseId(id);

        // 4. Facture line items (must be deleted before their parent factures)
        ligneFactureAchatRepository.deleteByFactureAchatEntrepriseId(id);
        ligneFactureVenteRepository.deleteByFactureVenteEntrepriseId(id);

        // 5. Facture detail records
        detaisFactureAchatRepo.deleteByFactureAchatEntrepriseId(id);
        detaisFactureDeVenteRepo.deleteByFactureDeVenteEntrepriseId(id);

        // 6. Factures (references commandes, fournisseurs, clients, entreprise)
        factureAchatRepo.deleteByEntrepriseId(id);
        factureDeVenteRepo.deleteByEntrepriseId(id);
        factureVenteRepository.deleteByEntrepriseId(id);

        // 7. Bons de réception/livraison (reference commandes and entreprise)
        bonReceptionRepository.deleteByEntrepriseId(id);
        bonLivraisonRepository.deleteByEntrepriseId(id);

        // 8. Commande line items (must be deleted before their parent commandes)
        ligneCommandeAchatRepository.deleteByCommandeAchatEntrepriseId(id);
        ligneCommandeVenteRepository.deleteByCommandeVenteEntrepriseId(id);

        // 9. Commandes (references fournisseurs, clients, entreprise)
        commandeAchatRepository.deleteByEntrepriseId(id);
        commandeVenteRepository.deleteByEntrepriseId(id);

        // 10. Article ventes (references article, client, stock)
        articleVenteRepo.deleteByEntrepriseId(id);

        // 11. Stocks (references article -> entreprise)
        stockRepository.deleteByEntrepriseId(id);

        // 12. Articles (references categorie, fournisseur, mesure, entreprise)
        articleRepository.deleteByEntrepriseId(id);

        // 13. Permissions linked to enterprise roles or users (before users and roles)
        permissionRepo.deleteByUserEntrepriseId(id);
        permissionRepo.deleteByRoleEntrepriseId(id);

        // 14. Users (Hibernate cleans up the user_roles join table on deleteAll)
        List<User> users = userRepo.findByEntrepriseId(id);
        if (!users.isEmpty()) {
            userRepo.deleteAll(users);
        }

        // 15. Roles – first remove any user_roles entries pointing to enterprise roles
        // (covers edge cases where non-enterprise users were assigned enterprise roles)
        roleRepo.deleteUserRolesByEntrepriseId(id);
        roleRepo.deleteByEntrepriseId(id);

        // 16. Remaining master data
        fournisseurRepo.deleteByEntrepriseId(id);
        // Clean the legacy client_entreprises junction table before deleting clients
        // to cover rows referencing this enterprise from clients owned by other enterprises.
        clientRepo.deleteClientEntreprisesByEntrepriseId(id);
        clientRepo.deleteByEntrepriseId(id);
        categorieArticleRepository.deleteByEntrepriseId(id);
        mesureRepository.deleteByEntrepriseId(id);

        // 17. Caracteristique facture entreprise (OneToOne with entreprise)
        caracteristiqueFactureEntrepriseRepo.deleteByEntrepriseId(id);

        // 18. Finally delete the entreprise itself
        entrepriseRepository.deleteById(id);
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
