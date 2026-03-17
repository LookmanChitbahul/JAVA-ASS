package ui;

import services.SettingsService;
import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Map;
import java.sql.*;
import java.io.File;
import java.io.IOException;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import database.DBConnection;

public class SettingsUI extends JPanel {
    private final SettingsService settingsService;

    private ModernToggle themeToggle;
    private ModernToggle notificationToggle;
    private ModernToggle accessibilityToggle;
    private JComboBox<String> reportFrequency;
    private JTextField emailSettings;

    public SettingsUI() {
        this.settingsService = new SettingsService();
        applyTheme();
    }

    // Custom panel for background image
    private class BackgroundPanel extends JPanel {
        private Image backgroundImage;

        public BackgroundPanel() {
            try {
                // Try classpath first
                java.net.URL imgUrl = getClass().getResource("/images/background_settings.jpg");
                if (imgUrl != null) {
                    backgroundImage = new ImageIcon(imgUrl).getImage();
                } else {
                    // Fallback to file path
                    backgroundImage = new ImageIcon("src/main/resources/images/background_settings.jpg").getImage();
                }
            } catch (Exception e) {
                System.err.println("Could not load background image: " + e.getMessage());
            }
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (backgroundImage != null) {
                g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
                // Add a semi-transparent overlay to keep text readable
                g.setColor(new Color(0, 0, 0, AppTheme.isDarkMode() ? 120 : 40));
                g.fillRect(0, 0, getWidth(), getHeight());
            }
        }
    }

    private void applyTheme() {
        removeAll();
        setLayout(new BorderLayout());
        
        BackgroundPanel background = new BackgroundPanel();
        background.setLayout(new BorderLayout());
        background.setBorder(new EmptyBorder(30, 40, 30, 40));
        background.setOpaque(true);
        background.setBackground(AppTheme.getBgColor());

        // Header
        background.add(createHeader(), BorderLayout.NORTH);

        // Content
        JPanel contentGrid = new JPanel(new GridLayout(1, 2, 30, 0));
        contentGrid.setOpaque(false);
        contentGrid.add(createPreferencesCard());
        contentGrid.add(createSystemCard());

        background.add(contentGrid, BorderLayout.CENTER);

        // Footer Action
        background.add(createActionFooter(), BorderLayout.SOUTH);
        
        add(background, BorderLayout.CENTER);

        loadCurrentSettings();
        revalidate();
        repaint();
    }

    private JPanel createHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setBorder(new EmptyBorder(0, 0, 30, 0));

        JLabel title = new JLabel("Settings & Configuration");
        title.setFont(new Font("Segoe UI", Font.BOLD, 28));
        title.setForeground(AppTheme.getTextColor());

        JLabel subtitle = new JLabel("Manage your preferences and system parameters");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        subtitle.setForeground(AppTheme.getSubTextColor());

        JPanel textPanel = new JPanel();
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
        textPanel.setOpaque(false);
        textPanel.add(title);
        textPanel.add(Box.createVerticalStrut(5));
        textPanel.add(subtitle);

