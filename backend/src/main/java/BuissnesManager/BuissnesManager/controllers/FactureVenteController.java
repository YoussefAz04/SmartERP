package BuissnesManager.BuissnesManager.controllers;

import BuissnesManager.BuissnesManager.dto.FactureVenteDto;
import BuissnesManager.BuissnesManager.entity.*;
import BuissnesManager.BuissnesManager.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/factures-vente")
public class FactureVenteController {

    @Autowired
    private FactureVenteRepository factureVenteRepository;
    @Autowired
    private ClientRepo clientRepo;
    @Autowired
    private EntrepriseRepo entrepriseRepo;
    @Autowired
    private CommandeVenteRepository commandeVenteRepository;

    @GetMapping
    public ResponseEntity<List<FactureVente>> getAllFacturesVente() {
        try {
            return ResponseEntity.ok(factureVenteRepository.findAll());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<FactureVente> getFactureVenteById(@PathVariable Long id) {
        try {
            return factureVenteRepository.findById(id)
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/entreprise/{entrepriseId}")
    public ResponseEntity<List<FactureVente>> getFacturesByEntreprise(@PathVariable Long entrepriseId) {
        try {
            return ResponseEntity.ok(factureVenteRepository.findByEntrepriseId(entrepriseId));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/client/{clientId}")
    public ResponseEntity<List<FactureVente>> getFacturesByClient(@PathVariable Long clientId) {
        try {
            return ResponseEntity.ok(factureVenteRepository.findByClientId(clientId));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<FactureVente> createFactureVente(@RequestBody FactureVenteDto dto) {
        try {
            FactureVente facture = new FactureVente();
            facture.setReferance(dto.getReferance());
            facture.setDateVente(dto.getDateVente());
            facture.setMontantHT(dto.getMontantHT());
            facture.setMontantTVA(dto.getMontantTVA());
            facture.setMontantTTC(dto.getMontantTTC());
            facture.setRemise(dto.getRemise());
            facture.setModePaiement(dto.getModePaiement());
            facture.setStatut(dto.getStatut());
            facture.setDescription(dto.getDescription());
            if (dto.getClientId() != null) {
                Client client = clientRepo.findById(dto.getClientId())
                        .orElseThrow(() -> new RuntimeException("Client non trouvé"));
                facture.setClient(client);
            }
            if (dto.getCommandeVenteId() != null) {
                CommandeVente cv = commandeVenteRepository.findById(dto.getCommandeVenteId())
                        .orElseThrow(() -> new RuntimeException("CommandeVente non trouvée"));
                facture.setCommandeVente(cv);
            }
            if (dto.getEntrepriseId() != null) {
                Entreprise entreprise = entrepriseRepo.findById(dto.getEntrepriseId())
                        .orElseThrow(() -> new RuntimeException("Entreprise non trouvée"));
                facture.setEntreprise(entreprise);
            }
            return ResponseEntity.status(HttpStatus.CREATED).body(factureVenteRepository.save(facture));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<FactureVente> updateFactureVente(@PathVariable Long id, @RequestBody FactureVenteDto dto) {
        try {
            FactureVente facture = factureVenteRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("FactureVente non trouvée avec id: " + id));
            if (dto.getReferance() != null) facture.setReferance(dto.getReferance());
            if (dto.getDateVente() != null) facture.setDateVente(dto.getDateVente());
            if (dto.getMontantHT() != null) facture.setMontantHT(dto.getMontantHT());
            if (dto.getMontantTVA() != null) facture.setMontantTVA(dto.getMontantTVA());
            if (dto.getMontantTTC() != null) facture.setMontantTTC(dto.getMontantTTC());
            if (dto.getRemise() != null) facture.setRemise(dto.getRemise());
            if (dto.getModePaiement() != null) facture.setModePaiement(dto.getModePaiement());
            if (dto.getStatut() != null) facture.setStatut(dto.getStatut());
            if (dto.getDescription() != null) facture.setDescription(dto.getDescription());
            return ResponseEntity.ok(factureVenteRepository.save(facture));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteFactureVente(@PathVariable Long id) {
        try {
            if (!factureVenteRepository.existsById(id)) {
                return ResponseEntity.notFound().build();
            }
            factureVenteRepository.deleteById(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
