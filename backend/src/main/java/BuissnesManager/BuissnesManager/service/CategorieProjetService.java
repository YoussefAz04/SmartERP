package BuissnesManager.BuissnesManager.service;

import BuissnesManager.BuissnesManager.entity.CategorieProjet;
import BuissnesManager.BuissnesManager.repository.CategorieProjetRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CategorieProjetService {

    @Autowired
    private CategorieProjetRepo categorieProjetRepo;

    public CategorieProjet createCategorie(CategorieProjet categorie) {
        return categorieProjetRepo.save(categorie);
    }

    public List<CategorieProjet> getAllCategories() {
        return categorieProjetRepo.findAll();
    }

    public Optional<CategorieProjet> getCategorieById(Long id) {
        return categorieProjetRepo.findById(id);
    }

    public CategorieProjet updateCategorie(Long id, CategorieProjet updatedCategorie) {
        return categorieProjetRepo.findById(id).map(categorie -> {
            categorie.setNom(updatedCategorie.getNom());
            categorie.setDescription(updatedCategorie.getDescription());
            return categorieProjetRepo.save(categorie);
        }).orElseThrow(() -> new RuntimeException("Catégorie introuvable avec l'id " + id));
    }

    public void deleteCategorie(Long id) {
        if (!categorieProjetRepo.existsById(id)) {
            throw new RuntimeException("Catégorie introuvable avec l'id " + id);
        }
        categorieProjetRepo.deleteById(id);
    }
}
