package BuissnesManager.BuissnesManager.controllers;

import BuissnesManager.BuissnesManager.dto.CategorieClientDto;
import BuissnesManager.BuissnesManager.entity.CategorieClient;
import BuissnesManager.BuissnesManager.entity.Client;
import BuissnesManager.BuissnesManager.security.JWTUtil;
import BuissnesManager.BuissnesManager.service.ClientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/clients")
public class ClientController {

    @Autowired
    private ClientService clientService;

    @Autowired
    private JWTUtil jwtUtil;

    //  ADD CLIENT
    @PreAuthorize("hasRole('ADMIN') or hasRole('EMPLOYEE')")
    @PostMapping("/addClient/{idCategorie}")
    public ResponseEntity<Client> addClient(@Valid @RequestBody Client client, @PathVariable Long idCategorie) {
        try {
            Client newClient = clientService.addClient(client, idCategorie);
            return ResponseEntity.ok(newClient);
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }

    //  GET ALL CLIENTS
    @GetMapping("/getAllClients")
    public ResponseEntity<List<Client>> getAllClients() {
        try {
            return ResponseEntity.ok(clientService.getAllClient());
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }

    //  GET CLIENTS BY ENTERPRISE
    @GetMapping("/byEntreprise/{entrepriseId}")
    public ResponseEntity<List<Client>> getClientsByEntreprise(
            @PathVariable Long entrepriseId,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        try {
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);
                Long tokenEntrepriseId = jwtUtil.getEntrepriseIdFromToken(token);
                if (tokenEntrepriseId != null && !tokenEntrepriseId.equals(entrepriseId)) {
                    return ResponseEntity.status(403).build();
                }
            }
            return ResponseEntity.ok(clientService.getClientsByEntreprise(entrepriseId));
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }

    //  GET CLIENT BY ID
    @GetMapping("/getClientById/{id}")
    public ResponseEntity<Client> getClientById(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(clientService.getClientById(id));
        } catch (Exception e) {
            return ResponseEntity.status(404).build();
        }
    }

    //  DELETE CLIENT
    @PreAuthorize("hasRole('ADMIN') or hasRole('EMPLOYEE')")
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
                "message", "Error: " + e.getMessage(),
                "status", "error"
            ));
        }
    }

    //  UPDATE CLIENT
    @PreAuthorize("hasRole('ADMIN') or hasRole('EMPLOYEE')")
    @PutMapping("/updateClient/{id}")
    public ResponseEntity<Client> updateClient(@PathVariable Long id, @Valid @RequestBody Client client,
            @RequestParam(required = false) Long idCategorie) {
        try {
            client.setId(id);
            Client updatedClient = clientService.updateClient(client, idCategorie);
            return ResponseEntity.ok(updatedClient);
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }

    //  ADD CATEGORY CLIENT
    @PreAuthorize("hasRole('ADMIN') or hasRole('EMPLOYEE')")
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

    //  GET ALL CATEGORIES
    @GetMapping("/getAllCategorieClients")
    public ResponseEntity<List<CategorieClientDto>> getAllCategorieClients() {
        try {
            return ResponseEntity.ok(clientService.getAllCategorieClient());
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }

    //  GET CATEGORY BY ID
    @GetMapping("/getCategorieClientById/{id}")
    public ResponseEntity<CategorieClient> getCategorieClientById(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(clientService.getCategorieClientById(id));
        } catch (Exception e) {
            return ResponseEntity.status(404).build();
        }
    }

    //  UPDATE CATEGORY CLIENT
    @PreAuthorize("hasRole('ADMIN') or hasRole('EMPLOYEE')")
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
    @PreAuthorize("hasRole('ADMIN') or hasRole('EMPLOYEE')")
    @DeleteMapping("/deleteCategorie/{id}")
    public ResponseEntity<Map<String, Object>> deleteCategorie(@PathVariable Long id) {
        try {
            clientService.deleteCategorie(id);
            return ResponseEntity.ok(Map.of(
                "message", "Category supprimée avec succès",
                "status", "success",
                "id", id
            ));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                "message", "Error: " + e.getMessage(),
                "status", "error"
            ));
        }
    }
}
