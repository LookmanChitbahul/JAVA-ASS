package services;

import java.sql.*;
import java.util.ArrayList;
import models.Product;

/**
 * ProductService - Service layer for managing product operations
 * Handles all business logic related to products including CRUD operations,
 * searching, filtering, and inventory management.
 */
public class ProductService {
    
    // Database connection object for executing queries
    private Connection connection;
    
    // Current user ID for audit logging purposes (tracks who made changes)
    private int currentUserId = -1;
   
    /**
     * Default constructor - initializes ProductService with database connection
     * Retrieves connection from DBConnection singleton
     */
    public ProductService() {
        try {
            this.connection = database.DBConnection.getInstance().getConnection();
        } catch (Exception e) {
            System.err.println("Error getting database connection: " + e.getMessage());
            this.connection = null;
        }
    }

    /**
     * Constructor with user ID for audit logging
     * @param userId The ID of the current user performing operations
     */
    public ProductService(int userId) {
        this();
        this.currentUserId = userId;
    }
  
    /**
     * Adds a new product to the database
     * @param product The product object containing product details
     * @return true if product was added successfully, false otherwise
     */
    public boolean addProduct(Product product) {
        // Validate product data before insertion
        if (!product.isValid()) {
            System.err.println("Invalid product data");
            return false;
        }
        
        // Check if database connection is available
        if (connection == null) {
            System.err.println("Database connection not available");
            return false;
        }
        
        // SQL query to insert new product
        String sql = "INSERT INTO Products (name, category, price, stock) VALUES (?, ?, ?, ?)";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql, 
                Statement.RETURN_GENERATED_KEYS)) {
            
            // Set parameters for prepared statement
            pstmt.setString(1, product.getName());
            pstmt.setString(2, product.getCategory());
            pstmt.setDouble(3, product.getPrice());
            pstmt.setInt(4, product.getStock());
            
            // Execute the insert
            int affectedRows = pstmt.executeUpdate();
            
            if (affectedRows > 0) {
                // Get the generated product ID
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        int productId = generatedKeys.getInt(1);
                        product.setProductId(productId);
                        
                        // Log the action to audit log
                        logAuditAction("ADD_PRODUCT", 
                            "Added product: " + product.getName() + " (ID: " + productId + ")");
                    }
                }
                
                System.out.println("Product added successfully: " + product.getName());
                return true;
            }
            
        } catch (SQLException e) {
            System.err.println("Error adding product: " + e.getMessage());
        }
        
        return false;
    }

    /**
     * Retrieves all products from the database
     * @return ArrayList of all Product objects, or empty list if no products exist
     */
    public ArrayList<Product> getAllProducts() {
        // Check if database connection is available
        if (connection == null) {
            System.err.println("Database connection not available");
            return new ArrayList<>();
        }
        
        // Initialize list to store products
        ArrayList<Product> products = new ArrayList<>();
        
        // SQL query to select all products
        String sql = "SELECT product_id, name, category, price, stock, " +
                     "created_at, updated_at FROM Products ORDER BY name";
        
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            // Iterate through result set and create Product objects
            while (rs.next()) {
                Product product = new Product(
                    rs.getInt("product_id"),
                    rs.getString("name"),
                    rs.getString("category"),
                    rs.getDouble("price"),
                    rs.getInt("stock"),
                    rs.getTimestamp("created_at"),
                    rs.getTimestamp("updated_at")
                );
                products.add(product);
            }
            
            System.out.println("Retrieved " + products.size() + " products from database");
            
        } catch (SQLException e) {
            System.err.println("Error retrieving products: " + e.getMessage());
        }
        
        return products;
    }
    
    /**
     * Retrieves a specific product by its ID
     * @param productId The unique identifier of the product to retrieve
     * @return Product object if found, null otherwise
     */
    public Product getProductById(int productId) {
        // Check if database connection is available
        if (connection == null) {
            System.err.println("Database connection not available");
            return null;
        }
        
        // SQL query to select product by ID
        String sql = "SELECT product_id, name, category, price, stock, " +
                     "created_at, updated_at FROM Products WHERE product_id = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            
            pstmt.setInt(1, productId);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    // Create and return Product object
                    return new Product(
                        rs.getInt("product_id"),
                        rs.getString("name"),
                        rs.getString("category"),
                        rs.getDouble("price"),
                        rs.getInt("stock"),
                        rs.getTimestamp("created_at"),
                        rs.getTimestamp("updated_at")
                    );
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Error retrieving product: " + e.getMessage());
        }
        
        return null; // Product not found
    }
    
    /**
     * Searches for products by name or category using fuzzy matching
     * @param searchTerm The term to search for in product names or categories
     * @return ArrayList of Product objects matching the search criteria
     */
    public ArrayList<Product> searchProducts(String searchTerm) {
        // Check if database connection is available
        if (connection == null) {
            System.err.println("Database connection not available");
            return new ArrayList<>();
        }
        
        // Initialize list to store search results
        ArrayList<Product> products = new ArrayList<>();
        
        // SQL query with LIKE operator for fuzzy search
        String sql = "SELECT product_id, name, category, price, stock, " +
                     "created_at, updated_at FROM Products " +
                     "WHERE name LIKE ? OR category LIKE ? " +
                     "ORDER BY name";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            
            String searchPattern = "%" + searchTerm + "%";
            pstmt.setString(1, searchPattern);
            pstmt.setString(2, searchPattern);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Product product = new Product(
                        rs.getInt("product_id"),
                        rs.getString("name"),
                        rs.getString("category"),
                        rs.getDouble("price"),
                        rs.getInt("stock"),
                        rs.getTimestamp("created_at"),
                        rs.getTimestamp("updated_at")
                    );
                    products.add(product);
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Error searching products: " + e.getMessage());
        }
        
        return products;
    }

    /**
     * Retrieves all products belonging to a specific category
     * @param category The category name to filter products by
     * @return ArrayList of Product objects in the specified category
     */
    public ArrayList<Product> getProductsByCategory(String category) {
        // Check if database connection is available
        if (connection == null) {
            System.err.println("Database connection not available");
            return new ArrayList<>();
        }
        
        // Initialize list to store products from the category
        ArrayList<Product> products = new ArrayList<>();
        
        String sql = "SELECT product_id, name, category, price, stock, " +
                     "created_at, updated_at FROM Products " +
                     "WHERE category = ? ORDER BY name";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            
            pstmt.setString(1, category);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Product product = new Product(
                        rs.getInt("product_id"),
                        rs.getString("name"),
                        rs.getString("category"),
                        rs.getDouble("price"),
                        rs.getInt("stock"),
                        rs.getTimestamp("created_at"),
                        rs.getTimestamp("updated_at")
                    );
                    products.add(product);
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Error retrieving products by category: " + e.getMessage());
        }
        
        return products;
    }

    /**
     * Retrieves products with stock below a specified threshold
     * Useful for inventory alerts and restocking notifications
     * @param threshold The stock level threshold (products with stock < threshold are returned)
     * @return ArrayList of Product objects with low stock, sorted by stock quantity
     */
    public ArrayList<Product> getLowStockProducts(int threshold) {
        // Check if database connection is available
        if (connection == null) {
            System.err.println("Database connection not available");
            return new ArrayList<>();
        }
        
        // Initialize list to store low stock products
        ArrayList<Product> products = new ArrayList<>();
        
        String sql = "SELECT product_id, name, category, price, stock, " +
                     "created_at, updated_at FROM Products " +
                     "WHERE stock < ? ORDER BY stock ASC";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            
            pstmt.setInt(1, threshold);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Product product = new Product(
                        rs.getInt("product_id"),
                        rs.getString("name"),
                        rs.getString("category"),
                        rs.getDouble("price"),
                        rs.getInt("stock"),
                        rs.getTimestamp("created_at"),
                        rs.getTimestamp("updated_at")
                    );
                    products.add(product);
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Error retrieving low stock products: " + e.getMessage());
        }
        
        return products;
    }

    /**
     * Updates an existing product in the database
     * @param product The product object with updated details
     * @return true if update was successful, false otherwise
     */
    public boolean updateProduct(Product product) {
        // Validate product data before updating
        if (!product.isValid()) {
            System.err.println("Invalid product data");
            return false;
        }
        
        // Check if database connection is available
        if (connection == null) {
            System.err.println("Database connection not available");
            return false;
        }
        
        // SQL query to update product
        String sql = "UPDATE Products SET name = ?, category = ?, price = ?, " +
                     "stock = ? WHERE product_id = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            
            // Set parameters
            pstmt.setString(1, product.getName());
            pstmt.setString(2, product.getCategory());
            pstmt.setDouble(3, product.getPrice());
            pstmt.setInt(4, product.getStock());
            pstmt.setInt(5, product.getProductId());
            
            // Execute update
            int affectedRows = pstmt.executeUpdate();
            
            if (affectedRows > 0) {
                // Log the action
                logAuditAction("UPDATE_PRODUCT", 
                    "Updated product: " + product.getName() + " (ID: " + product.getProductId() + ")");
                
                System.out.println("Product updated successfully: " + product.getName());
                return true;
            } else {
                System.err.println("Product not found with ID: " + product.getProductId());
            }
            
        } catch (SQLException e) {
            System.err.println("Error updating product: " + e.getMessage());
        }
        
        return false;
    }

    /**
     * Updates the stock quantity for a specific product
     * @param productId The ID of the product to update
     * @param newStock The new stock quantity
     * @return true if update was successful, false otherwise
     */
    public boolean updateStock(int productId, int newStock) {
        // Validate that stock is not negative
        if (newStock < 0) {
            System.err.println("Stock cannot be negative");
            return false;
        }
        
        // Check if database connection is available
        if (connection == null) {
            System.err.println("Database connection not available");
            return false;
        }
        
        String sql = "UPDATE Products SET stock = ? WHERE product_id = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            
            pstmt.setInt(1, newStock);
            pstmt.setInt(2, productId);
            
            int affectedRows = pstmt.executeUpdate();
            
            if (affectedRows > 0) {
                logAuditAction("UPDATE_STOCK", 
                    "Updated stock for product ID " + productId + " to " + newStock);
                return true;
            }
            
        } catch (SQLException e) {
            System.err.println("Error updating stock: " + e.getMessage());
        }
        
        return false;
    }

    /**
     * Decreases stock quantity for a product when items are sold
     * Verifies sufficient stock exists before reducing quantity
     * @param productId The ID of the product to decrease stock for
     * @param quantity The amount to decrease from stock
     * @return true if stock was decreased successfully, false if insufficient stock
     */
    public boolean decreaseStock(int productId, int quantity) {
        // First check if sufficient stock is available
        Product product = getProductById(productId);
        if (product == null) {
            System.err.println("Product not found");
            return false;
        }
        
        if (product.getStock() < quantity) {
            System.err.println("Insufficient stock. Available: " + product.getStock() + 
                             ", Required: " + quantity);
            return false;
        }
        
        // Update the stock
        int newStock = product.getStock() - quantity;
        return updateStock(productId, newStock);
    }

    /**
     * Deletes a product from the database
     * Note: Product deletion may fail if it is referenced in sales records
     * @param productId The ID of the product to delete
     * @return true if deletion was successful, false otherwise
     */
    public boolean deleteProduct(int productId) {
        // Check if database connection is available
        if (connection == null) {
            System.err.println("Database connection not available");
            return false;
        }
        
        // First get product details for audit logging
        Product product = getProductById(productId);
        if (product == null) {
            System.err.println("Product not found with ID: " + productId);
            return false;
        }
        
        // SQL query to delete product
        String sql = "DELETE FROM Products WHERE product_id = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            
            pstmt.setInt(1, productId);
            
            int affectedRows = pstmt.executeUpdate();
            
            if (affectedRows > 0) {
                // Log the deletion
                logAuditAction("DELETE_PRODUCT", 
                    "Deleted product: " + product.getName() + " (ID: " + productId + ")");
                
                System.out.println("Product deleted successfully: " + product.getName());
                return true;
            }
            
        } catch (SQLException e) {
            // Check if deletion failed due to foreign key constraint
            if (e.getMessage().contains("foreign key constraint")) {
                System.err.println("Cannot delete product: It is referenced in sales records");
            } else {
                System.err.println("Error deleting product: " + e.getMessage());
            }
        }
        
        return false;
    }

    /**
     * Gets the total count of products in the database
     * @return The total number of products, or 0 if error occurs
     */
    public int getTotalProductCount() {
        // Check if database connection is available
        if (connection == null) {
            System.err.println("Database connection not available");
            return 0;
        }
        
        String sql = "SELECT COUNT(*) as count FROM Products";
        
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            if (rs.next()) {
                return rs.getInt("count");
            }
            
        } catch (SQLException e) {
            System.err.println("Error getting product count: " + e.getMessage());
        }
        
        return 0;
    }

    /**
     * Calculates the total monetary value of all inventory (price × stock for all products)
     * Useful for financial reporting and inventory valuation
     * @return The total value of stock in the inventory
     */
    public double getTotalStockValue() {
        // Check if database connection is available
        if (connection == null) {
            System.err.println("Database connection not available");
            return 0.0;
        }
        
        String sql = "SELECT SUM(price * stock) as total_value FROM Products";
        
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            if (rs.next()) {
                return rs.getDouble("total_value");
            }
            
        } catch (SQLException e) {
            System.err.println("Error calculating stock value: " + e.getMessage());
        }
        
        return 0.0;
    }

    /**
     * Retrieves all unique product categories from the database
     * Sorted alphabetically for display in dropdowns and filters
     * @return ArrayList of category names
     */
    public ArrayList<String> getAllCategories() {
        // Check if database connection is available
        if (connection == null) {
            System.err.println("Database connection not available");
            return new ArrayList<>();
        }
        
        // Initialize list to store unique categories
        ArrayList<String> categories = new ArrayList<>();
        
        String sql = "SELECT DISTINCT category FROM Products ORDER BY category";
        
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                categories.add(rs.getString("category"));
            }
            
        } catch (SQLException e) {
            System.err.println("Error retrieving categories: " + e.getMessage());
        }
        
        return categories;
    }

    /**
     * Logs product-related actions to audit trail for tracking and compliance
     * Records who performed what action and when
     * @param action The type of action performed (e.g., ADD_PRODUCT, UPDATE_PRODUCT)
     * @param details Description of the action performed
     */
    private void logAuditAction(String action, String details) {
        // Only log if we have a valid user ID for accountability
        if (currentUserId <= 0) {
            return;
        }
        
        String sql = "INSERT INTO Audit_Logs (user_id, action, details) VALUES (?, ?, ?)";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            
            pstmt.setInt(1, currentUserId);
            pstmt.setString(2, action);
            pstmt.setString(3, details);
            
            pstmt.executeUpdate();
            
        } catch (SQLException e) {
            System.err.println("Error logging audit action: " + e.getMessage());
        }
    }
    
    /**
     * Checks if a product with the given name already exists in the database
     * Useful for preventing duplicate product names
     * @param name The product name to check
     * @return true if product exists, false otherwise
     */
    public boolean productExists(String name) {
        // Check if database connection is available
        if (connection == null) {
            System.err.println("Database connection not available");
            return false;
        }
        
        String sql = "SELECT COUNT(*) as count FROM Products WHERE name = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            
            pstmt.setString(1, name);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("count") > 0;
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Error checking product existence: " + e.getMessage());
        }
        
        return false;
    }

    /**
     * Sets the current user ID for audit logging
     * @param userId The ID of the current user performing operations
     */
    public void setCurrentUserId(int userId) {
        this.currentUserId = userId;
    }

    /**
     * Closes the database connection when done
     * Should be called when the service is no longer needed
     */
    public void closeConnection() {
        // Connection cleanup can be implemented here
    }
}