package BuissnesManager.BuissnesManager.service;

import BuissnesManager.BuissnesManager.dto.CommandeVenteDto;
import BuissnesManager.BuissnesManager.dto.LigneCommandeVenteDto;
import BuissnesManager.BuissnesManager.entity.*;
import BuissnesManager.BuissnesManager.repository.*;
import BuissnesManager.BuissnesManager.security.EnterpriseSecurityUtil;
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
public class CommandeVenteService {

    private static final Logger logger = LoggerFactory.getLogger(CommandeVenteService.class);

    @Autowired
    private CommandeVenteRepository commandeVenteRepository;
    @Autowired
    private LigneCommandeVenteRepository ligneCommandeVenteRepository;
    @Autowired
    private ClientRepo clientRepo;
    @Autowired
    private EntrepriseRepo entrepriseRepo;
    @Autowired
    private ArticleVenteRepo articleVenteRepo;
    @Autowired
    private FactureVenteRepository factureVenteRepository;
    @Autowired
    private LigneFactureVenteRepository ligneFactureVenteRepository;
    @Autowired
    private BonLivraisonRepository bonLivraisonRepository;
    @Autowired
    private StockAutomationService stockAutomationService;
    
    @Autowired
    private FactureDeVenteRepo factureDeVenteRepository;

    @Autowired
    private EnterpriseSecurityUtil enterpriseSecurityUtil;

    @Autowired
    private NotificationService notificationService;

