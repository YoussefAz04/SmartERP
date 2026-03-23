package BuissnesManager.BuissnesManager.repository;

import BuissnesManager.BuissnesManager.entity.LigneFactureVente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface LigneFactureVenteRepository extends JpaRepository<LigneFactureVente, Long> {
    List<LigneFactureVente> findByFactureVenteId(Long factureVenteId);

    @Transactional
    void deleteByFactureVenteId(Long factureVenteId);
}
