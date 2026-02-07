package models;

import java.sql.Timestamp;

public class SaleDetail {
    private int saleDetailId;
    private int saleId;
    private int productId;
    private String productName;
    private int quantity;
    private double unitPrice;
    private double totalPrice;
    private double discount;
    private Timestamp createdAt;

    // Constructors
    public SaleDetail() {}

    public SaleDetail(int productId, String productName, double unitPrice, int quantity) {
        this.productId = productId;
        this.productName = productName;
        this.unitPrice = unitPrice;
        this.quantity = quantity;
        calculateTotalPrice();
    }

    // Getters and Setters
    public int getSaleDetailId() { return saleDetailId; }
    public void setSaleDetailId(int saleDetailId) { this.saleDetailId = saleDetailId; }

    public int getSaleId() { return saleId; }
    public void setSaleId(int saleId) { this.saleId = saleId; }

    public int getProductId() { return productId; }
    public void setProductId(int productId) { this.productId = productId; }

    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) {
        this.quantity = quantity;
        calculateTotalPrice();
    }

    public double getUnitPrice() { return unitPrice; }
    public void setUnitPrice(double unitPrice) {
        this.unitPrice = unitPrice;
        calculateTotalPrice();
    }

    public double getTotalPrice() {
        calculateTotalPrice(); // Ensure it's calculated
        return totalPrice;
    }

    public void setTotalPrice(double totalPrice) { this.totalPrice = totalPrice; }

    public double getDiscount() { return discount; }
    public void setDiscount(double discount) {
        this.discount = discount;
        calculateTotalPrice();
    }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    // Business logic
    private void calculateTotalPrice() {
        this.totalPrice = (unitPrice * quantity) - discount;
    }

    @Override
    public String toString() {
        return productName + " x" + quantity + " @ $" + unitPrice + " = $" + getTotalPrice();
    }
}