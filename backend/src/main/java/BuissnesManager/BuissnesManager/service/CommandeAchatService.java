package BuissnesManager.BuissnesManager.service;

import BuissnesManager.BuissnesManager.dto.CommandeAchatDto;
import BuissnesManager.BuissnesManager.entity.*;
import BuissnesManager.BuissnesManager.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
public class CommandeAchatService {

    @Autowired
    private CommandeAchatRepository commandeAchatRepository;
    @Autowired
    private LigneCommandeAchatRepository ligneCommandeAchatRepository;
    @Autowired
    private FournisseurRepo fournisseurRepo;
    @Autowired
    private EntrepriseRepo entrepriseRepo;

    public CommandeAchat createCommande(CommandeAchatDto dto) {
        CommandeAchat commande = new CommandeAchat();
        commande.setReference(dto.getReference());
        commande.setDateCommande(dto.getDateCommande());
        commande.setDateLivraisonPrevue(dto.getDateLivraisonPrevue());
        commande.setStatut(dto.getStatut() != null ? dto.getStatut() : "EN_ATTENTE");
        commande.setMontantHT(dto.getMontantHT() != null ? dto.getMontantHT() : BigDecimal.ZERO);
        commande.setMontantTVA(dto.getMontantTVA() != null ? dto.getMontantTVA() : BigDecimal.ZERO);
        commande.setMontantTTC(dto.getMontantTTC() != null ? dto.getMontantTTC() : BigDecimal.ZERO);
        commande.setNote(dto.getNote());
        if (dto.getFournisseurId() != null) {
            Fournisseur fournisseur = fournisseurRepo.findById(dto.getFournisseurId())
                    .orElseThrow(() -> new RuntimeException("Fournisseur non trouvé: " + dto.getFournisseurId()));
            commande.setFournisseur(fournisseur);
        }
        if (dto.getEntrepriseId() != null) {
            Entreprise entreprise = entrepriseRepo.findById(dto.getEntrepriseId())
                    .orElseThrow(() -> new RuntimeException("Entreprise non trouvée: " + dto.getEntrepriseId()));
            commande.setEntreprise(entreprise);
        }
        return commandeAchatRepository.save(commande);
    }

    public List<CommandeAchat> getAllCommandes() {
        return commandeAchatRepository.findAll();
    }

    public List<CommandeAchat> getCommandesByEntreprise(Long entrepriseId) {
        return commandeAchatRepository.findByEntrepriseId(entrepriseId);
    }

    public Optional<CommandeAchat> getCommandeById(Long id) {
        return commandeAchatRepository.findById(id);
    }

    public CommandeAchat updateStatut(Long id, String statut) {
        CommandeAchat commande = commandeAchatRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("CommandeAchat non trouvée avec id: " + id));
        commande.setStatut(statut);
        return commandeAchatRepository.save(commande);
    }

    public CommandeAchat updateCommande(Long id, CommandeAchatDto dto) {
        CommandeAchat commande = commandeAchatRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("CommandeAchat non trouvée avec id: " + id));
        if (dto.getReference() != null) commande.setReference(dto.getReference());
        if (dto.getDateCommande() != null) commande.setDateCommande(dto.getDateCommande());
        if (dto.getDateLivraisonPrevue() != null) commande.setDateLivraisonPrevue(dto.getDateLivraisonPrevue());
        if (dto.getStatut() != null) commande.setStatut(dto.getStatut());
        if (dto.getMontantHT() != null) commande.setMontantHT(dto.getMontantHT());
        if (dto.getMontantTVA() != null) commande.setMontantTVA(dto.getMontantTVA());
        if (dto.getMontantTTC() != null) commande.setMontantTTC(dto.getMontantTTC());
        if (dto.getNote() != null) commande.setNote(dto.getNote());
        return commandeAchatRepository.save(commande);
    }

    @Transactional
    public void deleteCommande(Long id) {
        if (!commandeAchatRepository.existsById(id)) {
            throw new RuntimeException("CommandeAchat non trouvée avec id: " + id);
        }
        ligneCommandeAchatRepository.deleteByCommandeAchatId(id);
        commandeAchatRepository.deleteById(id);
    }

    public BigDecimal calculateTotal(Long commandeId) {
        List<LigneCommandeAchat> lignes = ligneCommandeAchatRepository.findByCommandeAchatId(commandeId);
        return lignes.stream()
                .map(l -> l.getMontantTTC() != null ? l.getMontantTTC() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
