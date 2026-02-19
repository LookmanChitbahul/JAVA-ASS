package ui;

import services.SettingsService;
import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Map;

public class SettingsUI extends JPanel {
    private final SettingsService settingsService;

    private ModernToggle themeToggle;
    private ModernToggle notificationToggle;
    private JComboBox<String> reportFrequency;
    private JTextField emailSettings;

    public SettingsUI() {
        this.settingsService = new SettingsService();
        applyTheme();
    }

    private void applyTheme() {
        removeAll();
        setLayout(new BorderLayout());
        setBackground(AppTheme.getBgColor());
        setBorder(new EmptyBorder(30, 40, 30, 40));

        // Header
        add(createHeader(), BorderLayout.NORTH);

        // Content
        JPanel contentGrid = new JPanel(new GridLayout(1, 2, 30, 0));
        contentGrid.setOpaque(false);

        contentGrid.add(createPreferencesCard());
        contentGrid.add(createSystemCard());

        add(contentGrid, BorderLayout.CENTER);

        // Footer Action
        add(createActionFooter(), BorderLayout.SOUTH);

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

        gbc.gridy = 0;
        form.add(createSettingRow("Appearance", "Toggle between light and dark themes", themeToggle), gbc);
        gbc.gridy = 1;
        form.add(createSettingRow("Notifications", "Receive real-time alerts on sales", notificationToggle), gbc);

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

        JButton btnBackup = new JButton("Run Database Backup");
        styleButton(btnBackup, AppTheme.getPrimaryColor());
        btnBackup
                .addActionListener(e -> JOptionPane.showMessageDialog(this, "Database backup initiated successfully!"));

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
        reportFrequency.setSelectedItem(settings.getOrDefault("report_frequency", "Daily"));
        emailSettings.setText(settings.getOrDefault("admin_email", "admin@store.com"));
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
