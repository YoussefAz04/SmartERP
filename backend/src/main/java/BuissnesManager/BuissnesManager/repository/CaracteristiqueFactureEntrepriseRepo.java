package BuissnesManager.BuissnesManager.repository;

import BuissnesManager.BuissnesManager.entity.CaracteristiqueFactureEntreprise;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface CaracteristiqueFactureEntrepriseRepo extends JpaRepository<CaracteristiqueFactureEntreprise,Long> {

    @Modifying
    @Transactional
    @Query("DELETE FROM CaracteristiqueFactureEntreprise c WHERE c.entreprise.id = :entrepriseId")
    void deleteByEntrepriseId(@Param("entrepriseId") Long entrepriseId);
}
