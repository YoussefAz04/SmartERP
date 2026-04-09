package BuissnesManager.BuissnesManager.repository;

import BuissnesManager.BuissnesManager.entity.Fournisseur;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface FournisseurRepo extends JpaRepository<Fournisseur, Long> {
    List<Fournisseur> findByNomContainingIgnoreCase(String nom);
    List<Fournisseur> findByEntrepriseId(Long entrepriseId);
    Optional<Fournisseur> findByIdAndEntrepriseId(Long id, Long entrepriseId);
}
