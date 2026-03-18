package BuissnesManager.BuissnesManager.service;

import BuissnesManager.BuissnesManager.entity.CaracteristiqueFactureEntreprise;
import BuissnesManager.BuissnesManager.entity.Entreprise;
import BuissnesManager.BuissnesManager.entity.User;
import BuissnesManager.BuissnesManager.repository.CaracteristiqueFactureEntrepriseRepo;
import BuissnesManager.BuissnesManager.repository.EntrepriseRepo;
import BuissnesManager.BuissnesManager.repository.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class EntrepriseService {
    @Autowired
    private EntrepriseRepo entrepriseRepository;
    @Autowired
    private UserRepo userRepo ;
    @Autowired
    private CaracteristiqueFactureEntrepriseRepo caracteristiqueFactureEntrepriseRepo ;


    public List<Entreprise> getAllEntreprise(){
        return entrepriseRepository.findAll();
    }

    public Entreprise ajouterEntreprise(Entreprise entreprise) {
        return entrepriseRepository.save(entreprise);
    }

    public List<Entreprise> getAllEntreprises() {
        return entrepriseRepository.findAll();
    }


    public Optional<Entreprise> getEntrepriseById(Long id) {
        return entrepriseRepository.findById(id);
    }
    public Entreprise updateEntreprise(Long id, Entreprise entrepriseDetails) {
        Optional<Entreprise> optionalEntreprise = entrepriseRepository.findById(id);
        if (optionalEntreprise.isPresent()) {
            Entreprise entreprise = optionalEntreprise.get();
            entreprise.setNom(entrepriseDetails.getNom());
            entreprise.setRaisonSociale(entrepriseDetails.getRaisonSociale());
            entreprise.setDescription(entrepriseDetails.getDescription());
            entreprise.setSecteurActivite(entrepriseDetails.getSecteurActivite());
            entreprise.setStatutJuridique(entrepriseDetails.getStatutJuridique());
            entreprise.setNumeroIdentificationFiscale(entrepriseDetails.getNumeroIdentificationFiscale());
            entreprise.setRegistreCommerce(entrepriseDetails.getRegistreCommerce());
            entreprise.setSiteWeb(entrepriseDetails.getSiteWeb());
            entreprise.setDateCreation(entrepriseDetails.getDateCreation());
            entreprise.setDateDerniereModification(entrepriseDetails.getDateDerniereModification());
            entreprise.setEmail(entrepriseDetails.getEmail());
            entreprise.setTelephone(entrepriseDetails.getTelephone());
            entreprise.setAdresse(entrepriseDetails.getAdresse());
            return entrepriseRepository.save(entreprise);
        } else {
            throw new RuntimeException("Entreprise non trouvée avec ID : " + id);
        }
    }

    public void deleteEntreprise(Long id) {
        entrepriseRepository.deleteById(id);
    }
    public void assignerUserToEntreprise(String username,String nom){
        Entreprise entreprise = entrepriseRepository.findByNom(nom).orElse(null);
        User user =userRepo.findByUsername(username).orElse(null);
        user.setEntreprise(entreprise);
        userRepo.save(user);
    }
    public Entreprise getById(Long idEntreprise){
        return entrepriseRepository.findById(idEntreprise).get();
    }


    public CaracteristiqueFactureEntreprise addCaracteristiqueFactureEntreprise(CaracteristiqueFactureEntreprise caracteristiqueFactureEntreprise){
        return caracteristiqueFactureEntrepriseRepo.save(caracteristiqueFactureEntreprise);
    }

}
