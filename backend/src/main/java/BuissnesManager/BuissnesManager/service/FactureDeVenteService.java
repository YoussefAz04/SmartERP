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

    @Autowired
    private AuditLogService auditLogService;



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

       FactureDeVente saved = factureDeVenteRepo.save(factureDeVente);
       auditLogService.log("FACTURE_VENTE_CREATED",
               "Facture de vente créée: ref=" + saved.getReferance()
                       + ", id=" + saved.getId()
                       + ", montantTTC=" + saved.getMontantTTC()
                       + ", statut=" + saved.getStatut());
       return saved;
    }

    public List<FactureDeVente> getAllFactures() {
        Long entrepriseId = enterpriseSecurityUtil.getContextEnterpriseId();
        if (entrepriseId == null) {
            throw new RuntimeException("Enterprise context required");
        }
        return factureDeVenteRepo.findByEntrepriseId(entrepriseId);
    }

    public Optional<FactureDeVente> getFactureById(Long id) {
        return factureDeVenteRepo.findByIdWithDetails(id);
    }


    @Transactional
    public FactureDeVente updateFactureVente(Long id, FactureDeVente updatedFacture) {
        return factureDeVenteRepo.findById(id)
                .map(existingFacture -> {
                    String previousStatut = existingFacture.getStatut();
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
                    FactureDeVente saved = factureDeVenteRepo.save(existingFacture);
                    auditLogService.log("FACTURE_VENTE_UPDATED",
                            "Facture de vente mise à jour: ref=" + saved.getReferance()
                                    + " (id=" + saved.getId() + "), statut: "
                                    + previousStatut + " -> " + saved.getStatut());
                    // Emit dedicated audit events for the validate / cancel transitions so that
                    // compliance reporting can pick them up without parsing details strings.
                    if (!java.util.Objects.equals(previousStatut, saved.getStatut())) {
                        if ("PAYEE".equals(saved.getStatut())) {
                            auditLogService.log("FACTURE_VENTE_VALIDATED",
                                    "Facture de vente validée (PAYEE): ref=" + saved.getReferance()
                                            + " (id=" + saved.getId() + ")");
                        } else if ("ANNULEE".equals(saved.getStatut())) {
                            auditLogService.log("FACTURE_VENTE_CANCELLED",
                                    "Facture de vente annulée: ref=" + saved.getReferance()
                                            + " (id=" + saved.getId() + ")");
                        }
                    }
                    return saved;
                })
                .orElseThrow(() -> new RuntimeException("FactureAchat non trouvée avec id " + id));
    }


    @Transactional
    public void deleteFacture(Long id) {
        FactureDeVente existing = factureDeVenteRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Facture non trouvée avec l'id : " + id));
        factureDeVenteRepo.deleteById(id);
        auditLogService.log("FACTURE_VENTE_DELETED",
                "Facture de vente supprimée: ref=" + existing.getReferance() + " (id=" + id + ")");
    }

    @Transactional
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
