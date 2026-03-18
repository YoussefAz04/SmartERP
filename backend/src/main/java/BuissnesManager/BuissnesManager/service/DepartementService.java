package BuissnesManager.BuissnesManager.service;

import BuissnesManager.BuissnesManager.dto.DepartementDTO;
import BuissnesManager.BuissnesManager.entity.Departement;
import BuissnesManager.BuissnesManager.entity.Entreprise;
import BuissnesManager.BuissnesManager.repository.DepartementRepo;
import BuissnesManager.BuissnesManager.repository.EntrepriseRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.annotation.AccessType;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class DepartementService {

    @Autowired
    private EntrepriseRepo entrepriseRepo ;
    @Autowired
    private DepartementRepo departementRepo;


    public Departement createDepartement(Departement departement, Long entrepriseId) {
        Optional<Entreprise> entreprise = entrepriseRepo.findById(entrepriseId);
        if (entreprise.isPresent()) {
            departement.setEntreprise(entreprise.get());
            return departementRepo.save(departement);
        } else {
            throw new RuntimeException("Entreprise non trouvée avec ID : " + entrepriseId);
        }
    }
    public List<DepartementDTO> getAllDepartements() {
        List<Departement> departements = departementRepo.findAll();

        return departements.stream().map(departement -> {
            DepartementDTO dto = new DepartementDTO();
            dto.setId(departement.getId());
            dto.setNom(departement.getNom());
            dto.setDescription(departement.getDescription());
            dto.setResponsable(departement.getResponsable());
            dto.setEmail(departement.getEmail());
            dto.setTelephone(departement.getTelephone());
            dto.setNombreEmployes(departement.getNombreEmployes());
            if (departement.getEntreprise() != null) {
                dto.setEntrepriseId(departement.getEntreprise().getId());
                dto.setEntrepriseNom(departement.getEntreprise().getNom());
            }
            return  dto ;
        }).collect(Collectors.toList());
    }

    public DepartementDTO getDepartementDTOById(Long id) {
        Departement departement = departementRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Département non trouvé avec ID : " + id));
        DepartementDTO dto = new DepartementDTO() ;
        dto.setId(departement.getId());
        dto.setNom(departement.getNom());
        dto.setDescription(departement.getDescription());
        dto.setResponsable(departement.getResponsable());
        dto.setEmail(departement.getEmail());
        dto.setTelephone(departement.getTelephone());
        dto.setNombreEmployes(departement.getNombreEmployes());
        if (departement.getEntreprise() != null) {
            dto.setEntrepriseId(departement.getEntreprise().getId());
            dto.setEntrepriseNom(departement.getEntreprise().getNom());
        }
        return  dto ;
    }


    public Departement getDepartementById(Long id) {
        return departementRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Département non trouvé avec ID : " + id));
    }


    public Departement updateDepartement(Long id, Departement departementDetails) {
        Departement departement = getDepartementById(id);

        departement.setNom(departementDetails.getNom());
        departement.setDescription(departementDetails.getDescription());
        departement.setResponsable(departementDetails.getResponsable());
        departement.setEmail(departementDetails.getEmail());
        departement.setTelephone(departementDetails.getTelephone());
        departement.setNombreEmployes(departementDetails.getNombreEmployes());

        return departementRepo.save(departement);
    }

    public void deleteDepartement(Long id) {
        departementRepo.deleteById(id);
    }







}
