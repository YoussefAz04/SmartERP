package BuissnesManager.BuissnesManager.repository;

import BuissnesManager.BuissnesManager.entity.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    List<AuditLog> findByEntrepriseIdOrderByCreatedAtDesc(Long entrepriseId);

    List<AuditLog> findAllByOrderByCreatedAtDesc();

    Page<AuditLog> findByEntrepriseId(Long entrepriseId, Pageable pageable);

    Page<AuditLog> findAll(Pageable pageable);
}
