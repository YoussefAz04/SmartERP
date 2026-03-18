package BuissnesManager.BuissnesManager.controllers;


import BuissnesManager.BuissnesManager.entity.CategorieClient;
import BuissnesManager.BuissnesManager.entity.Client;
import BuissnesManager.BuissnesManager.service.ClientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/clients")
public class ClientController {

    @Autowired
    private ClientService clientService;

    @PostMapping("addClient/{idCategorie}")
    public ResponseEntity<Client> addClient(@RequestBody Client client, @PathVariable Long idCategorie) {
        Client newClient = clientService.addClient(client, idCategorie);
        return ResponseEntity.ok(newClient);
    }

    @GetMapping("/getAllClients")
    public ResponseEntity<List<Client>> getAllClients() {
        return ResponseEntity.ok(clientService.getAllClient());
    }

    @GetMapping("getClientById/{id}")
    public ResponseEntity<Client> getClientById(@PathVariable Long id) {
        return ResponseEntity.ok(clientService.getClientById(id));
    }

    @PostMapping("addCategorieClient")
    public ResponseEntity<CategorieClient> addCategorieClient(@RequestBody CategorieClient categorieClient) {
        return ResponseEntity.ok(clientService.addCategorieClient(categorieClient));
    }

    @GetMapping("/getAllCategorieClients")
    public ResponseEntity<List<CategorieClient>> getAllCategorieClients() {
        return ResponseEntity.ok(clientService.getAllCategorieClient());
    }

    @GetMapping("getCategorieClientById/{id}")
    public ResponseEntity<CategorieClient> getCategorieClientById(@PathVariable Long id) {
        return ResponseEntity.ok(clientService.getCategorieClientById(id));
    }

}
