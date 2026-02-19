package database;

import java.sql.*;
import java.util.Random;

public class DatabaseInitializer {
    public static void main(String[] args) {
        try (Connection conn = DBConnection.getConnection();
                Statement stmt = conn.createStatement()) {

            if (conn == null)
                return;

            System.out.println("Initializing Database with consistent data...");

            // 1. Sync Products (schema.sql names match)
            stmt.executeUpdate("INSERT IGNORE INTO products (product_id, name, category, price, stock) VALUES " +
                    "(1, 'Laptop HP', 'Electronics', 45000.00, 10), " +
                    "(2, 'Mouse Wireless', 'Electronics', 800.00, 50), " +
                    "(3, 'USB-C Cable', 'Electronics', 350.00, 100), " +
                    "(4, 'Blue Jeans', 'Clothing', 2500.00, 30), " +
                    "(5, 'Cotton T-Shirt', 'Clothing', 800.00, 100), " +
                    "(6, 'Basmati Rice', 'Food', 120.00, 200), " +
                    "(7, 'Orange Juice', 'Beverages', 150.00, 100), " +
                    "(8, 'Notebook A4', 'Stationery', 150.00, 500)");

            // 2. Sync Customers (handle name vs first_name/last_name)
            boolean hasNameCol = false;
            try (ResultSet rs = conn.getMetaData().getColumns(null, null, "customers", "name")) {
                if (rs.next())
                    hasNameCol = true;
            }

            if (hasNameCol) {
                stmt.executeUpdate("INSERT IGNORE INTO customers (customer_id, name, email) VALUES " +
                        "(1, 'John Doe', 'john@example.com'), " +
                        "(2, 'Jane Smith', 'jane@example.com'), " +
                        "(3, 'Mohammed Ahmed', 'mohammed@example.com'), " +
                        "(4, 'Sarah Wilson', 'sarah@example.com'), " +
                        "(5, 'Robert Johnson', 'robert@example.com')");
            } else {
                stmt.executeUpdate("INSERT IGNORE INTO customers (customer_id, first_name, last_name, email) VALUES " +
                        "(1, 'John', 'Doe', 'john@example.com'), " +
                        "(2, 'Jane', 'Smith', 'jane@example.com'), " +
                        "(3, 'Mohammed', 'Ahmed', 'mohammed@example.com'), " +
                        "(4, 'Sarah', 'Wilson', 'sarah@example.com'), " +
                        "(5, 'Robert', 'Johnson', 'robert@example.com')");
            }

            // 3. Clear and regenerate Sales for consistency
            System.out.println("Cleaning old sales for a fresh start...");
            stmt.executeUpdate("SET FOREIGN_KEY_CHECKS = 0");
            stmt.executeUpdate("TRUNCATE TABLE sale_details");
            stmt.executeUpdate("TRUNCATE TABLE sales");
            stmt.executeUpdate("SET FOREIGN_KEY_CHECKS = 1");

            System.out.println("Generating 25 fresh sales for Analytics...");
            Random rand = new Random();
            for (int i = 0; i < 25; i++) {
                int customerId = rand.nextInt(5) + 1;
                double amount = 1000 + rand.nextDouble() * 10000;
                String date = "DATE_SUB(NOW(), INTERVAL " + rand.nextInt(14) + " DAY)";

                stmt.executeUpdate("INSERT INTO sales (customer_id, sale_date, total_amount, payment_method) " +
                        "VALUES (" + customerId + ", " + date + ", " + amount + ", 'Card')");

                int saleId = 0;
                try (ResultSet rs = stmt.executeQuery("SELECT LAST_INSERT_ID()")) {
                    if (rs.next())
                        saleId = rs.getInt(1);
                }

                if (saleId > 0) {
                    // 1-3 items per sale
                    int items = rand.nextInt(3) + 1;
                    for (int j = 0; j < items; j++) {
                        int productId = rand.nextInt(8) + 1;
                        int qty = rand.nextInt(3) + 1;
                        stmt.executeUpdate(
                                "INSERT INTO sale_details (sale_id, product_id, quantity, unit_price, subtotal) " +
                                        "VALUES (" + saleId + ", " + productId + ", " + qty + ", 500, " + (qty * 500)
                                        + ")");
                    }
                }
            }

            System.out.println("Database successfully initialized and synced!");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
