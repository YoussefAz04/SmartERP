package BuissnesManager.BuissnesManager.service;

import BuissnesManager.BuissnesManager.entity.SousTache;
import BuissnesManager.BuissnesManager.entity.Tache;
import BuissnesManager.BuissnesManager.repository.SousTacheRepo;
import BuissnesManager.BuissnesManager.repository.TacheRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class SousTacheService {

    @Autowired
    private SousTacheRepo sousTacheRepo;
    @Autowired
    private TacheRepo tacheRepo;

    public SousTache create(SousTache sousTache) {
        return sousTacheRepo.save(sousTache);
    }
    public SousTache createSousTacheWithTache(SousTache sousTache,Long idTache) {
        sousTache.setTache(tacheRepo.findById(idTache).get());
        return sousTacheRepo.save(sousTache);
    }

    public List<SousTache> getAll() {
        return sousTacheRepo.findAll();
    }

    public Optional<SousTache> getById(Long id) {
        return sousTacheRepo.findById(id);
    }

    public SousTache update(Long id, SousTache updated) {
        return sousTacheRepo.findById(id).map(sousTache -> {
            sousTache.setTitre(updated.getTitre());
            sousTache.setDescription(updated.getDescription());
            sousTache.setType(updated.getType());
            sousTache.setDateDebut(updated.getDateDebut());
            sousTache.setDateFin(updated.getDateFin());
            sousTache.setProgres(updated.getProgres());
            sousTache.setDuree(updated.getDuree());
            sousTache.setTache(updated.getTache());
            return sousTacheRepo.save(sousTache);
        }).orElseThrow(() -> new RuntimeException("Sous-tâche introuvable avec id: " + id));
    }

    public void delete(Long id) {
        sousTacheRepo.deleteById(id);
    }

    public void assignedSousTache(Long idTache , Long idSousTache){
        SousTache sousTache = sousTacheRepo.findById(idSousTache).get();
        sousTache.setTache( tacheRepo.findById(idTache).get());
        sousTacheRepo.save(sousTache);
    }
}
