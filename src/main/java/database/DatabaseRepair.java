package database;

import java.sql.Connection;
import java.sql.Statement;

public class DatabaseRepair {
    public static void main(String[] args) {
        try (Connection conn = DBConnection.getConnection();
                Statement stmt = conn.createStatement()) {

            if (conn == null)
                return;

            System.out.println("Forcing database to match schema.sql...");
            stmt.executeUpdate("SET FOREIGN_KEY_CHECKS = 0");

            String[] drops = { "Analytics", "Audit_Logs", "Cash_Logs", "Sale_Details", "Sales", "Products", "Customers",
                    "Users" };
            for (String t : drops) {
                stmt.execute("DROP TABLE IF EXISTS " + t);
                stmt.execute("DROP TABLE IF EXISTS " + t.toLowerCase());
            }

            System.out.println("Creating tables...");

            stmt.execute(
                    "CREATE TABLE Users (user_id INT AUTO_INCREMENT PRIMARY KEY, username VARCHAR(50) NOT NULL UNIQUE, password VARCHAR(255) NOT NULL, email VARCHAR(100), phone VARCHAR(20), status VARCHAR(20) DEFAULT 'Active', department VARCHAR(50), created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)");
            stmt.execute(
                    "INSERT INTO Users (username, password, status) VALUES ('admin', 'admin123', 'Active'), ('Demo', 'Demo1234', 'Active')");

            stmt.execute(
                    "CREATE TABLE Products (product_id INT AUTO_INCREMENT PRIMARY KEY, name VARCHAR(100) NOT NULL, category VARCHAR(50) NOT NULL, price DECIMAL(10, 2) NOT NULL, stock INT NOT NULL DEFAULT 0, description TEXT, supplier VARCHAR(100), created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)");
            stmt.execute("INSERT INTO Products (name, category, price, stock) VALUES " +
                    "('Laptop HP', 'Electronics', 45000, 10), ('Mouse Wireless', 'Electronics', 800, 50), ('USB-C Cable', 'Electronics', 350, 100), "
                    +
                    "('Blue Jeans', 'Clothing', 2500, 30), ('Cotton T-Shirt', 'Clothing', 800, 100), ('Running Shoes', 'Clothing', 3500, 25), "
                    +
                    "('Basmati Rice', 'Food', 120, 200), ('Wheat Flour', 'Food', 80, 150), ('Cooking Oil', 'Food', 250, 75), "
                    +
                    "('Orange Juice', 'Beverages', 150, 100), ('Cola Drink', 'Beverages', 100, 150), ('Coffee Powder', 'Beverages', 450, 50), "
                    +
                    "('Notebook A4', 'Stationery', 150, 500), ('Pen Pack', 'Stationery', 200, 300), ('Pencil Set', 'Stationery', 250, 250)");

            stmt.execute(
                    "CREATE TABLE Customers (customer_id INT AUTO_INCREMENT PRIMARY KEY, first_name VARCHAR(50) NOT NULL, last_name VARCHAR(50) NOT NULL, email VARCHAR(100), phone VARCHAR(20), address VARCHAR(200), loyalty_points INT DEFAULT 0, created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)");
            stmt.execute("INSERT INTO Customers (first_name, last_name, email, phone, address) VALUES " +
                    "('John', 'Doe', 'john@email.com', '1234567', 'Main St'), ('Jane', 'Smith', 'jane@email.com', '2345678', 'Oak Ave'), "
                    +
                    "('Mohammed', 'Ahmed', 'mohammed@email.com', '3456789', 'Palm Rd'), ('Sarah', 'Wilson', 'sarah@email.com', '4567890', 'Rose Ln'), "
                    +
                    "('Robert', 'Johnson', 'robert@email.com', '5678901', 'Lily St')");

            stmt.execute(
                    "CREATE TABLE Sales (sale_id INT AUTO_INCREMENT PRIMARY KEY, customer_id INT, user_id INT NOT NULL, sale_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP, total_amount DECIMAL(12, 2) NOT NULL, final_amount DECIMAL(12, 2), payment_method VARCHAR(20), FOREIGN KEY (customer_id) REFERENCES Customers(customer_id), FOREIGN KEY (user_id) REFERENCES Users(user_id))");

            stmt.execute(
                    "CREATE TABLE Sale_Details (sale_detail_id INT AUTO_INCREMENT PRIMARY KEY, sale_id INT NOT NULL, product_id INT NOT NULL, quantity INT NOT NULL, unit_price DECIMAL(10, 2) NOT NULL, total_price DECIMAL(12, 2) NOT NULL, FOREIGN KEY (sale_id) REFERENCES Sales(sale_id), FOREIGN KEY (product_id) REFERENCES Products(product_id))");

            System.out.println("Generating 30 sales for Analytics...");
            java.util.Random rand = new java.util.Random();
            for (int i = 0; i < 30; i++) {
                int cid = rand.nextInt(5) + 1;
                double amt = 1000 + rand.nextDouble() * 5000;
                String date = "DATE_SUB(NOW(), INTERVAL " + rand.nextInt(15) + " DAY)";
                stmt.executeUpdate(
                        "INSERT INTO Sales (customer_id, user_id, sale_date, total_amount, final_amount, payment_method) VALUES ("
                                + cid + ", 1, " + date + ", " + amt + ", " + amt + ", 'Cash')");

                int sid = 0;
                try (var rs = stmt.executeQuery("SELECT LAST_INSERT_ID()")) {
                    if (rs.next())
                        sid = rs.getInt(1);
                }
                int p1 = rand.nextInt(15) + 1;
                stmt.executeUpdate(
                        "INSERT INTO Sale_Details (sale_id, product_id, quantity, unit_price, total_price) VALUES ("
                                + sid + ", " + p1 + ", 1, 500, 500)");
            }

            stmt.executeUpdate("SET FOREIGN_KEY_CHECKS = 1");
            System.out.println("Database repaired successfully! All 15 products and 5 customers present.");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
