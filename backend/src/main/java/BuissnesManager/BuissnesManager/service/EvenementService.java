package BuissnesManager.BuissnesManager.service;

import BuissnesManager.BuissnesManager.entity.CategorieEvenement;
import BuissnesManager.BuissnesManager.entity.Evenement;
import BuissnesManager.BuissnesManager.repository.CategorieEvenementRepo;
import BuissnesManager.BuissnesManager.repository.EvenementRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EvenementService {
    @Autowired
    private EvenementRepo evenementRepo;
    @Autowired
    private CategorieEvenementRepo categorieEvenementRepo ;

    public List<Evenement> getAllEvenement() {
        return evenementRepo.findAll();
    }
    public List<CategorieEvenement> getAllCategorie() {
        return categorieEvenementRepo.findAll();
    }


    public Evenement save(Evenement e, Long idCategorie) {
        CategorieEvenement cat = categorieEvenementRepo.findById(idCategorie)
                .orElseThrow(() -> new RuntimeException("Catégorie non trouvée"));
        e.setCategorieEvenement(cat);
        return evenementRepo.save(e);
    }


    public void delete(Long id) {
        evenementRepo.deleteById(id);
    }

    public CategorieEvenement saveCategorieEvenement(CategorieEvenement e) {
        return  categorieEvenementRepo.save(e);
    }


}


