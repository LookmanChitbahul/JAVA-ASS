package utils;

import models.Sale;
import models.SaleDetail;
import java.io.File;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.List;

public class PDFUtil {

    public static boolean generateReceipt(Sale sale, String filePath) {
        try {
            StringBuilder receipt = new StringBuilder();
            receipt.append("========================================\n");
            receipt.append("           SALES RECEIPT\n");
            receipt.append("========================================\n");
            receipt.append("Receipt #: ").append(sale.getSaleId()).append("\n");
            receipt.append("Date: ").append(new SimpleDateFormat("dd-MMM-yyyy HH:mm:ss").format(sale.getSaleDate())).append("\n");
            receipt.append("Customer ID: ").append(sale.getCustomerId()).append("\n");
            receipt.append("Payment Method: ").append(sale.getPaymentMethod()).append("\n");
            receipt.append("----------------------------------------\n");
            receipt.append(String.format("%-3s %-20s %6s %10s %12s\n",
                    "No.", "Description", "Qty", "Price", "Amount"));
            receipt.append("----------------------------------------\n");

            int itemNo = 1;
            if (sale.getSaleDetails() != null) {
                for (SaleDetail detail : sale.getSaleDetails()) {
                    receipt.append(String.format("%-3d %-20s %6d %10.2f %12.2f\n",
                            itemNo++,
                            detail.getProductName(),
                            detail.getQuantity(),
                            detail.getUnitPrice(),
                            detail.getSubtotal()));
                }
            }

            receipt.append("----------------------------------------\n");
            receipt.append(String.format("%41s: $%10.2f\n", "Subtotal", sale.getTotalAmount()));

            if (sale.getTaxAmount() > 0) {
                receipt.append(String.format("%41s: $%10.2f\n", "Tax", sale.getTaxAmount()));
            }

            if (sale.getDiscountAmount() > 0) {
                receipt.append(String.format("%41s: $%10.2f\n", "Discount", -sale.getDiscountAmount()));
            }

            receipt.append("----------------------------------------\n");
            receipt.append(String.format("%41s: $%10.2f\n", "GRAND TOTAL", sale.getGrandTotal()));
            receipt.append("========================================\n");
            receipt.append("Thank you for your business!\n");
            receipt.append("This is a computer-generated receipt.\n");

            // Write to file
            File file = new File(filePath);
            try (FileWriter writer = new FileWriter(file)) {
                writer.write(receipt.toString());
            }

            return true;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}