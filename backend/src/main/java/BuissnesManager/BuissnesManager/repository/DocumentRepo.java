package BuissnesManager.BuissnesManager.repository;

import BuissnesManager.BuissnesManager.entity.Document;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DocumentRepo extends JpaRepository<Document,Long> {
}
