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
    private ArticleAchatRepo articleAchatRepo;

    @Autowired
    private EntrepriseRepo entrepriseRepo;

    @Autowired
    private EntityManager entityManager;  // ✅ ADD THIS

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
                    logger.info("   articleAchatId from DTO: {}", ligneDto.getArticleAchatId());
                    logger.info("   quantite from DTO: {}", ligneDto.getQuantite());
                    logger.info("   prixUnitaireHT from DTO: {}", ligneDto.getPrixUnitaireHT());

                    // ✅ VALIDATE ARTICLE ID - DO NOT SKIP, THROW ERROR INSTEAD
                    if (ligneDto.getArticleAchatId() == null) {
                        logger.error("❌ Line {}: articleAchatId is NULL!", lineNumber);
                        throw new RuntimeException(
                                String.format("Ligne %d: articleAchatId is NULL. Sélectionnez un article!", lineNumber)
                        );
                    }

                    if (ligneDto.getArticleAchatId() <= 0) {
                        logger.error("❌ Line {}: articleAchatId is INVALID: {}", lineNumber, ligneDto.getArticleAchatId());
                        throw new RuntimeException(
                                String.format("Ligne %d: Article ID invalide: %d", lineNumber, ligneDto.getArticleAchatId())
                        );
                    }

                    // ✅ FETCH AND VALIDATE THE ARTICLE EXISTS
                    ArticleAchat article = articleAchatRepo.findById(ligneDto.getArticleAchatId())
                            .orElseThrow(() -> {
                                logger.error("❌ Line {}: Article not found with ID: {}", lineNumber, ligneDto.getArticleAchatId());
                                return new RuntimeException(
                                        String.format("Ligne %d: Article non trouvé avec l'ID: %d",
                                                lineNumber, ligneDto.getArticleAchatId())
                                );
                            });

                    logger.info("✅ Line {}: Article found: ID={}, Nom={}", lineNumber, article.getId(), article.getNom());

                    // ✅ CREATE LINE ITEM WITH ALL VALIDATIONS
                    LigneCommandeAchat ligne = new LigneCommandeAchat();

                    // ✅ KEY FIX: Merge the article to ensure it's managed by Hibernate
                    ArticleAchat managedArticle = entityManager.merge(article);
                    ligne.setArticleAchat(managedArticle);

                    ligne.setQuantite(ligneDto.getQuantite() != null ? ligneDto.getQuantite() : BigDecimal.ONE);
                    ligne.setPrixUnitaireHT(ligneDto.getPrixUnitaireHT() != null ? ligneDto.getPrixUnitaireHT() : BigDecimal.ZERO);
                    ligne.setTva(ligneDto.getTva() != null ? ligneDto.getTva() : BigDecimal.ZERO);
                    ligne.setMontantHT(ligneDto.getMontantHT() != null ? ligneDto.getMontantHT() : BigDecimal.ZERO);
                    ligne.setMontantTTC(ligneDto.getMontantTTC() != null ? ligneDto.getMontantTTC() : BigDecimal.ZERO);
                    ligne.setCommandeAchat(saved);

                    logger.info("📝 Line {}: Setting articleAchat to ID={}", lineNumber, managedArticle.getId());
                    logger.info("📝 Line {}: Before save - articleAchat={}", lineNumber, ligne.getArticleAchat());

                    // ✅ SAVE LINE
                    LigneCommandeAchat savedLigne = ligneCommandeAchatRepository.save(ligne);

                    logger.info("📝 Line {}: After save - ID={}, articleAchat={}", 
                            lineNumber, savedLigne.getId(), savedLigne.getArticleAchat());

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

    public void deleteCommande(Long id) {
        if (!commandeAchatRepository.existsById(id)) {
            throw new RuntimeException("Commande non trouvée avec id: " + id);
        }
        commandeAchatRepository.deleteById(id);
    }
}