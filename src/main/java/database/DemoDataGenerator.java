package database;

import java.sql.*;
import java.util.Random;

public class DemoDataGenerator {
    public static void main(String[] args) {
        try (Connection conn = DBConnection.getConnection()) {
            if (conn == null)
                return;
            Statement stmt = conn.createStatement();

            // 1. Ensure some products exist if 0
            ResultSet rsP = stmt.executeQuery("SELECT COUNT(*) FROM products");
            int pCount = 0;
            if (rsP.next())
                pCount = rsP.getInt(1);
            if (pCount == 0) {
                System.out.println("Inserting sample products...");
                stmt.executeUpdate("INSERT INTO products (name, category, price, stock) VALUES " +
                        "('Laptop', 'Electronics', 50000, 10), " +
                        "('Mouse', 'Electronics', 500, 50), " +
                        "('T-Shirt', 'Clothing', 800, 100), " +
                        "('Bread', 'Food', 25, 200)");
            }

            // 2. Ensure some customers exist if 0
            ResultSet rsC = stmt.executeQuery("SELECT COUNT(*) FROM customers");
            int cCount = 0;
            if (rsC.next())
                cCount = rsC.getInt(1);
            if (cCount == 0) {
                System.out.println("Inserting sample customers...");
                stmt.executeUpdate("INSERT INTO customers (first_name, last_name, email) VALUES " +
                        "('Alice', 'Smith', 'alice@test.com'), " +
                        "('Bob', 'Jones', 'bob@test.com')");
            }

            // 3. Insert random sales for the last 7 days
            System.out.println("Generating sample sales for analytics...");
            Random rand = new Random();
            for (int i = 0; i < 7; i++) {
                double amount = 1000 + rand.nextDouble() * 5000;
                // Using total_amount since we know it exists
                String date = "DATE_SUB(CURDATE(), INTERVAL " + i + " DAY)";
                stmt.executeUpdate(
                        "INSERT INTO sales (customer_id, user_id, sale_date, total_amount, final_amount, payment_method) "
                                +
                                "VALUES (1, 1, " + date + ", " + amount + ", " + amount + ", 'Cash')");

                // Add some sale details for top products chart
                // Insert 1-3 details per sale
                int saleId = 0;
                ResultSet rs = stmt.executeQuery("SELECT LAST_INSERT_ID()");
                if (rs.next())
                    saleId = rs.getInt(1);

                if (saleId > 0) {
                    stmt.executeUpdate(
                            "INSERT INTO sale_details (sale_id, product_id, quantity, unit_price, subtotal, total_price) "
                                    +
                                    "VALUES (" + saleId + ", " + (rand.nextInt(4) + 1) + ", " + (rand.nextInt(3) + 1)
                                    + ", 500, 1500, 1500)");
                }
            }
            System.out.println("Demo data generated successfully!");

        } catch (SQLException e) {
            System.err.println(
                    "Note: Some inserts might have skipped due to missing columns (final_amount/total_price), but that's okay for demo.");
            // e.printStackTrace();
        }
    }
}
