package BuissnesManager.BuissnesManager.service;

import BuissnesManager.BuissnesManager.entity.CategorieTache;
import BuissnesManager.BuissnesManager.repository.CategorieTacheRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CategorieTacheService {

    @Autowired
    private CategorieTacheRepo categorieTacheRepo;

    public CategorieTache createCategorie(CategorieTache categorie) {
        return categorieTacheRepo.save(categorie);
    }

    public List<CategorieTache> getAllCategories() {
        return categorieTacheRepo.findAll();
    }

    public Optional<CategorieTache> getCategorieById(Long id) {
        return categorieTacheRepo.findById(id);
    }

    public CategorieTache updateCategorie(Long id, CategorieTache updatedCategorie) {
        return categorieTacheRepo.findById(id).map(categorie -> {
            categorie.setNom(updatedCategorie.getNom());
            categorie.setDescription(updatedCategorie.getDescription());
            return categorieTacheRepo.save(categorie);
        }).orElseThrow(() -> new RuntimeException("Catégorie introuvable avec l'id " + id));
    }

    public void deleteCategorie(Long id) {
        if (!categorieTacheRepo.existsById(id)) {
            throw new RuntimeException("Catégorie introuvable avec l'id " + id);
        }
        categorieTacheRepo.deleteById(id);
    }
}
