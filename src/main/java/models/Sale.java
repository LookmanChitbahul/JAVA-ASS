package models;

import java.sql.Timestamp;
import java.util.Date;
import java.util.List;

public class Sale {
    private int saleId;
    private int customerId;
    private int userId;  // Added to match SQL
    private Date saleDate;
    private double totalAmount;
    private double discount;  // Changed from discountAmount to match SQL
    private double finalAmount;  // Changed from grandTotal to match SQL
    private String paymentMethod;
    private String status;  // Added to match SQL
    private String notes;  // Added to match SQL
    private String createdBy;
    private Timestamp createdAt;
    private Timestamp updatedAt;
    private List<SaleDetail> saleDetails;

    // Constructors
    public Sale() {
        this.saleDate = new Date();
        this.status = "Completed";  // Default status
        this.paymentMethod = "Cash";
        this.discount = 0.0;
        this.userId = 1;  // Default user ID
    }

    public Sale(int customerId, double totalAmount) {
        this();
        this.customerId = customerId;
        this.totalAmount = totalAmount;
        calculateFinalAmount();  // Calculate final amount
    }

    // Getters and Setters - UPDATED TO MATCH SQL
    public int getSaleId() { return saleId; }
    public void setSaleId(int saleId) { this.saleId = saleId; }

    public int getCustomerId() { return customerId; }
    public void setCustomerId(int customerId) { this.customerId = customerId; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public Date getSaleDate() { return saleDate; }
    public void setSaleDate(Date saleDate) { this.saleDate = saleDate; }

    public double getTotalAmount() { return totalAmount; }
    public void setTotalAmount(double totalAmount) {
        this.totalAmount = totalAmount;
        calculateFinalAmount();  // Recalculate
    }

    public double getDiscount() { return discount; }
    public void setDiscount(double discount) {
        this.discount = discount;
        calculateFinalAmount();  // Recalculate
    }

    public double getFinalAmount() {
        calculateFinalAmount();  // Ensure it's calculated
        return finalAmount;
    }

    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    public Timestamp getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Timestamp updatedAt) { this.updatedAt = updatedAt; }

    public List<SaleDetail> getSaleDetails() { return saleDetails; }
    public void setSaleDetails(List<SaleDetail> saleDetails) { this.saleDetails = saleDetails; }

    // Helper method to calculate final amount
    private void calculateFinalAmount() {
        this.finalAmount = this.totalAmount - this.discount;
    }

    // Add sale detail
    public void addSaleDetail(SaleDetail detail) {
        detail.setSaleId(this.saleId);
        if (this.saleDetails != null) {
            this.saleDetails.add(detail);
        }
    }

    @Override
    public String toString() {
        return "Sale #" + saleId +
                " | Customer: " + customerId +
                " | Total: $" + totalAmount +
                " | Discount: $" + discount +
                " | Final: $" + getFinalAmount();
    }
}