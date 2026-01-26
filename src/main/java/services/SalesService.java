package services;

import models.Sale;
import models.SaleDetail;
import database.DBConnection;
import utils.PDFUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SalesService {

    // Get database connection
    private Connection getConnection() throws SQLException {
        Connection conn = DBConnection.getConnection();
        if (conn == null) {
            throw new SQLException("Unable to establish database connection");
        }
        return conn;
    }

    // Create new sale
    public int createSale(Sale sale) throws SQLException {
        Connection conn = null;
        PreparedStatement pstmtSale = null;
        PreparedStatement pstmtDetail = null;
        ResultSet rs = null;

        try {
            conn = getConnection();
            conn.setAutoCommit(false);

            // Insert into Sales table
            String sqlSale = "INSERT INTO Sales (customer_id, total_amount, tax_amount, " +
                    "discount_amount, grand_total, payment_method, created_by) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?)";

            pstmtSale = conn.prepareStatement(sqlSale, Statement.RETURN_GENERATED_KEYS);
            pstmtSale.setInt(1, sale.getCustomerId());
            pstmtSale.setDouble(2, sale.getTotalAmount());
            pstmtSale.setDouble(3, sale.getTaxAmount());
            pstmtSale.setDouble(4, sale.getDiscountAmount());
            pstmtSale.setDouble(5, sale.getGrandTotal());
            pstmtSale.setString(6, sale.getPaymentMethod());
            pstmtSale.setString(7, sale.getCreatedBy());

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

            // Insert sale details
            if (sale.getSaleDetails() != null && !sale.getSaleDetails().isEmpty()) {
                String sqlDetail = "INSERT INTO Sale_Details (sale_id, product_id, product_name, " +
                        "quantity, unit_price, subtotal) VALUES (?, ?, ?, ?, ?, ?)";

                pstmtDetail = conn.prepareStatement(sqlDetail);

                for (SaleDetail detail : sale.getSaleDetails()) {
                    pstmtDetail.setInt(1, saleId);
                    pstmtDetail.setInt(2, detail.getProductId());
                    pstmtDetail.setString(3, detail.getProductName());
                    pstmtDetail.setInt(4, detail.getQuantity());
                    pstmtDetail.setDouble(5, detail.getUnitPrice());
                    pstmtDetail.setDouble(6, detail.getSubtotal());
                    pstmtDetail.addBatch();
                }

                pstmtDetail.executeBatch();

                // Update product stock using ProductService
                updateProductStock(sale.getSaleDetails());
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
            }
        }
    }

    private void updateProductStock(List<SaleDetail> saleDetails) {
        ProductService productService = new ProductService();

        for (SaleDetail detail : saleDetails) {
            try {
                // Use your ProductService's decreaseStock method
                boolean success = productService.decreaseStock(detail.getProductId(), detail.getQuantity());
                if (!success) {
                    System.err.println("Failed to decrease stock for product ID: " + detail.getProductId());
                }
            } catch (Exception e) {
                System.err.println("Error updating stock for product ID " + detail.getProductId() + ": " + e.getMessage());
            }
        }
    }

    // Get sale by ID
    public Sale getSaleById(int saleId) throws SQLException {
        String sql = "SELECT s.*, c.contact as customer_contact, c.email as customer_email " +
                "FROM Sales s " +
                "LEFT JOIN Customers c ON s.customer_id = c.customer_id " +
                "WHERE s.sale_id = ?";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, saleId);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    Sale sale = new Sale();
                    sale.setSaleId(rs.getInt("sale_id"));
                    sale.setCustomerId(rs.getInt("customer_id"));
                    sale.setSaleDate(rs.getTimestamp("sale_date"));
                    sale.setTotalAmount(rs.getDouble("total_amount"));
                    sale.setTaxAmount(rs.getDouble("tax_amount"));
                    sale.setDiscountAmount(rs.getDouble("discount_amount"));
                    sale.setPaymentMethod(rs.getString("payment_method"));
                    sale.setCreatedBy(rs.getString("created_by"));

                    // Load sale details
                    sale.setSaleDetails(getSaleDetails(saleId));

                    return sale;
                }
            }
        }
        return null;
    }

    // Get sale details
    public List<SaleDetail> getSaleDetails(int saleId) throws SQLException {
        List<SaleDetail> details = new ArrayList<>();
        String sql = "SELECT * FROM Sale_Details WHERE sale_id = ?";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, saleId);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    SaleDetail detail = new SaleDetail();
                    detail.setDetailId(rs.getInt("detail_id"));
                    detail.setSaleId(rs.getInt("sale_id"));
                    detail.setProductId(rs.getInt("product_id"));
                    detail.setProductName(rs.getString("product_name"));
                    detail.setQuantity(rs.getInt("quantity"));
                    detail.setUnitPrice(rs.getDouble("unit_price"));
                    detail.setSubtotal(rs.getDouble("subtotal"));
                    details.add(detail);
                }
            }
        }
        return details;
    }

    // Generate receipt PDF
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