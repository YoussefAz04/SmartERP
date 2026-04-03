package BuissnesManager.BuissnesManager.service;

import BuissnesManager.BuissnesManager.dto.CommandeAchatDto;
import BuissnesManager.BuissnesManager.dto.LigneCommandeAchatDto;
import BuissnesManager.BuissnesManager.entity.*;
import BuissnesManager.BuissnesManager.repository.*;
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
    private EntityManager entityManager;

    public List<CommandeAchat> getAllCommandes() {
        return commandeAchatRepository.findAll();
    }

    public Optional<CommandeAchat> getCommandeById(Long id) {
        return commandeAchatRepository.findById(id);
    }

    @Transactional
    public CommandeAchat createCommande(CommandeAchatDto dto) {
        // ✅ VALIDATE REQUIRED INPUTS FIRST
        if (dto.getEntrepriseId() == null) {
            logger.error("❌ Enterprise ID is required");
            throw new RuntimeException("L'entreprise est obligatoire. Veuillez vous reconnecter.");
        }

        if (dto.getFournisseurId() == null) {
            logger.error("❌ Fournisseur ID is required");
            throw new RuntimeException("Le fournisseur est obligatoire.");
        }

        logger.info("🔄 Creating commande");
        logger.info("📥 Received DTO: {}", dto);

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
        logger.info("✅ Supplier set: {}", fournisseur.getId());

        // ✅ SET ENTERPRISE
        Entreprise entreprise = entrepriseRepo.findById(dto.getEntrepriseId())
                .orElseThrow(() -> {
                    logger.error("❌ Enterprise not found with ID: {}", dto.getEntrepriseId());
                    return new RuntimeException("Entreprise non trouvée: " + dto.getEntrepriseId());
                });
        commande.setEntreprise(entreprise);
        logger.info("✅ Enterprise set: {}", entreprise.getId());

        // ✅ SAVE COMMANDE FIRST
        CommandeAchat saved = commandeAchatRepository.save(commande);
        logger.info("✅ Commande saved with ID: {}", saved.getId());

        // ✅ CREATE LINES WITH ARTICLE VALIDATION
        if (dto.getLignes() != null && !dto.getLignes().isEmpty()) {
            logger.info("📝 Processing {} lines", dto.getLignes().size());

            int lineCount = 0;
            for (int idx = 0; idx < dto.getLignes().size(); idx++) {
                final int lineNumber = idx + 1;
                LigneCommandeAchatDto ligneDto = dto.getLignes().get(idx);

                try {
                    logger.info("=== PROCESSING LINE {} ===", lineNumber);
                    logger.info("   articleId from DTO: {}", ligneDto.getArticleAchatId());
                    logger.info("   quantite from DTO: {}", ligneDto.getQuantite());
                    logger.info("   prixUnitaireHT from DTO: {}", ligneDto.getPrixUnitaireHT());

                    // ✅ VALIDATE ARTICLE ID
                    if (ligneDto.getArticleAchatId() == null) {
                        logger.error("❌ Line {}: articleId is NULL!", lineNumber);
                        throw new RuntimeException(
                                String.format("Ligne %d: articleId is NULL. Sélectionnez un article!", lineNumber)
                        );
                    }

                    if (ligneDto.getArticleAchatId() <= 0) {
                        logger.error("❌ Line {}: articleId is INVALID: {}", lineNumber, ligneDto.getArticleAchatId());
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

                    logger.info("✅ Line {}: Article found: ID={}, Nom={}", lineNumber, article.getId(), article.getNom());

                    // ✅ VALIDATE prixUnitaireHT BEFORE INSERTION
                    if (ligneDto.getPrixUnitaireHT() == null) {
                        logger.error("❌ Line {}: prixUnitaireHT is NULL from frontend DTO!", lineNumber);
                        logger.error("   Full DTO: {}", ligneDto);
                        throw new RuntimeException(
                                String.format("Ligne %d: Le prix unitaire HT est obligatoire. Veuillez saisir un prix.", lineNumber)
                        );
                    }

                    // ✅ VALIDATE prixUnitaireHT > 0
                    if (ligneDto.getPrixUnitaireHT().compareTo(BigDecimal.ZERO) <= 0) {
                        logger.error("❌ Line {}: prixUnitaireHT is <= 0: {}", lineNumber, ligneDto.getPrixUnitaireHT());
                        throw new RuntimeException(
                                String.format("Ligne %d: Le prix unitaire HT doit être supérieur à 0.", lineNumber)
                        );
                    }

                    logger.info("✅ Line {}: prixUnitaireHT validated: {}", lineNumber, ligneDto.getPrixUnitaireHT());

                    // ✅ CREATE LINE ITEM: SET SCALAR FIELDS FIRST
                    LigneCommandeAchat ligne = new LigneCommandeAchat();

                    ligne.setQuantite(ligneDto.getQuantite() != null ? ligneDto.getQuantite() : BigDecimal.ONE);
                    ligne.setPrixUnitaireHT(ligneDto.getPrixUnitaireHT());
                    ligne.setTva(ligneDto.getTva() != null ? ligneDto.getTva() : BigDecimal.ZERO);
                    ligne.setMontantHT(ligneDto.getMontantHT() != null ? ligneDto.getMontantHT() : BigDecimal.ZERO);
                    ligne.setMontantTTC(ligneDto.getMontantTTC() != null ? ligneDto.getMontantTTC() : BigDecimal.ZERO);

                    // ✅ SET RELATIONSHIPS AFTER SCALAR FIELDS
                    ligne.setArticle(article);
                    ligne.setCommandeAchat(saved);

                    logger.info("📝 Line {}: Before save - prixUnitaireHT={}, article={}",
                            lineNumber, ligne.getPrixUnitaireHT(), ligne.getArticle().getId());

                    // ✅ SAVE LINE
                    LigneCommandeAchat savedLigne = ligneCommandeAchatRepository.saveAndFlush(ligne);

                    logger.info("📝 Line {}: After save - prixUnitaireHT={}, ID={}",
                            lineNumber, savedLigne.getPrixUnitaireHT(), savedLigne.getId());

                    lineCount++;
                    logger.info("✅ Line {} saved successfully", lineNumber);
                    logger.info("=== END LINE {} ===", lineNumber);

                } catch (RuntimeException e) {
                    logger.error("❌ Error processing line {}: {}", lineNumber, e.getMessage());
                    throw e;
                } catch (Exception e) {
                    logger.error("❌ Unexpected error saving line {}: {}", lineNumber, e.getMessage(), e);
                    throw new RuntimeException(
                            String.format("Erreur ligne %d: %s", lineNumber, e.getMessage())
                    );
                }
            }

            if (lineCount == 0) {
                logger.error("❌ CRITICAL: No lines were saved!");
                throw new RuntimeException("Aucune ligne n'a pu être sauvegardée. Vérifiez vos données.");
            }

            logger.info("✅ All {} lines saved successfully", lineCount);
        } else {
            logger.error("❌ No lines provided in commande");
            throw new RuntimeException("Au moins une ligne de commande est requise.");
        }

        logger.info("✅ Commande creation complete with ID: {}", saved.getId());
        return saved;
    }

    public List<CommandeAchat> getCommandesByEntreprise(Long entrepriseId) {
        return commandeAchatRepository.findByEntrepriseId(entrepriseId);
    }

    @Transactional
    public CommandeAchat updateCommande(Long id, CommandeAchatDto dto) {
        CommandeAchat commande = commandeAchatRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Commande non trouvée avec id: " + id));

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
                .orElseThrow(() -> new RuntimeException("Commande non trouvée avec id: " + id));
        commande.setStatut(statut);
        return commandeAchatRepository.save(commande);
    }

    @Transactional
    public FactureAchat validerCommande(Long id) {
        CommandeAchat commande = commandeAchatRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Commande non trouvée avec id: " + id));

        // ✅ CHECK CONSTRAINT VALID VALUES: 'BROUILLON', 'EN_ATTENTE', 'CONFIRMEE', 'RECUE', 'ANNULEE'
        if ("CONFIRMEE".equals(commande.getStatut())) {
            throw new RuntimeException("Cette commande a déjà été confirmée.");
        }
        if ("ANNULEE".equals(commande.getStatut())) {
            throw new RuntimeException("Impossible de confirmer une commande annulée.");
        }

        // ✅ CHANGE TO CONFIRMEE (VALID CHECK CONSTRAINT VALUE)
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

        // ✅ CREATE INVOICE LINES FROM ORDER LINES
        if (commande.getLignes() != null && !commande.getLignes().isEmpty()) {
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

                ligneFactureAchatRepository.saveAndFlush(ligneFacture);
            }
        }

        logger.info("✅ Commande {} confirmed, facture {} created", id, savedFacture.getId());
        return savedFacture;
    }

    @Transactional
    public CommandeAchat annulerCommande(Long id) {
        CommandeAchat commande = commandeAchatRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Commande non trouvée avec id: " + id));

        if ("ANNULEE".equals(commande.getStatut())) {
            throw new RuntimeException("Cette commande est déjà annulée.");
        }

        commande.setStatut("ANNULEE");
        logger.info("✅ Commande {} cancelled", id);
        return commandeAchatRepository.save(commande);
    }

    @Transactional
    public void deleteCommande(Long id) {
        CommandeAchat commande = commandeAchatRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Commande non trouvée avec id: " + id));

        // ✅ BEST APPROACH: Check if order is CONFIRMED (has associated invoice)
        // Prevent deletion if it has invoices (business data integrity)
        if ("CONFIRMEE".equals(commande.getStatut())) {
            throw new RuntimeException(
                    "Impossible de supprimer une commande confirmée. Veuillez d'abord annuler la commande."
            );
        }

        // ✅ STEP 1: Delete all associated invoice lines (if any exist)
        List<LigneFactureAchat> factureLines = commande.getLignes().stream()
                .flatMap(ligne -> ligneFactureAchatRepository.findAll().stream()
                        .filter(facLigne -> facLigne.getFactureAchat().getCommandeAchat().getId().equals(id)))
                .toList();

        if (!factureLines.isEmpty()) {
            ligneFactureAchatRepository.deleteAll(factureLines);
            logger.info("✅ All associated invoice lines deleted for commande {}", id);
        }

        // ✅ STEP 2: Delete all associated invoices
        List<FactureAchat> invoices = factureAchatRepository.findAll().stream()
                .filter(facture -> facture.getCommandeAchat() != null && facture.getCommandeAchat().getId().equals(id))
                .toList();

        if (!invoices.isEmpty()) {
            factureAchatRepository.deleteAll(invoices);
            logger.info("✅ All associated invoices deleted for commande {}", id);
        }

        // ✅ STEP 3: Delete all order lines
        if (commande.getLignes() != null && !commande.getLignes().isEmpty()) {
            ligneCommandeAchatRepository.deleteAll(commande.getLignes());
            logger.info("✅ All lines deleted for commande {}", id);
        }

        // ✅ STEP 4: Delete the order itself
        commandeAchatRepository.deleteById(id);
        logger.info("✅ Commande {} and all associated data deleted", id);
    }
}