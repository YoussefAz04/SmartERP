package BuissnesManager.BuissnesManager.service;

import BuissnesManager.BuissnesManager.entity.Equipe;
import BuissnesManager.BuissnesManager.entity.Membre;
import BuissnesManager.BuissnesManager.entity.MembreDto;
import BuissnesManager.BuissnesManager.entity.User;
import BuissnesManager.BuissnesManager.repository.EquipeRepo;
import BuissnesManager.BuissnesManager.repository.MembreRepo;
import BuissnesManager.BuissnesManager.repository.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.w3c.dom.stylesheets.LinkStyle;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class MembreService {


    @Autowired
    private MembreRepo membreRepo ;
    @Autowired
    private UserRepo userRepo ;
    @Autowired
    private EquipeRepo equipeRepo ;


    public Membre addMembre(Membre membre,Long idEquipe){
        membre.setEquipe(equipeRepo.findById(idEquipe).get());
        return membreRepo.save(membre);
    }
    public void affecterUserToMembre(List<User> users, Long idMembre){
        Membre membre = membreRepo.findById(idMembre).get();
        for (User user : users){
            user.getMembres().add(membre);
            membre.getUsers().add(user);
        }
        membreRepo.save(membre);
        userRepo.saveAll(users);
    }
    public List<Membre> getAllMembre(){
        return membreRepo.findAll();
    }

    public MembreDto getMembreDtoById(Long idMembre){
        Membre membre = membreRepo.findById(idMembre).get();
        MembreDto membreDto = new MembreDto();
        membreDto.setId(membre.getId());
        membreDto.setSpecialite(membre.getSpecialite());
        membreDto.setMembreTitre(membre.getMembreTitre());
        membreDto.setDescription(membre.getDescription());
        membreDto.setUsers(membre.getUsers());
        membreDto.setEquipeId(membre.getEquipe().getId());
        return membreDto ;
    }

    public  void updateMembre(MembreDto membreDto){
        Membre membre = membreRepo.findById(membreDto.getId()).get();
        membre.setId(membreDto.getId());
        membre.setSpecialite(membreDto.getSpecialite());
        membre.setMembreTitre(membreDto.getMembreTitre());
        membre.setDescription(membreDto.getDescription());
        membre.setUsers(membreDto.getUsers());
        membreRepo.save(membre);
    }
    public List<Membre> getAllMembreNotAffected(){
        List<Membre> membres =membreRepo.findAll().stream().filter(membre -> membre.getEquipe()==null).toList();
        return membres ;
    }

    public void  affectedMembreToEquipe(List<Membre> membres , Long idEquipe){
        for (Membre membre : membres){
            membre.setEquipe(equipeRepo.findById(idEquipe).get());
        }
        membreRepo.saveAll(membres);
    }





}