    @Transactional
    public CommandeVente createCommande(CommandeVenteDto dto) {
        CommandeVente commande = new CommandeVente();
        commande.setReference(dto.getReference());
        commande.setDateCommande(dto.getDateCommande());
        commande.setDateLivraisonPrevue(dto.getDateLivraisonPrevue());
        commande.setStatut(dto.getStatut() != null ? dto.getStatut() : "EN_ATTENTE");
        commande.setMontantHT(dto.getMontantHT() != null ? dto.getMontantHT() : BigDecimal.ZERO);
        commande.setMontantTVA(dto.getMontantTVA() != null ? dto.getMontantTVA() : BigDecimal.ZERO);
        commande.setMontantTTC(dto.getMontantTTC() != null ? dto.getMontantTTC() : BigDecimal.ZERO);
        commande.setNote(dto.getNote());
        
        if (dto.getClientId() != null) {
            Client client = clientRepo.findById(dto.getClientId())
                    .orElseThrow(() -> new RuntimeException("Client non trouvé: " + dto.getClientId()));
            commande.setClient(client);
        }
        
        // ✅ Set Entreprise - from JWT or DTO
        final Long resolvedEntrepriseId = dto.getEntrepriseId() != null
                ? dto.getEntrepriseId()
                : enterpriseSecurityUtil.getContextEnterpriseId();
        if (resolvedEntrepriseId != null) {
            Entreprise entreprise = entrepriseRepo.findById(resolvedEntrepriseId)
                    .orElseThrow(() -> new RuntimeException("Entreprise non trouvée: " + resolvedEntrepriseId));
            commande.setEntreprise(entreprise);
        } else {
            var entreprises = entrepriseRepo.findAll();
            if (entreprises.isEmpty()) {
                throw new RuntimeException("Aucune entreprise trouvée dans la base de données.");
            }
            commande.setEntreprise(entreprises.get(0));
        }
        
        CommandeVente saved = commandeVenteRepository.save(commande);

        if (dto.getLignes() != null && !dto.getLignes().isEmpty()) {
            for (LigneCommandeVenteDto ligneDto : dto.getLignes()) {
                LigneCommandeVente ligne = new LigneCommandeVente();
                ligne.setQuantite(ligneDto.getQuantite() != null ? ligneDto.getQuantite() : BigDecimal.ZERO);
                ligne.setPrixUnitaireHT(ligneDto.getPrixUnitaireHT() != null ? ligneDto.getPrixUnitaireHT() : BigDecimal.ZERO);
                ligne.setTva(ligneDto.getTva() != null ? ligneDto.getTva() : BigDecimal.ZERO);
                ligne.setMontantHT(ligneDto.getMontantHT() != null ? ligneDto.getMontantHT() : BigDecimal.ZERO);
                ligne.setMontantTTC(ligneDto.getMontantTTC() != null ? ligneDto.getMontantTTC() : BigDecimal.ZERO);
                ligne.setCommandeVente(saved);
                if (ligneDto.getArticleVenteId() != null) {
                    ArticleVente articleVente = articleVenteRepo.findById(ligneDto.getArticleVenteId())
                            .orElseThrow(() -> new RuntimeException("ArticleVente non trouvé: " + ligneDto.getArticleVenteId()));
                    ligne.setArticleVente(articleVente);
                }
                ligneCommandeVenteRepository.save(ligne);
            }
        }

        // Reserve stock for the created order
        try {
            stockAutomationService.reserverStockPourCommande(saved.getId(), saved.getReference());
        } catch (RuntimeException e) {
            logger.error("❌ Stock reservation failed for commande {} ({}): {}", saved.getId(), saved.getReference(), e.getMessage(), e);
            throw e;
        }

        // ✅ AUTO-CREATE preliminary FactureDeVente (EN_ATTENTE) so it appears in the factures list immediately
        FactureDeVente factureDeVente = new FactureDeVente();
        factureDeVente.setDateAchat(saved.getDateCommande() != null ? saved.getDateCommande().toString() : LocalDate.now().toString());
        factureDeVente.setMontantHT(dto.getMontantHT() != null ? dto.getMontantHT().doubleValue() : 0.0);
        factureDeVente.setMontantTVA(dto.getMontantTVA() != null ? dto.getMontantTVA().doubleValue() : 0.0);
        factureDeVente.setMontantTTC(dto.getMontantTTC() != null ? dto.getMontantTTC().doubleValue() : 0.0);
        factureDeVente.setMontantTotal(dto.getMontantTTC() != null ? dto.getMontantTTC().doubleValue() : 0.0);
        factureDeVente.setStatut("EN_ATTENTE");
        factureDeVente.setModePaiement("A_DEFINIR");
        factureDeVente.setDescription("Facture générée depuis la commande " + saved.getReference());
        factureDeVente.setClient(saved.getClient());
        factureDeVente.setEntreprise(saved.getEntreprise());
        factureDeVente.setCommandeVente(saved);
        FactureDeVente savedFacture = factureDeVenteRepository.save(factureDeVente);
        String referance = "FV-" + String.format("%04d", savedFacture.getId());
        savedFacture.setReferance(referance);
        factureDeVenteRepository.save(savedFacture);
        logger.info("✅ Preliminary FactureDeVente created for commande {} (enterprise: {})", saved.getId(), saved.getEntreprise() != null ? saved.getEntreprise().getId() : null);

        if (saved.getEntreprise() != null) {
            notificationService.createCommandeVenteNotification(
                    BuissnesManager.BuissnesManager.entity.NotificationType.COMMANDE_VENTE_CREEE,
                    saved.getReference(),
                    saved.getEntreprise());
        }

        return saved;
    }

    @Transactional
    public List<CommandeVente> getAllCommandes() {
        Long entrepriseId = enterpriseSecurityUtil.getContextEnterpriseId();
        List<CommandeVente> commandes;
        if (entrepriseId == null) {
            commandes = commandeVenteRepository.findAll();
        } else {
            commandes = commandeVenteRepository.findByEntrepriseId(entrepriseId);
        }
        // Recalculate totals for commandes with missing amounts and batch save
        List<CommandeVente> toSave = new java.util.ArrayList<>();
        for (CommandeVente commande : commandes) {
            if (recalculateTotalsIfMissing(commande)) {
                toSave.add(commande);
            }
        }
        if (!toSave.isEmpty()) {
            commandeVenteRepository.saveAll(toSave);
        }
        return commandes;
    }

