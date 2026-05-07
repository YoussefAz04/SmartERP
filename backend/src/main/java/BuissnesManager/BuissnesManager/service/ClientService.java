package BuissnesManager.BuissnesManager.service;

import BuissnesManager.BuissnesManager.dto.CategorieClientDto;
import BuissnesManager.BuissnesManager.entity.ArticleVente;
import BuissnesManager.BuissnesManager.entity.CategorieClient;
import BuissnesManager.BuissnesManager.entity.Client;
import BuissnesManager.BuissnesManager.entity.Entreprise;
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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    @Autowired
    private AuditLogService auditLogService;

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
        auditLogService.log("CLIENT_CREATED",
                "Client créé: " + saved.getNom() + " " + saved.getPrenom() + " (id=" + saved.getId() + ")");
        return saved;
    }

    // ✅ GET ALL CLIENTS (enterprise-scoped)
    public List<Client> getAllClient() {
        Long entrepriseId = enterpriseSecurityUtil.getContextEnterpriseId();
        if (entrepriseId == null) {
            throw new RuntimeException("Enterprise context required");
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
        Client client = clientRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Client not found with id: " + id));
        // Detach any article_vente rows that reference this client before deletion
        List<ArticleVente> relatedArticles = articleVenteRepo.findByClientId(id);
        for (ArticleVente av : relatedArticles) {
            av.setClient(null);
        }
        articleVenteRepo.saveAll(relatedArticles);
        // Nullify client FK in facture_de_vente and commande_vente in a single query each
        factureDeVenteRepo.detachClient(id);
        commandeVenteRepository.detachClient(id);
        clientRepo.deleteById(id);
        auditLogService.log("CLIENT_DELETED",
                "Client supprimé: " + client.getNom() + " " + client.getPrenom() + " (id=" + id + ")");
    }

    // ✅ UPDATE CLIENT
    @Transactional
    public Client updateClient(Client client, Long idCategorie) {
        Long id = client.getId();
        Client existing = clientRepo.findById(id)
            .orElseThrow(() -> new RuntimeException("Client not found with id: " + id));

        existing.setNom(client.getNom());
        existing.setPrenom(client.getPrenom());
        existing.setEmail(client.getEmail());
        existing.setTelephone(client.getTelephone());
        existing.setAdresse(client.getAdresse());
        existing.setSecteurActivite(client.getSecteurActivite());

        if (idCategorie != null) {
            CategorieClient categorie = categorieClientRepo.findById(idCategorie)
                .orElseThrow(() -> new RuntimeException("Categorie not found with id: " + idCategorie));
            existing.setCategorieClient(categorie);
        }

        Client updated = clientRepo.save(existing);
        auditLogService.log("CLIENT_UPDATED",
                "Client mis à jour: " + updated.getNom() + " " + updated.getPrenom() + " (id=" + updated.getId() + ")");
        return updated;
    }

    // ✅ ADD CATEGORY CLIENT
    @Transactional
    public CategorieClient addCategorieClient(CategorieClient categorieClient) {
        return categorieClientRepo.save(categorieClient);
    }

    // ✅ GET ALL CATEGORIES
    public List<CategorieClientDto> getAllCategorieClient() {
        Long entrepriseId = enterpriseSecurityUtil.getContextEnterpriseId();
        List<Object[]> rows = (entrepriseId != null)
            ? clientRepo.countGroupedByCategorieAndEntreprise(entrepriseId)
            : clientRepo.countGroupedByCategorie();
        Map<Long, Long> countMap = new HashMap<>();
        for (Object[] row : rows) {
            countMap.put((Long) row[0], (Long) row[1]);
        }
        return categorieClientRepo.findAll().stream().map(cat ->
            new CategorieClientDto(cat.getId(), cat.getNom(), cat.getDescription(),
                countMap.getOrDefault(cat.getId(), 0L))
        ).toList();
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