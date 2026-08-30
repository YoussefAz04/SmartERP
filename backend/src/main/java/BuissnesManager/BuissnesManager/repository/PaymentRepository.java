package BuissnesManager.BuissnesManager.repository;

import BuissnesManager.BuissnesManager.entity.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    List<Payment> findByEntrepriseIdOrderByPaymentDateDescIdDesc(Long entrepriseId);

    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Payment p WHERE p.salesInvoice.id = :invoiceId AND p.status = 'CONFIRMED'")
    BigDecimal sumConfirmedBySalesInvoice(@Param("invoiceId") Long invoiceId);

    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Payment p WHERE p.purchaseInvoice.id = :invoiceId AND p.status = 'CONFIRMED'")
    BigDecimal sumConfirmedByPurchaseInvoice(@Param("invoiceId") Long invoiceId);
}
