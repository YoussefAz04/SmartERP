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
            // Roll back the entire transaction if stock is insufficient
            throw e;
        }

        return saved;
    }

    public List<CommandeVente> getAllCommandes() {
        Long entrepriseId = enterpriseSecurityUtil.getContextEnterpriseId();
        if (entrepriseId == null) {
            return commandeVenteRepository.findAll();
        }
        return commandeVenteRepository.findByEntrepriseId(entrepriseId);
    }

    public List<CommandeVente> getCommandesByEntreprise(Long entrepriseId) {
        return commandeVenteRepository.findByEntrepriseId(entrepriseId);
    }

    public Optional<CommandeVente> getCommandeById(Long id) {
        return commandeVenteRepository.findById(id);
    }

    public CommandeVente updateStatut(Long id, String statut) {
        CommandeVente commande = commandeVenteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("CommandeVente non trouvée avec id: " + id));
        commande.setStatut(statut);
        return commandeVenteRepository.save(commande);
    }

    public CommandeVente updateCommande(Long id, CommandeVenteDto dto) {
        CommandeVente commande = commandeVenteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("CommandeVente non trouvée avec id: " + id));
        if (dto.getReference() != null) commande.setReference(dto.getReference());
        if (dto.getDateCommande() != null) commande.setDateCommande(dto.getDateCommande());
        if (dto.getDateLivraisonPrevue() != null) commande.setDateLivraisonPrevue(dto.getDateLivraisonPrevue());
        if (dto.getStatut() != null) commande.setStatut(dto.getStatut());
        if (dto.getMontantHT() != null) commande.setMontantHT(dto.getMontantHT());
        if (dto.getMontantTVA() != null) commande.setMontantTVA(dto.getMontantTVA());
        if (dto.getMontantTTC() != null) commande.setMontantTTC(dto.getMontantTTC());
        if (dto.getNote() != null) commande.setNote(dto.getNote());
        return commandeVenteRepository.save(commande);
    }

    @Transactional
    public void deleteCommande(Long id) {
        if (!commandeVenteRepository.existsById(id)) {
            throw new RuntimeException("CommandeVente non trouvée avec id: " + id);
        }
        ligneCommandeVenteRepository.deleteByCommandeVenteId(id);
        commandeVenteRepository.deleteById(id);
    }

    @Transactional
    public FactureDeVente validerCommande(Long id) {
        CommandeVente commande = commandeVenteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("CommandeVente non trouvée avec id: " + id));

        // ✅ Change status to CONFIRMEE (valid for database constraint)
        commande.setStatut("CONFIRMEE");
        commandeVenteRepository.save(commande);

        // ✅ Calculate totals from order lines
        List<LigneCommandeVente> lignesCommande = ligneCommandeVenteRepository.findByCommandeVenteId(id);
        
        Double totalHT = 0.0;
        Double totalTVA = 0.0;
        Double totalTTC = 0.0;
        
        for (LigneCommandeVente ligne : lignesCommande) {
            BigDecimal ht = ligne.getMontantHT() != null ? ligne.getMontantHT() : BigDecimal.ZERO;
            BigDecimal tva = ligne.getTva() != null ? ligne.getTva() : BigDecimal.ZERO;
            BigDecimal ttc = ligne.getMontantTTC() != null ? ligne.getMontantTTC() : BigDecimal.ZERO;
            
            totalHT += ht.doubleValue();
            totalTVA += tva.doubleValue();
            totalTTC += ttc.doubleValue();
        }

        // ✅ Use OLD FactureDeVente entity (NOT FactureVente)
        FactureDeVente facture = new FactureDeVente();
        facture.setDateAchat(LocalDate.now().toString());
        facture.setMontantHT(totalHT);
        facture.setMontantTVA(totalTVA);
        facture.setMontantTTC(totalTTC);
        facture.setMontantTotal(totalTTC);
        facture.setStatut("EN_ATTENTE");
        facture.setDescription("Facture générée automatiquement depuis la commande " + commande.getReference());
        facture.setClient(commande.getClient());
        facture.setModePaiement("A_DEFINIR");
        facture.setEntreprise(commande.getEntreprise());  // ✅ ADD THIS LINE
        
        FactureDeVente savedFacture = factureDeVenteRepository.save(facture);
        
        // ✅ Generate reference
        String referance = "FV-" + String.format("%04d", savedFacture.getId());
        savedFacture.setReferance(referance);
        savedFacture = factureDeVenteRepository.save(savedFacture);

        logger.info("✅ Commande {} confirmed, facture {} created with amounts: HT={}, TVA={}, TTC={}", 
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

        // Release stock reservations if the order was not yet delivered
        if (!"LIVREE".equals(ancienStatut)) {
            try {
                stockAutomationService.annulerReservationStock(id, commande.getReference());
            } catch (RuntimeException e) {
                logger.warn("⚠️ Could not fully release stock reservation for commande {}: {}", id, e.getMessage());
            }
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
            throw new RuntimeException("Cette commande a déjà été livrée");
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

        stockAutomationService.decrementStockFromDelivery(id, commande.getReference());

        return savedBon;
    }

    public BigDecimal calculateTotal(Long commandeId) {
        List<LigneCommandeVente> lignes = ligneCommandeVenteRepository.findByCommandeVenteId(commandeId);
        return lignes.stream()
                .map(l -> l.getMontantTTC() != null ? l.getMontantTTC() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}