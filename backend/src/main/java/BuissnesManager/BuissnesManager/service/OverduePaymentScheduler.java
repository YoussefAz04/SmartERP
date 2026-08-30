package BuissnesManager.BuissnesManager.service;

import BuissnesManager.BuissnesManager.entity.Entreprise;
import BuissnesManager.BuissnesManager.repository.EntrepriseRepo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "app.finance.overdue-scan.enabled", havingValue = "true", matchIfMissing = true)
public class OverduePaymentScheduler {
    private static final Logger log = LoggerFactory.getLogger(OverduePaymentScheduler.class);

    private final EntrepriseRepo entrepriseRepository;
    private final FinancialService financialService;

    public OverduePaymentScheduler(EntrepriseRepo entrepriseRepository, FinancialService financialService) {
        this.entrepriseRepository = entrepriseRepository;
        this.financialService = financialService;
    }

    @Scheduled(cron = "${app.finance.overdue-scan.cron:0 15 6 * * *}",
            zone = "${app.finance.overdue-scan.zone:Africa/Tunis}")
    public void scanActiveEnterprises() {
        int created = 0;
        for (Entreprise entreprise : entrepriseRepository.findByActiveTrue()) {
            try {
                created += financialService.createOverdueNotificationsForEnterprise(entreprise.getId());
            } catch (RuntimeException error) {
                log.error("Overdue payment scan failed for enterprise {}", entreprise.getId(), error);
            }
        }
        log.info("Overdue payment scan completed: {} notification(s) created", created);
    }
}
