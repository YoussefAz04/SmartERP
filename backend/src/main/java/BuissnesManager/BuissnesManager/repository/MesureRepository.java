package BuissnesManager.BuissnesManager.repository;

import BuissnesManager.BuissnesManager.entity.Mesure;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MesureRepository extends JpaRepository<Mesure, Long> {
    List<Mesure> findByEntrepriseId(Long entrepriseId);
}
