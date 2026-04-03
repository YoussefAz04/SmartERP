package BuissnesManager.BuissnesManager.service;

import BuissnesManager.BuissnesManager.dto.CommandeAchatDto;
import BuissnesManager.BuissnesManager.dto.LigneCommandeAchatDto;
import BuissnesManager.BuissnesManager.entity.*;
import BuissnesManager.BuissnesManager.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class CommandeAchatService {

    // ✅ ADD THIS LOGGER DECLARATION
    private static final Logger logger = LoggerFactory.getLogger(CommandeAchatService.class);

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
        // ✅ VALIDATE ENTERPRISE IS PROVIDED
        if (dto.getEntrepriseId() == null) {
            logger.error("❌ Enterprise ID is required but was null");
            throw new RuntimeException("L'entreprise est obligatoire. Veuillez vous reconnecter.");
        }

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
        } else {
            throw new RuntimeException("Le fournisseur est obligatoire");
        }
        
        // ✅ SET ENTERPRISE - REQUIRED!
        Entreprise entreprise = entrepriseRepo.findById(dto.getEntrepriseId())
                .orElseThrow(() -> new RuntimeException("Entreprise non trouvée: " + dto.getEntrepriseId()));
        commande.setEntreprise(entreprise);
        logger.info("✅ Enterprise set: {}", entreprise.getId());
        
        CommandeAchat saved = commandeAchatRepository.save(commande);
        logger.info("✅ Commande created with ID: {}", saved.getId());

        if (dto.getLignes() != null && !dto.getLignes().isEmpty()) {
            for (LigneCommandeAchatDto ligneDto : dto.getLignes()) {
                LigneCommandeAchat ligne = new LigneCommandeAchat();
                ligne.setQuantite(ligneDto.getQuantite() != null ? ligneDto.getQuantite() : BigDecimal.ZERO);
                ligne.setPrixUnitaireHT(ligneDto.getPrixUnitaireHT() != null ? ligneDto.getPrixUnitaireHT() : BigDecimal.ZERO);
                ligne.setTva(ligneDto.getTva() != null ? ligneDto.getTva() : BigDecimal.ZERO);
                ligne.setMontantHT(ligneDto.getMontantHT() != null ? ligneDto.getMontantHT() : BigDecimal.ZERO);
                ligne.setMontantTTC(ligneDto.getMontantTTC() != null ? ligneDto.getMontantTTC() : BigDecimal.ZERO);
                ligne.setCommandeAchat(saved);
                ligneCommandeAchatRepository.save(ligne);
            }
        }
        return saved;
    }

    public List<CommandeAchat> getAllCommandes() {
        return commandeAchatRepository.findAll();
    }

    public Optional<CommandeAchat> getCommandeById(Long id) {
        return commandeAchatRepository.findById(id);
    }

    public List<CommandeAchat> getCommandesByEntreprise(Long entrepriseId) {
        return commandeAchatRepository.findByEntrepriseId(entrepriseId);
    }

    @Transactional
    public CommandeAchat updateCommande(Long id, CommandeAchatDto dto) {
        CommandeAchat commande = commandeAchatRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("CommandeAchat non trouvée avec id: " + id));

        commande.setReference(dto.getReference());
        commande.setDateCommande(dto.getDateCommande());
        commande.setDateLivraisonPrevue(dto.getDateLivraisonPrevue());
        commande.setMontantHT(dto.getMontantHT());
        commande.setMontantTVA(dto.getMontantTVA());
        commande.setMontantTTC(dto.getMontantTTC());
        commande.setNote(dto.getNote());

        if (dto.getFournisseurId() != null) {
            Fournisseur fournisseur = fournisseurRepo.findById(dto.getFournisseurId())
                    .orElseThrow(() -> new RuntimeException("Fournisseur non trouvé"));
            commande.setFournisseur(fournisseur);
        }

        return commandeAchatRepository.save(commande);
    }

    @Transactional
    public CommandeAchat updateStatut(Long id, String statut) {
        CommandeAchat commande = commandeAchatRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("CommandeAchat non trouvée"));
        commande.setStatut(statut);
        return commandeAchatRepository.save(commande);
    }

    @Transactional
    public FactureAchat validerCommande(Long id) {
        logger.info("🔄 [VALIDATION] Starting invoice generation for order ID: {}", id);
        
        // 1️⃣ Get the command
        CommandeAchat commande = commandeAchatRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("CommandeAchat non trouvée avec id: " + id));
        
        logger.info("✅ Found commande: {} with status: {}", id, commande.getStatut());

        // 2️⃣ Validate enterprise exists
        if (commande.getEntreprise() == null) {
            throw new RuntimeException("L'entreprise n'est pas définie dans la commande");
        }
        logger.info("✅ Enterprise verified: {}", commande.getEntreprise().getId());

        // 3️⃣ Validate supplier exists
        if (commande.getFournisseur() == null) {
            throw new RuntimeException("Le fournisseur n'est pas défini dans la commande");
        }
        logger.info("✅ Supplier verified: {}", commande.getFournisseur().getId());

        // 4️⃣ Update status to valid value
        commande.setStatut("CONFIRMEE");
        commandeAchatRepository.save(commande);
        logger.info("✅ Order status updated to CONFIRMEE");

        // 5️⃣ Create invoice
        FactureAchat facture = new FactureAchat();
        facture.setDateAchat(LocalDate.now().toString());
        facture.setReferance("FA-" + System.currentTimeMillis());
        facture.setMontantHT(commande.getMontantHT() != null ? commande.getMontantHT().doubleValue() : 0.0);
        facture.setMontantTVA(commande.getMontantTVA() != null ? commande.getMontantTVA().doubleValue() : 0.0);
        facture.setMontantTTC(commande.getMontantTTC() != null ? commande.getMontantTTC().doubleValue() : 0.0);
        facture.setStatut("EN_ATTENTE");
        facture.setCommandeAchat(commande);
        facture.setFournisseur(commande.getFournisseur());
        facture.setEntreprise(commande.getEntreprise());
        
        FactureAchat savedFacture = factureAchatRepository.save(facture);
        logger.info("✅ Invoice created with ID: {}", savedFacture.getId());

        // 6️⃣ Create invoice lines from order lines
        List<LigneCommandeAchat> lignesCommande = ligneCommandeAchatRepository.findByCommandeAchatId(id);
        logger.info("📝 Processing {} invoice lines", lignesCommande.size());

        int lineCount = 0;
        int skippedCount = 0;
        
        for (LigneCommandeAchat ligneCommande : lignesCommande) {
            try {
                // ✅ SKIP LINES WITH NO PRICE
                if (ligneCommande.getPrixUnitaireHT() == null || ligneCommande.getPrixUnitaireHT().compareTo(BigDecimal.ZERO) == 0) {
                    logger.warn("⚠️ SKIPPING line {} - no price set", ligneCommande.getId());
                    skippedCount++;
                    continue; // Skip this line and continue to next
                }
                
                LigneFactureAchat ligneFacture = new LigneFactureAchat();
                
                // Set designation
                ligneFacture.setDesignation(
                    ligneCommande.getArticleAchat() != null ? 
                    ligneCommande.getArticleAchat().getNom() : 
                    "Article"
                );
                
                // Set quantite
                BigDecimal quantite = ligneCommande.getQuantite() != null ? 
                                     ligneCommande.getQuantite() : 
                                     new BigDecimal("1");
                ligneFacture.setQuantite(quantite);
                
                // Set prix unitaire HT (already validated above)
                ligneFacture.setPrixUnitaireHT(ligneCommande.getPrixUnitaireHT());
                
                // Set TVA
                BigDecimal tva = ligneCommande.getTva() != null ? 
                               ligneCommande.getTva() : 
                               new BigDecimal("0.00");
                ligneFacture.setTva(tva);
                
                // Calculate montantHT
                BigDecimal montantHT = quantite.multiply(ligneCommande.getPrixUnitaireHT());
                ligneFacture.setMontantHT(montantHT);
                
                // Calculate montantTTC
                BigDecimal montantTTC = montantHT.multiply(
                    new BigDecimal("1").add(tva.divide(new BigDecimal("100"), 4, BigDecimal.ROUND_HALF_UP))
                );
                ligneFacture.setMontantTTC(montantTTC);
                
                // Set invoice reference
                ligneFacture.setFactureAchat(savedFacture);
                
                logger.info("📝 Line {}: designation={}, quantite={}, prixUnitaire={}", 
                           lineCount + 1,
                           ligneFacture.getDesignation(),
                           quantite,
                           ligneCommande.getPrixUnitaireHT());
                
                // Save line
                LigneFactureAchat saved = ligneFactureAchatRepository.save(ligneFacture);
                lineCount++;
                logger.info("✅ Line {} saved with ID: {}", lineCount, saved.getId());
                
            } catch (Exception e) {
                logger.error("❌ Error saving line {}: {}", ligneCommande.getId(), e.getMessage(), e);
                throw new RuntimeException("Erreur lors de la création de la ligne facture: " + e.getMessage());
            }
        }

        logger.info("✅ [VALIDATION COMPLETE] Invoice {} created with {} lines ({} skipped due to missing price)", 
                   savedFacture.getId(), lineCount, skippedCount);
        
        if (lineCount == 0 && skippedCount > 0) {
            logger.warn("⚠️ Invoice {} was created but has NO LINES - all lines were skipped due to missing prices", savedFacture.getId());
        }
        
        return savedFacture;
    }
    @Transactional
    public CommandeAchat annulerCommande(Long id) {
        CommandeAchat commande = commandeAchatRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("CommandeAchat non trouvée"));
        commande.setStatut("ANNULEA");
        return commandeAchatRepository.save(commande);
    }

    @Transactional
    public void deleteCommande(Long id) {
        commandeAchatRepository.deleteById(id);
    }
}