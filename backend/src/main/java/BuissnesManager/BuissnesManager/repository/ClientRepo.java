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

    @Query("SELECT c.categorieClient.id, COUNT(c) FROM Client c WHERE c.entreprise.id = :entrepriseId GROUP BY c.categorieClient.id")
    List<Object[]> countGroupedByCategorieAndEntreprise(@Param("entrepriseId") Long entrepriseId);

    @Query("SELECT c.categorieClient.id, COUNT(c) FROM Client c GROUP BY c.categorieClient.id")
    List<Object[]> countGroupedByCategorie();
}
