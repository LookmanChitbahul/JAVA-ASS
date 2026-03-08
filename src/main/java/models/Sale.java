package models;

import java.sql.Timestamp;
import java.util.Date;
import java.util.List;

/*
 Represents a sales transaction in the retail system.
 Supports two types of sales:
 1. Quick Cash Sale - For walk-in customers (customerId = 1)
 2. Regular Sale - For registered customers with full details
 */
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
    private Timestamp createdAt;
    private Timestamp updatedAt;
    private List<SaleDetail> saleDetails;

    // Cash handling variables
    private Double cashReceived;
    private Double changeGiven;

    // Constants
    public static final int WALK_IN_CUSTOMER_ID = 1; // customer_id for walk-in customers
    public static final double DEFAULT_TAX_RATE = 0.10; // 10% tax

    //Constructors

    public Sale() {
        this.saleDate = new Date();
        this.status = "Completed";
        this.paymentMethod = "Cash";
        this.discount = 0.0;
    }


    public Sale(int customerId, double totalAmount) {
        this();
        this.customerId = customerId;
        this.totalAmount = totalAmount;
        calculateFinalAmount();
    }

    //Factory method for creating a quick cash sale (walk-in customer)


    public static Sale createQuickCashSale(int userId, double totalAmount, double cashReceived) {
        Sale sale = new Sale();
        sale.setCustomerId(WALK_IN_CUSTOMER_ID);
        sale.setUserId(userId);
        sale.setTotalAmount(totalAmount);
        sale.setPaymentMethod("Cash");
        sale.setCashReceived(cashReceived);
        sale.setNotes("Quick cash sale - Walk-in customer");
        sale.calculateFinalAmount();

        double change = cashReceived - sale.getFinalAmount();
        sale.setChangeGiven(change > 0 ? change : 0);

        return sale;
    }

    //Factory method for creating a regular sale with registered customer

    public static Sale createRegularSale(int customerId, int userId, double totalAmount, String paymentMethod) {
        Sale sale = new Sale();
        sale.setCustomerId(customerId);
        sale.setUserId(userId);
        sale.setTotalAmount(totalAmount);
        sale.setPaymentMethod(paymentMethod);
        sale.setNotes("Regular sale - Registered customer");
        sale.calculateFinalAmount();
        return sale;
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

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    public Timestamp getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Timestamp updatedAt) { this.updatedAt = updatedAt; }

    public List<SaleDetail> getSaleDetails() { return saleDetails; }
    public void setSaleDetails(List<SaleDetail> saleDetails) { this.saleDetails = saleDetails; }

    public Double getCashReceived() { return cashReceived; }
    public void setCashReceived(Double cashReceived) { this.cashReceived = cashReceived; }

    public Double getChangeGiven() { return changeGiven; }
    public void setChangeGiven(Double changeGiven) { this.changeGiven = changeGiven; }

    //Checks if this is a quick cash sale (walk-in customer)

    public boolean isQuickCashSale() {
        return this.customerId == WALK_IN_CUSTOMER_ID && "Cash".equalsIgnoreCase(this.paymentMethod);
    }

    //Calculates the tax amount based on total and tax rate

    public double calculateTax() {
        return this.totalAmount * DEFAULT_TAX_RATE;
    }

    //Calculates the grand total including tax

    public double calculateGrandTotal() {
        return this.totalAmount + calculateTax() - this.discount;
    }

    // Private method to calculate final amount
    private void calculateFinalAmount() {
        this.finalAmount = this.totalAmount - this.discount;
    }

    // Public method to calculate change
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
        StringBuilder sb = new StringBuilder();
        sb.append("Sale #").append(saleId);
        if (isQuickCashSale()) {
            sb.append(" [QUICK CASH SALE]");
        }
        sb.append(" | Customer: ").append(customerId == WALK_IN_CUSTOMER_ID ? "Walk-in" : customerId);
        sb.append(" | Total: $").append(String.format("%.2f", totalAmount));
        sb.append(" | Tax: $").append(String.format("%.2f", calculateTax()));
        sb.append(" | Final: $").append(String.format("%.2f", getFinalAmount()));

        if (cashReceived != null) {
            sb.append(" | Cash: $").append(String.format("%.2f", cashReceived));
        }
        if (changeGiven != null) {
            sb.append(" | Change: $").append(String.format("%.2f", changeGiven));
        }
        return sb.toString();
    }
}