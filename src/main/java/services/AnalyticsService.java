package services;

import database.DBConnection;
import java.sql.*;
import java.util.*;

public class AnalyticsService {

    public Map<String, Double> getSalesTrend(String timePeriod, String category) throws SQLException {
        Map<String, Double> data = new LinkedHashMap<>();
        try (Connection conn = DBConnection.getConnection()) {
            if (conn == null)
                return data;

            String interval = getTimeInterval(timePeriod);
            String col = getSalesAmountColumn(conn);
            String table = getSalesTable(conn);

            String sql = "SELECT DATE(s.sale_date) as date, SUM(s." + col + ") as total " +
                    "FROM " + table + " s ";

            if (!"All Categories".equals(category)) {
                sql += "JOIN sale_details sd ON s.sale_id = sd.sale_id " +
                        "JOIN products p ON sd.product_id = p.product_id ";
            }

            sql += "WHERE s.sale_date >= " + interval + " ";

            if (!"All Categories".equals(category)) {
                sql += "AND p.category = ? ";
            }

            sql += "GROUP BY DATE(s.sale_date) ORDER BY date ASC";

            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                if (!"All Categories".equals(category)) {
                    pstmt.setString(1, category);
                }
                try (ResultSet rs = pstmt.executeQuery()) {
                    while (rs.next()) {
                        data.put(rs.getString("date"), rs.getDouble("total"));
                    }
                }
            }
        }
        return data;
    }

    public Map<String, Integer> getTopProducts(String timePeriod, String category) throws SQLException {
        Map<String, Integer> data = new LinkedHashMap<>();
        try (Connection conn = DBConnection.getConnection()) {
            if (conn == null)
                return data;

            String interval = getTimeInterval(timePeriod);
            String sTable = getSaleDetailsTable(conn);
            String pTable = getProductsTable(conn);
            String salesTable = getSalesTable(conn);

            String sql = "SELECT p.name, SUM(sd.quantity) as total_qty " +
                    "FROM " + sTable + " sd " +
                    "JOIN " + pTable + " p ON sd.product_id = p.product_id " +
                    "JOIN " + salesTable + " s ON sd.sale_id = s.sale_id " +
                    "WHERE s.sale_date >= " + interval + " ";

            if (!"All Categories".equals(category)) {
                sql += "AND p.category = ? ";
            }

            sql += "GROUP BY p.name ORDER BY total_qty DESC LIMIT 5";

            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                if (!"All Categories".equals(category)) {
                    pstmt.setString(1, category);
                }
                try (ResultSet rs = pstmt.executeQuery()) {
                    while (rs.next()) {
                        data.put(rs.getString("name"), rs.getInt("total_qty"));
                    }
                }
            }
        }
        return data;
    }

    public Map<String, Double> getRevenueDistribution(String timePeriod, String category) throws SQLException {
        Map<String, Double> data = new HashMap<>();
        try (Connection conn = DBConnection.getConnection()) {
            if (conn == null)
                return data;

            String interval = getTimeInterval(timePeriod);
            String sTable = getSaleDetailsTable(conn);
            String pTable = getProductsTable(conn);
            String salesTable = getSalesTable(conn);
            String col = getSubtotalColumn(conn);

            String sql;
            if ("All Categories".equals(category)) {
                sql = "SELECT p.category as label, SUM(sd." + col + ") as revenue " +
                        "FROM " + sTable + " sd " +
                        "JOIN " + pTable + " p ON sd.product_id = p.product_id " +
                        "JOIN " + salesTable + " s ON sd.sale_id = s.sale_id " +
                        "WHERE s.sale_date >= " + interval + " " +
                        "GROUP BY p.category";
            } else {
                sql = "SELECT p.name as label, SUM(sd." + col + ") as revenue " +
                        "FROM " + sTable + " sd " +
                        "JOIN " + pTable + " p ON sd.product_id = p.product_id " +
                        "JOIN " + salesTable + " s ON sd.sale_id = s.sale_id " +
                        "WHERE s.sale_date >= " + interval + " AND p.category = ? " +
                        "GROUP BY p.name";
            }

            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                if (!"All Categories".equals(category)) {
                    pstmt.setString(1, category);
                }
                try (ResultSet rs = pstmt.executeQuery()) {
                    while (rs.next()) {
                        data.put(rs.getString("label"), rs.getDouble("revenue"));
                    }
                }
            }
        }
        return data;
    }

    // --- Helper for Filters ---
    private String getTimeInterval(String period) {
        switch (period) {
            case "Today":
                return "CURDATE()";
            case "Last 7 Days":
                return "DATE_SUB(CURDATE(), INTERVAL 7 DAY)";
            case "This Month":
                return "DATE_SUB(CURDATE(), INTERVAL 30 DAY)";
            case "This Year":
                return "DATE_SUB(CURDATE(), INTERVAL 1 YEAR)";
            default:
                return "DATE_SUB(CURDATE(), INTERVAL 30 DAY)";
        }
    }

    // --- Helper for Dynamic Schema ---
    private String getSalesAmountColumn(Connection conn) {
        return checkColumnExists(conn, "sales", "final_amount") ? "final_amount" : "total_amount";
    }

    private String getSubtotalColumn(Connection conn) {
        return checkColumnExists(conn, "sale_details", "total_price") ? "total_price" : "subtotal";
    }

    private String getSalesTable(Connection conn) {
        return checkTableExists(conn, "Sales") ? "Sales" : "sales";
    }

    private String getProductsTable(Connection conn) {
        return checkTableExists(conn, "Products") ? "Products" : "products";
    }

    private String getSaleDetailsTable(Connection conn) {
        return checkTableExists(conn, "Sale_Details") ? "Sale_Details" : "sale_details";
    }

    private boolean checkColumnExists(Connection conn, String table, String column) {
        try {
            DatabaseMetaData meta = conn.getMetaData();
            try (ResultSet rs = meta.getColumns(null, null, table, column)) {
                if (rs.next())
                    return true;
            }
            // try lowercase
            try (ResultSet rs = meta.getColumns(null, null, table.toLowerCase(), column)) {
                return rs.next();
            }
        } catch (SQLException e) {
            return false;
        }
    }

    private boolean checkTableExists(Connection conn, String table) {
        try {
            DatabaseMetaData meta = conn.getMetaData();
            try (ResultSet rs = meta.getTables(null, null, table, null)) {
                return rs.next();
            }
        } catch (SQLException e) {
            return false;
        }
    }
}
