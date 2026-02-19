package database;

import java.sql.*;

public class DBInspector {
    public static void main(String[] args) {
        try (Connection conn = DBConnection.getConnection()) {
            if (conn == null) {
                System.out.println("Connection failed!");
                return;
            }
            DatabaseMetaData meta = conn.getMetaData();

            System.out.println("--- TABLES ---");
            ResultSet tables = meta.getTables(null, null, "%", new String[] { "TABLE" });
            while (tables.next()) {
                String tableName = tables.getString("TABLE_NAME");
                System.out.println("Table: " + tableName);

                ResultSet columns = meta.getColumns(null, null, tableName, "%");
                while (columns.next()) {
                    System.out.println("  Col: " + columns.getString("COLUMN_NAME"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
