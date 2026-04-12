package BuissnesManager.BuissnesManager.service;

import BuissnesManager.BuissnesManager.dto.CommandeAchatDto;
import BuissnesManager.BuissnesManager.dto.LigneCommandeAchatDto;
import BuissnesManager.BuissnesManager.entity.*;
import BuissnesManager.BuissnesManager.repository.*;
import BuissnesManager.BuissnesManager.security.EnterpriseSecurityUtil;
import jakarta.persistence.EntityManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class CommandeAchatService {

    private static final Logger logger = LoggerFactory.getLogger(CommandeAchatService.class);

    @Autowired
    private CommandeAchatRepository commandeAchatRepository;

    @Autowired
    private LigneCommandeAchatRepository ligneCommandeAchatRepository;

    @Autowired
    private FournisseurRepo fournisseurRepo;

    @Autowired
    private ArticleRepository articleRepository;

    @Autowired
    private EntrepriseRepo entrepriseRepo;

    @Autowired
    private FactureAchatRepository factureAchatRepository;

    @Autowired
    private LigneFactureAchatRepository ligneFactureAchatRepository;

    @Autowired
    private BonReceptionRepository bonReceptionRepository;

    @Autowired
    private StockAutomationService stockAutomationService;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private EnterpriseSecurityUtil enterpriseSecurityUtil;

    // ✅ FIXED: Proper enterprise scoping with error handling
    public List<CommandeAchat> getAllCommandes() {
        Long entrepriseId = enterpriseSecurityUtil.getContextEnterpriseId();
        
        logger.info("🔵 getAllCommandes() called");
        logger.info("   Enterprise ID from JWT: {}", entrepriseId);
        
        if (entrepriseId == null) {
            logger.error("❌ CRITICAL: No enterprise ID found in JWT token!");
            logger.error("   User is not properly authenticated with an enterprise.");
            logger.error("   Returning empty list to prevent data leaks.");
            return List.of();  // ← Return EMPTY instead of all orders
        }
        
        List<CommandeAchat> commandes = commandeAchatRepository.findByEntrepriseId(entrepriseId);
        logger.info("✅ Found {} purchase orders for enterprise {}", commandes.size(), entrepriseId);
        return commandes;
    }

    public Optional<CommandeAchat> getCommandeById(Long id) {
        return commandeAchatRepository.findById(id);
    }

    @Transactional
    public CommandeAchat createCommande(CommandeAchatDto dto) {
        // ✅ RESOLVE ENTERPRISE ID FROM DTO OR JWT (without mutating DTO)
        final Long resolvedEntrepriseId;
        if (dto.getEntrepriseId() != null) {
            resolvedEntrepriseId = dto.getEntrepriseId();
        } else {
            Long jwtEntrepriseId = enterpriseSecurityUtil.getContextEnterpriseId();
            if (jwtEntrepriseId == null) {
                logger.error("❌ Enterprise ID is required");
                throw new RuntimeException("L'entreprise est obligatoire. Veuillez vous reconnecter.");
            }
            resolvedEntrepriseId = jwtEntrepriseId;
        }

        if (dto.getFournisseurId() == null) {
            logger.error("❌ Fournisseur ID is required");
            throw new RuntimeException("Le fournisseur est obligatoire.");
        }

        logger.info("🔄 Creating commande");
        logger.info("   Reference: {}", dto.getReference());
        logger.info("   Fournisseur ID: {}", dto.getFournisseurId());
        logger.info("   Enterprise ID: {}", resolvedEntrepriseId);

        // ✅ CREATE COMMANDE ENTITY
        CommandeAchat commande = new CommandeAchat();
        commande.setReference(dto.getReference());
        commande.setDateCommande(dto.getDateCommande());
        commande.setDateLivraisonPrevue(dto.getDateLivraisonPrevue());
        commande.setStatut(dto.getStatut() != null ? dto.getStatut() : "EN_ATTENTE");
        commande.setMontantHT(dto.getMontantHT() != null ? dto.getMontantHT() : BigDecimal.ZERO);
        commande.setMontantTVA(dto.getMontantTVA() != null ? dto.getMontantTVA() : BigDecimal.ZERO);
        commande.setMontantTTC(dto.getMontantTTC() != null ? dto.getMontantTTC() : BigDecimal.ZERO);
        commande.setNote(dto.getNote());

        // ✅ SET SUPPLIER
        Fournisseur fournisseur = fournisseurRepo.findById(dto.getFournisseurId())
                .orElseThrow(() -> {
                    logger.error("❌ Supplier not found with ID: {}", dto.getFournisseurId());
                    return new RuntimeException("Fournisseur non trouvé: " + dto.getFournisseurId());
                });
        commande.setFournisseur(fournisseur);
        logger.info("✅ Supplier set: {} (ID: {})", fournisseur.getNom(), fournisseur.getId());

        // ✅ SET ENTERPRISE
        Entreprise entreprise = entrepriseRepo.findById(resolvedEntrepriseId)
                .orElseThrow(() -> {
                    logger.error("❌ Enterprise not found with ID: {}", resolvedEntrepriseId);
                    return new RuntimeException("Entreprise non trouvée: " + resolvedEntrepriseId);
                });
        commande.setEntreprise(entreprise);
        logger.info("✅ Enterprise set: {} (ID: {})", entreprise.getNom(), entreprise.getId());

        // ✅ SAVE COMMANDE FIRST
        CommandeAchat saved = commandeAchatRepository.save(commande);
        logger.info("✅ Commande saved with ID: {}", saved.getId());

        // ✅ CREATE LINES WITH ARTICLE VALIDATION
        if (dto.getLignes() != null && !dto.getLignes().isEmpty()) {
            logger.info("📝 Processing {} line items", dto.getLignes().size());

            int lineCount = 0;
            for (int idx = 0; idx < dto.getLignes().size(); idx++) {
                final int lineNumber = idx + 1;
                LigneCommandeAchatDto ligneDto = dto.getLignes().get(idx);

                try {
                    logger.info("=== PROCESSING LINE {} ===", lineNumber);
                    logger.info("   Article ID: {}", ligneDto.getArticleAchatId());
                    logger.info("   Quantity: {}", ligneDto.getQuantite());
                    logger.info("   Unit Price HT: {}", ligneDto.getPrixUnitaireHT());

                    // ✅ VALIDATE ARTICLE ID
                    if (ligneDto.getArticleAchatId() == null) {
                        logger.error("❌ Line {}: Article ID is NULL!", lineNumber);
                        throw new RuntimeException(
                                String.format("Ligne %d: Sélectionnez un article!", lineNumber)
                        );
                    }

                    if (ligneDto.getArticleAchatId() <= 0) {
                        logger.error("❌ Line {}: Article ID is invalid: {}", lineNumber, ligneDto.getArticleAchatId());
                        throw new RuntimeException(
                                String.format("Ligne %d: Article ID invalide: %d", lineNumber, ligneDto.getArticleAchatId())
                        );
                    }

                    // ✅ FETCH AND VALIDATE THE ARTICLE EXISTS
                    Article article = articleRepository.findById(ligneDto.getArticleAchatId())
                            .orElseThrow(() -> {
                                logger.error("❌ Line {}: Article not found with ID: {}", lineNumber, ligneDto.getArticleAchatId());
                                return new RuntimeException(
                                        String.format("Ligne %d: Article non trouvé avec l'ID: %d",
                                                lineNumber, ligneDto.getArticleAchatId())
                                );
                            });

                    logger.info("✅ Line {}: Article found - {} (ID: {})", lineNumber, article.getNom(), article.getId());

                    // ✅ VALIDATE PRICE
                    if (ligneDto.getPrixUnitaireHT() == null) {
                        logger.error("❌ Line {}: Unit price HT is NULL!", lineNumber);
                        throw new RuntimeException(
                                String.format("Ligne %d: Le prix unitaire HT est obligatoire.", lineNumber)
                        );
                    }

                    if (ligneDto.getPrixUnitaireHT().compareTo(BigDecimal.ZERO) <= 0) {
                        logger.error("❌ Line {}: Unit price HT is invalid: {}", lineNumber, ligneDto.getPrixUnitaireHT());
                        throw new RuntimeException(
                                String.format("Ligne %d: Le prix unitaire HT doit être supérieur à 0.", lineNumber)
                        );
                    }

                    logger.info("✅ Line {}: Price validated - {}", lineNumber, ligneDto.getPrixUnitaireHT());

                    // ✅ CREATE LINE ITEM
                    LigneCommandeAchat ligne = new LigneCommandeAchat();
                    ligne.setQuantite(ligneDto.getQuantite() != null ? ligneDto.getQuantite() : BigDecimal.ONE);
                    ligne.setPrixUnitaireHT(ligneDto.getPrixUnitaireHT());
                    ligne.setTva(ligneDto.getTva() != null ? ligneDto.getTva() : BigDecimal.ZERO);
                    ligne.setMontantHT(ligneDto.getMontantHT() != null ? ligneDto.getMontantHT() : BigDecimal.ZERO);
                    ligne.setMontantTTC(ligneDto.getMontantTTC() != null ? ligneDto.getMontantTTC() : BigDecimal.ZERO);
                    ligne.setArticle(article);
                    ligne.setCommandeAchat(saved);

                    // ✅ SAVE LINE
                    LigneCommandeAchat savedLigne = ligneCommandeAchatRepository.saveAndFlush(ligne);
                    lineCount++;
                    logger.info("✅ Line {} saved successfully (ID: {})", lineNumber, savedLigne.getId());

                } catch (RuntimeException e) {
                    logger.error("❌ Error processing line {}: {}", lineNumber, e.getMessage());
                    throw e;
                }
            }

            if (lineCount == 0) {
                logger.error("❌ CRITICAL: No lines were saved!");
                throw new RuntimeException("Aucune ligne n'a pu être sauvegardée.");
            }

            logger.info("✅ All {} lines saved successfully", lineCount);
        } else {
            logger.error("❌ No lines provided in order");
            throw new RuntimeException("Au moins une ligne de commande est requise.");
        }

        logger.info("✅ Purchase order creation complete - ID: {}", saved.getId());
        return saved;
    }

    public List<CommandeAchat> getCommandesByEntreprise(Long entrepriseId) {
        return commandeAchatRepository.findByEntrepriseId(entrepriseId);
    }

    @Transactional
    public CommandeAchat updateCommande(Long id, CommandeAchatDto dto) {
        CommandeAchat commande = commandeAchatRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Commande non trouvée: " + id));

        if (dto.getReference() != null) commande.setReference(dto.getReference());
        if (dto.getDateCommande() != null) commande.setDateCommande(dto.getDateCommande());
        if (dto.getDateLivraisonPrevue() != null) commande.setDateLivraisonPrevue(dto.getDateLivraisonPrevue());
        if (dto.getStatut() != null) commande.setStatut(dto.getStatut());
        if (dto.getMontantHT() != null) commande.setMontantHT(dto.getMontantHT());
        if (dto.getMontantTVA() != null) commande.setMontantTVA(dto.getMontantTVA());
        if (dto.getMontantTTC() != null) commande.setMontantTTC(dto.getMontantTTC());
        if (dto.getNote() != null) commande.setNote(dto.getNote());

        if (dto.getFournisseurId() != null) {
            Fournisseur fournisseur = fournisseurRepo.findById(dto.getFournisseurId())
                    .orElseThrow(() -> new RuntimeException("Fournisseur non trouvé: " + dto.getFournisseurId()));
            commande.setFournisseur(fournisseur);
        }

        return commandeAchatRepository.save(commande);
    }

    @Transactional
    public CommandeAchat updateStatut(Long id, String statut) {
        CommandeAchat commande = commandeAchatRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Commande non trouvée: " + id));
        commande.setStatut(statut);
        return commandeAchatRepository.save(commande);
    }

    @Transactional
    public FactureAchat validerCommande(Long id) {
        CommandeAchat commande = commandeAchatRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Commande non trouvée: " + id));

        if ("CONFIRMEE".equals(commande.getStatut())) {
            throw new RuntimeException("Cette commande a déjà été confirmée.");
        }
        if ("ANNULEA".equals(commande.getStatut())) {
            throw new RuntimeException("Impossible de confirmer une commande annulée.");
        }

        commande.setStatut("CONFIRMEE");
        commandeAchatRepository.save(commande);

        // ✅ CREATE INVOICE
        FactureAchat facture = new FactureAchat();
        facture.setReferance("FAC-" + commande.getReference());
        facture.setDateAchat(commande.getDateCommande() != null
                ? commande.getDateCommande().toString()
                : LocalDate.now().toString());
        facture.setMontantHT(commande.getMontantHT() != null ? commande.getMontantHT().doubleValue() : 0.0);
        facture.setMontantTVA(commande.getMontantTVA() != null ? commande.getMontantTVA().doubleValue() : 0.0);
        facture.setMontantTTC(commande.getMontantTTC() != null ? commande.getMontantTTC().doubleValue() : 0.0);
        facture.setMontantTotal(commande.getMontantTTC() != null ? commande.getMontantTTC().doubleValue() : 0.0);
        facture.setStatut("EN_ATTENTE");
        facture.setModePaiement("A_DEFINIR");
        facture.setFournisseur(commande.getFournisseur());
        facture.setEntreprise(commande.getEntreprise());
        facture.setCommandeAchat(commande);

        FactureAchat savedFacture = factureAchatRepository.save(facture);

        // ✅ CREATE INVOICE LINES
        List<LigneFactureAchat> savedLignes = new java.util.ArrayList<>();
        if (commande.getLignes() != null && !commande.getLignes().isEmpty()) {
            logger.info("📝 Creating {} invoice lines", commande.getLignes().size());
            for (LigneCommandeAchat ligneCommande : commande.getLignes()) {
                LigneFactureAchat ligneFacture = new LigneFactureAchat();
                ligneFacture.setDesignation(ligneCommande.getArticle() != null
                        ? ligneCommande.getArticle().getNom() : "");
                ligneFacture.setQuantite(ligneCommande.getQuantite());
                ligneFacture.setPrixUnitaireHT(ligneCommande.getPrixUnitaireHT() != null
                        ? ligneCommande.getPrixUnitaireHT() : BigDecimal.ZERO);
                ligneFacture.setTva(ligneCommande.getTva());
                ligneFacture.setMontantHT(ligneCommande.getMontantHT());
                ligneFacture.setMontantTTC(ligneCommande.getMontantTTC());
                ligneFacture.setFactureAchat(savedFacture);

                savedLignes.add(ligneFactureAchatRepository.saveAndFlush(ligneFacture));
            }
        }

        if (!savedLignes.isEmpty()) {
            recalculateInvoiceAmounts(savedFacture, savedLignes);
        }

        logger.info("✅ Order validated and invoice created - Order ID: {}, Invoice ID: {}", id, savedFacture.getId());
        return savedFacture;
    }

    private void recalculateInvoiceAmounts(FactureAchat facture, List<LigneFactureAchat> lignes) {
        BigDecimal totalHT = BigDecimal.ZERO;
        BigDecimal totalTVA = BigDecimal.ZERO;
        BigDecimal totalTTC = BigDecimal.ZERO;

        for (LigneFactureAchat ligne : lignes) {
            BigDecimal montantHT = ligne.getMontantHT() != null ? ligne.getMontantHT() : BigDecimal.ZERO;
            BigDecimal montantTTC = ligne.getMontantTTC() != null ? ligne.getMontantTTC() : BigDecimal.ZERO;
            totalHT = totalHT.add(montantHT);
            totalTVA = totalTVA.add(montantTTC.subtract(montantHT));
            totalTTC = totalTTC.add(montantTTC);
        }

        facture.setMontantHT(totalHT.doubleValue());
        facture.setMontantTVA(totalTVA.doubleValue());
        facture.setMontantTTC(totalTTC.doubleValue());
        facture.setMontantTotal(totalTTC.doubleValue());

        factureAchatRepository.save(facture);
        logger.info("✅ Invoice totals recalculated: HT={}, TVA={}, TTC={}", totalHT, totalTVA, totalTTC);
    }

    @Transactional
    public CommandeAchat annulerCommande(Long id) {
        CommandeAchat commande = commandeAchatRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Commande non trouvée: " + id));

        if ("ANNULEA".equals(commande.getStatut())) {
            throw new RuntimeException("Cette commande est déjà annulée.");
        }

        commande.setStatut("ANNULEA");
        logger.info("✅ Order cancelled - ID: {}", id);
        return commandeAchatRepository.save(commande);
    }

    @Transactional
    public BonReception recevoirCommande(Long id) {
        CommandeAchat commande = commandeAchatRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Commande non trouvée: " + id));

        if ("ANNULEA".equals(commande.getStatut())) {
            throw new RuntimeException("Impossible de réceptionner une commande annulée.");
        }
        if ("RECUE".equals(commande.getStatut())) {
            throw new RuntimeException("Cette commande a déjà été réceptionnée.");
        }

        commande.setStatut("RECUE");
        commandeAchatRepository.save(commande);

        BonReception bon = new BonReception();
        bon.setReference("BR-" + commande.getReference());
        bon.setDateReception(LocalDate.now());
        bon.setStatut("RECU");
        bon.setCommandeAchat(commande);
        bon.setEntreprise(commande.getEntreprise());
        BonReception savedBon = bonReceptionRepository.save(bon);

        stockAutomationService.incrementStockFromReception(id, commande.getReference());

        logger.info("✅ Order received and stock updated - Order ID: {}, Reception ID: {}", id, savedBon.getId());
        return savedBon;
    }

    @Transactional
    public void deleteCommande(Long id) {
        CommandeAchat commande = commandeAchatRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Commande non trouvée: " + id));

        if ("CONFIRMEE".equals(commande.getStatut())) {
            throw new RuntimeException("Impossible de supprimer une commande confirmée.");
        }

        // ✅ DELETE INVOICE LINES
        List<LigneFactureAchat> factureLines = commande.getLignes().stream()
                .flatMap(ligne -> ligneFactureAchatRepository.findAll().stream()
                        .filter(facLigne -> facLigne.getFactureAchat().getCommandeAchat().getId().equals(id)))
                .toList();

        if (!factureLines.isEmpty()) {
            ligneFactureAchatRepository.deleteAll(factureLines);
        }

        // ✅ DELETE INVOICES
        List<FactureAchat> invoices = factureAchatRepository.findAll().stream()
                .filter(facture -> facture.getCommandeAchat() != null && facture.getCommandeAchat().getId().equals(id))
                .toList();

        if (!invoices.isEmpty()) {
            factureAchatRepository.deleteAll(invoices);
        }

        // ✅ DELETE ORDER LINES
        if (commande.getLignes() != null && !commande.getLignes().isEmpty()) {
            ligneCommandeAchatRepository.deleteAll(commande.getLignes());
        }

        // ✅ DELETE ORDER
        commandeAchatRepository.deleteById(id);
        logger.info("✅ Order deleted - ID: {}", id);
    }
}