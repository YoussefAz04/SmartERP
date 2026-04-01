package BuissnesManager.BuissnesManager.service;

import BuissnesManager.BuissnesManager.entity.CategorieFournisseur;
import BuissnesManager.BuissnesManager.entity.Fournisseur;
import BuissnesManager.BuissnesManager.repository.CategorieFournisseurRepo;
import BuissnesManager.BuissnesManager.repository.FournisseurRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class FournisseurService {

    @Autowired
    private FournisseurRepo fournisseurRepo;

    @Autowired
    private CategorieFournisseurRepo categorieFournisseurRepo;

    // ✅ ADD FOURNISSEUR
    @Transactional
    public Fournisseur addFournisseur(Fournisseur fournisseur, Long idCategorie) {
        CategorieFournisseur categorie = categorieFournisseurRepo.findById(idCategorie)
            .orElseThrow(() -> new RuntimeException("Categorie not found with id: " + idCategorie));
        fournisseur.setCategorieFournisseur(categorie);
        return fournisseurRepo.save(fournisseur);
    }

    // ✅ GET ALL FOURNISSEURS
    public List<Fournisseur> getAllFournisseurs() {
        return fournisseurRepo.findAll();
    }

    // ✅ GET FOURNISSEUR BY ID
    public Fournisseur getFournisseurById(Long id) {
        return fournisseurRepo.findById(id)
            .orElseThrow(() -> new RuntimeException("Fournisseur not found with id: " + id));
    }

    // ✅ DELETE FOURNISSEUR
    @Transactional
    public void deleteFournisseur(Long id) {
        if (!fournisseurRepo.existsById(id)) {
            throw new RuntimeException("Fournisseur not found with id: " + id);
        }
        fournisseurRepo.deleteById(id);
    }

    // ✅ UPDATE FOURNISSEUR
    @Transactional
    public Fournisseur updateFournisseur(Long id, Fournisseur fournisseur) {
        if (!fournisseurRepo.existsById(id)) {
            throw new RuntimeException("Fournisseur not found with id: " + id);
        }
        fournisseur.setId(id);
        return fournisseurRepo.save(fournisseur);
    }

    // ✅ ADD CATEGORIE FOURNISSEUR
    @Transactional
    public CategorieFournisseur addCategorieFournisseur(CategorieFournisseur categorie) {
        return categorieFournisseurRepo.save(categorie);
    }

    // ✅ GET ALL CATEGORIES FOURNISSEUR
    public List<CategorieFournisseur> getAllCategorieFournisseurs() {
        return categorieFournisseurRepo.findAll();
    }

    // ✅ GET CATEGORIE FOURNISSEUR BY ID
    public CategorieFournisseur getCategorieFournisseurById(Long id) {
        return categorieFournisseurRepo.findById(id)
            .orElseThrow(() -> new RuntimeException("Categorie not found with id: " + id));
    }

    // ✅ DELETE CATEGORIE FOURNISSEUR
    @Transactional
    public void deleteCategorieFournisseur(Long id) {
        if (!categorieFournisseurRepo.existsById(id)) {
            throw new RuntimeException("Categorie not found with id: " + id);
        }
        categorieFournisseurRepo.deleteById(id);
    }
}
