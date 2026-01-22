package database;
import java.sql.*;

public class DBConnection {
    // DB connection placeholder
    private static final String URL ="jdbc:mysql://localhost:3306/smart_retail";
    private static final String USER ="root";
    private static final String PASSWORD ="root@12345678";

    public static Connection getConnection(){
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");

            Connection conn = DriverManager.getConnection(URL,USER,PASSWORD);
            System.out.println("Connection succesfull.!");
            return conn;

        } catch ( ClassNotFoundException e) {
            System.err.println("Error !!!!: MySQL JDBC Driver not found !");
            e.printStackTrace();
            return null;

        }catch(SQLException e){
            System.err.println("Error !!!!:Could not connect to the database");
            e.printStackTrace();
            return null;

        }
    }

    public static void main(String[] args) {
        Connection conn = getConnection();

        if (conn != null){
            System.out.println("Connection Successfull !!");
            try {
                conn.close();  //close connection
            } catch ( SQLException e) {
                e.printStackTrace();
            }
        }else{
            System.out.println("Error !!! couldn't connect to Database");
        }
    }
        

    
    }

