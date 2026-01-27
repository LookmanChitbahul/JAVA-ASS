package models;

public class Customer {
    private int customerId;
    private String fullName;
    private String contact;
    private String email;
    private String address;
    private int loyaltyPoints;
    private String createdAt;
    private String updatedAt;

    // Default constructor
    public Customer() {
        this.loyaltyPoints = 0;
    }

    // Constructor with all fields (for database retrieval)
    public Customer(int customerId, String fullName, String contact, String email, String address, int loyaltyPoints, String createdAt, String updatedAt) {
        this.customerId = customerId;
        this.fullName = fullName;
        this.contact = contact;
        this.email = email;
        this.address = address;
        this.loyaltyPoints = loyaltyPoints;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // Constructor without ID (for new customer creation)
    public Customer(String fullName, String contact, String email, String address, int loyaltyPoints) {
        this.fullName = fullName;
        this.contact = contact;
        this.email = email;
        this.address = address;
        this.loyaltyPoints = loyaltyPoints;
    }

    // Getters
    public int getCustomerId() {
        return customerId;
    }

    public String getFullName() {
        return fullName;
    }

    public String getContact() {
        return contact;
    }

    public String getEmail() {
        return email;
    }

    public String getAddress() {
        return address;
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

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public void setContact(String contact) {
        this.contact = contact;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setAddress(String address) {
        this.address = address;
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
                ", fullName='" + fullName + '\'' +
                ", contact='" + contact + '\'' +
                ", email='" + email + '\'' +
                ", address='" + address + '\'' +
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