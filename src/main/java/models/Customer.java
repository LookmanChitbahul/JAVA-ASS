package models;

public class Customer {
    private int customerId;
    private String contact;
    private String email;
    private int loyaltyPoints;
    private String createdAt;
    private String updatedAt;

    // Default constructor
    public Customer() {
        this.loyaltyPoints = 0;
    }

    // Constructor with all fields (for database retrieval)
    public Customer(int customerId, String contact, String email, int loyaltyPoints, String createdAt, String updatedAt) {
        this.customerId = customerId;
        this.contact = contact;
        this.email = email;
        this.loyaltyPoints = loyaltyPoints;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // Constructor without ID (for new customer creation)
    public Customer(String contact, String email, int loyaltyPoints) {
        this.contact = contact;
        this.email = email;
        this.loyaltyPoints = loyaltyPoints;
    }

    // Getters
    public int getCustomerId() {
        return customerId;
    }

    public String getContact() {
        return contact;
    }

    public String getEmail() {
        return email;
    }

    public int getLoyaltyPoints() {
        return loyaltyPoints;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    // Setters
    public void setCustomerId(int customerId) {
        this.customerId = customerId;
    }

    public void setContact(String contact) {
        this.contact = contact;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setLoyaltyPoints(int loyaltyPoints) {
        this.loyaltyPoints = loyaltyPoints;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }

    // toString for debugging
    @Override
    public String toString() {
        return "Customer{" +
                "customerId=" + customerId +
                ", contact='" + contact + '\'' +
                ", email='" + email + '\'' +
                ", loyaltyPoints=" + loyaltyPoints +
                ", createdAt='" + createdAt + '\'' +
                ", updatedAt='" + updatedAt + '\'' +
                '}';
    }

    // equals and hashCode
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Customer customer = (Customer) o;
        return customerId == customer.customerId;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(customerId);
    }
}