package BuissnesManager.BuissnesManager.service;

import BuissnesManager.BuissnesManager.entity.Entreprise;
import BuissnesManager.BuissnesManager.repository.EntrepriseRepo;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.mockito.Mockito.*;

class OverduePaymentSchedulerTest {
    @Test
    void scansEveryActiveEnterprise() {
        EntrepriseRepo entrepriseRepository = mock(EntrepriseRepo.class);
        FinancialService financialService = mock(FinancialService.class);
        Entreprise first = new Entreprise();
        first.setId(1L);
        Entreprise second = new Entreprise();
        second.setId(2L);
        when(entrepriseRepository.findByActiveTrue()).thenReturn(List.of(first, second));
        new OverduePaymentScheduler(entrepriseRepository, financialService).scanActiveEnterprises();

        verify(financialService).createOverdueNotificationsForEnterprise(1L);
        verify(financialService).createOverdueNotificationsForEnterprise(2L);
    }
}
