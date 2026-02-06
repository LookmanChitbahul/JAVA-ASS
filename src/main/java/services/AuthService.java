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
            String sql = "SELECT * FROM Users WHERE username=? AND password=?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, username);
            stmt.setString(2, password);

            ResultSet rs = stmt.executeQuery();
            boolean isAuthenticated = rs.next();

            // Log the login attempt
            if (isAuthenticated) {
                JSONUtil.logLogin(username);
            }

            return isAuthenticated;
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
            // We assume the login table has these columns or there is a users table
            // Based on the requirement, we fetch from the database
            String sql = "SELECT * FROM Users WHERE username=?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, username);

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                // Try to get values, providing defaults if columns are missing in some
                // environments
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
