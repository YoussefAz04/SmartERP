package BuissnesManager.BuissnesManager.repository;

import BuissnesManager.BuissnesManager.entity.ReturnLine;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;

public interface ReturnLineRepository extends JpaRepository<ReturnLine, Long> {
    @Query("SELECT COALESCE(SUM(l.quantity), 0) FROM ReturnLine l WHERE l.salesOrderLine.id = :lineId AND l.returnDocument.status <> 'CANCELLED'")
    BigDecimal sumActiveBySalesOrderLine(@Param("lineId") Long lineId);

    @Query("SELECT COALESCE(SUM(l.quantity), 0) FROM ReturnLine l WHERE l.purchaseOrderLine.id = :lineId AND l.returnDocument.status <> 'CANCELLED'")
    BigDecimal sumActiveByPurchaseOrderLine(@Param("lineId") Long lineId);
}
