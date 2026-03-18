package BuissnesManager.BuissnesManager.service;

import BuissnesManager.BuissnesManager.entity.*;
import BuissnesManager.BuissnesManager.repository.*;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class TacheService {

    @Autowired
    private TacheRepo tacheRepo;

    @Autowired
    private CategorieTacheRepo categorieTacheRepo ;
    @Autowired
    private ProjetRepo projetRepo ;
    @Autowired
    private SousTacheRepo sousTacheRepo ;

    @Autowired
    private MembreRepo membreRepo ;

    public Tache createTache(Tache tache, Long idCategorieTache, Long idProjet) {
        Optional<Projet> optionalProjet = projetRepo.findById(idProjet);
        Optional<CategorieTache> optionalCategorieTache = categorieTacheRepo.findById(idCategorieTache);

        if (optionalProjet.isPresent() && optionalCategorieTache.isPresent()) {
            Projet projet = optionalProjet.get();

            if (tache.getDateDebut().isAfter(projet.getDateDebut()) &&
                    tache.getDateFin().isBefore(projet.getDateFin())) {

                tache.setCategorieTache(optionalCategorieTache.get());
                tache.setProjet(projet);
                return tacheRepo.save(tache);

            } else {
                throw new IllegalArgumentException("Les dates de la tâche doivent être comprises dans celles du projet.");
            }
        } else {
            throw new EntityNotFoundException("Projet ou catégorie de tâche introuvable.");
        }
    }

    public List<Tache> getAllTaches() {
        return tacheRepo.findAll();
    }

    public Optional<Tache> getTacheById(Long id) {
        return tacheRepo.findById(id);
    }

    public Tache updateTache(Long id, Tache updatedTache) {
        return tacheRepo.findById(id).map(tache -> {
            tache.setTitre(updatedTache.getTitre());
            tache.setDescription(updatedTache.getDescription());
            tache.setDateDebut(updatedTache.getDateDebut());
            tache.setDateFin(updatedTache.getDateFin());
            tache.setStatut(updatedTache.getStatut());
            tache.setPriorite(updatedTache.getPriorite());
            tache.setProjet(updatedTache.getProjet());
            tache.setCategorieTache(updatedTache.getCategorieTache());
            return tacheRepo.save(tache);
        }).orElseThrow(() -> new RuntimeException("Tâche non trouvée avec l'id : " + id));
    }

    public void deleteTache(Long id) {
        if (!tacheRepo.existsById(id)) {
            throw new RuntimeException("Tâche non trouvée avec l'id : " + id);
        }
        tacheRepo.deleteById(id);
    }
    public List<Tache> getAllTachesByProjet(Long idProjet){
        return tacheRepo.findAllByProjetId(idProjet);
    }
    public void assigneTacheToMembre(Long idTache , Long idMembre){
            Tache tache = tacheRepo.findById(idTache).get() ;
            tache.setMembre(membreRepo.findById(idMembre).get());
            tacheRepo.save(tache);
    }

    public void updateTacheStatut(Long idTache, StatutTache statutTache){
        Tache tache = tacheRepo.findById(idTache).get();
        tache.setStatut(statutTache);
        tacheRepo.save(tache) ;
    }



    @Scheduled(cron = "0 0 0 * * *")
    public void updateStatutsDeTacheTerminee() {
        List<Tache> taches = tacheRepo.findAll();
        for (Tache tache : taches) {
            if (tache.getDateFin() != null && !tache.getStatut().equals(StatutTache.TERMINEE)) {
                if (!tache.getDateFin().isAfter(LocalDate.now())) {
                    tache.setStatut(StatutTache.TERMINEE);
                    tacheRepo.save(tache);
                }
            }
        }
    }


    public int nombreTacheByStatus(StatutTache statutTache){
        List<Tache> taches = tacheRepo.findAllByStatut(statutTache) ;
        return taches.size() ;
    }

    public SousTache getSousTacheById(Long id){
        return   sousTacheRepo.findById(id).get() ;
    }



    public SousTache updateSousTache(Long id, SousTache updatedSousTache) {
        return sousTacheRepo.findById(id).map(sousTache -> {
            sousTache.setTitre(updatedSousTache.getTitre());
            sousTache.setDescription(updatedSousTache.getDescription());
            sousTache.setType(updatedSousTache.getType());
            sousTache.setDateDebut(updatedSousTache.getDateDebut());
            sousTache.setDateFin(updatedSousTache.getDateFin());
            sousTache.setProgres(updatedSousTache.getProgres());
            sousTache.setDuree(updatedSousTache.getDuree());
            sousTache.setTache(updatedSousTache.getTache());
            return sousTacheRepo.save(sousTache);
        }).orElseThrow(() -> new RuntimeException("Sous-tâche non trouvée avec l'id : " + id));
    }






}