        header.add(textPanel, BorderLayout.WEST);
        return header;
    }

    private JPanel createPreferencesCard() {
        JPanel card = createBaseCard("User Preferences");
        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(10, 0, 10, 0);
        gbc.weightx = 1.0;

        themeToggle = new ModernToggle("Dark Mode");
        notificationToggle = new ModernToggle("Push Notifications");
        accessibilityToggle = new ModernToggle("Color Blind Mode");

        gbc.gridy = 0;
        form.add(createSettingRow("Appearance", "Toggle between light and dark themes", themeToggle), gbc);
        gbc.gridy = 1;
        form.add(createSettingRow("Notifications", "Receive real-time alerts on sales", notificationToggle), gbc);
        gbc.gridy = 2;
        form.add(createSettingRow("Accessibility", "Enable high-contrast color blind palette", accessibilityToggle), gbc);

        card.add(form, BorderLayout.CENTER);
        return card;
    }

    private JPanel createSystemCard() {
        JPanel card = createBaseCard("System Settings");
        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(10, 0, 10, 0);
        gbc.weightx = 1.0;

        reportFrequency = new JComboBox<>(new String[] { "Daily", "Weekly", "Monthly" });
        styleComboBox(reportFrequency);

        emailSettings = new JTextField();
        styleTextField(emailSettings);

        JButton btnBackup = new JButton("Export Database to PDF");
        styleButton(btnBackup, AppTheme.getPrimaryColor());
        btnBackup.addActionListener(e -> exportDatabaseToPDF());

        gbc.gridy = 0;
        form.add(createSettingRow("Report Frequency", "Automatic generation interval", reportFrequency), gbc);
        gbc.gridy = 1;
        form.add(createSettingRow("Admin Email", "Primary contact for system alerts", emailSettings), gbc);
        gbc.gridy = 2;
        form.add(createSettingRow("Database", "Manual data redundancy task", btnBackup), gbc);

        card.add(form, BorderLayout.CENTER);
        return card;
    }

    private JPanel createActionFooter() {
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        footer.setOpaque(false);
        footer.setBorder(new EmptyBorder(20, 0, 0, 0));

        JButton btnSave = new JButton("Save Changes");
        styleButton(btnSave, AppTheme.getPrimaryColor());
        btnSave.setPreferredSize(new Dimension(150, 40));
        btnSave.addActionListener(e -> saveSettings());

        footer.add(btnSave);
        return footer;
    }

    private JPanel createBaseCard(String title) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(AppTheme.getCardColor());
        card.setBorder(new CompoundBorder(
                BorderFactory.createLineBorder(AppTheme.getBorderColor(), 1),
                new EmptyBorder(20, 20, 20, 20)));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLabel.setForeground(AppTheme.getTextColor());
        titleLabel.setBorder(new EmptyBorder(0, 0, 20, 0));

        card.add(titleLabel, BorderLayout.NORTH);
        return card;
    }

    private JPanel createSettingRow(String label, String desc, JComponent component) {
        JPanel row = new JPanel(new BorderLayout());
        row.setOpaque(false);

        JPanel left = new JPanel();
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
        left.setOpaque(false);

        JLabel l = new JLabel(label);
        l.setFont(new Font("Segoe UI", Font.BOLD, 14));
        l.setForeground(AppTheme.getTextColor());

        JLabel d = new JLabel(desc);
        d.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        d.setForeground(AppTheme.getSubTextColor());

        left.add(l);
        left.add(d);

        row.add(left, BorderLayout.WEST);
        row.add(component, BorderLayout.EAST);

        return row;
    }

    private void styleComboBox(JComboBox<String> cb) {
        cb.setBackground(AppTheme.getBgColor());
        cb.setForeground(AppTheme.getTextColor());
        cb.setBorder(BorderFactory.createLineBorder(AppTheme.getBorderColor()));
        cb.setFont(new Font("Segoe UI", Font.PLAIN, 13));
    }

    private void styleTextField(JTextField tf) {
        tf.setBackground(AppTheme.getBgColor());
        tf.setForeground(AppTheme.getTextColor());
        tf.setCaretColor(AppTheme.getTextColor());
        tf.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(AppTheme.getBorderColor()),
                new EmptyBorder(5, 10, 5, 10)));
        tf.setPreferredSize(new Dimension(180, 30));
    }

    private void styleButton(JButton btn, Color bg) {
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }

    private void saveSettings() {
        boolean dark = themeToggle.isSelected();
        AppTheme.setDarkMode(dark);
        AppTheme.setColorBlindMode(accessibilityToggle.isSelected());
        
        settingsService.saveSetting("notifications_enabled", String.valueOf(notificationToggle.isSelected()));
        settingsService.saveSetting("report_frequency", (String) reportFrequency.getSelectedItem());
        settingsService.saveSetting("admin_email", emailSettings.getText());

        if (DashboardUI.getInstance() != null) {
            DashboardUI.getInstance().reloadAllTabs();
        } else {
            applyTheme();
        }

        JOptionPane.showMessageDialog(this, "Settings saved successfully!");
    }

    private void loadCurrentSettings() {
        Map<String, String> settings = settingsService.getAllSettings();
        themeToggle.setSelected(Boolean.parseBoolean(settings.getOrDefault("theme_dark", "true")));
        notificationToggle.setSelected(Boolean.parseBoolean(settings.getOrDefault("notifications_enabled", "true")));
        accessibilityToggle.setSelected(AppTheme.isColorBlindMode());
        reportFrequency.setSelectedItem(settings.getOrDefault("report_frequency", "Daily"));
        emailSettings.setText(settings.getOrDefault("admin_email", "admin@store.com"));
    }

    private void exportDatabaseToPDF() {
        try (PDDocument document = new PDDocument()) {
            PDFContext ctx = new PDFContext(document);
            ctx.newPage();

            // Report Header
            ctx.stream.beginText();
            ctx.stream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 22);
            ctx.stream.newLineAtOffset(50, 750);
            ctx.stream.showText("Smart Retail - Database Backup Report");
            ctx.stream.endText();

            ctx.stream.beginText();
            ctx.stream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 10);
            ctx.stream.newLineAtOffset(50, 730);
            ctx.stream.showText("Generated on: " + new java.util.Date().toString());
            ctx.stream.endText();

            ctx.y = 690;

            // Export Tables
            addTableToPDF(ctx, "USERS", 
                new String[]{"ID", "Username", "Email", "Status"}, 
                new String[]{"user_id", "username", "email", "status"});
            
            ctx.y -= 40;
            addTableToPDF(ctx, "PRODUCTS", 
                new String[]{"ID", "Name", "Price", "Stock"}, 
                new String[]{"product_id", "name", "price", "stock"});
            
            ctx.y -= 40;
            addTableToPDF(ctx, "CUSTOMERS", 
                new String[]{"ID", "First Name", "Last Name", "Email"}, 
                new String[]{"customer_id", "first_name", "last_name", "email"});

            ctx.closeStream();

            File file = new File("Database_Backup.pdf");
            document.save(file);
            JOptionPane.showMessageDialog(this, "Professional PDF backup saved to: " + file.getAbsolutePath());
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error generating backup: " + e.getMessage());
        }
    }

    private static class PDFContext {
        PDDocument doc;
        PDPage page;
        PDPageContentStream stream;
        float y;
        int pageNum = 0;

        PDFContext(PDDocument doc) { this.doc = doc; }

        void newPage() throws IOException {
            if (stream != null) {
                drawFooter();
                stream.close();
            }
            page = new PDPage();
            doc.addPage(page);
            stream = new PDPageContentStream(doc, page);
            y = 750;
            pageNum++;
        }

        void closeStream() throws IOException {
            if (stream != null) {
                drawFooter();
                stream.close();
            }
        }

        private void drawFooter() throws IOException {
            stream.beginText();
            stream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_OBLIQUE), 8);
            stream.newLineAtOffset(500, 30);
            stream.showText("Page " + pageNum);
            stream.endText();
        }

        void checkSpace(float needed) throws IOException {
            if (y - needed < 50) {
                newPage();
            }
        }
    }

    private void addTableToPDF(PDFContext ctx, String title, String[] headers, String[] dbCols) throws SQLException, IOException {
        float margin = 50;
        float rowHeight = 20;
        float[] xOffsets = {50, 120, 250, 420}; // Fixed absolute x-coordinates for columns

        ctx.checkSpace(60);

        // Draw Table Title
        ctx.stream.beginText();
        ctx.stream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 14);
        ctx.stream.newLineAtOffset(margin, ctx.y);
        ctx.stream.showText(title + " DATA");
        ctx.stream.endText();
        ctx.y -= 25;

        // Draw Line
        ctx.stream.setLineWidth(1.0f);
        ctx.stream.moveTo(margin, ctx.y + 5);
        ctx.stream.lineTo(550, ctx.y + 5);
        ctx.stream.stroke();

        // Draw Headers
        ctx.stream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 10);
        for (int i = 0; i < headers.length; i++) {
            ctx.stream.beginText();
            ctx.stream.newLineAtOffset(xOffsets[i], ctx.y);
            ctx.stream.showText(headers[i]);
            ctx.stream.endText();
        }
        ctx.y -= rowHeight;

        // Fetch Data
        String sql = "SELECT * FROM " + title.toLowerCase();
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                ctx.checkSpace(rowHeight);
                if (ctx.y > 700) { // New page was triggered
                    ctx.y -= rowHeight; // Adjust for title gap
                }

                ctx.stream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 9);
                for (int j = 0; j < dbCols.length; j++) {
                    ctx.stream.beginText();
                    ctx.stream.newLineAtOffset(xOffsets[j], ctx.y);
                    String val = rs.getString(dbCols[j]);
                    if (val == null) val = "";
                    if (val.length() > 25) val = val.substring(0, 22) + "..";
                    ctx.stream.showText(val);
                    ctx.stream.endText();
                }
                ctx.y -= rowHeight;
            }
        }
        ctx.y -= 20; // Extra gap between tables
    }

    // Modern iOS Style Toggle Switch
    private class ModernToggle extends JComponent {
        private boolean selected = true;
        private Timer timer;
        private float animationPos = 1.0f; // 0 for off, 1 for on

        public ModernToggle(String name) {
            setPreferredSize(new Dimension(50, 26));
            setCursor(new Cursor(Cursor.HAND_CURSOR));

            timer = new Timer(20, e -> {
                if (selected && animationPos < 1.0f)
                    animationPos += 0.1f;
                else if (!selected && animationPos > 0.0f)
                    animationPos -= 0.1f;
                else
                    timer.stop();
                repaint();
            });

            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    selected = !selected;
                    timer.start();
                }
            });
        }

        public boolean isSelected() {
            return selected;
        }

        public void setSelected(boolean b) {
            this.selected = b;
            animationPos = b ? 1.0f : 0.0f;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            // Background track
            Color trackColor = selected ? AppTheme.getPrimaryColor() : Color.decode("#9CA3AF");
            g2.setColor(trackColor);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), getHeight(), getHeight());

            // Knob position
            int knobSize = getHeight() - 6;
            int x = (int) (3 + animationPos * (getWidth() - knobSize - 6));
            g2.setColor(Color.WHITE);
            g2.fillOval(x, 3, knobSize, knobSize);

            g2.dispose();
        }
    }
}
