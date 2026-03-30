package BuissnesManager.BuissnesManager.service;

import BuissnesManager.BuissnesManager.entity.CategorieClient;
import BuissnesManager.BuissnesManager.entity.Client;
import BuissnesManager.BuissnesManager.repository.CategorieClientRepo;
import BuissnesManager.BuissnesManager.repository.ClientRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ClientService {

    @Autowired
    private ClientRepo clientRepo;
    
    @Autowired
    private CategorieClientRepo categorieClientRepo;

    // ✅ ADD CLIENT
    @Transactional
    public Client addClient(Client client, Long idCategorie) {
        CategorieClient categorie = categorieClientRepo.findById(idCategorie)
            .orElseThrow(() -> new RuntimeException("Categorie not found with id: " + idCategorie));
        client.setCategorieClient(categorie);
        return clientRepo.save(client);
    }

    // ✅ GET ALL CLIENTS
    public List<Client> getAllClient() {
        return clientRepo.findAll();
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