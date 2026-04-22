package BuissnesManager.BuissnesManager.dto;

import java.util.ArrayList;
import java.util.List;

public class CashFlowDto {

    private CashFlowSummaryDto summary;
    private List<CashFlowPointDto> monthlyFlow = new ArrayList<>();
    private List<InvoiceAgingDto> invoiceAging = new ArrayList<>();
    private List<PaymentMethodDto> paymentMethods = new ArrayList<>();
    private List<CashFlowForecastDto> forecast = new ArrayList<>();
    private List<OutstandingInvoiceDto> outstandingInvoices = new ArrayList<>();

    public static class CashFlowSummaryDto {
        private double totalReceivables;
        private double totalPayables;
        private double netCashPosition;
        private double receivablesOverdue;
        private double payablesOverdue;
        private double cashConversionCycle;
        private double avgCollectionDays;
        private double avgPaymentDays;

        public double getTotalReceivables() { return totalReceivables; }
        public void setTotalReceivables(double totalReceivables) { this.totalReceivables = totalReceivables; }
        public double getTotalPayables() { return totalPayables; }
        public void setTotalPayables(double totalPayables) { this.totalPayables = totalPayables; }
        public double getNetCashPosition() { return netCashPosition; }
        public void setNetCashPosition(double netCashPosition) { this.netCashPosition = netCashPosition; }
        public double getReceivablesOverdue() { return receivablesOverdue; }
        public void setReceivablesOverdue(double receivablesOverdue) { this.receivablesOverdue = receivablesOverdue; }
        public double getPayablesOverdue() { return payablesOverdue; }
        public void setPayablesOverdue(double payablesOverdue) { this.payablesOverdue = payablesOverdue; }
        public double getCashConversionCycle() { return cashConversionCycle; }
        public void setCashConversionCycle(double cashConversionCycle) { this.cashConversionCycle = cashConversionCycle; }
        public double getAvgCollectionDays() { return avgCollectionDays; }
        public void setAvgCollectionDays(double avgCollectionDays) { this.avgCollectionDays = avgCollectionDays; }
        public double getAvgPaymentDays() { return avgPaymentDays; }
        public void setAvgPaymentDays(double avgPaymentDays) { this.avgPaymentDays = avgPaymentDays; }
    }

    public static class CashFlowPointDto {
        private String month;
        private double inflows;
        private double outflows;
        private double netFlow;
        private double cumulativeFlow;

        public String getMonth() { return month; }
        public void setMonth(String month) { this.month = month; }
        public double getInflows() { return inflows; }
        public void setInflows(double inflows) { this.inflows = inflows; }
        public double getOutflows() { return outflows; }
        public void setOutflows(double outflows) { this.outflows = outflows; }
        public double getNetFlow() { return netFlow; }
        public void setNetFlow(double netFlow) { this.netFlow = netFlow; }
        public double getCumulativeFlow() { return cumulativeFlow; }
        public void setCumulativeFlow(double cumulativeFlow) { this.cumulativeFlow = cumulativeFlow; }
    }

    public static class InvoiceAgingDto {
        private String bucket;
        private double amount;
        private int invoiceCount;
        private String riskLevel;

        public String getBucket() { return bucket; }
        public void setBucket(String bucket) { this.bucket = bucket; }
        public double getAmount() { return amount; }
        public void setAmount(double amount) { this.amount = amount; }
        public int getInvoiceCount() { return invoiceCount; }
        public void setInvoiceCount(int invoiceCount) { this.invoiceCount = invoiceCount; }
        public String getRiskLevel() { return riskLevel; }
        public void setRiskLevel(String riskLevel) { this.riskLevel = riskLevel; }
    }

    public static class PaymentMethodDto {
        private String method;
        private int transactionCount;
        private double totalAmount;
        private double percentage;

        public String getMethod() { return method; }
        public void setMethod(String method) { this.method = method; }
        public int getTransactionCount() { return transactionCount; }
        public void setTransactionCount(int transactionCount) { this.transactionCount = transactionCount; }
        public double getTotalAmount() { return totalAmount; }
        public void setTotalAmount(double totalAmount) { this.totalAmount = totalAmount; }
        public double getPercentage() { return percentage; }
        public void setPercentage(double percentage) { this.percentage = percentage; }
    }

    public static class CashFlowForecastDto {
        private String period;
        private double predictedInflows;
        private double predictedOutflows;
        private double confidence;
        private String scenario;

        public String getPeriod() { return period; }
        public void setPeriod(String period) { this.period = period; }
        public double getPredictedInflows() { return predictedInflows; }
        public void setPredictedInflows(double predictedInflows) { this.predictedInflows = predictedInflows; }
        public double getPredictedOutflows() { return predictedOutflows; }
        public void setPredictedOutflows(double predictedOutflows) { this.predictedOutflows = predictedOutflows; }
        public double getConfidence() { return confidence; }
        public void setConfidence(double confidence) { this.confidence = confidence; }
        public String getScenario() { return scenario; }
        public void setScenario(String scenario) { this.scenario = scenario; }
    }

    public static class OutstandingInvoiceDto {
        private Long invoiceId;
        private String reference;
        private String customerName;
        private double amount;
        private String dueDate;
        private int daysOverdue;
        private String riskLevel;

        public Long getInvoiceId() { return invoiceId; }
        public void setInvoiceId(Long invoiceId) { this.invoiceId = invoiceId; }
        public String getReference() { return reference; }
        public void setReference(String reference) { this.reference = reference; }
        public String getCustomerName() { return customerName; }
        public void setCustomerName(String customerName) { this.customerName = customerName; }
        public double getAmount() { return amount; }
        public void setAmount(double amount) { this.amount = amount; }
        public String getDueDate() { return dueDate; }
        public void setDueDate(String dueDate) { this.dueDate = dueDate; }
        public int getDaysOverdue() { return daysOverdue; }
        public void setDaysOverdue(int daysOverdue) { this.daysOverdue = daysOverdue; }
        public String getRiskLevel() { return riskLevel; }
        public void setRiskLevel(String riskLevel) { this.riskLevel = riskLevel; }
    }

    public CashFlowSummaryDto getSummary() { return summary; }
    public void setSummary(CashFlowSummaryDto summary) { this.summary = summary; }
    public List<CashFlowPointDto> getMonthlyFlow() { return monthlyFlow; }
    public void setMonthlyFlow(List<CashFlowPointDto> monthlyFlow) { this.monthlyFlow = monthlyFlow; }
    public List<InvoiceAgingDto> getInvoiceAging() { return invoiceAging; }
    public void setInvoiceAging(List<InvoiceAgingDto> invoiceAging) { this.invoiceAging = invoiceAging; }
    public List<PaymentMethodDto> getPaymentMethods() { return paymentMethods; }
    public void setPaymentMethods(List<PaymentMethodDto> paymentMethods) { this.paymentMethods = paymentMethods; }
    public List<CashFlowForecastDto> getForecast() { return forecast; }
    public void setForecast(List<CashFlowForecastDto> forecast) { this.forecast = forecast; }
    public List<OutstandingInvoiceDto> getOutstandingInvoices() { return outstandingInvoices; }
    public void setOutstandingInvoices(List<OutstandingInvoiceDto> outstandingInvoices) { this.outstandingInvoices = outstandingInvoices; }
}
