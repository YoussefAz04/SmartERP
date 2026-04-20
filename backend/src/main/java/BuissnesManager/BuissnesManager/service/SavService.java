package BuissnesManager.BuissnesManager.service;

import BuissnesManager.BuissnesManager.dto.SavDto;
import BuissnesManager.BuissnesManager.dto.SavInterventionDto;
import BuissnesManager.BuissnesManager.entity.*;
import BuissnesManager.BuissnesManager.repository.*;
import BuissnesManager.BuissnesManager.security.EnterpriseSecurityUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class SavService {

    private static final Logger logger = LoggerFactory.getLogger(SavService.class);

    private static final Set<String> VALID_STATUTS = new HashSet<>(Arrays.asList(
            "NOUVEAU", "EN_COURS", "EN_ATTENTE_PIECE", "RESOLU", "CLOTURE", "ANNULE"
    ));
    private static final Set<String> VALID_TYPES = new HashSet<>(Arrays.asList(
            "GARANTIE", "REPARATION", "RETOUR", "ECHANGE", "RECLAMATION"
    ));
    private static final Set<String> VALID_PRIORITES = new HashSet<>(Arrays.asList(
            "BASSE", "NORMALE", "HAUTE", "URGENTE"
    ));

    @Autowired private SavRepository savRepository;
    @Autowired private SavInterventionRepository interventionRepository;
    @Autowired private ClientRepo clientRepo;
    @Autowired private EntrepriseRepo entrepriseRepo;
    @Autowired private FactureDeVenteRepo factureDeVenteRepo;
    @Autowired private ArticleVenteRepo articleVenteRepo;
    @Autowired private UserRepo userRepo;
    @Autowired private EnterpriseSecurityUtil enterpriseSecurityUtil;

    // -------- Queries --------

    public List<Sav> getAll() {
        Long entId = enterpriseSecurityUtil.getContextEnterpriseId();
        if (entId != null) {
            return savRepository.findByEntrepriseIdOrderByCreatedAtDesc(entId);
        }
        return savRepository.findAll();
    }

    public List<Sav> getByEntreprise(Long entrepriseId) {
        return savRepository.findByEntrepriseIdOrderByCreatedAtDesc(entrepriseId);
    }

    public List<Sav> getByClient(Long clientId) {
        return savRepository.findByClientIdOrderByCreatedAtDesc(clientId);
    }

    public Sav getById(Long id) {
        return savRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Ticket SAV introuvable: " + id));
    }

    // -------- Mutations --------

    @Transactional
    public Sav create(SavDto dto) {
        Sav sav = new Sav();
        applyDto(sav, dto, true);

        if (sav.getReference() == null || sav.getReference().isBlank()) {
            sav.setReference(generateReference());
        }
        if (sav.getDateOuverture() == null) {
            sav.setDateOuverture(LocalDate.now());
        }
        if (sav.getStatut() == null || sav.getStatut().isBlank()) {
            sav.setStatut("NOUVEAU");
        }
        if (sav.getPriorite() == null || sav.getPriorite().isBlank()) {
            sav.setPriorite("NORMALE");
        }

        validateEnums(sav);
        Sav saved = savRepository.save(sav);

        // Seed initial timeline entry
        SavIntervention first = new SavIntervention();
        first.setSav(saved);
        first.setType("CHANGEMENT_STATUT");
        first.setStatut(saved.getStatut());
        first.setAuteur(currentUsername());
        first.setCommentaire("Ticket SAV créé");
        interventionRepository.save(first);

        logger.info("✅ SAV ticket created: {} (id={})", saved.getReference(), saved.getId());
        return saved;
    }

    @Transactional
    public Sav update(Long id, SavDto dto) {
        Sav sav = getById(id);
        if ("CLOTURE".equals(sav.getStatut())) {
            throw new RuntimeException("Un ticket clôturé ne peut plus être modifié");
        }
        applyDto(sav, dto, false);
        validateEnums(sav);
        return savRepository.save(sav);
    }

    @Transactional
    public void delete(Long id) {
        Sav sav = getById(id);
        savRepository.delete(sav);
    }

    @Transactional
    public Sav changeStatut(Long id, String nouveauStatut, String commentaire) {
        Sav sav = getById(id);
        if (nouveauStatut == null || !VALID_STATUTS.contains(nouveauStatut)) {
            throw new RuntimeException("Statut SAV invalide: " + nouveauStatut);
        }
        String ancien = sav.getStatut();
        if (ancien != null && ancien.equals(nouveauStatut)) {
            return sav; // no-op
        }
        sav.setStatut(nouveauStatut);
        if ("CLOTURE".equals(nouveauStatut) || "RESOLU".equals(nouveauStatut)) {
            if (sav.getDateCloture() == null) sav.setDateCloture(LocalDate.now());
        }
        Sav saved = savRepository.save(sav);

        SavIntervention evt = new SavIntervention();
        evt.setSav(saved);
        evt.setType("CHANGEMENT_STATUT");
        evt.setStatut(nouveauStatut);
        evt.setAuteur(currentUsername());
        String msg = "Statut : " + (ancien != null ? ancien : "—") + " → " + nouveauStatut;
        if (commentaire != null && !commentaire.isBlank()) {
            msg = msg + "\n" + commentaire;
        }
        evt.setCommentaire(msg);
        interventionRepository.save(evt);
        return saved;
    }

    @Transactional
    public SavIntervention addIntervention(Long savId, SavInterventionDto dto) {
        Sav sav = getById(savId);
        SavIntervention evt = new SavIntervention();
        evt.setSav(sav);
        evt.setType(dto.getType() != null ? dto.getType() : "COMMENTAIRE");
        evt.setStatut(dto.getStatut());
        evt.setCommentaire(dto.getCommentaire());
        evt.setAuteur(dto.getAuteur() != null && !dto.getAuteur().isBlank()
                ? dto.getAuteur() : currentUsername());
        return interventionRepository.save(evt);
    }

    public List<SavIntervention> getInterventions(Long savId) {
        return interventionRepository.findBySavIdOrderByCreatedAtAsc(savId);
    }

    // -------- Stats --------

    public java.util.Map<String, Object> stats(Long entrepriseId) {
        Long entId = entrepriseId != null ? entrepriseId : enterpriseSecurityUtil.getContextEnterpriseId();
        java.util.Map<String, Object> out = new java.util.HashMap<>();
        if (entId == null) {
            out.put("total", savRepository.count());
            return out;
        }
        out.put("total", savRepository.countByEntreprise(entId));
        java.util.Map<String, Long> byStatut = new java.util.HashMap<>();
        for (Object[] row : savRepository.getStatutBreakdown(entId)) {
            byStatut.put(String.valueOf(row[0]), ((Number) row[1]).longValue());
        }
        out.put("byStatut", byStatut);
        java.util.Map<String, Long> byType = new java.util.HashMap<>();
        for (Object[] row : savRepository.getTypeBreakdown(entId)) {
            byType.put(String.valueOf(row[0]), ((Number) row[1]).longValue());
        }
        out.put("byType", byType);
        return out;
    }

    // -------- Helpers --------

    private void applyDto(Sav sav, SavDto dto, boolean isCreate) {
        if (dto.getReference() != null && !dto.getReference().isBlank()) sav.setReference(dto.getReference());
        if (dto.getDateOuverture() != null) sav.setDateOuverture(dto.getDateOuverture());
        if (dto.getDateCloture() != null) sav.setDateCloture(dto.getDateCloture());
        if (dto.getType() != null) sav.setType(dto.getType());
        if (dto.getPriorite() != null) sav.setPriorite(dto.getPriorite());
        if (dto.getStatut() != null) sav.setStatut(dto.getStatut());
        if (dto.getSujet() != null) sav.setSujet(dto.getSujet());
        if (dto.getDescription() != null) sav.setDescription(dto.getDescription());
        if (dto.getResolution() != null) sav.setResolution(dto.getResolution());
        if (dto.getCoutEstime() != null) sav.setCoutEstime(dto.getCoutEstime());
        if (dto.getCoutReel() != null) sav.setCoutReel(dto.getCoutReel());

        if (dto.getClientId() != null) {
            Client c = clientRepo.findById(dto.getClientId())
                    .orElseThrow(() -> new RuntimeException("Client introuvable: " + dto.getClientId()));
            sav.setClient(c);
        } else if (isCreate) {
            throw new RuntimeException("Client requis pour créer un ticket SAV");
        }

        Long resolvedEnt = dto.getEntrepriseId();
        if (resolvedEnt == null) resolvedEnt = enterpriseSecurityUtil.getContextEnterpriseId();
        if (resolvedEnt != null) {
            Entreprise e = entrepriseRepo.findById(resolvedEnt)
                    .orElseThrow(() -> new RuntimeException("Entreprise introuvable: " + dto.getEntrepriseId()));
            sav.setEntreprise(e);
        } else if (isCreate && sav.getEntreprise() == null) {
            // Fallback: attach to first available enterprise, matches CommandeVenteService behavior
            List<Entreprise> all = entrepriseRepo.findAll();
            if (!all.isEmpty()) sav.setEntreprise(all.get(0));
        }

        if (dto.getFactureDeVenteId() != null) {
            sav.setFactureDeVente(factureDeVenteRepo.findById(dto.getFactureDeVenteId())
                    .orElse(null));
        }
        if (dto.getArticleVenteId() != null) {
            sav.setArticleVente(articleVenteRepo.findById(dto.getArticleVenteId())
                    .orElse(null));
        }
        if (dto.getTechnicienId() != null) {
            sav.setTechnicien(userRepo.findById(dto.getTechnicienId()).orElse(null));
        }
    }

    private void validateEnums(Sav sav) {
        if (sav.getStatut() != null && !VALID_STATUTS.contains(sav.getStatut())) {
            throw new RuntimeException("Statut SAV invalide: " + sav.getStatut());
        }
        if (sav.getType() != null && !VALID_TYPES.contains(sav.getType())) {
            throw new RuntimeException("Type SAV invalide: " + sav.getType());
        }
        if (sav.getPriorite() != null && !VALID_PRIORITES.contains(sav.getPriorite())) {
            throw new RuntimeException("Priorité SAV invalide: " + sav.getPriorite());
        }
    }

    private String generateReference() {
        // SAV-YYYYMMDD-XXXX
        String date = LocalDate.now().toString().replace("-", "");
        String suffix = String.valueOf(System.currentTimeMillis()).substring(8);
        String ref = "SAV-" + date + "-" + suffix;
        int attempts = 0;
        while (savRepository.countByReference(ref) > 0 && attempts < 5) {
            ref = "SAV-" + date + "-" + (Long.parseLong(suffix) + (++attempts));
        }
        return ref;
    }

    private String currentUsername() {
        try {
            var auth = org.springframework.security.core.context.SecurityContextHolder
                    .getContext().getAuthentication();
            if (auth != null && auth.getName() != null) return auth.getName();
        } catch (Exception ignored) { }
        return "system";
    }
}
