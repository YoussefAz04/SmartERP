package BuissnesManager.BuissnesManager.repository;

import BuissnesManager.BuissnesManager.entity.LigneCommandeVente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface LigneCommandeVenteRepository extends JpaRepository<LigneCommandeVente, Long> {
    List<LigneCommandeVente> findByCommandeVenteId(Long commandeVenteId);

    @Transactional
    void deleteByCommandeVenteId(Long commandeVenteId);
}
