package models;

import java.sql.Timestamp;
import java.util.Date;
import java.util.List;

public class Sale {
    private int saleId;
    private int customerId;
    private int userId;
    private Date saleDate;
    private double totalAmount;
    private double discount;
    private double finalAmount;
    private String paymentMethod;
    private String status;
    private String notes;
    private String createdBy;
    private Timestamp createdAt;
    private Timestamp updatedAt;
    private List<SaleDetail> saleDetails;

    // cash handling variables
    private Double cashReceived;
    private Double changeGiven;

    // Constructors
    public Sale() {
        this.saleDate = new Date();
        this.status = "Completed";
        this.paymentMethod = "Cash";
        this.discount = 0.0;
        this.userId = 1;
        this.cashReceived = null;
        this.changeGiven = null;
    }

    public Sale(int customerId, double totalAmount) {
        this();
        this.customerId = customerId;
        this.totalAmount = totalAmount;
        calculateFinalAmount();
    }

    // Getters and Setters
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
        calculateFinalAmount();
    }

    public double getDiscount() { return discount; }
    public void setDiscount(double discount) {
        this.discount = discount;
        calculateFinalAmount();
    }

    public double getFinalAmount() {
        calculateFinalAmount();
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

    // Cash handling getters and setters
    public Double getCashReceived() { return cashReceived; }
    public void setCashReceived(Double cashReceived) { this.cashReceived = cashReceived; }

    public Double getChangeGiven() { return changeGiven; }
    public void setChangeGiven(Double changeGiven) { this.changeGiven = changeGiven; }

    // method to calculate final amount
    private void calculateFinalAmount() {
        this.finalAmount = this.totalAmount - this.discount;
    }

    // method to calculate change if cash received
    public double calculateChange() {
        if (cashReceived != null && cashReceived > 0) {
            double change = cashReceived - getFinalAmount();
            return Math.max(change, 0);
        }
        return 0.0;
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
                " | Final: $" + getFinalAmount() +
                (cashReceived != null ? " | Cash: $" + cashReceived : "") +
                (changeGiven != null ? " | Change: $" + changeGiven : "");
    }
}