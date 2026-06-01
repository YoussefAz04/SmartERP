package BuissnesManager.BuissnesManager.service;

import BuissnesManager.BuissnesManager.dto.DashboardHomeSummaryDto;
import BuissnesManager.BuissnesManager.dto.DashboardStatsDto;
import BuissnesManager.BuissnesManager.dto.MonthlySalesDto;
import BuissnesManager.BuissnesManager.dto.TopCustomerDto;
import BuissnesManager.BuissnesManager.dto.TopProductDto;
import BuissnesManager.BuissnesManager.repository.ArticleRepository;
import BuissnesManager.BuissnesManager.repository.ClientRepo;
import BuissnesManager.BuissnesManager.repository.CommandeAchatRepository;
import BuissnesManager.BuissnesManager.repository.CommandeVenteRepository;
import BuissnesManager.BuissnesManager.repository.DetaisFactureDeVenteRepo;
import BuissnesManager.BuissnesManager.repository.FactureAchatRepo;
import BuissnesManager.BuissnesManager.repository.FactureVenteRepository;
import BuissnesManager.BuissnesManager.repository.FournisseurRepo;
import BuissnesManager.BuissnesManager.repository.StockRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.Month;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
public class DashboardService {

    @Autowired
    private CommandeVenteRepository commandeVenteRepository;

    @Autowired
    private CommandeAchatRepository commandeAchatRepository;

    @Autowired
    private StockRepository stockRepository;

    @Autowired
    private ClientRepo clientRepo;

    @Autowired
    private FournisseurRepo fournisseurRepo;

    @Autowired
    private ArticleRepository articleRepository;

    @Autowired
    private FactureVenteRepository factureVenteRepository;

    @Autowired
    private FactureAchatRepo factureAchatRepo;

    



    @Autowired(required = false)
    private DetaisFactureDeVenteRepo detaisFactureDeVenteRepo;

    






    @Cacheable(cacheNames = "dashboardStats",
               key = "#entrepriseId + '-' + (T(java.lang.System).currentTimeMillis() / 300000)")
    public DashboardStatsDto getDashboardStats(Long entrepriseId) {
        DashboardStatsDto stats = new DashboardStatsDto();

        LocalDate now = LocalDate.now();
        LocalDate startOfMonth = now.withDayOfMonth(1);
        LocalDate endOfMonth = now.withDayOfMonth(now.lengthOfMonth());
        LocalDate startOfYear = now.withDayOfYear(1);
        LocalDate endOfYear = now.withDayOfYear(now.lengthOfYear());
        int currentYear = now.getYear();

        
        Double totalSales = commandeVenteRepository.getTotalSalesRevenue(entrepriseId);
        stats.setTotalSalesRevenue(orZero(totalSales));

        Double salesThisMonth = commandeVenteRepository.getSalesInPeriod(entrepriseId, startOfMonth, endOfMonth);
        stats.setSalesThisMonth(orZero(salesThisMonth));

        Double salesThisYear = commandeVenteRepository.getSalesInPeriod(entrepriseId, startOfYear, endOfYear);
        stats.setSalesThisYear(orZero(salesThisYear));

        Long totalOrders = commandeVenteRepository.getTotalSalesOrders(entrepriseId);
        stats.setTotalOrders(orZeroLong(totalOrders));

        if (totalOrders != null && totalOrders > 0 && totalSales != null) {
            stats.setAverageOrderValue(totalSales / totalOrders);
        } else {
            stats.setAverageOrderValue(0.0);
        }

        
        Double totalPurchases = commandeAchatRepository.getTotalPurchases(entrepriseId);
        stats.setTotalPurchases(orZero(totalPurchases));

        Double purchasesThisMonth = commandeAchatRepository.getPurchasesInPeriod(entrepriseId, startOfMonth, endOfMonth);
        stats.setPurchasesThisMonth(orZero(purchasesThisMonth));

        Long totalPurchaseOrders = commandeAchatRepository.getTotalPurchaseOrders(entrepriseId);
        stats.setTotalPurchaseOrders(orZeroLong(totalPurchaseOrders));

        
        Double totalStockValue = stockRepository.getTotalStockValue(entrepriseId);
        stats.setTotalStockValue(orZero(totalStockValue));

        Long lowStockItems = stockRepository.getLowStockItemsCount(entrepriseId);
        stats.setLowStockItemsCount(orZeroLong(lowStockItems));

        Long outOfStockItems = stockRepository.getOutOfStockItemsCount(entrepriseId);
        stats.setOutOfStockItemsCount(orZeroLong(outOfStockItems));

        Long totalSKUs = stockRepository.getTotalSKUs(entrepriseId);
        stats.setTotalSKUs(orZeroLong(totalSKUs));

        
        Double totalRevenue = factureVenteRepository.getTotalRevenuePaid(entrepriseId);
        stats.setTotalRevenue(orZero(totalRevenue));

        Double totalExpenses = factureAchatRepo.getTotalExpensesPaid(entrepriseId);
        stats.setTotalExpenses(orZero(totalExpenses));

        stats.setProfitLoss(orZero(totalRevenue) - orZero(totalExpenses));

        Double accountsReceivable = factureVenteRepository.getAccountsReceivable(entrepriseId);
        stats.setAccountsReceivable(orZero(accountsReceivable));

        Double accountsPayable = factureAchatRepo.getAccountsPayable(entrepriseId);
        stats.setAccountsPayable(orZero(accountsPayable));

        
        Long totalCustomers = clientRepo.getTotalClients(entrepriseId);
        stats.setTotalCustomers(orZeroLong(totalCustomers));
        
        stats.setNewCustomersThisMonth(0L);

        
        List<Object[]> topCustomersRaw = commandeVenteRepository.getTopCustomers(entrepriseId);
        List<TopCustomerDto> topCustomers = new ArrayList<>();
        int limit = Math.min(5, topCustomersRaw.size());
        for (int i = 0; i < limit; i++) {
            Object[] row = topCustomersRaw.get(i);
            String name = row[0] != null ? row[0].toString() : "Unknown";
            Double spent = row[1] != null ? ((Number) row[1]).doubleValue() : 0.0;
            Long orders = row[2] != null ? ((Number) row[2]).longValue() : 0L;
            topCustomers.add(new TopCustomerDto(name, spent, orders));
        }
        stats.setTopCustomers(topCustomers);

        
        List<Object[]> salesStatusRaw = commandeVenteRepository.getOrderStatusBreakdown(entrepriseId);
        Map<String, Long> salesStatusMap = new HashMap<>();
        for (Object[] row : salesStatusRaw) {
            String status = row[0] != null ? row[0].toString() : "UNKNOWN";
            Long count = row[1] != null ? ((Number) row[1]).longValue() : 0L;
            salesStatusMap.put(status, count);
        }
        stats.setOrderStatusBreakdown(salesStatusMap);

        List<Object[]> purchaseStatusRaw = commandeAchatRepository.getPurchaseOrderStatusBreakdown(entrepriseId);
        Map<String, Long> purchaseStatusMap = new HashMap<>();
        for (Object[] row : purchaseStatusRaw) {
            String status = row[0] != null ? row[0].toString() : "UNKNOWN";
            Long count = row[1] != null ? ((Number) row[1]).longValue() : 0L;
            purchaseStatusMap.put(status, count);
        }
        stats.setPurchaseOrderStatusBreakdown(purchaseStatusMap);

        
        List<Object[]> monthlySalesRaw = commandeVenteRepository.getMonthlySales(entrepriseId, currentYear);
        List<MonthlySalesDto> monthlySalesChart = buildMonthlyChart(monthlySalesRaw);
        stats.setMonthlySalesChart(monthlySalesChart);

        
        List<Object[]> monthlyPurchasesRaw = commandeAchatRepository.getMonthlyPurchases(entrepriseId, currentYear);
        List<MonthlySalesDto> monthlyPurchasesChart = buildMonthlyChart(monthlyPurchasesRaw);
        stats.setMonthlyPurchasesChart(monthlyPurchasesChart);

        
        List<Object[]> topProductsRaw = commandeVenteRepository.getTopProducts(entrepriseId);
        
        
        
        if (topProductsRaw.isEmpty() && detaisFactureDeVenteRepo != null) {
            try {
                List<Object[]> fallback = detaisFactureDeVenteRepo.getTopProductsFromFactures(entrepriseId);
                if (fallback != null && !fallback.isEmpty()) {
                    topProductsRaw = fallback;
                }
            } catch (Exception ignored) {
                
            }
        }
        List<TopProductDto> topProducts = new ArrayList<>();
        int productLimit = Math.min(10, topProductsRaw.size());
        for (int i = 0; i < productLimit; i++) {
            Object[] row = topProductsRaw.get(i);
            String productName = row[0] != null ? row[0].toString() : "Unknown";
            Long qtySold = row[1] != null ? ((Number) row[1]).longValue() : 0L;
            Double revenue = row[2] != null ? ((Number) row[2]).doubleValue() : 0.0;
            topProducts.add(new TopProductDto(productName, qtySold, revenue));
        }
        stats.setTopProducts(topProducts);

        return stats;
    }

