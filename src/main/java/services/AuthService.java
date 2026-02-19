package services;

import database.DBConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import models.User;
import utils.JSONUtil;

public class AuthService {

    /**
     * Authenticate user by verifying username and password against the database
     * 
     * @param username The username to authenticate
     * @param password The password to authenticate
     * @return true if authentication is successful, false otherwise
     * @throws SQLException if database error occurs
     */
    public boolean authenticateUser(String username, String password) throws SQLException {
        if (username == null || username.isEmpty() || password == null || password.isEmpty()) {
            throw new IllegalArgumentException("Username and password cannot be empty");
        }

        try (Connection conn = DBConnection.getConnection()) {
            if (conn == null)
                return false;

            // Dynamically check for password or password_hash column
            String passwordColumn = "password"; // default
            try {
                java.sql.DatabaseMetaData meta = conn.getMetaData();
                try (ResultSet rs = meta.getColumns(null, null, "users", "password_hash")) {
                    if (rs.next()) {
                        passwordColumn = "password_hash";
                    } else {
                        // try uppercase Users
                        try (ResultSet rs2 = meta.getColumns(null, null, "Users", "password_hash")) {
                            if (rs2.next())
                                passwordColumn = "password_hash";
                        }
                    }
                }
            } catch (Exception e) {
                // Fallback to default if metadata check fails
            }

            String sql = "SELECT * FROM users WHERE username=? AND " + passwordColumn + "=?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, username);
                stmt.setString(2, password);

                try (ResultSet rs = stmt.executeQuery()) {
                    boolean isAuthenticated = rs.next();
                    if (isAuthenticated) {
                        JSONUtil.logLogin(username);
                    }
                    return isAuthenticated;
                }
            } catch (SQLException e) {
                // Secondary fallback try uppercase Users if lowercase users fails
                String sql2 = "SELECT * FROM Users WHERE username=? AND " + passwordColumn + "=?";
                try (PreparedStatement stmt = conn.prepareStatement(sql2)) {
                    stmt.setString(1, username);
                    stmt.setString(2, password);
                    try (ResultSet rs = stmt.executeQuery()) {
                        boolean isAuthenticated = rs.next();
                        if (isAuthenticated)
                            JSONUtil.logLogin(username);
                        return isAuthenticated;
                    }
                }
            }
        }
    }

    /**
     * Validate user input before authentication
     * 
     * @param username The username to validate
     * @param password The password to validate
     * @return true if both fields are non-empty
     */
    public boolean validateInputs(String username, String password) {
        return username != null && !username.isEmpty() &&
                password != null && !password.isEmpty();
    }

    /**
     * Get full user details after successful authentication
     * 
     * @param username The username to fetch details for
     * @return A User object if found, null otherwise
     * @throws SQLException if database error occurs
     */
    public User getUserDetails(String username) throws SQLException {
        try (Connection conn = DBConnection.getConnection()) {
            if (conn == null)
                return null;

            String sql = "SELECT * FROM users WHERE username=?";
            PreparedStatement stmt = null;
            ResultSet rs = null;

            try {
                stmt = conn.prepareStatement(sql);
                stmt.setString(1, username);
                rs = stmt.executeQuery();
            } catch (SQLException e) {
                // Try uppercase Users
                sql = "SELECT * FROM Users WHERE username=?";
                stmt = conn.prepareStatement(sql);
                stmt.setString(1, username);
                rs = stmt.executeQuery();
            }

            if (rs != null && rs.next()) {
                String email = getColumnStringSafe(rs, "email", "not set");
                String phone = getColumnStringSafe(rs, "phone", "not set");
                String status = getColumnStringSafe(rs, "status", "Active");
                String department = getColumnStringSafe(rs, "department", "General");
                String memberSince = getColumnStringSafe(rs, "created_at", "Unknown");

                return new User(username, email, phone, status, department, memberSince);
            }
            return null;
        }
    }

    private String getColumnStringSafe(ResultSet rs, String columnName, String defaultValue) {
        try {
            String val = rs.getString(columnName);
            return (val != null) ? val : defaultValue;
        } catch (SQLException e) {
            return defaultValue;
        }
    }

    /**
     * Log out a user by recording the logout event
     * 
     * @param username The username of the user logging out
     */
    public void logoutUser(String username) {
        JSONUtil.logLogout(username);
    }
}
