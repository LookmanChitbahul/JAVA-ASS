package utils;

import models.Sale;
import models.SaleDetail;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.List;

public class PDFUtil {

    public static boolean generateReceipt(Sale sale, String filePath) {
        PDDocument document = null;

        try {
            // Create a new document
            document = new PDDocument();
            PDPage page = new PDPage(new PDRectangle(300, 600)); // Narrow receipt size
            document.addPage(page);

            // Prepare content stream
            PDPageContentStream contentStream = new PDPageContentStream(document, page);

            // Set up fonts
            PDType1Font titleFont = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
            PDType1Font headerFont = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
            PDType1Font normalFont = new PDType1Font(Standard14Fonts.FontName.HELVETICA);
            PDType1Font smallFont = new PDType1Font(Standard14Fonts.FontName.HELVETICA_OBLIQUE);
            PDType1Font boldFont = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);

            // Set margins
            float margin = 15;
            float yPosition = 580; // Start from top
            float lineHeight = 12;
            float centerX = 150; // Center of 300 width page

            // Store header
            contentStream.beginText();
            contentStream.setFont(titleFont, 16);
            contentStream.newLineAtOffset(centerX - 60, yPosition);
            contentStream.showText("SUPERMART STORE");
            contentStream.endText();
            yPosition -= 20;

            contentStream.beginText();
            contentStream.setFont(normalFont, 10);
            contentStream.newLineAtOffset(centerX - 70, yPosition);
            contentStream.showText("123 Main Street, City");
            contentStream.endText();
            yPosition -= 15;

            contentStream.beginText();
            contentStream.setFont(normalFont, 10);
            contentStream.newLineAtOffset(centerX - 60, yPosition);
            contentStream.showText("Tel: (123) 456-7890");
            contentStream.endText();
            yPosition -= 20;

            // Separator line
            contentStream.moveTo(margin, yPosition);
            contentStream.lineTo(300 - margin, yPosition);
            contentStream.stroke();
            yPosition -= 20;

            // Receipt header
            contentStream.beginText();
            contentStream.setFont(headerFont, 12);
            contentStream.newLineAtOffset(centerX - 40, yPosition);
            contentStream.showText("SALES RECEIPT");
            contentStream.endText();
            yPosition -= 20;

            // Receipt details in two columns
            float leftCol = margin;
            float rightCol = 150;

            // Sale number
            contentStream.beginText();
            contentStream.setFont(boldFont, 10);
            contentStream.newLineAtOffset(leftCol, yPosition);
            contentStream.showText("Receipt #:");
            contentStream.endText();

            contentStream.beginText();
            contentStream.setFont(normalFont, 10);
            contentStream.newLineAtOffset(rightCol, yPosition);
            contentStream.showText(String.valueOf(sale.getSaleId()));
            contentStream.endText();
            yPosition -= lineHeight;

            // Date
            contentStream.beginText();
            contentStream.setFont(boldFont, 10);
            contentStream.newLineAtOffset(leftCol, yPosition);
            contentStream.showText("Date:");
            contentStream.endText();

            contentStream.beginText();
            contentStream.setFont(normalFont, 10);
            contentStream.newLineAtOffset(rightCol, yPosition);
            contentStream.showText(new SimpleDateFormat("dd/MM/yyyy").format(sale.getSaleDate()));
            contentStream.endText();
            yPosition -= lineHeight;

            // Time
            contentStream.beginText();
            contentStream.setFont(boldFont, 10);
            contentStream.newLineAtOffset(leftCol, yPosition);
            contentStream.showText("Time:");
            contentStream.endText();

            contentStream.beginText();
            contentStream.setFont(normalFont, 10);
            contentStream.newLineAtOffset(rightCol, yPosition);
            contentStream.showText(new SimpleDateFormat("HH:mm:ss").format(sale.getSaleDate()));
            contentStream.endText();
            yPosition -= lineHeight;

            // Customer
            contentStream.beginText();
            contentStream.setFont(boldFont, 10);
            contentStream.newLineAtOffset(leftCol, yPosition);
            contentStream.showText("Customer ID:");
            contentStream.endText();

            contentStream.beginText();
            contentStream.setFont(normalFont, 10);
            contentStream.newLineAtOffset(rightCol, yPosition);
            contentStream.showText(String.valueOf(sale.getCustomerId()));
            contentStream.endText();
            yPosition -= lineHeight;

            // Payment method
            contentStream.beginText();
            contentStream.setFont(boldFont, 10);
            contentStream.newLineAtOffset(leftCol, yPosition);
            contentStream.showText("Payment:");
            contentStream.endText();

            contentStream.beginText();
            contentStream.setFont(normalFont, 10);
            contentStream.newLineAtOffset(rightCol, yPosition);
            contentStream.showText(sale.getPaymentMethod());
            contentStream.endText();
            yPosition -= 20;

            // Separator
            contentStream.moveTo(margin, yPosition);
            contentStream.lineTo(300 - margin, yPosition);
            contentStream.stroke();
            yPosition -= 15;

            // Table header
            contentStream.beginText();
            contentStream.setFont(headerFont, 10);
            contentStream.newLineAtOffset(margin, yPosition);
            contentStream.showText("QTY");
            contentStream.endText();

            contentStream.beginText();
            contentStream.setFont(headerFont, 10);
            contentStream.newLineAtOffset(margin + 40, yPosition);
            contentStream.showText("DESCRIPTION");
            contentStream.endText();

            contentStream.beginText();
            contentStream.setFont(headerFont, 10);
            contentStream.newLineAtOffset(margin + 180, yPosition);
            contentStream.showText("AMOUNT");
            contentStream.endText();
            yPosition -= 15;

            // Header line
            contentStream.moveTo(margin, yPosition);
            contentStream.lineTo(300 - margin, yPosition);
            contentStream.stroke();
            yPosition -= 10;

            // Add sale items
            double subtotal = 0;
            if (sale.getSaleDetails() != null) {
                for (SaleDetail detail : sale.getSaleDetails()) {
                    // Check if we need a new page
                    if (yPosition < 150) {
                        contentStream.close();
                        PDPage newPage = new PDPage(new PDRectangle(300, 600));
                        document.addPage(newPage);
                        contentStream = new PDPageContentStream(document, newPage);
                        yPosition = 580;
                    }

                    // Quantity
                    contentStream.beginText();
                    contentStream.setFont(normalFont, 9);
                    contentStream.newLineAtOffset(margin, yPosition);
                    contentStream.showText(String.valueOf(detail.getQuantity()));
                    contentStream.endText();

                    // Product name (truncate if too long)
                    String productName = detail.getProductName();
                    if (productName.length() > 20) {
                        productName = productName.substring(0, 20) + "...";
                    }

                    contentStream.beginText();
                    contentStream.setFont(normalFont, 9);
                    contentStream.newLineAtOffset(margin + 40, yPosition);
                    contentStream.showText(productName);
                    contentStream.endText();

                    // Price
                    contentStream.beginText();
                    contentStream.setFont(normalFont, 9);
                    contentStream.newLineAtOffset(margin + 180, yPosition);
                    contentStream.showText(String.format("$%.2f", detail.getUnitPrice()));
                    contentStream.endText();
                    yPosition -= lineHeight;

                    // Subtotal for this item
                    contentStream.beginText();
                    contentStream.setFont(normalFont, 8);
                    contentStream.newLineAtOffset(margin + 160, yPosition);
                    contentStream.showText("x" + detail.getQuantity() + " =");
                    contentStream.endText();

                    contentStream.beginText();
                    contentStream.setFont(boldFont, 9);
                    contentStream.newLineAtOffset(margin + 200, yPosition);
                    contentStream.showText(String.format("$%.2f", detail.getTotalPrice()));
                    contentStream.endText();
                    yPosition -= lineHeight + 5;

                    subtotal += detail.getTotalPrice();
                }
            }

            // Check space for totals
            if (yPosition < 200) {
                contentStream.close();
                PDPage newPage = new PDPage(new PDRectangle(300, 600));
                document.addPage(newPage);
                contentStream = new PDPageContentStream(document, newPage);
                yPosition = 580;
            }

            // Separator
            contentStream.moveTo(margin, yPosition);
            contentStream.lineTo(300 - margin, yPosition);
            contentStream.stroke();
            yPosition -= 15;

            // Totals section
            float totalsX = 180;

            // Subtotal
            contentStream.beginText();
            contentStream.setFont(boldFont, 10);
            contentStream.newLineAtOffset(totalsX, yPosition);
            contentStream.showText("Subtotal:");
            contentStream.endText();

            contentStream.beginText();
            contentStream.setFont(normalFont, 10);
            contentStream.newLineAtOffset(totalsX + 70, yPosition);
            contentStream.showText(String.format("$%.2f", subtotal));
            contentStream.endText();
            yPosition -= lineHeight;

            // Tax (assuming 10%)
            double tax = subtotal * 0.10;
            contentStream.beginText();
            contentStream.setFont(boldFont, 10);
            contentStream.newLineAtOffset(totalsX, yPosition);
            contentStream.showText("Tax (10%):");
            contentStream.endText();

            contentStream.beginText();
            contentStream.setFont(normalFont, 10);
            contentStream.newLineAtOffset(totalsX + 70, yPosition);
            contentStream.showText(String.format("$%.2f", tax));
            contentStream.endText();
            yPosition -= lineHeight;

            // Total
            double total = subtotal + tax;
            contentStream.beginText();
            contentStream.setFont(headerFont, 11);
            contentStream.newLineAtOffset(totalsX, yPosition);
            contentStream.showText("TOTAL:");
            contentStream.endText();

            contentStream.beginText();
            contentStream.setFont(headerFont, 11);
            contentStream.newLineAtOffset(totalsX + 70, yPosition);
            contentStream.showText(String.format("$%.2f", total));
            contentStream.endText();
            yPosition -= 20;

            // Cash details if cash payment
            if ("Cash".equalsIgnoreCase(sale.getPaymentMethod()) && sale.getCashReceived() != null) {
                contentStream.moveTo(margin, yPosition);
                contentStream.lineTo(300 - margin, yPosition);
                contentStream.stroke();
                yPosition -= 15;

                // Cash received
                contentStream.beginText();
                contentStream.setFont(boldFont, 10);
                contentStream.newLineAtOffset(totalsX, yPosition);
                contentStream.showText("Cash:");
                contentStream.endText();

                contentStream.beginText();
                contentStream.setFont(normalFont, 10);
                contentStream.newLineAtOffset(totalsX + 70, yPosition);
                contentStream.showText(String.format("$%.2f", sale.getCashReceived()));
                contentStream.endText();
                yPosition -= lineHeight;

                // Change
                contentStream.beginText();
                contentStream.setFont(boldFont, 10);
                contentStream.newLineAtOffset(totalsX, yPosition);
                contentStream.showText("Change:");
                contentStream.endText();

                contentStream.beginText();
                contentStream.setFont(normalFont, 10);
                contentStream.newLineAtOffset(totalsX + 70, yPosition);
                contentStream.showText(String.format("$%.2f", sale.getChangeGiven()));
                contentStream.endText();
                yPosition -= 20;
            }

            // Final separator
            contentStream.moveTo(margin, yPosition);
            contentStream.lineTo(300 - margin, yPosition);
            contentStream.stroke();
            yPosition -= 20;

            // Thank you message
            contentStream.beginText();
            contentStream.setFont(boldFont, 10);
            contentStream.newLineAtOffset(centerX - 40, yPosition);
            contentStream.showText("THANK YOU!");
            contentStream.endText();
            yPosition -= 15;

            contentStream.beginText();
            contentStream.setFont(smallFont, 8);
            contentStream.newLineAtOffset(centerX - 60, yPosition);
            contentStream.showText("Please retain this receipt");
            contentStream.endText();
            yPosition -= 12;

            contentStream.beginText();
            contentStream.setFont(smallFont, 8);
            contentStream.newLineAtOffset(centerX - 100, yPosition);
            contentStream.showText("Items can be exchanged within 7 days with receipt");
            contentStream.endText();
            yPosition -= 12;

            contentStream.beginText();
            contentStream.setFont(smallFont, 7);
            contentStream.newLineAtOffset(centerX - 90, yPosition);
            contentStream.showText("Generated by: " + sale.getCreatedBy());
            contentStream.endText();

            // Close content stream
            contentStream.close();

            // Save the document
            document.save(new File(filePath));
            document.close();

            System.out.println("✓ PDF receipt generated: " + filePath);
            return true;

        } catch (IOException e) {
            System.err.println("✗ Error generating PDF: " + e.getMessage());
            e.printStackTrace();

            if (document != null) {
                try {
                    document.close();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
            return false;
        }
    }
}