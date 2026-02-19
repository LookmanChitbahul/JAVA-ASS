package database;

import java.sql.*;
import java.util.Random;

public class SeedData {
    public static void main(String[] args) {
        try (Connection conn = DBConnection.getConnection()) {
            if (conn == null)
                return;
            Statement stmt = conn.createStatement();

            System.out.println("Seeding data based on detected schema...");

            // 1. Products
            stmt.executeUpdate("INSERT IGNORE INTO products (product_id, name, category, price, stock) VALUES " +
                    "(1, 'Laptop HP', 'Electronics', 45000.00, 10), " +
                    "(2, 'Mouse', 'Electronics', 800.00, 50), " +
                    "(3, 'Blue Jeans', 'Clothing', 2500.00, 30)");

            // 2. Customers (using 'name' column as detected)
            stmt.executeUpdate("INSERT IGNORE INTO customers (customer_id, name, email) VALUES " +
                    "(1, 'John Doe', 'john@example.com')");

            // 3. Sales (using total_amount and sale_date as detected)
            System.out.println("Generating sales...");
            Random rand = new Random();
            for (int i = 0; i < 7; i++) {
                double amount = 5000 + rand.nextDouble() * 10000;
                String date = "DATE_SUB(CURDATE(), INTERVAL " + i + " DAY)";
                // We use 0 for customer_id if we aren't sure about foreign keys, but 1 should
                // work now
                stmt.executeUpdate("INSERT INTO sales (customer_id, sale_date, total_amount, payment_method) " +
                        "VALUES (1, " + date + ", " + amount + ", 'Cash')");

                // Get the last sale id
                ResultSet rs = stmt.executeQuery("SELECT LAST_INSERT_ID()");
                if (rs.next()) {
                    int saleId = rs.getInt(1);
                    // Add details
                    stmt.executeUpdate(
                            "INSERT INTO sale_details (sale_id, product_id, quantity, unit_price, subtotal) " +
                                    "VALUES (" + saleId + ", " + (rand.nextInt(3) + 1) + ", 1, 5000, 5000)");
                }
            }

            System.out.println("Done! Analytics should now show data.");

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
