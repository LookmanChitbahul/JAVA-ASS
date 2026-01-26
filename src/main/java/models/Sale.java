package models;

import java.util.Date;
import java.util.List;

public class Sale {
    private int saleId;
    private int customerId;
    private Date saleDate;
    private double totalAmount;
    private double taxAmount;
    private double discountAmount;
    private double grandTotal;
    private String paymentMethod;
    private String createdBy;
    private List<SaleDetail> saleDetails;

    // Constructors
    public Sale() {
        this.saleDate = new Date();
    }

    public Sale(int customerId, double totalAmount) {
        this.customerId = customerId;
        this.totalAmount = totalAmount;
        this.grandTotal = totalAmount;
        this.saleDate = new Date();
    }

    // Getters and Setters
    public int getSaleId() { return saleId; }
    public void setSaleId(int saleId) { this.saleId = saleId; }

    public int getCustomerId() { return customerId; }
    public void setCustomerId(int customerId) { this.customerId = customerId; }

    public Date getSaleDate() { return saleDate; }
    public void setSaleDate(Date saleDate) { this.saleDate = saleDate; }

    public double getTotalAmount() { return totalAmount; }
    public void setTotalAmount(double totalAmount) {
        this.totalAmount = totalAmount;
        calculateGrandTotal();
    }

    public double getTaxAmount() { return taxAmount; }
    public void setTaxAmount(double taxAmount) {
        this.taxAmount = taxAmount;
        calculateGrandTotal();
    }

    public double getDiscountAmount() { return discountAmount; }
    public void setDiscountAmount(double discountAmount) {
        this.discountAmount = discountAmount;
        calculateGrandTotal();
    }

    public double getGrandTotal() {
        calculateGrandTotal();
        return grandTotal;
    }

    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }

    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }

    public List<SaleDetail> getSaleDetails() { return saleDetails; }
    public void setSaleDetails(List<SaleDetail> saleDetails) { this.saleDetails = saleDetails; }

    // Helper method to calculate grand total
    private void calculateGrandTotal() {
        this.grandTotal = totalAmount + taxAmount - discountAmount;
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
        return "Sale #" + saleId + " | Customer: " + customerId + " | Total: $" + getGrandTotal();
    }
}