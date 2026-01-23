package database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {

    // === DATABASE CONFIGURATION ===
    private static final String URL = "jdbc:mysql://localhost:3306/smart_retail";
    private static final String USER = "root";
    private static final String PASSWORD = "";  // CHANGED: Was "root@12345678" → Now "" (your actual password)
    
    private static DBConnection instance;

    private DBConnection() {
    }

    public static DBConnection getInstance() {
        if (instance == null) {
            instance = new DBConnection();
        }
        return instance;
    }

    // === METHOD TO GET CONNECTION ===
    public static Connection getConnection() {
        try {
            // Load the MySQL JDBC driver
            Class.forName("com.mysql.cj.jdbc.Driver");
            
            // Try to connect
            Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
            System.out.println("✓ Connection successful!");
            return conn;
            
        } catch (ClassNotFoundException e) {
            System.err.println("✗ ERROR: MySQL JDBC Driver not found!");
            e.printStackTrace();
            return null;
            
        } catch (SQLException e) {
            System.err.println("✗ ERROR: Could not connect to the database!");
            e.printStackTrace();
            return null;
        }
    }

    // === MAIN METHOD TO TEST CONNECTION ===
    public static void main(String[] args) {
        Connection conn = getConnection();
        
        if (conn != null) {
            System.out.println("✓ Database is reachable!");
            try {
                conn.close();
                System.out.println("✓ Connection closed successfully");
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("✗ Cannot connect to the database.");
        }
    }
}