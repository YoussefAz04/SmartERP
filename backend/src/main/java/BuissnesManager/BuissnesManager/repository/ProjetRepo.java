package BuissnesManager.BuissnesManager.repository;

import BuissnesManager.BuissnesManager.entity.Projet;
import BuissnesManager.BuissnesManager.entity.StatutProjet;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProjetRepo extends JpaRepository<Projet,Long> {

    List<Projet> findAllByStatut(StatutProjet statutProjet) ;

}
