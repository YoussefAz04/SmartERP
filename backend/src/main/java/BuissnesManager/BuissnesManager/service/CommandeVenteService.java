package BuissnesManager.BuissnesManager.service;

import BuissnesManager.BuissnesManager.dto.CommandeVenteDto;
import BuissnesManager.BuissnesManager.dto.CommandeVenteResponseDto;
import BuissnesManager.BuissnesManager.dto.LigneCommandeVenteDto;
import BuissnesManager.BuissnesManager.dto.LivraisonResponseDto;
import BuissnesManager.BuissnesManager.dto.StockAlertDto;
import BuissnesManager.BuissnesManager.dto.ValidationResponseDto;
import BuissnesManager.BuissnesManager.entity.*;
import BuissnesManager.BuissnesManager.repository.*;
import BuissnesManager.BuissnesManager.security.EnterpriseSecurityUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
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
    private StockRepository stockRepository;
    
    @Autowired
    private FactureDeVenteRepo factureDeVenteRepository;

    @Autowired
    private EnterpriseSecurityUtil enterpriseSecurityUtil;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private AuditLogService auditLogService;

    @Caching(evict = {
        @CacheEvict(cacheNames = "productIntelligence", allEntries = true),
        @CacheEvict(cacheNames = "salesAnalysis",       allEntries = true),
        @CacheEvict(cacheNames = "salesForecast",       allEntries = true)
    })
    @Transactional
    public CommandeVente createCommande(CommandeVenteDto dto) {
        CommandeVente commande = new CommandeVente();
        commande.setReference(dto.getReference());
        commande.setDateCommande(dto.getDateCommande());
        commande.setDateLivraisonPrevue(dto.getDateLivraisonPrevue());
        
        
        
        
        commande.setStatut("EN_ATTENTE");
        commande.setMontantHT(dto.getMontantHT() != null ? dto.getMontantHT() : BigDecimal.ZERO);
        commande.setMontantTVA(dto.getMontantTVA() != null ? dto.getMontantTVA() : BigDecimal.ZERO);
        commande.setMontantTTC(dto.getMontantTTC() != null ? dto.getMontantTTC() : BigDecimal.ZERO);
        commande.setNote(dto.getNote());
        
        
        
        
        if (dto.getClientId() == null) {
            throw new RuntimeException("Le client est obligatoire.");
        }
        Client client = clientRepo.findById(dto.getClientId())
                .orElseThrow(() -> new RuntimeException("Client non trouvé: " + dto.getClientId()));
        commande.setClient(client);
        
        
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
            for (int idx = 0; idx < dto.getLignes().size(); idx++) {
                final int lineNumber = idx + 1;
                LigneCommandeVenteDto ligneDto = dto.getLignes().get(idx);

                
                
                
                if (ligneDto.getQuantite() == null || ligneDto.getQuantite().compareTo(BigDecimal.ZERO) <= 0) {
                    throw new RuntimeException(
                            String.format("Ligne %d: La quantité doit être supérieure à 0.", lineNumber));
                }
                if (ligneDto.getPrixUnitaireHT() == null || ligneDto.getPrixUnitaireHT().compareTo(BigDecimal.ZERO) <= 0) {
                    throw new RuntimeException(
                            String.format("Ligne %d: Le prix unitaire HT doit être supérieur à 0.", lineNumber));
                }

                LigneCommandeVente ligne = new LigneCommandeVente();
                ligne.setQuantite(ligneDto.getQuantite());
                ligne.setPrixUnitaireHT(ligneDto.getPrixUnitaireHT());
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

        auditLogService.log("COMMANDE_VENTE_CREEE",
                "Commande de vente créée: " + saved.getReference() + " (id=" + saved.getId() + ")");

        return saved;
    }

    @Transactional
    public List<CommandeVente> getAllCommandes() {
        Long entrepriseId = enterpriseSecurityUtil.getContextEnterpriseId();
        if (entrepriseId == null) {
            throw new RuntimeException("Enterprise context required");
        }
        List<CommandeVente> commandes = commandeVenteRepository.findByEntrepriseId(entrepriseId);
        
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

    @Transactional
    public List<CommandeVenteResponseDto> getAllCommandesResponse() {
        return getAllCommandes().stream()
                .map(commande -> toResponseDto(commande, false))
                .toList();
    }

    @Transactional(readOnly = true)
    public Optional<CommandeVenteResponseDto> getCommandeResponseById(Long id) {
        return commandeVenteRepository.findById(id)
                .map(commande -> toResponseDto(commande, true));
    }

    
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

    private CommandeVenteResponseDto toResponseDto(CommandeVente commande, boolean includeLignes) {
        CommandeVenteResponseDto dto = new CommandeVenteResponseDto();
        dto.setId(commande.getId());
        dto.setReference(commande.getReference());
        dto.setDateCommande(commande.getDateCommande());
        dto.setDateLivraisonPrevue(commande.getDateLivraisonPrevue());
        dto.setStatut(commande.getStatut());
        dto.setMontantHT(commande.getMontantHT());
        dto.setMontantTVA(commande.getMontantTVA());
        dto.setMontantTTC(commande.getMontantTTC());
        dto.setNote(commande.getNote());
        dto.setCreatedAt(commande.getCreatedAt());
        dto.setUpdatedAt(commande.getUpdatedAt());

        Client client = commande.getClient();
        if (client != null) {
            dto.setClient(new CommandeVenteResponseDto.ClientInfo(
                    client.getId(),
                    client.getNom(),
                    client.getPrenom()
            ));
        }

        if (includeLignes && commande.getLignes() != null) {
            dto.setLignes(commande.getLignes().stream()
                    .map(this::toLigneResponseDto)
                    .toList());
        }

        return dto;
    }

    private CommandeVenteResponseDto.LigneInfo toLigneResponseDto(LigneCommandeVente ligne) {
        CommandeVenteResponseDto.LigneInfo dto = new CommandeVenteResponseDto.LigneInfo();
        dto.setId(ligne.getId());
        dto.setQuantite(ligne.getQuantite());
        dto.setPrixUnitaireHT(ligne.getPrixUnitaireHT());
        dto.setTva(ligne.getTva());
        dto.setMontantHT(ligne.getMontantHT());
        dto.setMontantTTC(ligne.getMontantTTC());

        ArticleVente articleVente = ligne.getArticleVente();
        if (articleVente != null) {
            dto.setArticleVente(new CommandeVenteResponseDto.ArticleVenteInfo(
                    articleVente.getId(),
                    articleVente.getNom()
            ));
        }

        return dto;
    }

    private static final java.util.Set<String> VALID_STATUTS = java.util.Set.of(
            "EN_ATTENTE", "CONFIRMEE", "LIVREE", "ANNULEE"
    );

    @Caching(evict = {
        @CacheEvict(cacheNames = "productIntelligence", allEntries = true),
        @CacheEvict(cacheNames = "salesAnalysis",       allEntries = true),
        @CacheEvict(cacheNames = "salesForecast",       allEntries = true)
    })
    public CommandeVente updateStatut(Long id, String statut) {
        if (!VALID_STATUTS.contains(statut)) {
            throw new RuntimeException("Statut invalide: " + statut + ". Statuts autorisés: " + VALID_STATUTS);
        }
        CommandeVente commande = commandeVenteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("CommandeVente non trouvée avec id: " + id));

        
        String currentStatut = commande.getStatut();
        if ("ANNULEE".equals(currentStatut)) {
            throw new RuntimeException("Une commande annulée ne peut pas être modifiée.");
        }
        if ("LIVREE".equals(currentStatut)) {
            throw new RuntimeException("Une commande livrée ne peut être modifiée que via l'annulation.");
        }
        
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

    @Caching(evict = {
        @CacheEvict(cacheNames = "productIntelligence", allEntries = true),
        @CacheEvict(cacheNames = "salesAnalysis",       allEntries = true),
        @CacheEvict(cacheNames = "salesForecast",       allEntries = true)
    })
    @Transactional
    public CommandeVente updateCommande(Long id, CommandeVenteDto dto) {
        CommandeVente commande = commandeVenteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("CommandeVente non trouvée avec id: " + id));
        if (dto.getReference() != null) commande.setReference(dto.getReference());
        if (dto.getDateCommande() != null) commande.setDateCommande(dto.getDateCommande());
        if (dto.getDateLivraisonPrevue() != null) commande.setDateLivraisonPrevue(dto.getDateLivraisonPrevue());
        
        if (dto.getMontantHT() != null) commande.setMontantHT(dto.getMontantHT());
        if (dto.getMontantTVA() != null) commande.setMontantTVA(dto.getMontantTVA());
        if (dto.getMontantTTC() != null) commande.setMontantTTC(dto.getMontantTTC());
        if (dto.getNote() != null) commande.setNote(dto.getNote());
        CommandeVente saved = commandeVenteRepository.save(commande);

        auditLogService.log("COMMANDE_VENTE_MODIFIEE",
                "Commande de vente modifiée: " + saved.getReference() + " (id=" + id + ")");

        
        if (dto.getLignes() != null) {
            ligneCommandeVenteRepository.deleteByCommandeVenteId(id);
            for (int idx = 0; idx < dto.getLignes().size(); idx++) {
                final int lineNumber = idx + 1;
                LigneCommandeVenteDto ligneDto = dto.getLignes().get(idx);
                if (ligneDto.getQuantite() == null || ligneDto.getQuantite().compareTo(BigDecimal.ZERO) <= 0) {
                    throw new RuntimeException(
                            String.format("Ligne %d: La quantité doit être supérieure à 0.", lineNumber));
                }
                if (ligneDto.getPrixUnitaireHT() == null || ligneDto.getPrixUnitaireHT().compareTo(BigDecimal.ZERO) <= 0) {
                    throw new RuntimeException(
                            String.format("Ligne %d: Le prix unitaire HT doit être supérieur à 0.", lineNumber));
                }
                LigneCommandeVente ligne = new LigneCommandeVente();
                ligne.setQuantite(ligneDto.getQuantite());
                ligne.setPrixUnitaireHT(ligneDto.getPrixUnitaireHT());
                ligne.setTva(ligneDto.getTva() != null ? ligneDto.getTva() : BigDecimal.ZERO);
                ligne.setMontantHT(ligneDto.getMontantHT() != null ? ligneDto.getMontantHT() : BigDecimal.ZERO);
                ligne.setMontantTTC(ligneDto.getMontantTTC() != null ? ligneDto.getMontantTTC() : BigDecimal.ZERO);
                ligne.setCommandeVente(saved);
                if (ligneDto.getArticleVenteId() != null) {
                    final long avId = ligneDto.getArticleVenteId();
                    ArticleVente articleVente = articleVenteRepo.findById(avId)
                            .orElseThrow(() -> new RuntimeException(
                                    String.format("Ligne %d: ArticleVente non trouvé: %d", lineNumber, avId)));
                    ligne.setArticleVente(articleVente);
                }
                ligneCommandeVenteRepository.save(ligne);
            }

            
            factureDeVenteRepository.findByCommandeVenteId(id).ifPresent(facture -> {
                if ("EN_ATTENTE".equals(facture.getStatut())) {
                    double totalTTC = saved.getMontantTTC() != null ? saved.getMontantTTC().doubleValue() : 0.0;
                    facture.setMontantHT(saved.getMontantHT() != null ? saved.getMontantHT().doubleValue() : 0.0);
                    facture.setMontantTVA(saved.getMontantTVA() != null ? saved.getMontantTVA().doubleValue() : 0.0);
                    facture.setMontantTTC(totalTTC);
                    facture.setMontantTotal(totalTTC);
                    factureDeVenteRepository.save(facture);
                }
            });
        }

        return saved;
    }

    @Caching(evict = {
        @CacheEvict(cacheNames = "productIntelligence", allEntries = true),
        @CacheEvict(cacheNames = "salesAnalysis",       allEntries = true),
        @CacheEvict(cacheNames = "salesForecast",       allEntries = true)
    })
    @Transactional
    public void deleteCommande(Long id) {
        CommandeVente commande = commandeVenteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("CommandeVente non trouvée avec id: " + id));

      
        if ("CONFIRMEE".equals(commande.getStatut())) {
            try {
                stockAutomationService.annulerReservationStock(id, commande.getReference());
                logger.info("✅ Stock reservations released for deleted commande {}", id);
            } catch (RuntimeException e) {
                logger.warn("⚠️ Could not fully release stock reservation for commande {}: {}", id, e.getMessage());
            }
        }

        
        factureDeVenteRepository.findByCommandeVenteId(id).ifPresent(facture -> {
            facture.setCommandeVente(null);
            factureDeVenteRepository.save(facture);
        });
        
        List<FactureVente> factures = factureVenteRepository.findAllByCommandeVenteId(id);
        for (FactureVente facture : factures) {
            facture.setCommandeVente(null);
            factureVenteRepository.save(facture);
        }
        
        List<BonLivraison> bons = bonLivraisonRepository.findByCommandeVenteId(id);
        for (BonLivraison bon : bons) {
            bon.setCommandeVente(null);
            bonLivraisonRepository.save(bon);
        }
        ligneCommandeVenteRepository.deleteByCommandeVenteId(id);
        commandeVenteRepository.deleteById(id);
        auditLogService.log("COMMANDE_VENTE_SUPPRIMEE",
                "Commande de vente supprimée: " + commande.getReference() + " (id=" + id + ")");
    }

    @Caching(evict = {
        @CacheEvict(cacheNames = "productIntelligence", allEntries = true),
        @CacheEvict(cacheNames = "salesAnalysis",       allEntries = true),
        @CacheEvict(cacheNames = "salesForecast",       allEntries = true)
    })
    @Transactional
    public ValidationResponseDto validerCommande(Long id) {
        CommandeVente commande = commandeVenteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("CommandeVente non trouvée avec id: " + id));

        if ("CONFIRMEE".equals(commande.getStatut()) || "LIVREE".equals(commande.getStatut())) {
            throw new RuntimeException("Cette commande a déjà été confirmée ou livrée.");
        }
        if ("ANNULEE".equals(commande.getStatut())) {
            throw new RuntimeException("Impossible de confirmer une commande annulée.");
        }

        
        
        
        
        try {
            stockAutomationService.reserverStockPourCommande(id, commande.getReference());
        } catch (RuntimeException e) {
            logger.error("❌ Stock reservation failed for commande {} ({}): {}", id, commande.getReference(), e.getMessage(), e);
            throw e;
        }

        
        commande.setStatut("CONFIRMEE");
        commandeVenteRepository.save(commande);

        
        List<LigneCommandeVente> lignesCommande = ligneCommandeVenteRepository.findByCommandeVenteId(id);
        
        Double totalHT = 0.0;
        Double totalTTC = 0.0;
        
        for (LigneCommandeVente ligne : lignesCommande) {
            BigDecimal ht = ligne.getMontantHT() != null ? ligne.getMontantHT() : BigDecimal.ZERO;
            BigDecimal ttc = ligne.getMontantTTC() != null ? ligne.getMontantTTC() : BigDecimal.ZERO;
            
            totalHT += ht.doubleValue();
            totalTTC += ttc.doubleValue();
        }

        
        Double totalTVA = totalTTC - totalHT;

        
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
        
        
        if (savedFacture.getReferance() == null || savedFacture.getReferance().isEmpty()) {
            String referance = "FV-" + String.format("%04d", savedFacture.getId());
            savedFacture.setReferance(referance);
            savedFacture = factureDeVenteRepository.save(savedFacture);
        }

        
        
        
        
        

        
        
        
        List<StockAlertDto> stockAlerts = collectLowStockAlerts(lignesCommande);

        logger.info("✅ Commande {} confirmed, facture {} updated/created with amounts: HT={}, TVA={}, TTC={} ({} stock alert(s))",
            id, savedFacture.getId(), totalHT, totalTVA, totalTTC, stockAlerts.size());

        auditLogService.log("COMMANDE_VENTE_VALIDEE",
                "Commande de vente validée: " + commande.getReference()
                        + " (id=" + id + "), facture: " + savedFacture.getReferance());

        return new ValidationResponseDto(savedFacture, stockAlerts);
    }

    




    private List<StockAlertDto> collectLowStockAlerts(List<LigneCommandeVente> lignes) {
        List<StockAlertDto> alerts = new ArrayList<>();
        BigDecimal defaultThreshold = BigDecimal.TEN;
        for (LigneCommandeVente ligne : lignes) {
            ArticleVente articleVente = ligne.getArticleVente();
            if (articleVente == null) continue;
            Article article = articleVente.getArticleAchat();
            if (article == null) continue;

            List<Stock> stocks = stockRepository.findByArticleAchatId(article.getId());
            if (stocks.isEmpty()) continue;

            Stock stock = stocks.get(0);
            BigDecimal disponible = stock.getQuantiteDisponible() != null
                    ? stock.getQuantiteDisponible() : BigDecimal.ZERO;
            String articleName = article.getNom() != null ? article.getNom() : ("Article #" + article.getId());
            BigDecimal threshold = stock.getSeuilMinimum() != null ? stock.getSeuilMinimum() : defaultThreshold;

            if (disponible.compareTo(BigDecimal.ZERO) <= 0) {
                alerts.add(new StockAlertDto(
                        "RUPTURE",
                        article.getId(),
                        articleName,
                        disponible,
                        stock.getSeuilMinimum(),
                        String.format("OUT OF STOCK: '%s' is depleted!", articleName)));
            } else if (disponible.compareTo(threshold) < 0) {
                alerts.add(new StockAlertDto(
                        "FAIBLE_STOCK",
                        article.getId(),
                        articleName,
                        disponible,
                        threshold,
                        String.format("STOCK FAIBLE : '%s' en dessous du seuil (reste : %s, seuil : %s).",
                                articleName,
                                disponible.stripTrailingZeros().toPlainString(),
                                threshold.stripTrailingZeros().toPlainString())));
            }
        }
        return alerts;
    }

    @Caching(evict = {
        @CacheEvict(cacheNames = "productIntelligence", allEntries = true),
        @CacheEvict(cacheNames = "salesAnalysis",       allEntries = true),
        @CacheEvict(cacheNames = "salesForecast",       allEntries = true)
    })
    @Transactional
    public CommandeVente annulerCommande(Long id) {
        CommandeVente commande = commandeVenteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("CommandeVente non trouvée avec id: " + id));
        if ("ANNULEE".equals(commande.getStatut())) {
            throw new RuntimeException("Cette commande est déjà annulée.");
        }
        String ancienStatut = commande.getStatut();
        commande.setStatut("ANNULEE");
        CommandeVente saved = commandeVenteRepository.save(commande);

        
        factureDeVenteRepository.findByCommandeVenteId(id).ifPresent(facture -> {
            facture.setStatut("ANNULEE");
            factureDeVenteRepository.save(facture);
            logger.info("✅ Associated FactureDeVente {} cancelled", facture.getId());
        });

        
        
        
        if ("CONFIRMEE".equals(ancienStatut)) {
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
        auditLogService.log("COMMANDE_VENTE_ANNULEE",
                "Commande de vente annulée: " + saved.getReference() + " (id=" + id + ")");
        return saved;
    }

    @Caching(evict = {
        @CacheEvict(cacheNames = "productIntelligence", allEntries = true),
        @CacheEvict(cacheNames = "salesAnalysis",       allEntries = true),
        @CacheEvict(cacheNames = "salesForecast",       allEntries = true)
    })
    @Transactional
    public LivraisonResponseDto livrerCommande(Long id) {
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

        
        List<StockAlertDto> stockAlerts =
                stockAutomationService.decrementStockFromDelivery(id, commande.getReference());

        if (commande.getEntreprise() != null) {
            notificationService.createCommandeVenteNotification(
                    BuissnesManager.BuissnesManager.entity.NotificationType.COMMANDE_VENTE_LIVREE,
                    commande.getReference(),
                    commande.getEntreprise());
        }

        logger.info("✅ Commande {} delivered, BonLivraison {} created and stock decremented ({} stock alert(s))",
                id, savedBon.getId(), stockAlerts.size());
        auditLogService.log("COMMANDE_VENTE_LIVREE",
                "Commande de vente livrée: " + commande.getReference() + " (id=" + id + ", bon=" + savedBon.getReference() + ")");
        return new LivraisonResponseDto(savedBon, stockAlerts);
    }
}
