package services;

import models.Sale;
import models.SaleDetail;
import models.Product;
import models.Customer;
import database.DBConnection;
import utils.PDFUtil;

import javax.swing.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SalesService {
    private DBConnection dbConnection;

    public SalesService() {
        this.dbConnection = DBConnection.getInstance();
    }

    // Create new sale
    public int createSale(Sale sale) throws SQLException {
        Connection conn = null;
        PreparedStatement pstmtSale = null;
        PreparedStatement pstmtDetail = null;
        ResultSet rs = null;

        try {
            conn = dbConnection.getConnection();
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

                // Update product stock
                updateProductStock(conn, sale.getSaleDetails());
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

    private void updateProductStock(Connection conn, List<SaleDetail> saleDetails)
            throws SQLException {

        String sql = "UPDATE Products SET stock_quantity = stock_quantity - ? WHERE product_id = ?";
        PreparedStatement pstmt = conn.prepareStatement(sql);

        for (SaleDetail detail : saleDetails) {
            pstmt.setInt(1, detail.getQuantity());
            pstmt.setInt(2, detail.getProductId());
            pstmt.addBatch();
        }

        pstmt.executeBatch();
        pstmt.close();
    }

    // Get sale by ID
    public Sale getSaleById(int saleId) throws SQLException {
        String sql = "SELECT s.*, c.customer_name FROM Sales s " +
                "LEFT JOIN Customers c ON s.customer_id = c.customer_id " +
                "WHERE s.sale_id = ?";

        try (Connection conn = dbConnection.getConnection();
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
                    sale.setGrandTotal(rs.getDouble("grand_total"));
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

        try (Connection conn = dbConnection.getConnection();
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

    // Get all sales
    public List<Sale> getAllSales() throws SQLException {
        List<Sale> sales = new ArrayList<>();
        String sql = "SELECT * FROM Sales ORDER BY sale_date DESC";

        try (Connection conn = dbConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Sale sale = new Sale();
                sale.setSaleId(rs.getInt("sale_id"));
                sale.setCustomerId(rs.getInt("customer_id"));
                sale.setSaleDate(rs.getTimestamp("sale_date"));
                sale.setTotalAmount(rs.getDouble("total_amount"));
                sale.setGrandTotal(rs.getDouble("grand_total"));
                sales.add(sale);
            }
        }
        return sales;
    }

    // Generate receipt PDF
    public boolean generateReceiptPDF(int saleId, String filePath) {
        try {
            Sale sale = getSaleById(saleId);
            if (sale == null) {
                return false;
            }

            return PDFUtil.generateSaleReceipt(sale, filePath);

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