package database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {

    // === DATABASE CONFIGURATION ===
    // === DATABASE CONFIGURATION ===
    private static String URL;
    private static String USER;
    private static String PASSWORD;

    static {
        loadConfig();
    }

    private static void loadConfig() {
        try (java.io.InputStream input = DBConnection.class.getClassLoader().getResourceAsStream("config.properties")) {
            java.util.Properties prop = new java.util.Properties();
            if (input == null) {
                System.err.println("Sorry, unable to find config.properties");
                // Fallback to defaults if config is missing to avoid NPE
                URL = "jdbc:mysql://localhost:3306/smart_retail";
                USER = "root";
                PASSWORD = "";
                return;
            }
            prop.load(input);
            URL = prop.getProperty("db.url");
            USER = prop.getProperty("db.user");
            PASSWORD = prop.getProperty("db.password");
        } catch (java.io.IOException ex) {
            ex.printStackTrace();
        }
    }

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
            Class.forName("com.mysql.cj.jdbc.Driver");

            // Supporting multiple passwords (OR operator style) as requested
            // Splitting by || or ,
            String[] passwords = PASSWORD.split("\\|\\||,");

            for (String pwd : passwords) {
                try {
                    Connection conn = DriverManager.getConnection(URL, USER, pwd.trim());
                    System.out.println("â?? Connection successful!");
                    return conn;
                } catch (SQLException e) {
                    // Try next password...
                }
            }

            System.err.println("â?? ERROR: Could not connect to the database with any provided password!");
            return null;

        } catch (ClassNotFoundException e) {
            System.err.println("â?? ERROR: MySQL JDBC Driver not found!");
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