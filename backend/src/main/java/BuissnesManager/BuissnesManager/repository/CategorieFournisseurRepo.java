package BuissnesManager.BuissnesManager.repository;

import BuissnesManager.BuissnesManager.entity.CategorieFournisseur;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CategorieFournisseurRepo extends JpaRepository<CategorieFournisseur, Long> {
}
