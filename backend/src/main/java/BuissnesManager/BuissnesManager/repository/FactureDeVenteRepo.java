package BuissnesManager.BuissnesManager.repository;

import BuissnesManager.BuissnesManager.entity.FactureDeVente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface FactureDeVenteRepo extends JpaRepository<FactureDeVente,Long> {
    List<FactureDeVente> findByEntrepriseId(Long entrepriseId);
    Optional<FactureDeVente> findByCommandeVenteId(Long commandeVenteId);
    List<FactureDeVente> findByClientId(Long clientId);

    @Modifying
    @Query("UPDATE FactureDeVente f SET f.client = null WHERE f.client.id = :clientId")
    void detachClient(@Param("clientId") Long clientId);
}
