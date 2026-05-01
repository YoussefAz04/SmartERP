package BuissnesManager.BuissnesManager.service;

import BuissnesManager.BuissnesManager.entity.ArticleVente;
import BuissnesManager.BuissnesManager.entity.CategorieClient;
import BuissnesManager.BuissnesManager.entity.Client;
import BuissnesManager.BuissnesManager.entity.CommandeVente;
import BuissnesManager.BuissnesManager.entity.Entreprise;
import BuissnesManager.BuissnesManager.entity.FactureDeVente;
import BuissnesManager.BuissnesManager.repository.ArticleVenteRepo;
import BuissnesManager.BuissnesManager.repository.CategorieClientRepo;
import BuissnesManager.BuissnesManager.repository.ClientRepo;
import BuissnesManager.BuissnesManager.repository.CommandeVenteRepository;
import BuissnesManager.BuissnesManager.repository.EntrepriseRepo;
import BuissnesManager.BuissnesManager.repository.FactureDeVenteRepo;
import BuissnesManager.BuissnesManager.security.EnterpriseSecurityUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ClientService {

    @Autowired
    private ClientRepo clientRepo;

    @Autowired
    private ArticleVenteRepo articleVenteRepo;

    @Autowired
    private CategorieClientRepo categorieClientRepo;

    @Autowired
    private FactureDeVenteRepo factureDeVenteRepo;

    @Autowired
    private CommandeVenteRepository commandeVenteRepository;

    @Autowired
    private EnterpriseSecurityUtil enterpriseSecurityUtil;

    @Autowired
    private EntrepriseRepo entrepriseRepo;

    @Autowired
    private NotificationService notificationService;

    // ✅ ADD CLIENT
    @Transactional
    public Client addClient(Client client, Long idCategorie) {
        CategorieClient categorie = categorieClientRepo.findById(idCategorie)
            .orElseThrow(() -> new RuntimeException("Categorie not found with id: " + idCategorie));
        client.setCategorieClient(categorie);
        Long entrepriseId = enterpriseSecurityUtil.getContextEnterpriseId();
        if (entrepriseId != null && client.getEntreprise() == null) {
            Entreprise entreprise = entrepriseRepo.findById(entrepriseId)
                .orElseThrow(() -> new RuntimeException("Entreprise not found with id: " + entrepriseId));
            client.setEntreprise(entreprise);
        }
        Client saved = clientRepo.save(client);
        if (saved.getEntreprise() != null) {
            notificationService.createClientAddedNotification(saved, saved.getEntreprise());
        }
        return saved;
    }

    // ✅ GET ALL CLIENTS (enterprise-scoped)
    public List<Client> getAllClient() {
        Long entrepriseId = enterpriseSecurityUtil.getContextEnterpriseId();
        if (entrepriseId == null) {
            return clientRepo.findAll();
        }
        return clientRepo.findByEntrepriseId(entrepriseId);
    }

    // ✅ GET CLIENTS BY ENTERPRISE
    public List<Client> getClientsByEntreprise(Long entrepriseId) {
        return clientRepo.findByEntrepriseId(entrepriseId);
    }

    // ✅ GET CLIENT BY ID
    public Client getClientById(Long idClient) {
        return clientRepo.findById(idClient)
            .orElseThrow(() -> new RuntimeException("Client not found with id: " + idClient));
    }

    // ✅ DELETE CLIENT
    @Transactional
    public void deleteClient(Long id) {
        if (!clientRepo.existsById(id)) {
            throw new RuntimeException("Client not found with id: " + id);
        }
        // Detach any article_vente rows that reference this client before deletion
        List<ArticleVente> relatedArticles = articleVenteRepo.findByClientId(id);
        for (ArticleVente av : relatedArticles) {
            av.setClient(null);
        }
        articleVenteRepo.saveAll(relatedArticles);
        // Detach any facture_de_vente rows that reference this client
        List<FactureDeVente> relatedFactures = factureDeVenteRepo.findByClientId(id);
        for (FactureDeVente fdv : relatedFactures) {
            fdv.setClient(null);
        }
        factureDeVenteRepo.saveAll(relatedFactures);
        // Detach any commande_vente rows that reference this client
        List<CommandeVente> relatedCommandes = commandeVenteRepository.findByClientId(id);
        for (CommandeVente cv : relatedCommandes) {
            cv.setClient(null);
        }
        commandeVenteRepository.saveAll(relatedCommandes);
        clientRepo.deleteById(id);
    }

    // ✅ UPDATE CLIENT
    @Transactional
    public Client updateClient(Client client) {
        Long id = client.getId();
        if (!clientRepo.existsById(id)) {
            throw new RuntimeException("Client not found with id: " + id);
        }
        return clientRepo.save(client);
    }

    // ✅ ADD CATEGORY CLIENT
    @Transactional
    public CategorieClient addCategorieClient(CategorieClient categorieClient) {
        return categorieClientRepo.save(categorieClient);
    }

    // ✅ GET ALL CATEGORIES
    public List<CategorieClient> getAllCategorieClient() {
        return categorieClientRepo.findAll();
    }

    // ✅ GET CATEGORY BY ID
    public CategorieClient getCategorieClientById(Long id) {
        return categorieClientRepo.findById(id)
            .orElseThrow(() -> new RuntimeException("Categorie not found with id: " + id));
    }

    // ✅ UPDATE CATEGORY CLIENT
    @Transactional
    public CategorieClient updateCategorieClient(CategorieClient categorieClient) {
        Long id = categorieClient.getId();
        if (!categorieClientRepo.existsById(id)) {
            throw new RuntimeException("Categorie not found with id: " + id);
        }
        return categorieClientRepo.save(categorieClient);
    }
    @Transactional
    public void deleteCategorie(Long id) {
        if (!categorieClientRepo.existsById(id)) {
            throw new RuntimeException("Categorie not found with id: " + id);
        }
        categorieClientRepo.deleteById(id);
    }
}