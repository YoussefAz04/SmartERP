package BuissnesManager.BuissnesManager.repository;

import BuissnesManager.BuissnesManager.entity.Membre;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface MembreRepo extends JpaRepository<Membre,Long> {




}
