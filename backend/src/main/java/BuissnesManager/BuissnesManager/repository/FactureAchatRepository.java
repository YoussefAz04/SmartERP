package BuissnesManager.BuissnesManager.repository;

import BuissnesManager.BuissnesManager.entity.FactureAchat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FactureAchatRepository extends JpaRepository<FactureAchat, Long> {
	List<FactureAchat> findAll();
}
