package ui;

import services.AnalyticsService;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.sql.SQLException;
import java.util.Map;
import java.util.LinkedHashMap;
import java.io.File;
import java.io.ByteArrayOutputStream;
import javax.imageio.ImageIO;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;

public class AnalyticsUI extends JPanel {
    // Dynamic Theme Colors
    private final Color DARK_BG = AppTheme.getBgColor();
    private final Color CARD_BG = AppTheme.getCardColor();
    private final Color PRIMARY_COLOR = AppTheme.getPrimaryColor();
    private final Color SUCCESS_COLOR = AppTheme.getSuccessColor();
    private final Color TEXT_PRIMARY = AppTheme.getTextColor();
    private final Color TEXT_SECONDARY = AppTheme.getSubTextColor();

    private final AnalyticsService analyticsService;
    private final JPanel chartsGrid;
    private JComboBox<String> timeFilter;
    private JComboBox<String> categoryFilter;

    public AnalyticsUI() {
        this.analyticsService = new AnalyticsService();
        setLayout(new BorderLayout());
        setBackground(DARK_BG);

        // Header
        add(createHeader(), BorderLayout.NORTH);

        // Main Content
        chartsGrid = new JPanel(new GridLayout(2, 2, 20, 20));
        chartsGrid.setBackground(DARK_BG);
        chartsGrid.setBorder(new EmptyBorder(20, 20, 20, 20));

        loadData();

        add(new JScrollPane(chartsGrid), BorderLayout.CENTER);
    }

    private JPanel createHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setPreferredSize(new Dimension(0, 140));
        header.setBorder(new EmptyBorder(20, 40, 0, 40));

        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setOpaque(false);
        JLabel title = new JLabel("Analytics & Reports");
        title.setFont(new Font("Segoe UI", Font.BOLD, 28));
        title.setForeground(TEXT_PRIMARY);
        JLabel subtitle = new JLabel("Synchronized Dynamic Dashboard");
        subtitle.setForeground(TEXT_SECONDARY);
        titlePanel.add(title, BorderLayout.NORTH);
        titlePanel.add(subtitle, BorderLayout.SOUTH);

        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actionPanel.setOpaque(false);
        JButton btnExport = createStyledButton("Export PDF", PRIMARY_COLOR);
        btnExport.addActionListener(e -> exportToPDF());
        JButton btnRefresh = createStyledButton("Refresh", SUCCESS_COLOR);
        btnRefresh.addActionListener(e -> {
            System.out.println("Analytics manually refreshed...");
            loadData();
            JOptionPane.showMessageDialog(this, "Data Refreshed Successfully!");
        });
        actionPanel.add(btnExport);
        actionPanel.add(btnRefresh);

