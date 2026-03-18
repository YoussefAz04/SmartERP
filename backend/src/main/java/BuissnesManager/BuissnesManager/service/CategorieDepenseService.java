package BuissnesManager.BuissnesManager.service;

import BuissnesManager.BuissnesManager.entity.CategorieDepense;
import BuissnesManager.BuissnesManager.repository.CategorieDepenseRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategorieDepenseService {

    @Autowired
    private CategorieDepenseRepo categorieDepenseRepo;

    public List<CategorieDepense> getAllCategories() {
        return categorieDepenseRepo.findAll();
    }

    public CategorieDepense getCategorieById(Long id) {
        return categorieDepenseRepo.findById(id).get();
    }

    public CategorieDepense saveCategorie(CategorieDepense categorie) {
        return categorieDepenseRepo.save(categorie);
    }

    public void deleteCategorie(Long id) {
        categorieDepenseRepo.deleteById(id);
    }
}
