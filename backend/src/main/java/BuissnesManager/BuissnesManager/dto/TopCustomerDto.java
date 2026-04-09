package BuissnesManager.BuissnesManager.dto;

public class TopCustomerDto {
    private String name;
    private Double totalSpent;
    private Long ordersCount;

    public TopCustomerDto() {}

    public TopCustomerDto(String name, Double totalSpent, Long ordersCount) {
        this.name = name;
        this.totalSpent = totalSpent;
        this.ordersCount = ordersCount;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Double getTotalSpent() { return totalSpent; }
    public void setTotalSpent(Double totalSpent) { this.totalSpent = totalSpent; }

    public Long getOrdersCount() { return ordersCount; }
    public void setOrdersCount(Long ordersCount) { this.ordersCount = ordersCount; }
}
