package BuissnesManager.BuissnesManager.repository;

import BuissnesManager.BuissnesManager.entity.Projet;
import BuissnesManager.BuissnesManager.entity.StatutProjet;
import BuissnesManager.BuissnesManager.entity.StatutTache;
import BuissnesManager.BuissnesManager.entity.Tache;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TacheRepo extends JpaRepository<Tache,Long> {
    List<Tache> findAllByProjetId(Long idProjet);
    List<Tache> findAllByStatut(StatutTache statutTache) ;

}
