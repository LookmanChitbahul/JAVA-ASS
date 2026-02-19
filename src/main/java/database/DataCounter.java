package database;

import java.sql.*;

public class DataCounter {
    public static void main(String[] args) {
        String[] tables = { "Users", "Products", "Customers", "Sales", "Sale_Details" };
        try (Connection conn = DBConnection.getConnection()) {
            if (conn == null)
                return;
            for (String table : tables) {
                try (Statement stmt = conn.createStatement()) {
                    // Try exact name, then lowercase if fails
                    String sql = "SELECT COUNT(*) FROM " + table;
                    try (ResultSet rs = stmt.executeQuery(sql)) {
                        if (rs.next())
                            System.out.println(table + ": " + rs.getInt(1));
                    } catch (SQLException e) {
                        try (ResultSet rs = stmt.executeQuery(sql.toLowerCase())) {
                            if (rs.next())
                                System.out.println(table.toLowerCase() + ": " + rs.getInt(1));
                        } catch (SQLException e2) {
                            System.out.println(table + ": Table not found");
                        }
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
