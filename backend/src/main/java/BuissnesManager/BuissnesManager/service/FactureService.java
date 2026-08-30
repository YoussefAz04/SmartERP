package BuissnesManager.BuissnesManager.service;

import BuissnesManager.BuissnesManager.entity.*;
import BuissnesManager.BuissnesManager.dto.FactureAchatListDto;
import BuissnesManager.BuissnesManager.repository.*;
import BuissnesManager.BuissnesManager.security.EnterpriseSecurityUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FactureService {

    @Autowired
    private FactureAchatRepo factureAchatRepo ;
    @Autowired
    private FactureDeVenteRepo factureDeVenteRepo ;

    @Autowired
    private EnterpriseSecurityUtil enterpriseSecurityUtil;

    @Autowired
    private AuditLogService auditLogService;



public List<FactureAchatListDto> getAllFactureAchat(){
        Long entrepriseId = enterpriseSecurityUtil.getContextEnterpriseId();
        if (entrepriseId == null) {
            entrepriseId = enterpriseSecurityUtil.getCurrentUser()
                    .map(User::getEntreprise)
                    .map(Entreprise::getId)
                    .orElse(null);
        }
        if (entrepriseId == null) {
            throw new RuntimeException("Enterprise context required");
        }
        return factureAchatRepo.findListDtoByEntrepriseId(entrepriseId);
}
public FactureAchat getById(Long id){
   return  this.factureAchatRepo.findById(id)
           .orElseThrow(() -> new RuntimeException("Facture non trouvée avec l'id : " + id));
}

    public FactureAchat updateFactureAchat(Long id, FactureAchat updatedFacture) {
        return factureAchatRepo.findById(id)
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
                    existingFacture.setStatut(updatedFacture.getStatut());
                    existingFacture.setDescription(updatedFacture.getDescription());
                    existingFacture.setCheminFichier(updatedFacture.getCheminFichier());
                    existingFacture.setDateEcheance(updatedFacture.getDateEcheance());
                    FactureAchat saved = factureAchatRepo.save(existingFacture);
                    auditLogService.log("FACTURE_ACHAT_UPDATED",
                            "Facture d'achat mise à jour: ref=" + saved.getReferance()
                                    + " (id=" + saved.getId() + "), statut: "
                                    + previousStatut + " -> " + saved.getStatut());
                    return saved;
                })
                .orElseThrow(() -> new RuntimeException("FactureAchat non trouvée avec id " + id));
    }

    public void deleteFactureAchat(Long id) {
        FactureAchat existing = factureAchatRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("FactureAchat non trouvée avec id " + id));
        factureAchatRepo.deleteById(id);
        auditLogService.log("FACTURE_ACHAT_DELETED",
                "Facture d'achat supprimée: ref=" + existing.getReferance() + " (id=" + id + ")");
    }











public FactureDeVente ajouterFactureDeVente(FactureDeVente factureDeVente){
 return factureDeVenteRepo.save(factureDeVente);
}










}
