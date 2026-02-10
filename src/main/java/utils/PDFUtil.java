package utils;

// Import model classes that contain sale and item details
import models.Sale;
import models.SaleDetail;

// Apache PDFBox imports for creating and writing PDF files
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;

// Java utility imports
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.List;

public class PDFUtil {

    /**
     * Generates a PDF receipt for a given Sale object
     * Sale containing all sale information
     * Path where the PDF will be saved
     * return true if PDF generation is successful, else false
     */
    public static boolean generateReceipt(Sale sale, String filePath) {

        // PDDocument represents the entire PDF document
        PDDocument document = null;

        try {
            // Create a new empty PDF document
            document = new PDDocument();

            // Create a narrow page to look like a receipt (300 x 600 points)
            PDPage page = new PDPage(new PDRectangle(300, 600));
            document.addPage(page);

            // Content stream is used to write text and graphics onto the page
            PDPageContentStream contentStream =
                    new PDPageContentStream(document, page);

            // Define fonts used in different sections of the receipt
            PDType1Font titleFont =
                    new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
            PDType1Font headerFont =
                    new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
            PDType1Font normalFont =
                    new PDType1Font(Standard14Fonts.FontName.HELVETICA);
            PDType1Font smallFont =
                    new PDType1Font(Standard14Fonts.FontName.HELVETICA_OBLIQUE);
            PDType1Font boldFont =
                    new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);

            // Layout variables
            float margin = 15;          // Left and right margin
            float yPosition = 580;      // Vertical starting point (top of page)
            float lineHeight = 12;      // Space between lines
            float centerX = 150;        // Horizontal center of the page

            // STORE HEADER
            // Store name (centered and bold)
            contentStream.beginText();
            contentStream.setFont(titleFont, 16);
            contentStream.newLineAtOffset(centerX - 60, yPosition);
            contentStream.showText("SUPERMART STORE");
            contentStream.endText();
            yPosition -= 20;

            // Store address
            contentStream.beginText();
            contentStream.setFont(normalFont, 10);
            contentStream.newLineAtOffset(centerX - 70, yPosition);
            contentStream.showText("123 Main Street, City");
            contentStream.endText();
            yPosition -= 15;

            // Store contact number
            contentStream.beginText();
            contentStream.setFont(normalFont, 10);
            contentStream.newLineAtOffset(centerX - 60, yPosition);
            contentStream.showText("Tel: (123) 456-7890");
            contentStream.endText();
            yPosition -= 20;

            // Draw a horizontal separator line
            contentStream.moveTo(margin, yPosition);
            contentStream.lineTo(300 - margin, yPosition);
            contentStream.stroke();
            yPosition -= 20;

            //RECEIPT HEADER

            contentStream.beginText();
            contentStream.setFont(headerFont, 12);
            contentStream.newLineAtOffset(centerX - 40, yPosition);
            contentStream.showText("SALES RECEIPT");
            contentStream.endText();
            yPosition -= 20;

            // Column positions for receipt details
            float leftCol = margin;
            float rightCol = 150;

            // Receipt number
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

            // Sale date
            contentStream.beginText();
            contentStream.setFont(boldFont, 10);
            contentStream.newLineAtOffset(leftCol, yPosition);
            contentStream.showText("Date:");
            contentStream.endText();

            contentStream.beginText();
            contentStream.setFont(normalFont, 10);
            contentStream.newLineAtOffset(rightCol, yPosition);
            contentStream.showText(
                    new SimpleDateFormat("dd/MM/yyyy")
                            .format(sale.getSaleDate()));
            contentStream.endText();
            yPosition -= lineHeight;

            // Sale time
            contentStream.beginText();
            contentStream.setFont(boldFont, 10);
            contentStream.newLineAtOffset(leftCol, yPosition);
            contentStream.showText("Time:");
            contentStream.endText();

            contentStream.beginText();
            contentStream.setFont(normalFont, 10);
            contentStream.newLineAtOffset(rightCol, yPosition);
            contentStream.showText(
                    new SimpleDateFormat("HH:mm:ss")
                            .format(sale.getSaleDate()));
            contentStream.endText();
            yPosition -= lineHeight;

            // Customer ID
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

            //ITEMS TABLE
            // Separator before table
            contentStream.moveTo(margin, yPosition);
            contentStream.lineTo(300 - margin, yPosition);
            contentStream.stroke();
            yPosition -= 15;

            // Table column headers
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

            // Draw line under table header
            contentStream.moveTo(margin, yPosition);
            contentStream.lineTo(300 - margin, yPosition);
            contentStream.stroke();
            yPosition -= 10;

            //SALE ITEMS

            double subtotal = 0;

            // Loop through all sale items
            if (sale.getSaleDetails() != null) {
                for (SaleDetail detail : sale.getSaleDetails()) {

                    // If page space is low, create a new page
                    if (yPosition < 150) {
                        contentStream.close();
                        PDPage newPage =
                                new PDPage(new PDRectangle(300, 600));
                        document.addPage(newPage);
                        contentStream =
                                new PDPageContentStream(document, newPage);
                        yPosition = 580;
                    }

                    // Item quantity
                    contentStream.beginText();
                    contentStream.setFont(normalFont, 9);
                    contentStream.newLineAtOffset(margin, yPosition);
                    contentStream.showText(
                            String.valueOf(detail.getQuantity()));
                    contentStream.endText();

                    // Product name (limited length to fit receipt)
                    String productName = detail.getProductName();
                    if (productName.length() > 20) {
                        productName = productName.substring(0, 20) + "...";
                    }

                    contentStream.beginText();
                    contentStream.setFont(normalFont, 9);
                    contentStream.newLineAtOffset(margin + 40, yPosition);
                    contentStream.showText(productName);
                    contentStream.endText();

                    // Unit price
                    contentStream.beginText();
                    contentStream.setFont(normalFont, 9);
                    contentStream.newLineAtOffset(margin + 180, yPosition);
                    contentStream.showText(
                            String.format("$%.2f",
                                    detail.getUnitPrice()));
                    contentStream.endText();
                    yPosition -= lineHeight;

                    // Quantity × price calculation
                    contentStream.beginText();
                    contentStream.setFont(normalFont, 8);
                    contentStream.newLineAtOffset(margin + 160, yPosition);
                    contentStream.showText(
                            "x" + detail.getQuantity() + " =");
                    contentStream.endText();

                    // Total price for the item
                    contentStream.beginText();
                    contentStream.setFont(boldFont, 9);
                    contentStream.newLineAtOffset(margin + 200, yPosition);
                    contentStream.showText(
                            String.format("$%.2f",
                                    detail.getTotalPrice()));
                    contentStream.endText();
                    yPosition -= lineHeight + 5;

                    // Accumulate subtotal
                    subtotal += detail.getTotalPrice();
                }
            }

            //TOTALS

            // If space is insufficient, add new page
            if (yPosition < 200) {
                contentStream.close();
                PDPage newPage =
                        new PDPage(new PDRectangle(300, 600));
                document.addPage(newPage);
                contentStream =
                        new PDPageContentStream(document, newPage);
                yPosition = 580;
            }

            // Separator before totals
            contentStream.moveTo(margin, yPosition);
            contentStream.lineTo(300 - margin, yPosition);
            contentStream.stroke();
            yPosition -= 15;

            float totalsX = 180;

            // Subtotal display
            contentStream.beginText();
            contentStream.setFont(boldFont, 10);
            contentStream.newLineAtOffset(totalsX, yPosition);
            contentStream.showText("Subtotal:");
            contentStream.endText();

            contentStream.beginText();
            contentStream.setFont(normalFont, 10);
            contentStream.newLineAtOffset(totalsX + 70, yPosition);
            contentStream.showText(
                    String.format("$%.2f", subtotal));
            contentStream.endText();
            yPosition -= lineHeight;

            // Tax calculation (10%)
            double tax = subtotal * 0.10;

            contentStream.beginText();
            contentStream.setFont(boldFont, 10);
            contentStream.newLineAtOffset(totalsX, yPosition);
            contentStream.showText("Tax (10%):");
            contentStream.endText();

            contentStream.beginText();
            contentStream.setFont(normalFont, 10);
            contentStream.newLineAtOffset(totalsX + 70, yPosition);
            contentStream.showText(
                    String.format("$%.2f", tax));
            contentStream.endText();
            yPosition -= lineHeight;

            // Final total
            double total = subtotal + tax;

            contentStream.beginText();
            contentStream.setFont(headerFont, 11);
            contentStream.newLineAtOffset(totalsX, yPosition);
            contentStream.showText("TOTAL:");
            contentStream.endText();

            contentStream.beginText();
            contentStream.setFont(headerFont, 11);
            contentStream.newLineAtOffset(totalsX + 70, yPosition);
            contentStream.showText(
                    String.format("$%.2f", total));
            contentStream.endText();
            yPosition -= 20;

            //CASH PAYMENT DETAILS

            if ("Cash".equalsIgnoreCase(sale.getPaymentMethod())
                    && sale.getCashReceived() != null) {

                // Separator
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
                contentStream.showText(
                        String.format("$%.2f",
                                sale.getCashReceived()));
                contentStream.endText();
                yPosition -= lineHeight;

                // Change returned
                contentStream.beginText();
                contentStream.setFont(boldFont, 10);
                contentStream.newLineAtOffset(totalsX, yPosition);
                contentStream.showText("Change:");
                contentStream.endText();

                contentStream.beginText();
                contentStream.setFont(normalFont, 10);
                contentStream.newLineAtOffset(totalsX + 70, yPosition);
                contentStream.showText(
                        String.format("$%.2f",
                                sale.getChangeGiven()));
                contentStream.endText();
                yPosition -= 20;
            }

            //FOOTER PARt
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

            // Receipt note
            contentStream.beginText();
            contentStream.setFont(smallFont, 8);
            contentStream.newLineAtOffset(centerX - 60, yPosition);
            contentStream.showText("Please retain this receipt");
            contentStream.endText();
            yPosition -= 12;

            // Exchange policy
            contentStream.beginText();
            contentStream.setFont(smallFont, 8);
            contentStream.newLineAtOffset(centerX - 100, yPosition);
            contentStream.showText(
                    "Items can be exchanged within 7 days with receipt");
            contentStream.endText();
            yPosition -= 12;

            // User who generated the receipt
            contentStream.beginText();
            contentStream.setFont(smallFont, 7);
            contentStream.newLineAtOffset(centerX - 90, yPosition);
            contentStream.showText(
                    "Generated by: " + sale.getCreatedBy());
            contentStream.endText();

            // Close the content stream
            contentStream.close();

            // Save and close the PDF document
            document.save(new File(filePath));
            document.close();

            System.out.println("✓ PDF receipt generated: " + filePath);
            return true;

        } catch (IOException e) {
            System.err.println("✗ Error generating PDF: " + e.getMessage());
            e.printStackTrace();

            // Ensure document is closed even if an error occurs
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
