package services;

import database.DBConnection;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;

public class SettingsService {

    public SettingsService() {
        ensureTableExists();
    }

    private void ensureTableExists() {
        String sql = "CREATE TABLE IF NOT EXISTS sys_config (" +
                "variable VARCHAR(100) PRIMARY KEY, " +
                "value TEXT, " +
                "set_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                "set_by VARCHAR(50))";
        try (Connection conn = DBConnection.getConnection();
                Statement stmt = conn.createStatement()) {
            if (conn != null) {
                stmt.execute(sql);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public String getSetting(String key, String defaultValue) {
        String sql = "SELECT value FROM sys_config WHERE variable = ?";
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            if (conn == null)
                return defaultValue;
            pstmt.setString(1, key);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("value");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return defaultValue;
    }

    public void saveSetting(String key, String value) {
        String sql = "INSERT INTO sys_config (variable, value, set_time) VALUES (?, ?, CURRENT_TIMESTAMP) " +
                "ON DUPLICATE KEY UPDATE value = ?, set_time = CURRENT_TIMESTAMP";
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            if (conn == null)
                return;
            pstmt.setString(1, key);
            pstmt.setString(2, value);
            pstmt.setString(3, value);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public Map<String, String> getAllSettings() {
        Map<String, String> settings = new HashMap<>();
        String sql = "SELECT variable, value FROM sys_config";
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql);
                ResultSet rs = pstmt.executeQuery()) {
            if (conn == null)
                return settings;
            while (rs.next()) {
                settings.put(rs.getString("variable"), rs.getString("value"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return settings;
    }
}
