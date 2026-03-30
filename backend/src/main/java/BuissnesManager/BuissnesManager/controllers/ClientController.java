package BuissnesManager.BuissnesManager.controllers;

import BuissnesManager.BuissnesManager.entity.CategorieClient;
import BuissnesManager.BuissnesManager.entity.Client;
import BuissnesManager.BuissnesManager.service.ClientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/clients")
@CrossOrigin(origins = "http://localhost:4200")
public class ClientController {

    @Autowired
    private ClientService clientService;

    // ✅ ADD CLIENT
    @PostMapping("/addClient/{idCategorie}")
    public ResponseEntity<Client> addClient(@RequestBody Client client, @PathVariable Long idCategorie) {
        try {
            Client newClient = clientService.addClient(client, idCategorie);
            return ResponseEntity.ok(newClient);
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }

    // ✅ GET ALL CLIENTS
    @GetMapping("/getAllClients")
    public ResponseEntity<List<Client>> getAllClients() {
        try {
            return ResponseEntity.ok(clientService.getAllClient());
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }

    // ✅ GET CLIENT BY ID
    @GetMapping("/getClientById/{id}")
    public ResponseEntity<Client> getClientById(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(clientService.getClientById(id));
        } catch (Exception e) {
            return ResponseEntity.status(404).build();
        }
    }

    // ✅ DELETE CLIENT
    @DeleteMapping("/deleteClient/{id}")
    public ResponseEntity<Map<String, Object>> deleteClient(@PathVariable Long id) {
        try {
            clientService.deleteClient(id);
            return ResponseEntity.ok(Map.of(
                "message", "Client supprimé avec succès",
                "status", "success",
                "id", id
            ));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                "message", "Erreur: " + e.getMessage(),
                "status", "error"
            ));
        }
    }

    // ✅ UPDATE CLIENT
    @PutMapping("/updateClient/{id}")
    public ResponseEntity<Client> updateClient(@PathVariable Long id, @RequestBody Client client) {
        try {
            client.setId(id);
            Client updatedClient = clientService.updateClient(client);
            return ResponseEntity.ok(updatedClient);
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }

    // ✅ ADD CATEGORY CLIENT
    @PostMapping("/addCategorieClient")
    public ResponseEntity<CategorieClient> addCategorieClient(@RequestBody CategorieClient categorieClient) {
        try {
            CategorieClient newCategorie = clientService.addCategorieClient(categorieClient);
            return ResponseEntity.ok(newCategorie);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).build();
        }
    }

    // ✅ GET ALL CATEGORIES
    @GetMapping("/getAllCategorieClients")
    public ResponseEntity<List<CategorieClient>> getAllCategorieClients() {
        try {
            return ResponseEntity.ok(clientService.getAllCategorieClient());
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }

    // ✅ GET CATEGORY BY ID
    @GetMapping("/getCategorieClientById/{id}")
    public ResponseEntity<CategorieClient> getCategorieClientById(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(clientService.getCategorieClientById(id));
        } catch (Exception e) {
            return ResponseEntity.status(404).build();
        }
    }

    // ✅ UPDATE CATEGORY CLIENT
    @PutMapping("/updateCategorieClient/{id}")
    public ResponseEntity<CategorieClient> updateCategorieClient(
        @PathVariable Long id, 
        @RequestBody CategorieClient categorieClient) {
        try {
            categorieClient.setId(id);
            CategorieClient updated = clientService.updateCategorieClient(categorieClient);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).build();
        }
    }
    @DeleteMapping("/deleteCategorie/{id}")
    public ResponseEntity<Map<String, Object>> deleteCategorie(@PathVariable Long id) {
        try {
            clientService.deleteCategorie(id);
            return ResponseEntity.ok(Map.of(
                "message", "Catégorie supprimée avec succès",
                "status", "success",
                "id", id
            ));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                "message", "Erreur: " + e.getMessage(),
                "status", "error"
            ));
        }
    }
}