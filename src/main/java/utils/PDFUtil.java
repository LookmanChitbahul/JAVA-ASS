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
            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);

            // Prepare content stream
            PDPageContentStream contentStream = new PDPageContentStream(document, page);

            // Set up fonts
            PDType1Font titleFont = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
            PDType1Font headerFont = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
            PDType1Font normalFont = new PDType1Font(Standard14Fonts.FontName.HELVETICA);
            PDType1Font smallFont = new PDType1Font(Standard14Fonts.FontName.HELVETICA_OBLIQUE);

            // Set margins
            float margin = 50;
            float yPosition = 750; // Start from top
            float lineHeight = 14;
            float tableY = yPosition;

            // Add title
            contentStream.beginText();
            contentStream.setFont(titleFont, 18);
            contentStream.newLineAtOffset(margin, yPosition);
            contentStream.showText("SALES RECEIPT");
            contentStream.endText();
            yPosition -= 30;

            // Add receipt details
            contentStream.beginText();
            contentStream.setFont(headerFont, 10);
            contentStream.newLineAtOffset(margin, yPosition);
            contentStream.showText("Receipt #: " + sale.getSaleId());
            contentStream.endText();
            yPosition -= lineHeight;

            contentStream.beginText();
            contentStream.setFont(normalFont, 10);
            contentStream.newLineAtOffset(margin, yPosition);
            contentStream.showText("Date: " + new SimpleDateFormat("dd-MMM-yyyy HH:mm:ss").format(sale.getSaleDate()));
            contentStream.endText();
            yPosition -= lineHeight;

            contentStream.beginText();
            contentStream.setFont(normalFont, 10);
            contentStream.newLineAtOffset(margin, yPosition);
            contentStream.showText("Customer ID: " + sale.getCustomerId());
            contentStream.endText();
            yPosition -= lineHeight;

            contentStream.beginText();
            contentStream.setFont(normalFont, 10);
            contentStream.newLineAtOffset(margin, yPosition);
            contentStream.showText("Payment Method: " + sale.getPaymentMethod());
            contentStream.endText();
            yPosition -= 20;

            // Draw a line
            contentStream.moveTo(margin, yPosition);
            contentStream.lineTo(margin + 500, yPosition);
            contentStream.stroke();
            yPosition -= 20;

            // Table header
            contentStream.beginText();
            contentStream.setFont(headerFont, 10);
            contentStream.newLineAtOffset(margin, yPosition);
            contentStream.showText("No.");
            contentStream.endText();

            contentStream.beginText();
            contentStream.setFont(headerFont, 10);
            contentStream.newLineAtOffset(margin + 30, yPosition);
            contentStream.showText("Description");
            contentStream.endText();

            contentStream.beginText();
            contentStream.setFont(headerFont, 10);
            contentStream.newLineAtOffset(margin + 200, yPosition);
            contentStream.showText("Qty");
            contentStream.endText();

            contentStream.beginText();
            contentStream.setFont(headerFont, 10);
            contentStream.newLineAtOffset(margin + 250, yPosition);
            contentStream.showText("Price");
            contentStream.endText();

            contentStream.beginText();
            contentStream.setFont(headerFont, 10);
            contentStream.newLineAtOffset(margin + 320, yPosition);
            contentStream.showText("Amount");
            contentStream.endText();

            yPosition -= lineHeight;

            // Draw header line
            contentStream.moveTo(margin, yPosition);
            contentStream.lineTo(margin + 500, yPosition);
            contentStream.stroke();
            yPosition -= 10;

            // Add sale items
            int itemNo = 1;
            double subtotal = 0;

            if (sale.getSaleDetails() != null) {
                for (SaleDetail detail : sale.getSaleDetails()) {
                    contentStream.beginText();
                    contentStream.setFont(normalFont, 9);
                    contentStream.newLineAtOffset(margin, yPosition);
                    contentStream.showText(String.valueOf(itemNo++));
                    contentStream.endText();

                    contentStream.beginText();
                    contentStream.setFont(normalFont, 9);
                    contentStream.newLineAtOffset(margin + 30, yPosition);
                    contentStream.showText(detail.getProductName());
                    contentStream.endText();

                    contentStream.beginText();
                    contentStream.setFont(normalFont, 9);
                    contentStream.newLineAtOffset(margin + 200, yPosition);
                    contentStream.showText(String.valueOf(detail.getQuantity()));
                    contentStream.endText();

                    contentStream.beginText();
                    contentStream.setFont(normalFont, 9);
                    contentStream.newLineAtOffset(margin + 250, yPosition);
                    contentStream.showText(String.format("$%.2f", detail.getUnitPrice()));
                    contentStream.endText();

                    contentStream.beginText();
                    contentStream.setFont(normalFont, 9);
                    contentStream.newLineAtOffset(margin + 320, yPosition);
                    contentStream.showText(String.format("$%.2f", detail.getTotalPrice()));
                    contentStream.endText();

                    subtotal += detail.getTotalPrice();
                    yPosition -= lineHeight;

                    // Break if page is full
                    if (yPosition < 100) {
                        contentStream.close();
                        PDPage newPage = new PDPage(PDRectangle.A4);
                        document.addPage(newPage);
                        contentStream = new PDPageContentStream(document, newPage);
                        yPosition = 750;
                    }
                }
            }

            yPosition -= 10;
            // Draw line
            contentStream.moveTo(margin, yPosition);
            contentStream.lineTo(margin + 500, yPosition);
            contentStream.stroke();
            yPosition -= 20;

            // Add totals
            contentStream.beginText();
            contentStream.setFont(headerFont, 10);
            contentStream.newLineAtOffset(margin + 250, yPosition);
            contentStream.showText("Subtotal:");
            contentStream.endText();

            contentStream.beginText();
            contentStream.setFont(normalFont, 10);
            contentStream.newLineAtOffset(margin + 320, yPosition);
            contentStream.showText(String.format("$%.2f", subtotal));
            contentStream.endText();
            yPosition -= lineHeight;

            if (sale.getDiscount() > 0) {
                contentStream.beginText();
                contentStream.setFont(headerFont, 10);
                contentStream.newLineAtOffset(margin + 250, yPosition);
                contentStream.showText("Discount:");
                contentStream.endText();

                contentStream.beginText();
                contentStream.setFont(normalFont, 10);
                contentStream.newLineAtOffset(margin + 320, yPosition);
                contentStream.showText(String.format("-$%.2f", sale.getDiscount()));
                contentStream.endText();
                yPosition -= lineHeight;
            }

            yPosition -= 10;
            contentStream.moveTo(margin + 240, yPosition);
            contentStream.lineTo(margin + 400, yPosition);
            contentStream.stroke();
            yPosition -= 20;

            contentStream.beginText();
            contentStream.setFont(titleFont, 12);
            contentStream.newLineAtOffset(margin + 250, yPosition);
            contentStream.showText("TOTAL:");
            contentStream.endText();

            contentStream.beginText();
            contentStream.setFont(titleFont, 12);
            contentStream.newLineAtOffset(margin + 320, yPosition);
            contentStream.showText(String.format("$%.2f", sale.getFinalAmount()));
            contentStream.endText();
            yPosition -= 30;

            // Add footer
            contentStream.beginText();
            contentStream.setFont(smallFont, 8);
            contentStream.newLineAtOffset(margin, yPosition);
            contentStream.showText("Thank you for your business!");
            contentStream.endText();
            yPosition -= lineHeight;

            contentStream.beginText();
            contentStream.setFont(smallFont, 8);
            contentStream.newLineAtOffset(margin, yPosition);
            contentStream.showText("This is a computer-generated receipt. No signature required.");
            contentStream.endText();
            yPosition -= lineHeight;

            contentStream.beginText();
            contentStream.setFont(smallFont, 8);
            contentStream.newLineAtOffset(margin, yPosition);
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