package BuissnesManager.BuissnesManager.service;

import BuissnesManager.BuissnesManager.entity.Budget;
import BuissnesManager.BuissnesManager.entity.CategorieProjet;
import BuissnesManager.BuissnesManager.entity.Projet;
import BuissnesManager.BuissnesManager.entity.StatutProjet;
import BuissnesManager.BuissnesManager.repository.BudgetRepo;
import BuissnesManager.BuissnesManager.repository.CategorieProjetRepo;
import BuissnesManager.BuissnesManager.repository.ProjetRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
public class ProjetService {

    @Autowired
    private ProjetRepo projetRepo;
    @Autowired
    private CategorieProjetRepo categorieProjetRepo ;
    @Autowired
    private BudgetRepo budgetRepo ;

    public Projet createProjet(Projet projet,Long idCategorieProjet) {
        CategorieProjet categorieProjet =categorieProjetRepo.findById(idCategorieProjet).get();
        projet.setCategorieProjet(categorieProjet);
        return projetRepo.save(projet);
    }
    public Projet addProjet(Projet projet) {
        return projetRepo.save(projet);
    }

    public List<Projet> getAllProjets() {
        return projetRepo.findAll();
    }

    public Optional<Projet> getProjetById(Long id) {
        return projetRepo.findById(id);
    }

    public Projet updateProjet(Long id, Projet updatedProjet) {
        return projetRepo.findById(id).map(projet -> {
            projet.setNom(updatedProjet.getNom());
            projet.setDescription(updatedProjet.getDescription());
            return projetRepo.save(projet);
        }).orElseThrow(() -> new RuntimeException("Projet introuvable avec l'id " + id));
    }

    public void deleteProjet(Long id) {
        if (!projetRepo.existsById(id)) {
            throw new RuntimeException("Projet introuvable avec l'id " + id);
        }
        projetRepo.deleteById(id);
    }
    public List<Projet> getAllProjetByStatus(StatutProjet statutProjet){
        return  projetRepo.findAllByStatut(statutProjet);
    }

    public void update(Long idProjet,StatutProjet statutProjet){
        Projet projet =  projetRepo.findById(idProjet).get();
        projet.setStatut(statutProjet);
        projetRepo.save(projet);
    }

    @Scheduled(cron = "0 0 0 * * *")
    public void updateStatutsDeProjetTerminee() {
        List<Projet> projets = projetRepo.findAll();
        List<Projet> projetsAMettreAJour = new ArrayList<>();

        for (Projet projet : projets) {
            if (projet.getDateFin() != null
                    && !projet.getStatut().equals(StatutProjet.TERMINE)
                    && !projet.getDateFin().isAfter(LocalDate.now())) {

                projet.setStatut(StatutProjet.TERMINE);
                projetsAMettreAJour.add(projet);
            }
        }

        if (!projetsAMettreAJour.isEmpty()) {
            projetRepo.saveAll(projetsAMettreAJour);
        }
    }
    public int nombreProjetByStatus(StatutProjet statutProjet){
        List<Projet> projets = projetRepo.findAllByStatut(statutProjet) ;
        return projets.size() ;
    }
    public int nombreDeTacheByProjet(Long id){
        Projet projet = projetRepo.findById(id).get();
        return  projet.getTaches().size() ;
    }

    public List<Projet> getAllProjetNotBudget(){
        List<Projet> projets = projetRepo.findAll() ;
        List<Budget> budgets = budgetRepo.findAll() ;
        List<Projet> projetfilter = new ArrayList<>();
        for (Projet p : projets){
            for (Budget b : budgets){
                if (!Objects.equals(p.getId(), b.getIdProjet())){
                    projetfilter.add(p);
                }
            }
        }
        return projetfilter ;
    }

}


