package database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {

  private static final String URL ="jdbc:mysql://localhost:3306/smart_retail";
  private static final String USER = "root";
  private static final String PASSWORD = "root@12345678";
  private static DBConnection instance;

  private DBConnection() {
  }

  public static DBConnection getInstance() {
    if (instance == null) {
      instance = new DBConnection();
    }
    return instance;
  }

  public static Connection getConnection(){
    try {
        Class.forName("com.mysql.cj.jdbc.Driver");

        Connection conn = DriverManager.getConnection(URL,USER, PASSWORD);
        System.out.println("Connection Successfull");
        return conn;
    } catch ( ClassNotFoundException e) {
        System.err.println("Error: MYSQL JDBC Driver not found!");
        return null;
    } catch (SQLException e){
        System.err.println("ERROR : Could not connect to the database! ");
        e.printStackTrace();
        return null;
    }
  }
  public static void main(String[] args) {
    Connection conn = getConnection();
    
    if (conn != null){
        System.out.println("Database is reachable!");
        try{
        conn.close();

        } catch (SQLException e){
            e.printStackTrace();
        }
    } else{
        System.out.println("Cannot connect to the database.");
    }
    }
  }