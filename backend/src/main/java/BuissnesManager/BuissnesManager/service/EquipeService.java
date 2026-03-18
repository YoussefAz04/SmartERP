package BuissnesManager.BuissnesManager.service;

import BuissnesManager.BuissnesManager.entity.Equipe;
import BuissnesManager.BuissnesManager.entity.EquipeDto;
import BuissnesManager.BuissnesManager.entity.Membre;
import BuissnesManager.BuissnesManager.repository.EquipeRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class EquipeService {

    @Autowired
    private EquipeRepo equipeRepo ;

    public Equipe createEquipe(Equipe equipe){
        return equipeRepo.save(equipe);
    }
    public List<Equipe> getAllEquipe(){
        return equipeRepo.findAll();
    }


    public EquipeDto getEquipeDtoById(Long idEquipe){
        Equipe equipe = equipeRepo.findById(idEquipe).get();
        EquipeDto equipeDto = new EquipeDto();
        equipeDto.setId(idEquipe);
        equipeDto.setNom(equipe.getNom());
        equipeDto.setDescription(equipe.getDescription());
        equipeDto.setDateCreation(equipe.getDateCreation());
//        equipeDto.setMembresIds(equipe.getMembres().stream().map(Membre::getId).collect(Collectors.toList()));
        equipeDto.setMembres(equipe.getMembres());
        return equipeDto ;
    }


    public void updateEquipe(EquipeDto equipeDto , Long idEquipe){
        Equipe equipe = equipeRepo.findById(idEquipe).get();
        equipe.setNom(equipeDto.getNom());
        equipe.setDescription(equipeDto.getDescription());
        equipe.setDateCreation(equipeDto.getDateCreation());
        equipe.setMembres(equipeDto.getMembres());
        equipeRepo.save(equipe);
    }


}
