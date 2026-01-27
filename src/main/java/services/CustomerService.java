package services;

import database.DBConnection;
import models.Customer;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CustomerService {
    
    // Get database connection using your DBConnection class
    private Connection getConnection() throws SQLException {
        Connection conn = DBConnection.getConnection();
        if (conn == null) {
            throw new SQLException("Unable to establish database connection");
        }
        return conn;
    }
    
    // Get all customers
    public List<Customer> getAllCustomers() {
        List<Customer> customers = new ArrayList<>();
        String query = "SELECT * FROM customers ORDER BY customer_id DESC";
        
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            while (rs.next()) {
                Customer customer = new Customer(
                    rs.getInt("customer_id"),
                    rs.getString("full_name"),
                    rs.getString("contact"),
                    rs.getString("email"),
                    rs.getString("address"),
                    rs.getInt("loyalty_points"),
                    rs.getString("created_at"),
                    rs.getString("updated_at")
                );
                customers.add(customer);
            }
        } catch (SQLException e) {
            System.err.println("Error fetching customers: " + e.getMessage());
            e.printStackTrace();
        }
        
        return customers;
    }
    
    // Add new customer
    public boolean addCustomer(Customer customer) {
        String query = "INSERT INTO customers (full_name, contact, email, address, loyalty_points, created_at, updated_at) VALUES (?, ?, ?, ?, ?, NOW(), NOW())";
        
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setString(1, customer.getFullName());
            pstmt.setString(2, customer.getContact());
            pstmt.setString(3, customer.getEmail());
            pstmt.setString(4, customer.getAddress());
            pstmt.setInt(5, customer.getLoyaltyPoints());
            
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
            
        } catch (SQLException e) {
            System.err.println("Error adding customer: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    // Update existing customer
    public boolean updateCustomer(Customer customer) {
        String query = "UPDATE customers SET full_name = ?, contact = ?, email = ?, address = ?, loyalty_points = ?, updated_at = NOW() WHERE customer_id = ?";
        
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setString(1, customer.getFullName());
            pstmt.setString(2, customer.getContact());
            pstmt.setString(3, customer.getEmail());
            pstmt.setString(4, customer.getAddress());
            pstmt.setInt(5, customer.getLoyaltyPoints());
            pstmt.setInt(6, customer.getCustomerId());
            
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
            
        } catch (SQLException e) {
            System.err.println("Error updating customer: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    // Delete customer
    public boolean deleteCustomer(int customerId) {
        String query = "DELETE FROM customers WHERE customer_id = ?";
        
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setInt(1, customerId);
            
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
            
        } catch (SQLException e) {
            System.err.println("Error deleting customer: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    // Search customers by full name, contact, email, address, or ID
    public List<Customer> searchCustomers(String keyword) {
        List<Customer> customers = new ArrayList<>();
        String query = "SELECT * FROM customers WHERE " +
                      "full_name LIKE ? OR " +
                      "contact LIKE ? OR " +
                      "email LIKE ? OR " +
                      "address LIKE ? OR " +
                      "CAST(customer_id AS CHAR) LIKE ? " +
                      "ORDER BY customer_id DESC";
        
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            String searchPattern = "%" + keyword + "%";
            pstmt.setString(1, searchPattern);
            pstmt.setString(2, searchPattern);
            pstmt.setString(3, searchPattern);
            pstmt.setString(4, searchPattern);
            pstmt.setString(5, searchPattern);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Customer customer = new Customer(
                        rs.getInt("customer_id"),
                        rs.getString("full_name"),
                        rs.getString("contact"),
                        rs.getString("email"),
                        rs.getString("address"),
                        rs.getInt("loyalty_points"),
                        rs.getString("created_at"),
                        rs.getString("updated_at")
                    );
                    customers.add(customer);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error searching customers: " + e.getMessage());
            e.printStackTrace();
        }
        
        return customers;
    }
    
    // Get customer by ID
    public Customer getCustomerById(int customerId) {
        String query = "SELECT * FROM customers WHERE customer_id = ?";
        
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setInt(1, customerId);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return new Customer(
                        rs.getInt("customer_id"),
                        rs.getString("full_name"),
                        rs.getString("contact"),
                        rs.getString("email"),
                        rs.getString("address"),
                        rs.getInt("loyalty_points"),
                        rs.getString("created_at"),
                        rs.getString("updated_at")
                    );
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching customer by ID: " + e.getMessage());
            e.printStackTrace();
        }
        
        return null;
    }
    
    // Get total customer count
    public int getTotalCustomers() {
        String query = "SELECT COUNT(*) as total FROM customers";
        
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            if (rs.next()) {
                return rs.getInt("total");
            }
        } catch (SQLException e) {
            System.err.println("Error counting customers: " + e.getMessage());
            e.printStackTrace();
        }
        
        return 0;
    }
    
    // Get total loyalty points
    public int getTotalLoyaltyPoints() {
        String query = "SELECT SUM(loyalty_points) as total FROM customers";
        
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            if (rs.next()) {
                Object total = rs.getObject("total");
                return total != null ? rs.getInt("total") : 0;
            }
        } catch (SQLException e) {
            System.err.println("Error summing loyalty points: " + e.getMessage());
            e.printStackTrace();
        }
        
        return 0;
    }
}