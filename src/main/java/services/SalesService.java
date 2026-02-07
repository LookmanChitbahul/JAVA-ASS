package services;

import models.Sale;
import models.SaleDetail;
import database.DBConnection;
import utils.PDFUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SalesService {

    // Create new sale - UPDATED TO MATCH SQL
    public int createSale(Sale sale) throws SQLException {
        Connection conn = null;
        PreparedStatement pstmtSale = null;
        PreparedStatement pstmtDetail = null;
        ResultSet rs = null;

        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false);

            // Insert into Sales table - MATCHING YOUR SQL SCHEMA
            String sqlSale = "INSERT INTO Sales (customer_id, user_id, total_amount, " +
                    "discount, final_amount, payment_method, status, notes, created_by) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

            pstmtSale = conn.prepareStatement(sqlSale, Statement.RETURN_GENERATED_KEYS);
            pstmtSale.setInt(1, sale.getCustomerId());
            pstmtSale.setInt(2, sale.getUserId());
            pstmtSale.setDouble(3, sale.getTotalAmount());
            pstmtSale.setDouble(4, sale.getDiscount());
            pstmtSale.setDouble(5, sale.getFinalAmount());
            pstmtSale.setString(6, sale.getPaymentMethod());
            pstmtSale.setString(7, sale.getStatus());
            pstmtSale.setString(8, sale.getNotes());
            pstmtSale.setString(9, sale.getCreatedBy());

            int rowsAffected = pstmtSale.executeUpdate();

            if (rowsAffected == 0) {
                throw new SQLException("Creating sale failed, no rows affected.");
            }

            // Get generated sale ID
            rs = pstmtSale.getGeneratedKeys();
            int saleId = 0;
            if (rs.next()) {
                saleId = rs.getInt(1);
                sale.setSaleId(saleId);
            } else {
                throw new SQLException("Creating sale failed, no ID obtained.");
            }

            // Insert sale details - MATCHING YOUR SQL SCHEMA
            if (sale.getSaleDetails() != null && !sale.getSaleDetails().isEmpty()) {
                String sqlDetail = "INSERT INTO Sale_Details (sale_id, product_id, " +
                        "quantity, unit_price, total_price, discount) " +
                        "VALUES (?, ?, ?, ?, ?, ?)";

                pstmtDetail = conn.prepareStatement(sqlDetail);

                for (SaleDetail detail : sale.getSaleDetails()) {
                    pstmtDetail.setInt(1, saleId);
                    pstmtDetail.setInt(2, detail.getProductId());
                    pstmtDetail.setInt(3, detail.getQuantity());
                    pstmtDetail.setDouble(4, detail.getUnitPrice());
                    pstmtDetail.setDouble(5, detail.getTotalPrice());
                    pstmtDetail.setDouble(6, detail.getDiscount());
                    pstmtDetail.addBatch();
                }

                pstmtDetail.executeBatch();

                // Update product stock
                updateProductStock(sale.getSaleDetails(), conn);
            }

            conn.commit();
            return saleId;

        } catch (SQLException e) {
            if (conn != null) {
                conn.rollback();
            }
            throw e;
        } finally {
            closeResources(rs, pstmtSale, pstmtDetail);
            if (conn != null) {
                conn.setAutoCommit(true);
                conn.close();
            }
        }
    }

    private void updateProductStock(List<SaleDetail> saleDetails, Connection conn) throws SQLException {
        String updateQuery = "UPDATE Products SET stock = stock - ? WHERE product_id = ?";
        PreparedStatement stmt = conn.prepareStatement(updateQuery);

        for (SaleDetail detail : saleDetails) {
            stmt.setInt(1, detail.getQuantity());
            stmt.setInt(2, detail.getProductId());
            stmt.addBatch();
        }

        stmt.executeBatch();
        stmt.close();
    }

    // Get sale by ID - UPDATED QUERY
    public Sale getSaleById(int saleId) throws SQLException {
        String sql = "SELECT s.*, c.full_name as customer_name, c.contact as customer_contact " +
                "FROM Sales s " +
                "LEFT JOIN customers c ON s.customer_id = c.customer_id " +
                "WHERE s.sale_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, saleId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                Sale sale = new Sale();
                sale.setSaleId(rs.getInt("sale_id"));
                sale.setCustomerId(rs.getInt("customer_id"));
                sale.setUserId(rs.getInt("user_id"));
                sale.setSaleDate(rs.getTimestamp("sale_date"));
                sale.setTotalAmount(rs.getDouble("total_amount"));
                sale.setDiscount(rs.getDouble("discount"));
                sale.setTotalAmount(rs.getDouble("total_amount"));
                sale.setDiscount(rs.getDouble("discount"));
                sale.setPaymentMethod(rs.getString("payment_method"));
                sale.setStatus(rs.getString("status"));
                sale.setNotes(rs.getString("notes"));

                // Load sale details
                sale.setSaleDetails(getSaleDetails(saleId));

                return sale;
            }
        }
        return null;
    }

    // Get sale details - UPDATED QUERY
    public List<SaleDetail> getSaleDetails(int saleId) throws SQLException {
        List<SaleDetail> details = new ArrayList<>();
        String sql = "SELECT sd.*, p.name as product_name " +
                "FROM Sale_Details sd " +
                "JOIN Products p ON sd.product_id = p.product_id " +
                "WHERE sd.sale_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, saleId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                SaleDetail detail = new SaleDetail();
                detail.setSaleDetailId(rs.getInt("sale_detail_id"));
                detail.setSaleId(rs.getInt("sale_id"));
                detail.setProductId(rs.getInt("product_id"));
                detail.setProductName(rs.getString("product_name"));
                detail.setQuantity(rs.getInt("quantity"));
                detail.setUnitPrice(rs.getDouble("unit_price"));
                detail.setTotalPrice(rs.getDouble("total_price"));
                detail.setDiscount(rs.getDouble("discount"));
                detail.setCreatedAt(rs.getTimestamp("created_at"));
                details.add(detail);
            }
        }
        return details;
    }

    // Generate receipt PDF - This might need updating too
    public boolean generateReceiptPDF(int saleId, String filePath) {
        try {
            Sale sale = getSaleById(saleId);
            if (sale == null) {
                return false;
            }

            return PDFUtil.generateReceipt(sale, filePath);

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // Close resources helper
    private void closeResources(ResultSet rs, Statement... statements) {
        try {
            if (rs != null) rs.close();
            for (Statement stmt : statements) {
                if (stmt != null) stmt.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}