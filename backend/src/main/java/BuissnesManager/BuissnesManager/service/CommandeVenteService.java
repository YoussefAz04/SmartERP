package BuissnesManager.BuissnesManager.service;

import BuissnesManager.BuissnesManager.dto.CommandeVenteDto;
import BuissnesManager.BuissnesManager.dto.LigneCommandeVenteDto;
import BuissnesManager.BuissnesManager.entity.*;
import BuissnesManager.BuissnesManager.repository.*;
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
        if (dto.getEntrepriseId() != null) {
            Entreprise entreprise = entrepriseRepo.findById(dto.getEntrepriseId())
                    .orElseThrow(() -> new RuntimeException("Entreprise non trouvée: " + dto.getEntrepriseId()));
            commande.setEntreprise(entreprise);
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
        return saved;
    }

    public List<CommandeVente> getAllCommandes() {
        return commandeVenteRepository.findAll();
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
    public FactureVente validerCommande(Long id) {
        CommandeVente commande = commandeVenteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("CommandeVente non trouvée avec id: " + id));

        commande.setStatut("VALIDEE");
        commandeVenteRepository.save(commande);

        FactureVente facture = new FactureVente();
        facture.setDateVente(LocalDate.now().toString());
        facture.setMontantHT(commande.getMontantHT());
        facture.setMontantTVA(commande.getMontantTVA());
        facture.setMontantTTC(commande.getMontantTTC());
        facture.setStatut("EN_ATTENTE");
        facture.setDescription("Facture générée automatiquement depuis la commande " + commande.getReference());
        facture.setClient(commande.getClient());
        facture.setCommandeVente(commande);
        facture.setEntreprise(commande.getEntreprise());
        FactureVente savedFacture = factureVenteRepository.save(facture);
        savedFacture.setReferance("FV-" + String.format("%04d", savedFacture.getId()));
        savedFacture = factureVenteRepository.save(savedFacture);

        List<LigneCommandeVente> lignesCommande = ligneCommandeVenteRepository.findByCommandeVenteId(id);
        logger.info("📝 Creating {} invoice lines for facture {}", lignesCommande.size(), savedFacture.getId());
        List<LigneFactureVente> savedLignes = new java.util.ArrayList<>();
        for (LigneCommandeVente ligneCommande : lignesCommande) {
            LigneFactureVente ligneFacture = new LigneFactureVente();
            if (ligneCommande.getArticleVente() != null) {
                ligneFacture.setDesignation(ligneCommande.getArticleVente().getNom());
            }
            ligneFacture.setQuantite(ligneCommande.getQuantite());
            ligneFacture.setPrixUnitaireHT(ligneCommande.getPrixUnitaireHT());
            ligneFacture.setTva(ligneCommande.getTva());
            ligneFacture.setMontantHT(ligneCommande.getMontantHT());
            ligneFacture.setMontantTTC(ligneCommande.getMontantTTC());
            ligneFacture.setFactureVente(savedFacture);
            savedLignes.add(ligneFactureVenteRepository.save(ligneFacture));
        }

        if (savedLignes.isEmpty()) {
            logger.warn("⚠️ No invoice lines created for facture {}", savedFacture.getId());
        } else {
            recalculateInvoiceAmounts(savedFacture, savedLignes);
        }

        logger.info("✅ Commande {} validated, facture {} created with {} lines", id, savedFacture.getId(), savedLignes.size());
        return savedFacture;
    }

    private void recalculateInvoiceAmounts(FactureVente facture, List<LigneFactureVente> lignes) {
        BigDecimal totalHT = BigDecimal.ZERO;
        BigDecimal totalTVA = BigDecimal.ZERO;
        BigDecimal totalTTC = BigDecimal.ZERO;

        for (LigneFactureVente ligne : lignes) {
            BigDecimal montantHT = ligne.getMontantHT() != null ? ligne.getMontantHT() : BigDecimal.ZERO;
            BigDecimal montantTTC = ligne.getMontantTTC() != null ? ligne.getMontantTTC() : BigDecimal.ZERO;
            totalHT = totalHT.add(montantHT);
            totalTVA = totalTVA.add(montantTTC.subtract(montantHT));
            totalTTC = totalTTC.add(montantTTC);
        }

        facture.setMontantHT(totalHT);
        facture.setMontantTVA(totalTVA);
        facture.setMontantTTC(totalTTC);

        factureVenteRepository.save(facture);
        logger.info("✅ Invoice amounts recalculated: HT={}, TVA={}, TTC={}", totalHT, totalTVA, totalTTC);
    }

    public CommandeVente annulerCommande(Long id) {
        CommandeVente commande = commandeVenteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("CommandeVente non trouvée avec id: " + id));
        commande.setStatut("ANNULEE");
        return commandeVenteRepository.save(commande);
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
