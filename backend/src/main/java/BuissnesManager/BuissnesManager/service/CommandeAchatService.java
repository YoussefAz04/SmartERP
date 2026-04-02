package BuissnesManager.BuissnesManager.service;

import BuissnesManager.BuissnesManager.dto.CommandeAchatDto;
import BuissnesManager.BuissnesManager.dto.LigneCommandeAchatDto;
import BuissnesManager.BuissnesManager.entity.*;
import BuissnesManager.BuissnesManager.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
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
    @Autowired
    private ArticleAchatRepo articleAchatRepo;
    @Autowired
    private FactureAchatRepository factureAchatRepository;
    @Autowired
    private LigneFactureAchatRepository ligneFactureAchatRepository;

    @Transactional
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
        CommandeAchat saved = commandeAchatRepository.save(commande);

        if (dto.getLignes() != null && !dto.getLignes().isEmpty()) {
            for (LigneCommandeAchatDto ligneDto : dto.getLignes()) {
                LigneCommandeAchat ligne = new LigneCommandeAchat();
                ligne.setQuantite(ligneDto.getQuantite() != null ? ligneDto.getQuantite() : BigDecimal.ZERO);
                ligne.setPrixUnitaireHT(ligneDto.getPrixUnitaireHT() != null ? ligneDto.getPrixUnitaireHT() : BigDecimal.ZERO);
                ligne.setTva(ligneDto.getTva() != null ? ligneDto.getTva() : BigDecimal.ZERO);
                ligne.setMontantHT(ligneDto.getMontantHT() != null ? ligneDto.getMontantHT() : BigDecimal.ZERO);
                ligne.setMontantTTC(ligneDto.getMontantTTC() != null ? ligneDto.getMontantTTC() : BigDecimal.ZERO);
                ligne.setCommandeAchat(saved);
                if (ligneDto.getArticleAchatId() != null) {
                    ArticleAchat articleAchat = articleAchatRepo.findById(ligneDto.getArticleAchatId())
                            .orElseThrow(() -> new RuntimeException("ArticleAchat non trouvé: " + ligneDto.getArticleAchatId()));
                    ligne.setArticleAchat(articleAchat);
                }
                ligneCommandeAchatRepository.save(ligne);
            }
        }
        return saved;
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

    @Transactional
    public FactureAchat validerCommande(Long id) {
        CommandeAchat commande = commandeAchatRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("CommandeAchat non trouvée avec id: " + id));

        commande.setStatut("VALIDEE");
        commandeAchatRepository.save(commande);

        FactureAchat facture = new FactureAchat();
        facture.setDateAchat(LocalDate.now().toString());
        facture.setMontantHT(commande.getMontantHT() != null ? commande.getMontantHT().doubleValue() : 0.0);
        facture.setMontantTVA(commande.getMontantTVA() != null ? commande.getMontantTVA().doubleValue() : 0.0);
        facture.setMontantTTC(commande.getMontantTTC() != null ? commande.getMontantTTC().doubleValue() : 0.0);
        facture.setMontantTotal(commande.getMontantTTC() != null ? commande.getMontantTTC().doubleValue() : 0.0);
        facture.setStatut("EN_ATTENTE");
        facture.setDescription("Facture générée automatiquement depuis la commande " + commande.getReference());
        facture.setFournisseur(commande.getFournisseur());
        facture.setCommandeAchat(commande);
        FactureAchat savedFacture = factureAchatRepository.save(facture);
        savedFacture.setReferance("FA-" + String.format("%04d", savedFacture.getId()));
        savedFacture = factureAchatRepository.save(savedFacture);

        List<LigneCommandeAchat> lignesCommande = ligneCommandeAchatRepository.findByCommandeAchatId(id);
        for (LigneCommandeAchat ligneCommande : lignesCommande) {
            LigneFactureAchat ligneFacture = new LigneFactureAchat();
            if (ligneCommande.getArticleAchat() != null) {
                ligneFacture.setDesignation(ligneCommande.getArticleAchat().getNom());
            }
            ligneFacture.setQuantite(ligneCommande.getQuantite());
            ligneFacture.setPrixUnitaireHT(ligneCommande.getPrixUnitaireHT());
            ligneFacture.setTva(ligneCommande.getTva());
            ligneFacture.setMontantHT(ligneCommande.getMontantHT());
            ligneFacture.setMontantTTC(ligneCommande.getMontantTTC());
            ligneFacture.setFactureAchat(savedFacture);
            ligneFactureAchatRepository.save(ligneFacture);
        }

        return savedFacture;
    }

    public CommandeAchat annulerCommande(Long id) {
        CommandeAchat commande = commandeAchatRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("CommandeAchat non trouvée avec id: " + id));
        commande.setStatut("ANNULEE");
        return commandeAchatRepository.save(commande);
    }

    public BigDecimal calculateTotal(Long commandeId) {
        List<LigneCommandeAchat> lignes = ligneCommandeAchatRepository.findByCommandeAchatId(commandeId);
        return lignes.stream()
                .map(l -> l.getMontantTTC() != null ? l.getMontantTTC() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
