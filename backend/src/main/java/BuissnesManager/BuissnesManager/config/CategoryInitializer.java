package BuissnesManager.BuissnesManager.config;

import BuissnesManager.BuissnesManager.entity.CategorieArticle;
import BuissnesManager.BuissnesManager.entity.CategorieClient;
import BuissnesManager.BuissnesManager.entity.CategorieFournisseur;
import BuissnesManager.BuissnesManager.repository.CategorieArticleRepository;
import BuissnesManager.BuissnesManager.repository.CategorieClientRepo;
import BuissnesManager.BuissnesManager.repository.CategorieFournisseurRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(10)
public class CategoryInitializer implements CommandLineRunner {

    @Autowired
    private CategorieArticleRepository categorieArticleRepository;

    @Autowired
    private CategorieClientRepo categorieClientRepo;

    @Autowired
    private CategorieFournisseurRepo categorieFournisseurRepo;

    @Override
    public void run(String... args) {
        seedCategoriesArticle();
        seedCategoriesClient();
        seedCategoriesFournisseur();
    }

    private void seedCategoriesArticle() {
        if (categorieArticleRepository.count() > 0) {
            return;
        }
        String[][] entries = {
            {"INF", "Informatique",   "Matériel et accessoires informatiques"},
            {"BUR", "Bureautique",    "Fournitures et équipements de bureau"},
            {"ALI", "Alimentaire",    "Produits alimentaires et boissons"},
            {"TEX", "Textile",        "Vêtements et tissus"},
            {"ELE", "Électronique",   "Appareils et composants électroniques"},
            {"MEC", "Mécanique",      "Pièces et équipements mécaniques"},
            {"CHI", "Chimie",         "Produits chimiques et matières premières"},
            {"SER", "Services",       "Prestations de services"},
            {"AUT", "Autres",         "Produits divers non classifiés"}
        };
        for (String[] e : entries) {
            CategorieArticle cat = new CategorieArticle();
            cat.setCode(e[0]);
            cat.setLibelle(e[1]);
            cat.setDescription(e[2]);
            categorieArticleRepository.save(cat);
        }
    }

    private void seedCategoriesClient() {
        if (categorieClientRepo.count() > 0) {
            return;
        }
        String[][] entries = {
            {"Particulier",    "Client individuel non professionnel"},
            {"Professionnel",  "Client exerçant une activité professionnelle"},
            {"Grossiste",      "Client achetant en grande quantité"},
            {"Entreprise",     "Société ou organisation cliente"},
            {"Administration", "Organisme public ou administratif"}
        };
        for (String[] e : entries) {
            CategorieClient cat = new CategorieClient();
            cat.setNom(e[0]);
            cat.setDescription(e[1]);
            categorieClientRepo.save(cat);
        }
    }

    private void seedCategoriesFournisseur() {
        if (categorieFournisseurRepo.count() > 0) {
            return;
        }
        String[][] entries = {
            {"Industrie",     "Fournisseur industriel"},
            {"Commerce",      "Fournisseur commercial / distributeur"},
            {"Services",      "Prestataire de services"},
            {"Technologie",   "Fournisseur de solutions technologiques"},
            {"Agroalimentaire","Fournisseur de produits alimentaires"},
            {"Logistique",    "Fournisseur de transport et logistique"},
            {"Autres",        "Fournisseurs divers"}
        };
        for (String[] e : entries) {
            CategorieFournisseur cat = new CategorieFournisseur();
            cat.setNom(e[0]);
            cat.setDescription(e[1]);
            categorieFournisseurRepo.save(cat);
        }
    }
}
