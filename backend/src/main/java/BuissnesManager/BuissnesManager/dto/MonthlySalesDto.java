package BuissnesManager.BuissnesManager.dto;

public class MonthlySalesDto {
    private String month;
    private Double amount;

    public MonthlySalesDto() {}

    public MonthlySalesDto(String month, Double amount) {
        this.month = month;
        this.amount = amount;
    }

    public String getMonth() { return month; }
    public void setMonth(String month) { this.month = month; }

    public Double getAmount() { return amount; }
    public void setAmount(Double amount) { this.amount = amount; }
}
