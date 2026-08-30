package BuissnesManager.BuissnesManager.repository;

import BuissnesManager.BuissnesManager.entity.CreditNote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;

public interface CreditNoteRepository extends JpaRepository<CreditNote, Long> {
    List<CreditNote> findByEntrepriseIdOrderByIssueDateDescIdDesc(Long entrepriseId);

    @Query("SELECT COALESCE(SUM(c.amountTotal), 0) FROM CreditNote c WHERE c.returnDocument.salesInvoice.id = :invoiceId AND c.status = 'ISSUED'")
    BigDecimal sumIssuedBySalesInvoice(@Param("invoiceId") Long invoiceId);

    @Query("SELECT COALESCE(SUM(c.amountTotal), 0) FROM CreditNote c WHERE c.returnDocument.purchaseInvoice.id = :invoiceId AND c.status = 'ISSUED'")
    BigDecimal sumIssuedByPurchaseInvoice(@Param("invoiceId") Long invoiceId);
}
