package models;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;

public class Product {

    private int productId;
    private String name;
    private String category;
    private double price;
    private int stock;
    private Timestamp createdAt;
    private Timestamp updatedAt;

    public Product() {
        // Empty constructor for flexibility
    }

    public Product(String name, String category, double price, int stock) {
        this.name = name;
        this.category = category;
        this.price = price;
        this.stock = stock;
    }

    public Product(int productId, String name, String category, double price, int stock) {
        this.productId = productId;
        this.name = name;
        this.category = category;
        this.price = price;
        this.stock = stock;
    }

    public Product(int productId, String name, String category, double price, 
                   int stock, Timestamp createdAt, Timestamp updatedAt) {
        this.productId = productId;
        this.name = name;
        this.category = category;
        this.price = price;
        this.stock = stock;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public int getProductId() {
        return productId;
    }

    public String getName() {
        return name;
    }

    public String getCategory() {
        return category;
    }

    public double getPrice() {
        return price;
    }

    public int getStock() {
        return stock;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public Timestamp getUpdatedAt() {
        return updatedAt;
    }
    

    public void setProductId(int productId) {
        this.productId = productId;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public void setPrice(double price) {
        // Validation: price must be positive
        if (price < 0) {
            throw new IllegalArgumentException("Price cannot be negative");
        }
        this.price = price;
    }

    public void setStock(int stock) {
        // Validation: stock cannot be negative
        if (stock < 0) {
            throw new IllegalArgumentException("Stock cannot be negative");
        }
        this.stock = stock;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public void setUpdatedAt(Timestamp updatedAt) {
        this.updatedAt = updatedAt;
    }

    public boolean isInStock() {
        return stock > 0;
    }

    public boolean isLowStock(int threshold) {
        return stock < threshold;
    }

    public void increaseStock(int quantity) {
        if (quantity < 0) {
            throw new IllegalArgumentException("Quantity cannot be negative");
        }
        this.stock += quantity;
    }

    public boolean decreaseStock(int quantity) {
        if (quantity < 0) {
            throw new IllegalArgumentException("Quantity cannot be negative");
        }
        if (quantity > stock) {
            return false; // Insufficient stock
        }
        this.stock -= quantity;
        return true;
    }

    public double calculateStockValue() {
        return price * stock;
    }

    public Map<String, Object> toJSON() {
        Map<String, Object> json = new HashMap<>();

        json.put("productId", productId);
        json.put("name", name);
        json.put("category", category);
        json.put("price", price);
        json.put("stock", stock);

        // Include timestamps if available
        if (createdAt != null) {
            json.put("createdAt", createdAt.toString());
        }
        if (updatedAt != null) {
            json.put("updatedAt", updatedAt.toString());
        }

        return json;
    }

    public static Product fromJSON(Map<String, Object> json) {
        Product product = new Product();

        // Set basic fields
        if (json.containsKey("productId")) {
            Object v = json.get("productId");
            if (v instanceof Number num) product.setProductId(num.intValue());
            else if (v != null) product.setProductId(Integer.parseInt(v.toString()));
        }
        if (json.containsKey("name")) {
            Object v = json.get("name");
            if (v != null) product.setName(v.toString());
        }
        if (json.containsKey("category")) {
            Object v = json.get("category");
            if (v != null) product.setCategory(v.toString());
        }
        if (json.containsKey("price")) {
            Object v = json.get("price");
            if (v instanceof Number num) product.setPrice(num.doubleValue());
            else if (v != null) product.setPrice(Double.parseDouble(v.toString()));
        }
        if (json.containsKey("stock")) {
            Object v = json.get("stock");
            if (v instanceof Number num) product.setStock(num.intValue());
            else if (v != null) product.setStock(Integer.parseInt(v.toString()));
        }

        // Parse timestamps if available
        if (json.containsKey("createdAt")) {
            Object v = json.get("createdAt");
            if (v != null) {
                try {
                    product.setCreatedAt(Timestamp.valueOf(v.toString()));
                } catch (Exception e) {
                    // If parsing fails, leave as null
                }
            }
        }
        if (json.containsKey("updatedAt")) {
            Object v = json.get("updatedAt");
            if (v != null) {
                try {
                    product.setUpdatedAt(Timestamp.valueOf(v.toString()));
                } catch (Exception e) {
                    // If parsing fails, leave as null
                }
            }
        }

        return product;
    }

    @Override
    public String toString() {
        return "Product{" +
                "productId=" + productId +
                ", name='" + name + '\'' +
                ", category='" + category + '\'' +
                ", price=" + price +
                ", stock=" + stock +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Product product = (Product) obj;
        return productId == product.productId;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(productId);
    }

    public boolean isValid() {
        // Check name is not empty
        if (name == null || name.trim().isEmpty()) {
            return false;
        }
        
        // Check category is not empty
        if (category == null || category.trim().isEmpty()) {
            return false;
        }
        
        // Check price is positive
        if (price <= 0) {
            return false;
        }
        
        return true;
    }
}