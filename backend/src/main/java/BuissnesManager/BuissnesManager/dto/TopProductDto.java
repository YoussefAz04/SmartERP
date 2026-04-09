package BuissnesManager.BuissnesManager.dto;

public class TopProductDto {
    private String productName;
    private Long quantitySold;
    private Double revenue;

    public TopProductDto() {}

    public TopProductDto(String productName, Long quantitySold, Double revenue) {
        this.productName = productName;
        this.quantitySold = quantitySold;
        this.revenue = revenue;
    }

    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }

    public Long getQuantitySold() { return quantitySold; }
    public void setQuantitySold(Long quantitySold) { this.quantitySold = quantitySold; }

    public Double getRevenue() { return revenue; }
    public void setRevenue(Double revenue) { this.revenue = revenue; }
}