        JPanel filterBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 10));
        filterBar.setOpaque(false);
        filterBar.add(createLabel("Time Period:"));
        timeFilter = createStyledComboBox(new String[] { "Today", "Last 7 Days", "This Month", "This Year" });
        filterBar.add(timeFilter);
        filterBar.add(createLabel("Category:"));
        categoryFilter = createStyledComboBox(
                new String[] { "All Categories", "Electronics", "Clothing", "Food", "Beverages", "Stationery" });
        filterBar.add(categoryFilter);

        // Add Listeners for instant filtering
        ActionListener filterListener = e -> loadData();
        timeFilter.addActionListener(filterListener);
        categoryFilter.addActionListener(filterListener);

        header.add(titlePanel, BorderLayout.WEST);
        header.add(actionPanel, BorderLayout.EAST);
        header.add(filterBar, BorderLayout.SOUTH);
        return header;
    }

    private JLabel createLabel(String text) {
        JLabel l = new JLabel(text);
        l.setForeground(TEXT_SECONDARY);
        l.setFont(new Font("Segoe UI", Font.BOLD, 12));
        return l;
    }

    private JComboBox<String> createStyledComboBox(String[] items) {
        JComboBox<String> combo = new JComboBox<>(items);
        combo.setBackground(AppTheme.getCardColor());
        combo.setForeground(AppTheme.getTextColor());
        combo.setPreferredSize(new Dimension(150, 30));
        return combo;
    }

    public void refreshData() {
        loadData();
    }

    private synchronized void loadData() {
        if (timeFilter == null || categoryFilter == null)
            return;
        String period = (String) timeFilter.getSelectedItem();
        String cat = (String) categoryFilter.getSelectedItem();

        chartsGrid.removeAll();
        try {
            // 1. Sales Trend
            Map<String, Double> salesTrend = analyticsService.getSalesTrend(period, cat);
            chartsGrid.add(createChartCard("Sales Trend (" + period + ")", new LineChart(salesTrend)));

            // 2. Top Products
            Map<String, Integer> topProducts = analyticsService.getTopProducts(period, cat);
            Map<String, Double> topProductData = new LinkedHashMap<>();
            topProducts.forEach((k, v) -> topProductData.put(k, v.doubleValue()));
            chartsGrid.add(createChartCard("Top Products (" + period + ")", new BarChart(topProductData)));

            // 3. Revenue Breakdown (Pie Chart) - Shows Products within category if filtered
            Map<String, Double> revenueDistribution = analyticsService.getRevenueDistribution(period, cat);
            String pieTitle = (cat.equals("All Categories")) ? "Revenue by Category" : "Revenue Breakdown: " + cat;
            chartsGrid.add(createChartCard(pieTitle, new PieChart(revenueDistribution)));

            // 4. Summary Stats
            double total = revenueDistribution.values().stream().mapToDouble(Double::doubleValue).sum();
            chartsGrid.add(createChartCard("Performance Summary", new SummaryPanel(total, topProducts.size())));

        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
        }
        chartsGrid.revalidate();
        chartsGrid.repaint();
    }

    private JPanel createChartCard(String title, JPanel chart) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(CARD_BG);
        card.setBorder(new EmptyBorder(15, 15, 15, 15));
        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblTitle.setForeground(TEXT_PRIMARY);
        lblTitle.setBorder(new EmptyBorder(0, 0, 10, 0));
        card.add(lblTitle, BorderLayout.NORTH);
        card.add(chart, BorderLayout.CENTER);
        return card;
    }

    private JButton createStyledButton(String text, Color bg) {
        JButton btn = new JButton(text);
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(120, 35));
        return btn;
    }

    private void exportToPDF() {
        String period = (String) timeFilter.getSelectedItem();
        String cat = (String) categoryFilter.getSelectedItem();

        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage();
            document.addPage(page);

            try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
                // Title
                contentStream.beginText();
                contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 24);
                contentStream.newLineAtOffset(50, 750);
                contentStream.showText("Smart Retail Analytics Report");
                contentStream.endText();

                // Filters Info
                contentStream.beginText();
                contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 12);
                contentStream.newLineAtOffset(50, 720);
                contentStream.showText("Report Filtering: Period [" + period + "] | Category [" + cat + "]");
                contentStream.newLineAtOffset(0, -20);
                contentStream.showText("Generated on: " + new java.util.Date().toString());
                contentStream.endText();

                // Capture and Add Charts as Images
                int yOffset = 450;
                Component[] cards = chartsGrid.getComponents();
                for (int i = 0; i < Math.min(cards.length, 2); i++) { // Top 2 charts on page 1
                    if (cards[i] instanceof JPanel) {
                        BufferedImage img = captureComponent(cards[i]);
                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        ImageIO.write(img, "PNG", baos);
                        PDImageXObject pdImage = PDImageXObject.createFromByteArray(document, baos.toByteArray(),
                                "chart" + i);

                        // Draw image
                        float scale = 0.5f;
                        contentStream.drawImage(pdImage, 50, yOffset, img.getWidth() * scale, img.getHeight() * scale);
                        yOffset -= 220;
                    }
                }
            }

            // Page 2 for remaining charts if needed
            if (chartsGrid.getComponentCount() > 2) {
                PDPage page2 = new PDPage();
                document.addPage(page2);
                try (PDPageContentStream contentStream2 = new PDPageContentStream(document, page2)) {
                    int yOffset = 500;
                    Component[] cards = chartsGrid.getComponents();
                    for (int i = 2; i < Math.min(cards.length, 4); i++) {
                        BufferedImage img = captureComponent(cards[i]);
                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        ImageIO.write(img, "PNG", baos);
                        PDImageXObject pdImage = PDImageXObject.createFromByteArray(document, baos.toByteArray(),
                                "chart" + i);

                        float scale = 0.5f;
                        contentStream2.drawImage(pdImage, 50, yOffset, img.getWidth() * scale, img.getHeight() * scale);
                        yOffset -= 220;
                    }
                }
            }

            File file = new File("Analytics_Report.pdf");
            document.save(file);
            JOptionPane.showMessageDialog(this, "Success: PDF Report with charts saved to " + file.getAbsolutePath());
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error generating PDF: " + e.getMessage());
        }
    }

    private BufferedImage captureComponent(Component comp) {
        BufferedImage img = new BufferedImage(comp.getWidth(), comp.getHeight(), BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = img.createGraphics();
        comp.paint(g2);
        g2.dispose();
        return img;
    }

    // --- Chart Components ---

    class SummaryPanel extends JPanel {
        SummaryPanel(double total, int count) {
            setOpaque(false);
            setLayout(new GridLayout(2, 1));
            JLabel lblTotal = new JLabel("Total Revenue: $" + String.format("%.2f", total));
            lblTotal.setForeground(SUCCESS_COLOR);
            lblTotal.setFont(new Font("Segoe UI", Font.BOLD, 20));
            lblTotal.setHorizontalAlignment(SwingConstants.CENTER);

            JLabel lblCount = new JLabel("Products Sold: " + count);
            lblCount.setForeground(TEXT_PRIMARY);
            lblCount.setFont(new Font("Segoe UI", Font.PLAIN, 18));
            lblCount.setHorizontalAlignment(SwingConstants.CENTER);

            add(lblTotal);
            add(lblCount);
        }
    }

    class LineChart extends JPanel {
        private final Map<String, Double> data;

        LineChart(Map<String, Double> data) {
            this.data = data;
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (data == null || data.isEmpty())
                return;
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int w = getWidth(), h = getHeight(), margin = 40;
            double max = data.values().stream().max(Double::compare).orElse(1.0);
            int xStep = (w - 2 * margin) / Math.max(1, data.size() - 1);
            int x = margin, prevX = -1, prevY = -1;
            g2.setColor(PRIMARY_COLOR);
            g2.setStroke(new BasicStroke(3f));
            for (Double val : data.values()) {
                int y = h - margin - (int) ((val / max) * (h - 2 * margin));
                if (prevX != -1)
                    g2.drawLine(prevX, prevY, x, y);
                g2.fillOval(x - 4, y - 4, 8, 8);
                prevX = x;
                prevY = y;
                x += xStep;
            }
            g2.dispose();
        }
    }

    class BarChart extends JPanel {
        private final Map<String, Double> data;

        BarChart(Map<String, Double> data) {
            this.data = data;
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (data == null || data.isEmpty())
                return;
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int w = getWidth(), h = getHeight(), margin = 40;
            double max = data.values().stream().max(Double::compare).orElse(1.0);
            int barWidth = Math.max(10, (w - 2 * margin) / data.size() - 10);
            int x = margin + 5;
            for (Map.Entry<String, Double> entry : data.entrySet()) {
                int barHeight = (int) ((entry.getValue() / max) * (h - 2 * margin));
                g2.setColor(SUCCESS_COLOR);
                g2.fillRoundRect(x, h - margin - barHeight, barWidth, barHeight, 8, 8);
                g2.setColor(TEXT_SECONDARY);
                g2.setFont(new Font("Segoe UI", Font.PLAIN, 10));
                String label = entry.getKey();
                g2.drawString(label.length() > 8 ? label.substring(0, 6) + ".." : label, x, h - margin + 15);
                x += barWidth + 10;
            }
            g2.dispose();
        }
    }

    class PieChart extends JPanel {
        private final Map<String, Double> data;

        PieChart(Map<String, Double> data) {
            this.data = data;
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (data == null || data.isEmpty())
                return;
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            double total = data.values().stream().mapToDouble(Double::doubleValue).sum();
            int w = getWidth(), h = getHeight(), size = Math.min(w, h) - 60;
            int x = (w - size) / 2, y = (h - size) / 2, startAngle = 0;
            Color[] colors = { 
                AppTheme.getPrimaryColor(), 
                AppTheme.getSuccessColor(), 
                AppTheme.getInfoColor(), 
                AppTheme.getWarningColor(), 
                AppTheme.getDangerColor() 
            };
            int i = 0;
            for (Double val : data.values()) {
                int arcAngle = (int) Math.round(val * 360 / Math.max(total, 1));
                g2.setColor(colors[i % colors.length]);
                g2.fillArc(x, y, size, size, startAngle, arcAngle);
                startAngle += arcAngle;
                i++;
            }
            g2.dispose();
        }
    }

    class PlaceholderChart extends JPanel {
        PlaceholderChart() {
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            g.setColor(AppTheme.getSubTextColor());
            g.drawString("Synchronization Active", getWidth() / 2 - 60, getHeight() / 2);
        }
    }
}