    /**
     * Recalculates montantHT, montantTVA, montantTTC from line items
     * when these fields are null or zero.
     * @return true if the commande was modified and needs saving
     */
    private boolean recalculateTotalsIfMissing(CommandeVente commande) {
        boolean hasMissingAmounts = commande.getMontantHT() == null
                || commande.getMontantTVA() == null
                || commande.getMontantTTC() == null
                || (commande.getMontantHT().compareTo(BigDecimal.ZERO) == 0
                    && commande.getMontantTVA().compareTo(BigDecimal.ZERO) == 0
                    && commande.getMontantTTC().compareTo(BigDecimal.ZERO) == 0);

        if (!hasMissingAmounts) {
            return false;
        }

        List<LigneCommandeVente> lignes = commande.getLignes();
        if (lignes == null || lignes.isEmpty()) {
            return false;
        }

        BigDecimal totalHT = BigDecimal.ZERO;
        BigDecimal totalTTC = BigDecimal.ZERO;

        for (LigneCommandeVente ligne : lignes) {
            BigDecimal ht = ligne.getMontantHT();
            BigDecimal ttc = ligne.getMontantTTC();

            // If line amounts are missing, calculate from quantity * unit price
            if (ht == null || ht.compareTo(BigDecimal.ZERO) == 0) {
                BigDecimal quantite = ligne.getQuantite() != null ? ligne.getQuantite() : BigDecimal.ZERO;
                BigDecimal prix = ligne.getPrixUnitaireHT() != null ? ligne.getPrixUnitaireHT() : BigDecimal.ZERO;
                ht = quantite.multiply(prix);
            }
            if (ttc == null || ttc.compareTo(BigDecimal.ZERO) == 0) {
                BigDecimal tva = ligne.getTva() != null ? ligne.getTva() : BigDecimal.ZERO;
                ttc = ht.multiply(BigDecimal.ONE.add(tva.divide(BigDecimal.valueOf(100), 4, java.math.RoundingMode.HALF_UP)));
            }

            totalHT = totalHT.add(ht);
            totalTTC = totalTTC.add(ttc);
        }

        BigDecimal totalTVA = totalTTC.subtract(totalHT);
        commande.setMontantHT(totalHT);
        commande.setMontantTVA(totalTVA);
        commande.setMontantTTC(totalTTC);
        return true;
    }

    public List<CommandeVente> getCommandesByEntreprise(Long entrepriseId) {
        return commandeVenteRepository.findByEntrepriseId(entrepriseId);
    }

    public Optional<CommandeVente> getCommandeById(Long id) {
        return commandeVenteRepository.findById(id);
    }

    private static final java.util.Set<String> VALID_STATUTS = java.util.Set.of(
            "EN_ATTENTE", "CONFIRMEE", "LIVREE", "ANNULEE"
    );

    public CommandeVente updateStatut(Long id, String statut) {
        if (!VALID_STATUTS.contains(statut)) {
            throw new RuntimeException("Statut invalide: " + statut + ". Statuts autorisés: " + VALID_STATUTS);
        }
        CommandeVente commande = commandeVenteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("CommandeVente non trouvée avec id: " + id));

        // Enforce valid transitions
        String currentStatut = commande.getStatut();
        if ("ANNULEE".equals(currentStatut)) {
            throw new RuntimeException("Une commande annulée ne peut pas être modifiée.");
        }
        if ("LIVREE".equals(currentStatut)) {
            throw new RuntimeException("Une commande livrée ne peut être modifiée que via l'annulation.");
        }
        // Lifecycle transitions with side effects (facture creation, stock moves, bon de livraison)
        // must go through the dedicated methods, not a raw status set — otherwise we would
        // skip stock decrement on delivery or skip releasing reservations on cancellation.
        if ("CONFIRMEE".equals(statut) && !"CONFIRMEE".equals(currentStatut)) {
            throw new RuntimeException("Utilisez l'endpoint de validation (/valider) pour confirmer une commande.");
        }
        if ("LIVREE".equals(statut)) {
            throw new RuntimeException("Utilisez l'endpoint de livraison (/livrer) pour livrer une commande.");
        }
        if ("ANNULEE".equals(statut)) {
            throw new RuntimeException("Utilisez l'endpoint d'annulation (/annuler) pour annuler une commande.");
        }

