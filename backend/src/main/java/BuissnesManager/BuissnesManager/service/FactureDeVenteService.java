package BuissnesManager.BuissnesManager.service;

import BuissnesManager.BuissnesManager.entity.Client;
import BuissnesManager.BuissnesManager.entity.DetailsFactureDeVente;
import BuissnesManager.BuissnesManager.entity.FactureAchat;
import BuissnesManager.BuissnesManager.entity.FactureDeVente;
import BuissnesManager.BuissnesManager.repository.ClientRepo;
import BuissnesManager.BuissnesManager.repository.DetaisFactureDeVenteRepo;
import BuissnesManager.BuissnesManager.repository.FactureDeVenteRepo;
import BuissnesManager.BuissnesManager.security.EnterpriseSecurityUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class FactureDeVenteService {

    @Autowired
    private FactureDeVenteRepo factureDeVenteRepo;

    @Autowired
    private DetaisFactureDeVenteRepo detaisFactureDeVenteRepo;
    @Autowired
    private ClientRepo clientRepo ;

    @Autowired
    private EnterpriseSecurityUtil enterpriseSecurityUtil;



    private static final java.util.Set<String> VALID_STATUTS = java.util.Set.of(
            "EN_ATTENTE", "PAYEE", "PARTIELLEMENT_PAYEE", "ANNULEE");

    public FactureDeVente ajouterFactureDeVente(FactureDeVente factureDeVente){
       if (factureDeVente.getStatut() == null || factureDeVente.getStatut().isEmpty()
               || !VALID_STATUTS.contains(factureDeVente.getStatut())) {
           factureDeVente.setStatut("EN_ATTENTE");
       }

       DetailsFactureDeVente detail = factureDeVente.getDetaisFactureDeVente();
       if (detail != null) {
           detail.setFactureDeVente(factureDeVente);
       }

       return factureDeVenteRepo.save(factureDeVente);
    }

    public List<FactureDeVente> getAllFactures() {
        Long entrepriseId = enterpriseSecurityUtil.getContextEnterpriseId();
        if (entrepriseId == null) {
            return factureDeVenteRepo.findAll();
        }
        return factureDeVenteRepo.findByEntrepriseId(entrepriseId);
    }

    public Optional<FactureDeVente> getFactureById(Long id) {
        return factureDeVenteRepo.findById(id);
    }


    public FactureDeVente updateFactureVente(Long id, FactureDeVente updatedFacture) {
        return factureDeVenteRepo.findById(id)
                .map(existingFacture -> {
                    existingFacture.setDateAchat(updatedFacture.getDateAchat());
                    existingFacture.setReferance(updatedFacture.getReferance());
                    existingFacture.setMontantTotal(updatedFacture.getMontantTotal());
                    existingFacture.setMontantHT(updatedFacture.getMontantHT());
                    existingFacture.setMontantTVA(updatedFacture.getMontantTVA());
                    existingFacture.setMontantTTC(updatedFacture.getMontantTTC());
                    existingFacture.setRemise(updatedFacture.getRemise());
                    existingFacture.setModePaiement(updatedFacture.getModePaiement());
                    if (updatedFacture.getStatut() != null
                            && !updatedFacture.getStatut().isEmpty()
                            && VALID_STATUTS.contains(updatedFacture.getStatut())) {
                        existingFacture.setStatut(updatedFacture.getStatut());
                    }
                    existingFacture.setDescription(updatedFacture.getDescription());
                    existingFacture.setCheminFichier(updatedFacture.getCheminFichier());
                    //
                    return factureDeVenteRepo.save(existingFacture);
                })
                .orElseThrow(() -> new RuntimeException("FactureAchat non trouvée avec id " + id));
    }


    public void deleteFacture(Long id) {
        if (!factureDeVenteRepo.existsById(id)) {
            throw new RuntimeException("Facture non trouvée avec l'id : " + id);
        }
        factureDeVenteRepo.deleteById(id);
    }

    public void affecterFactureClient(Long idFacture, Long idClient){
        FactureDeVente factureDeVente = factureDeVenteRepo.findById(idFacture)
                .orElseThrow(() -> new RuntimeException("Facture non trouvée avec l'id : " + idFacture));
        factureDeVente.setClient(clientRepo.findById(idClient)
                .orElseThrow(() -> new RuntimeException("Client non trouvé avec l'id : " + idClient)));
        factureDeVenteRepo.save(factureDeVente);
    }

    public List<FactureDeVente> getFacturesByClient(Long clientId) {
        return factureDeVenteRepo.findByClientId(clientId);
    }
}