    private List<MonthlySalesDto> buildMonthlyChart(List<Object[]> raw) {
        Map<Integer, Double> monthMap = new HashMap<>();
        for (Object[] row : raw) {
            int month = ((Number) row[0]).intValue();
            double amount = row[1] != null ? ((Number) row[1]).doubleValue() : 0.0;
            monthMap.put(month, amount);
        }
        List<MonthlySalesDto> chart = new ArrayList<>();
        for (int m = 1; m <= 12; m++) {
            String monthName = Month.of(m).getDisplayName(TextStyle.SHORT, Locale.ENGLISH);
            chart.add(new MonthlySalesDto(monthName, monthMap.getOrDefault(m, 0.0)));
        }
        return chart;
    }

    private Double orZero(Double value) {
        return value != null ? value : 0.0;
    }

    private Long orZeroLong(Long value) {
        return value != null ? value : 0L;
    }

    







    @Cacheable(cacheNames = "dashboardHomeSummary",
               key = "#entrepriseId + '-' + (T(java.lang.System).currentTimeMillis() / 60000)")
    public DashboardHomeSummaryDto getHomeSummary(Long entrepriseId) {
        DashboardHomeSummaryDto dto = new DashboardHomeSummaryDto();

        
        Long totalClients = clientRepo.getTotalClients(entrepriseId);
        dto.setTotalClients(totalClients != null ? totalClients : 0L);
        dto.setTotalFournisseurs(fournisseurRepo.countByEntrepriseId(entrepriseId));
        dto.setTotalArticles(articleRepository.countByEntrepriseId(entrepriseId));
        dto.setTotalCommandes(commandeVenteRepository.countByEntrepriseId(entrepriseId));

        
        PageRequest top5 = PageRequest.of(0, 5);
        dto.setRecentClients(clientRepo.findRecentByEntreprise(entrepriseId, top5));
        dto.setRecentFournisseurs(fournisseurRepo.findRecentByEntreprise(entrepriseId, top5));
        dto.setRecentStocks(stockRepository.findRecentByEntreprise(entrepriseId, top5));
        dto.setRecentCommandes(commandeVenteRepository.findRecentByEntreprise(entrepriseId, top5));

        return dto;
    }
}