        commande.setStatut(statut);
        return commandeVenteRepository.save(commande);
    }

    public CommandeVente updateCommande(Long id, CommandeVenteDto dto) {
        CommandeVente commande = commandeVenteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("CommandeVente non trouvée avec id: " + id));
        if (dto.getReference() != null) commande.setReference(dto.getReference());
        if (dto.getDateCommande() != null) commande.setDateCommande(dto.getDateCommande());
        if (dto.getDateLivraisonPrevue() != null) commande.setDateLivraisonPrevue(dto.getDateLivraisonPrevue());
        // Status changes must go through updateStatut/validerCommande/annulerCommande/livrerCommande
        if (dto.getMontantHT() != null) commande.setMontantHT(dto.getMontantHT());
        if (dto.getMontantTVA() != null) commande.setMontantTVA(dto.getMontantTVA());
        if (dto.getMontantTTC() != null) commande.setMontantTTC(dto.getMontantTTC());
        if (dto.getNote() != null) commande.setNote(dto.getNote());
        return commandeVenteRepository.save(commande);
    }

    @Transactional
    public void deleteCommande(Long id) {
        CommandeVente commande = commandeVenteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("CommandeVente non trouvée avec id: " + id));

        // Release stock reservations if the order has not been delivered
        if (!"LIVREE".equals(commande.getStatut()) && !"ANNULEE".equals(commande.getStatut())) {
            try {
                stockAutomationService.annulerReservationStock(id, commande.getReference());
                logger.info("✅ Stock reservations released for deleted commande {}", id);
            } catch (RuntimeException e) {
                logger.warn("⚠️ Could not fully release stock reservation for commande {}: {}", id, e.getMessage());
            }
        }

        // Nullify commandeVente reference on associated FactureDeVente before deleting commande
        factureDeVenteRepository.findByCommandeVenteId(id).ifPresent(facture -> {
            facture.setCommandeVente(null);
            factureDeVenteRepository.save(facture);
        });
        // Nullify commandeVente reference on associated FactureVente records before deleting commande
        List<FactureVente> factures = factureVenteRepository.findAllByCommandeVenteId(id);
        for (FactureVente facture : factures) {
            facture.setCommandeVente(null);
            factureVenteRepository.save(facture);
        }
        // Nullify commandeVente reference on associated BonLivraison before deleting commande
        List<BonLivraison> bons = bonLivraisonRepository.findByCommandeVenteId(id);
        for (BonLivraison bon : bons) {
            bon.setCommandeVente(null);
            bonLivraisonRepository.save(bon);
        }
        ligneCommandeVenteRepository.deleteByCommandeVenteId(id);
        commandeVenteRepository.deleteById(id);
    }

    @Transactional
    public FactureDeVente validerCommande(Long id) {
        CommandeVente commande = commandeVenteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("CommandeVente non trouvée avec id: " + id));

        if ("CONFIRMEE".equals(commande.getStatut()) || "LIVREE".equals(commande.getStatut())) {
            throw new RuntimeException("Cette commande a déjà été confirmée ou livrée.");
        }
        if ("ANNULEE".equals(commande.getStatut())) {
            throw new RuntimeException("Impossible de confirmer une commande annulée.");
        }

        // ✅ Change status to CONFIRMEE
        commande.setStatut("CONFIRMEE");
        commandeVenteRepository.save(commande);

        // ✅ Calculate totals from order lines
        List<LigneCommandeVente> lignesCommande = ligneCommandeVenteRepository.findByCommandeVenteId(id);
        
        Double totalHT = 0.0;
        Double totalTTC = 0.0;
        
        for (LigneCommandeVente ligne : lignesCommande) {
            BigDecimal ht = ligne.getMontantHT() != null ? ligne.getMontantHT() : BigDecimal.ZERO;
            BigDecimal ttc = ligne.getMontantTTC() != null ? ligne.getMontantTTC() : BigDecimal.ZERO;
            
            totalHT += ht.doubleValue();
            totalTTC += ttc.doubleValue();
        }

        // TVA is the difference between TTC and HT (not the sum of tax rates)
        Double totalTVA = totalTTC - totalHT;

        // ✅ Update existing FactureDeVente if created at commande creation, otherwise create new
        FactureDeVente facture = factureDeVenteRepository.findByCommandeVenteId(id)
                .orElse(new FactureDeVente());
        facture.setDateAchat(LocalDate.now().toString());
        facture.setMontantHT(totalHT);
        facture.setMontantTVA(totalTVA);
        facture.setMontantTTC(totalTTC);
        facture.setMontantTotal(totalTTC);
        facture.setStatut("EN_ATTENTE");
        facture.setDescription("Facture générée automatiquement depuis la commande " + commande.getReference());
        facture.setClient(commande.getClient());
        facture.setModePaiement("A_DEFINIR");
        facture.setEntreprise(commande.getEntreprise());
        facture.setCommandeVente(commande);
        
        FactureDeVente savedFacture = factureDeVenteRepository.save(facture);
        
        // ✅ Generate reference if not already set
        if (savedFacture.getReferance() == null || savedFacture.getReferance().isEmpty()) {
            String referance = "FV-" + String.format("%04d", savedFacture.getId());
            savedFacture.setReferance(referance);
            savedFacture = factureDeVenteRepository.save(savedFacture);
        }

        // ✅ Update stock: move reserved quantity to sold (quantite_reserve → quantite_vendue)
        // is handled at delivery (livrerCommande), not at confirmation.
        // At confirmation, the order is still "reserved" in stock terms.

        logger.info("✅ Commande {} confirmed, facture {} updated/created with amounts: HT={}, TVA={}, TTC={}",
            id, savedFacture.getId(), totalHT, totalTVA, totalTTC);
        
        return savedFacture;
    }

    public CommandeVente annulerCommande(Long id) {
        CommandeVente commande = commandeVenteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("CommandeVente non trouvée avec id: " + id));
        if ("ANNULEE".equals(commande.getStatut())) {
            throw new RuntimeException("Cette commande est déjà annulée.");
        }
        String ancienStatut = commande.getStatut();
        commande.setStatut("ANNULEE");
        CommandeVente saved = commandeVenteRepository.save(commande);

        // Cancel associated invoice
        factureDeVenteRepository.findByCommandeVenteId(id).ifPresent(facture -> {
            facture.setStatut("ANNULEE");
            factureDeVenteRepository.save(facture);
            logger.info("✅ Associated FactureDeVente {} cancelled", facture.getId());
        });

        // Release stock reservations if the order was not yet delivered
        if (!"LIVREE".equals(ancienStatut)) {
            try {
                stockAutomationService.annulerReservationStock(id, commande.getReference());
            } catch (RuntimeException e) {
                logger.warn("⚠️ Could not fully release stock reservation for commande {}: {}", id, e.getMessage());
            }
        }
        if (saved.getEntreprise() != null) {
            notificationService.createCommandeVenteNotification(
                    BuissnesManager.BuissnesManager.entity.NotificationType.COMMANDE_VENTE_ANNULEE,
                    saved.getReference(),
                    saved.getEntreprise());
        }
        return saved;
    }

    @Transactional
    public BonLivraison livrerCommande(Long id) {
        CommandeVente commande = commandeVenteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("CommandeVente non trouvée avec id: " + id));

        if ("ANNULEE".equals(commande.getStatut())) {
            throw new RuntimeException("Impossible de livrer une commande annulée.");
        }
        if ("LIVREE".equals(commande.getStatut())) {
            throw new RuntimeException("Cette commande a déjà été livrée.");
        }
        if (!"CONFIRMEE".equals(commande.getStatut())) {
            throw new RuntimeException("La commande doit être confirmée avant d'être livrée. Statut actuel: " + commande.getStatut());
        }

        commande.setStatut("LIVREE");
        commandeVenteRepository.save(commande);

        BonLivraison bon = new BonLivraison();
        bon.setReference("BL-" + commande.getReference());
        bon.setDateLivraison(LocalDate.now());
        bon.setStatut("LIVRE");
        bon.setCommandeVente(commande);
        bon.setEntreprise(commande.getEntreprise());
        BonLivraison savedBon = bonLivraisonRepository.save(bon);

        // ✅ Move stock from reserved → sold at the actual physical delivery
        stockAutomationService.decrementStockFromDelivery(id, commande.getReference());

        if (commande.getEntreprise() != null) {
            notificationService.createCommandeVenteNotification(
                    BuissnesManager.BuissnesManager.entity.NotificationType.COMMANDE_VENTE_LIVREE,
                    commande.getReference(),
                    commande.getEntreprise());
        }

        logger.info("✅ Commande {} delivered, BonLivraison {} created and stock decremented",
                id, savedBon.getId());
        return savedBon;
    }

    public BigDecimal calculateTotal(Long commandeId) {
        List<LigneCommandeVente> lignes = ligneCommandeVenteRepository.findByCommandeVenteId(commandeId);
        return lignes.stream()
                .map(l -> l.getMontantTTC() != null ? l.getMontantTTC() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}