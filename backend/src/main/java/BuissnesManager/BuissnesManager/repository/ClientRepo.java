package BuissnesManager.BuissnesManager.repository;

import BuissnesManager.BuissnesManager.entity.Client;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ClientRepo extends JpaRepository<Client,Long> {
    List<Client> findByEntrepriseId(Long entrepriseId);
    Optional<Client> findByIdAndEntrepriseId(Long id, Long entrepriseId);

    @Query("SELECT COUNT(c) FROM Client c WHERE c.entreprise.id = :entrepriseId")
    Long getTotalClients(@Param("entrepriseId") Long entrepriseId);

    @Query("SELECT COUNT(c) FROM Client c WHERE c.categorieClient.id = :categorieId AND c.entreprise.id = :entrepriseId")
    long countByCategorieClientIdAndEntrepriseId(@Param("categorieId") Long categorieId, @Param("entrepriseId") Long entrepriseId);

    long countByCategorieClientId(Long categorieId);
}
