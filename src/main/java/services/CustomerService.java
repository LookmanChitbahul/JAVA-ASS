package services;

import database.DBConnection;
import models.Customer;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CustomerService {

    // Use DBConnection for all queries
    private Connection getConnection() throws SQLException {
        return DBConnection.getConnection();
    }

    // Get all customers (Following schema.sql columns)
    public List<Customer> getAllCustomers() {
        List<Customer> customers = new ArrayList<>();
        String query = "SELECT *, CONCAT(first_name, ' ', last_name) as full_name FROM customers ORDER BY customer_id DESC";

        try (Connection conn = getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                Customer customer = new Customer(
                        rs.getInt("customer_id"),
                        rs.getString("full_name"),
                        hasColumn(rs, "phone") ? rs.getString("phone") : "",
                        rs.getString("email"),
                        rs.getString("address"),
                        rs.getInt("loyalty_points"),
                        hasColumn(rs, "created_at") ? rs.getString("created_at") : "",
                        hasColumn(rs, "updated_at") ? rs.getString("updated_at") : "");
                customers.add(customer);
            }
        } catch (SQLException e) {
            System.err.println("Error fetching customers: " + e.getMessage());
        }
        return customers;
    }

    private boolean hasColumn(ResultSet rs, String columnName) throws SQLException {
        ResultSetMetaData rsmd = rs.getMetaData();
        int columns = rsmd.getColumnCount();
        for (int x = 1; x <= columns; x++) {
            if (columnName.equalsIgnoreCase(rsmd.getColumnName(x))) {
                return true;
            }
        }
        return false;
    }

    public boolean addCustomer(Customer customer) {
        // Split name for schema
        String[] names = customer.getFullName().split(" ", 2);
        String fname = names[0];
        String lname = (names.length > 1) ? names[1] : "";

        String query = "INSERT INTO customers (first_name, last_name, phone, email, address, loyalty_points) VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = getConnection();
                PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, fname);
            pstmt.setString(2, lname);
            pstmt.setString(3, customer.getContact());
            pstmt.setString(4, customer.getEmail());
            pstmt.setString(5, customer.getAddress());
            pstmt.setInt(6, customer.getLoyaltyPoints());

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean searchCustomer(String keyword) {
        // Placeholder for consistency with UI calls
        return !searchCustomers(keyword).isEmpty();
    }

    public List<Customer> searchCustomers(String keyword) {
        List<Customer> customers = new ArrayList<>();
        String query = "SELECT *, CONCAT(first_name, ' ', last_name) as full_name FROM customers " +
                "WHERE first_name LIKE ? OR last_name LIKE ? OR email LIKE ? OR (phone LIKE ? AND 1=1) " +
                "ORDER BY customer_id DESC";

        try (Connection conn = getConnection();
                PreparedStatement pstmt = conn.prepareStatement(query)) {
            String pattern = "%" + keyword + "%";
            pstmt.setString(1, pattern);
            pstmt.setString(2, pattern);
            pstmt.setString(3, pattern);
            pstmt.setString(4, pattern);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    customers.add(new Customer(
                            rs.getInt("customer_id"),
                            rs.getString("full_name"),
                            hasColumn(rs, "phone") ? rs.getString("phone") : "",
                            rs.getString("email"),
                            rs.getString("address"),
                            rs.getInt("loyalty_points"),
                            hasColumn(rs, "created_at") ? rs.getString("created_at") : "",
                            hasColumn(rs, "updated_at") ? rs.getString("updated_at") : ""));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return customers;
    }

    public boolean updateCustomer(Customer customer) {
        String[] names = customer.getFullName().split(" ", 2);
        String fname = names[0];
        String lname = (names.length > 1) ? names[1] : "";
        String sql = "UPDATE customers SET first_name=?, last_name=?, phone=?, email=?, address=?, loyalty_points=? WHERE customer_id=?";
        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, fname);
            pstmt.setString(2, lname);
            pstmt.setString(3, customer.getContact());
            pstmt.setString(4, customer.getEmail());
            pstmt.setString(5, customer.getAddress());
            pstmt.setInt(6, customer.getLoyaltyPoints());
            pstmt.setInt(7, customer.getCustomerId());
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean deleteCustomer(int id) {
        try (Connection conn = getConnection();
                PreparedStatement pstmt = conn.prepareStatement("DELETE FROM customers WHERE customer_id=?")) {
            pstmt.setInt(1, id);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public Customer getCustomerById(int id) {
        String query = "SELECT *, CONCAT(first_name, ' ', last_name) as full_name FROM customers WHERE customer_id = ?";
        try (Connection conn = getConnection();
                PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return new Customer(
                            rs.getInt("customer_id"),
                            rs.getString("full_name"),
                            hasColumn(rs, "phone") ? rs.getString("phone") : "",
                            rs.getString("email"),
                            rs.getString("address"),
                            rs.getInt("loyalty_points"),
                            hasColumn(rs, "created_at") ? rs.getString("created_at") : "",
                            hasColumn(rs, "updated_at") ? rs.getString("updated_at") : "");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
}