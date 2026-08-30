package BuissnesManager.BuissnesManager.repository;

import BuissnesManager.BuissnesManager.entity.FactureDeVente;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface FactureDeVenteRepo extends JpaRepository<FactureDeVente,Long> {
    List<FactureDeVente> findByEntrepriseId(Long entrepriseId);
    Optional<FactureDeVente> findByCommandeVenteId(Long commandeVenteId);
    List<FactureDeVente> findByClientId(Long clientId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT f FROM FactureDeVente f WHERE f.id = :id")
    Optional<FactureDeVente> findByIdForUpdate(@Param("id") Long id);

    @Query("SELECT f FROM FactureDeVente f " +
           "LEFT JOIN FETCH f.commandeVente cv " +
           "LEFT JOIN FETCH cv.lignes l " +
           "LEFT JOIN FETCH l.articleVente " +
           "LEFT JOIN FETCH f.detaisFactureDeVente " +
           "LEFT JOIN FETCH f.client " +
           "WHERE f.id = :id")
    Optional<FactureDeVente> findByIdWithDetails(@Param("id") Long id);

    @Modifying
    @Query("UPDATE FactureDeVente f SET f.client = null WHERE f.client.id = :clientId")
    void detachClient(@Param("clientId") Long clientId);

    @Modifying
    @Transactional
    @Query("DELETE FROM FactureDeVente f WHERE f.entreprise.id = :entrepriseId")
    void deleteByEntrepriseId(@Param("entrepriseId") Long entrepriseId);
}
