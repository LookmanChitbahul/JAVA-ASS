import database.DBConnection;
import models.Customer;
import services.CustomerService;

import java.sql.Connection;
import java.util.List;

public class CustomerServiceTest {
    
    public static void main(String[] args) {
        System.out.println("========================================");
        System.out.println("   CUSTOMER SERVICE TEST SUITE");
        System.out.println("========================================\n");
        
        CustomerService service = new CustomerService();
        
        // Test 1: Database Connection
        testDatabaseConnection();
        
        // Test 2: Get All Customers
        testGetAllCustomers(service);
        
        // Test 3: Add Customer
        testAddCustomer(service);
        
        // Test 4: Search Customer
        testSearchCustomer(service);
        
        // Test 5: Update Customer
        testUpdateCustomer(service);
        
        // Test 6: Delete Customer (optional - uncomment to test deletion)
        testDeleteCustomer(service);
        
        System.out.println("\n========================================");
        System.out.println("   ALL TESTS COMPLETED");
        System.out.println("========================================");
    }
    
    // Test 1: Database Connection
    private static void testDatabaseConnection() {
        System.out.println("TEST 1: Database Connection");
        System.out.println("----------------------------");
        
        try {
            Connection conn = DBConnection.getConnection();
            if (conn != null && !conn.isClosed()) {
                System.out.println("✓ SUCCESS: Database connection established");
                System.out.println("  Database: smart_retail");
                conn.close();
            } else {
                System.out.println("✗ FAILED: Could not connect to database");
            }
        } catch (Exception e) {
            System.out.println("✗ ERROR: " + e.getMessage());
            e.printStackTrace();
        }
        System.out.println();
    }
    
    // Test 2: Get All Customers
    private static void testGetAllCustomers(CustomerService service) {
        System.out.println("TEST 2: Get All Customers");
        System.out.println("----------------------------");
        
        try {
            List<Customer> customers = service.getAllCustomers();
            System.out.println("✓ SUCCESS: Retrieved " + customers.size() + " customers");
            
            if (!customers.isEmpty()) {
                System.out.println("\nSample customer data:");
                Customer first = customers.get(0);
                System.out.println("  ID: " + first.getCustomerId());
                System.out.println("  Full Name: " + first.getFullName());
                System.out.println("  Contact: " + first.getContact());
                System.out.println("  Email: " + first.getEmail());
                System.out.println("  Address: " + first.getAddress());
                System.out.println("  Points: " + first.getLoyaltyPoints());
                System.out.println("  Created: " + first.getCreatedAt());
                System.out.println("  Updated: " + first.getUpdatedAt());
            } else {
                System.out.println("  Note: No customers in database yet");
            }
        } catch (Exception e) {
            System.out.println("✗ FAILED: " + e.getMessage());
            e.printStackTrace();
        }
        System.out.println();
    }
    
    // Test 3: Add Customer
    private static void testAddCustomer(CustomerService service) {
        System.out.println("TEST 3: Add New Customer");
        System.out.println("----------------------------");
        
        try {
            // Create test customer
            Customer testCustomer = new Customer();
            testCustomer.setFullName("Test Customer " + System.currentTimeMillis());
            testCustomer.setContact("555-" + String.format("%04d", System.currentTimeMillis() % 10000));
            testCustomer.setEmail("test" + System.currentTimeMillis() + "@example.com");
            testCustomer.setAddress("Test Address, City, State");
            testCustomer.setLoyaltyPoints(150);
            
            boolean result = service.addCustomer(testCustomer);
            
            if (result) {
                System.out.println("✓ SUCCESS: Customer added successfully");
                System.out.println("  Full Name: " + testCustomer.getFullName());
                System.out.println("  Contact: " + testCustomer.getContact());
                System.out.println("  Email: " + testCustomer.getEmail());
                System.out.println("  Address: " + testCustomer.getAddress());
                System.out.println("  Points: " + testCustomer.getLoyaltyPoints());
            } else {
                System.out.println("✗ FAILED: Could not add customer");
            }
        } catch (Exception e) {
            System.out.println("✗ ERROR: " + e.getMessage());
            e.printStackTrace();
        }
        System.out.println();
    }
    
    // Test 4: Search Customer
    private static void testSearchCustomer(CustomerService service) {
        System.out.println("TEST 4: Search Customers");
        System.out.println("----------------------------");
        
        try {
            // Search for "test" keyword
            String searchKeyword = "test";
            List<Customer> results = service.searchCustomers(searchKeyword);
            
            System.out.println("✓ SUCCESS: Search completed");
            System.out.println("  Keyword: '" + searchKeyword + "'");
            System.out.println("  Results found: " + results.size());
            
            if (!results.isEmpty()) {
                System.out.println("\nFirst result:");
                Customer first = results.get(0);
                System.out.println("  ID: " + first.getCustomerId());
                System.out.println("  Full Name: " + first.getFullName());
                System.out.println("  Contact: " + first.getContact());
                System.out.println("  Email: " + first.getEmail());
            } else {
                System.out.println("  No results found");
            }
        } catch (Exception e) {
            System.out.println("✗ ERROR: " + e.getMessage());
            e.printStackTrace();
        }
        System.out.println();
    }
    
    // Test 5: Update Customer
    private static void testUpdateCustomer(CustomerService service) {
        System.out.println("TEST 5: Update Customer");
        System.out.println("----------------------------");
        
        try {
            // Get first customer to update
            List<Customer> customers = service.getAllCustomers();
            
            if (!customers.isEmpty()) {
                Customer customer = customers.get(0);
                int originalPoints = customer.getLoyaltyPoints();
                
                // Update loyalty points
                customer.setLoyaltyPoints(originalPoints + 50);
                
                boolean result = service.updateCustomer(customer);
                
                if (result) {
                    System.out.println("✓ SUCCESS: Customer updated successfully");
                    System.out.println("  ID: " + customer.getCustomerId());
                    System.out.println("  Full Name: " + customer.getFullName());
                    System.out.println("  Contact: " + customer.getContact());
                    System.out.println("  Old Points: " + originalPoints);
                    System.out.println("  New Points: " + customer.getLoyaltyPoints());
                } else {
                    System.out.println("✗ FAILED: Could not update customer");
                }
            } else {
                System.out.println("⚠ SKIPPED: No customers available to update");
            }
        } catch (Exception e) {
            System.out.println("✗ ERROR: " + e.getMessage());
            e.printStackTrace();
        }
        System.out.println();
    }
    
    // Test 6: Delete Customer (commented out by default)
    private static void testDeleteCustomer(CustomerService service) {
        System.out.println("TEST 6: Delete Customer");
        System.out.println("----------------------------");
        
        try {
            // Search for test customers to delete
            List<Customer> testCustomers = service.searchCustomers("Test Customer");
            
            if (!testCustomers.isEmpty()) {
                Customer toDelete = testCustomers.get(0);
                int deleteId = toDelete.getCustomerId();
                
                boolean result = service.deleteCustomer(deleteId);
                
                if (result) {
                    System.out.println("✓ SUCCESS: Customer deleted successfully");
                    System.out.println("  Deleted ID: " + deleteId);
                    System.out.println("  Full Name: " + toDelete.getFullName());
                    System.out.println("  Contact: " + toDelete.getContact());
                } else {
                    System.out.println("✗ FAILED: Could not delete customer");
                }
            } else {
                System.out.println("⚠ SKIPPED: No test customers found to delete");
            }
        } catch (Exception e) {
            System.out.println("✗ ERROR: " + e.getMessage());
            e.printStackTrace();
        }
        System.out.println();
    }
}