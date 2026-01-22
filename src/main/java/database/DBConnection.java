package database;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class DBConnection {

    private static volatile DBConnection instance;
    private Connection connection;
    private Properties properties;
    
    // Database connection parameters (defaults)
    private static final String DEFAULT_DB_URL = "jdbc:mysql://localhost:3306/smart_retail_db";
    private static final String DEFAULT_DB_USER = "root";
    private static final String DEFAULT_DB_PASSWORD = "";
    private static final String DEFAULT_DB_DRIVER = "com.mysql.cj.jdbc.Driver";

    private DBConnection() throws SQLException {
        try {
            // Load configuration from properties file
            loadConfiguration();
            
            // Get connection parameters
            String dbUrl = properties.getProperty("db.url", DEFAULT_DB_URL);
            String dbUser = properties.getProperty("db.user", DEFAULT_DB_USER);
            String dbPassword = properties.getProperty("db.password", DEFAULT_DB_PASSWORD);
            String dbDriver = properties.getProperty("db.driver", DEFAULT_DB_DRIVER);
            
            // Load MySQL JDBC driver
            Class.forName(dbDriver);
            
            // Establish connection with connection timeout
            this.connection = DriverManager.getConnection(dbUrl, dbUser, dbPassword);
            
            System.out.println("✓ Database connection established successfully!");
            System.out.println("  Connected to: " + dbUrl);
            System.out.println("  User: " + dbUser);
            
        } catch (ClassNotFoundException e) {
            System.err.println("✗ MySQL JDBC Driver not found!");
            System.err.println("  Make sure mysql-connector-java.jar is in your classpath");
            throw new SQLException("Driver not found: " + e.getMessage());
            
        } catch (SQLException e) {
            System.err.println("✗ Failed to establish database connection!");
            System.err.println("  Error: " + e.getMessage());
            System.err.println("  Please check:");
            System.err.println("    1. MySQL server is running");
            System.err.println("    2. Database '" + properties.getProperty("db.url") + "' exists");
            System.err.println("    3. Username and password are correct");
            System.err.println("    4. Connection URL is correct");
            throw e;
        }
    }

    public static DBConnection getInstance() throws SQLException {
        // First check without synchronization for performance
        if (instance == null) {
            // Synchronize only when creating new instance
            synchronized (DBConnection.class) {
                // Double-check inside synchronized block
                if (instance == null) {
                    instance = new DBConnection();
                }
            }
        }
        return instance;
    }

    public Connection getConnection() throws SQLException {
        // Check if connection is still valid
        if (connection == null || connection.isClosed()) {
            System.out.println("⚠ Connection was closed. Reconnecting...");
            reconnect();
        }
        return connection;
    }

    public boolean testConnection() {
        try {
            // Use isValid() method to test connection (timeout: 2 seconds)
            return connection != null && connection.isValid(2);
        } catch (SQLException e) {
            System.err.println("Connection test failed: " + e.getMessage());
            return false;
        }
    }

    public void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                System.out.println("✓ Database connection closed successfully");
            }
        } catch (SQLException e) {
            System.err.println("✗ Error closing connection: " + e.getMessage());
        }
    }

    private void reconnect() throws SQLException {
        try {
            // Close existing connection if any
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
            
            // Get connection parameters
            String dbUrl = properties.getProperty("db.url", DEFAULT_DB_URL);
            String dbUser = properties.getProperty("db.user", DEFAULT_DB_USER);
            String dbPassword = properties.getProperty("db.password", DEFAULT_DB_PASSWORD);
            
            // Create new connection
            this.connection = DriverManager.getConnection(dbUrl, dbUser, dbPassword);
            System.out.println("✓ Database reconnected successfully");
            
        } catch (SQLException e) {
            System.err.println("✗ Reconnection failed: " + e.getMessage());
            throw e;
        }
    }

    private void loadConfiguration() {
        properties = new Properties();
        
        try {
            // Try to load from root directory first (for deployed applications)
            java.io.File configFile = new java.io.File("config.properties");
            
            if (configFile.exists()) {
                try (java.io.FileInputStream fis = new java.io.FileInputStream(configFile)) {
                    properties.load(fis);
                    System.out.println("✓ Configuration loaded from config.properties in project root");
                }
            } else {
                // Try to load from resources folder using classpath
                try (java.io.InputStream is = getClass().getClassLoader().getResourceAsStream("config.properties")) {
                    if (is != null) {
                        properties.load(is);
                        System.out.println("✓ Configuration loaded from classpath (resources folder)");
                    } else {
                        throw new IOException("config.properties not found in classpath");
                    }
                }
            }
            
        } catch (IOException e) {
            System.out.println("⚠ config.properties not found. Using default values.");
            System.out.println("  Creating default configuration...");
            
            // Set default properties
            properties.setProperty("db.url", DEFAULT_DB_URL);
            properties.setProperty("db.user", DEFAULT_DB_USER);
            properties.setProperty("db.password", DEFAULT_DB_PASSWORD);
            properties.setProperty("db.driver", DEFAULT_DB_DRIVER);
            
            // Try to create the config file in project root
            createDefaultConfigFile();
        }
    }

    private void createDefaultConfigFile() {
        try (java.io.FileOutputStream fos = new java.io.FileOutputStream("src/main/resources/config.properties")) {
            
            // Write default configuration with comments
            String config = """
                # Smart Retail System - Database Configuration
                # Edit these values according to your MySQL setup
                
                # Database URL (format: jdbc:mysql://host:port/database_name)
                db.url=%s
                
                # Database username
                db.user=%s
                
                # Database password
                db.password=%s
                
                # JDBC Driver class
                db.driver=%s
                """.formatted(DEFAULT_DB_URL, DEFAULT_DB_USER, DEFAULT_DB_PASSWORD, DEFAULT_DB_DRIVER);
            
            fos.write(config.getBytes());
            
            System.out.println("✓ Default config.properties created");
            
        } catch (IOException e) {
            System.err.println("⚠ Could not create config file: " + e.getMessage());
        }
    }

    public String getDatabaseUrl() {
        return properties.getProperty("db.url", DEFAULT_DB_URL);
    }

    public String getDatabaseUser() {
        return properties.getProperty("db.user", DEFAULT_DB_USER);
    }

    public boolean checkDatabaseSchema() {
        try {
            // Check if key tables exist
            String[] requiredTables = {
                "Products", "Customers", "Sales", "Sale_Details", "Users", "Audit_Logs"
            };
            
            java.sql.DatabaseMetaData metaData = connection.getMetaData();
            
            for (String tableName : requiredTables) {
                try (java.sql.ResultSet rs = metaData.getTables(null, null, tableName, null)) {
                    if (!rs.next()) {
                        System.err.println("✗ Required table not found: " + tableName);
                        return false;
                    }
                }
            }
            
            System.out.println("✓ All required database tables exist");
            return true;
            
        } catch (SQLException e) {
            System.err.println("✗ Error checking database schema: " + e.getMessage());
            return false;
        }
    }

    public String getDatabaseInfo() {
        try {
            java.sql.DatabaseMetaData metaData = connection.getMetaData();
            
            StringBuilder info = new StringBuilder();
            info.append("Database Product: ").append(metaData.getDatabaseProductName()).append("\n");
            info.append("Database Version: ").append(metaData.getDatabaseProductVersion()).append("\n");
            info.append("Driver Name: ").append(metaData.getDriverName()).append("\n");
            info.append("Driver Version: ").append(metaData.getDriverVersion()).append("\n");
            info.append("URL: ").append(metaData.getURL()).append("\n");
            info.append("Username: ").append(metaData.getUserName()).append("\n");
            
            return info.toString();
            
        } catch (SQLException e) {
            return "Error getting database info: " + e.getMessage();
        }
    }

    public static void main(String[] args) {
        System.out.println("========================================");
        System.out.println("Database Connection Test");
        System.out.println("========================================\n");
        
        try {
            // Get database instance
            DBConnection dbConn = DBConnection.getInstance();
            
            // Test connection
            System.out.println("\n1. Testing connection...");
            if (dbConn.testConnection()) {
                System.out.println("   ✓ Connection is active");
            } else {
                System.out.println("   ✗ Connection is not active");
            }
            
            // Show database info
            System.out.println("\n2. Database Information:");
            System.out.println(dbConn.getDatabaseInfo());
            
            // Check schema
            System.out.println("\n3. Checking database schema...");
            dbConn.checkDatabaseSchema();
            
            // Close connection
            System.out.println("\n4. Closing connection...");
            dbConn.closeConnection();
            
            System.out.println("\n========================================");
            System.out.println("Test completed successfully!");
            System.out.println("========================================");
            
        } catch (SQLException e) {
            System.err.println("\n========================================");
            System.err.println("Test failed!");
            System.err.println("========================================");
            System.err.println("Error: " + e.getMessage());
        }
    }
}