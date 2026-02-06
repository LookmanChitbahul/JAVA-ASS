package ui;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import services.AuthService;

public class Account extends JPanel {
    private static final Color PRIMARY_COLOR = Color.decode("#000428");
    private static final Color SECONDARY_COLOR = Color.decode("#004e92");
    private static final Color ACCENT_COLOR = new Color(46, 204, 113);
    private static final Color DARK_BG = Color.decode("#111827");
    private static final Color TEXT_COLOR = Color.WHITE;
    private static final Color CARD_BG = new Color(31, 41, 55);
    private static final Color BORDER_COLOR = new Color(55, 65, 81);
    public String currentUsername;

    // UI components for user data
    private JTextField nameField;
    private JTextField emailField;
    private JTextField phoneField;
    private JTextField statusField;
    private JTextField deptField;
    private JTextField memberSinceField;
    private JLabel titleLabel;

    public Account() {
        this("Guest");
    }

    public Account(String username) {
        this.currentUsername = username;
        setBackground(DARK_BG);
        setLayout(new BorderLayout(0, 0));

        // Header with gradient-like effect
        JPanel headerPanel = createHeader();
        add(headerPanel, BorderLayout.NORTH);

        // Main content area
        JPanel contentPanel = createContentPanel();
        add(contentPanel, BorderLayout.CENTER);

        // Load data from database
        loadUserData();
    }

    private void loadUserData() {
        AuthService authService = new AuthService();
        try {
            models.User user = authService.getUserDetails(currentUsername);
            if (user != null) {
                if (nameField != null)
                    nameField.setText(user.getUsername());
                if (emailField != null)
                    emailField.setText(user.getEmail());
                if (phoneField != null)
                    phoneField.setText(user.getPhone());
                if (statusField != null)
                    statusField.setText(user.getStatus());
                if (deptField != null)
                    deptField.setText(user.getDepartment());
                if (memberSinceField != null)
                    memberSinceField.setText(user.getMemberSince());

                if (titleLabel != null) {
                    titleLabel.setText("Account: " + user.getUsername());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading account details: " + e.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private JPanel createHeader() {
        JPanel header = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Gradient background
                GradientPaint gradient = new GradientPaint(0, 0, PRIMARY_COLOR,
                        getWidth(), 0, SECONDARY_COLOR);
                g2d.setPaint(gradient);
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        header.setLayout(new BorderLayout());
        header.setPreferredSize(new Dimension(0, 120));

        // Profile icon area
        JPanel iconArea = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 15));
        iconArea.setOpaque(false);

        JLabel profileIcon = new JLabel("👤");
        profileIcon.setFont(new Font("SansSerif", Font.PLAIN, 48));
        iconArea.add(profileIcon);

        // Title and subtitle
        JPanel textArea = new JPanel();
        textArea.setLayout(new BoxLayout(textArea, BoxLayout.Y_AXIS));
        textArea.setOpaque(false);
        textArea.setBorder(BorderFactory.createEmptyBorder(15, 0, 15, 0));

        titleLabel = new JLabel("Account Settings");
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 28));
        titleLabel.setForeground(Color.WHITE);

        JLabel subtitleLabel = new JLabel("Manage your profile and preferences");
        subtitleLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        subtitleLabel.setForeground(new Color(160, 174, 192));

        textArea.add(titleLabel);
        textArea.add(subtitleLabel);

        header.add(iconArea, BorderLayout.WEST);
        header.add(textArea, BorderLayout.CENTER);

        return header;
    }

    private JPanel createContentPanel() {
        JPanel content = new JPanel();
        content.setBackground(DARK_BG);
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBorder(BorderFactory.createEmptyBorder(20, 30, 30, 30));

        // Profile Information Section
        content.add(createSectionCard("Profile Information", createProfileForm()));
        content.add(Box.createVerticalStrut(20));

        // Action Buttons
        content.add(createActionButtonsPanel());

        content.add(Box.createVerticalGlue());

        // Wrap in scroll pane
        JScrollPane scrollPane = new JScrollPane(content);
        scrollPane.setBorder(null);
        scrollPane.setBackground(DARK_BG);
        scrollPane.getViewport().setBackground(DARK_BG);

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBackground(DARK_BG);
        wrapper.add(scrollPane, BorderLayout.CENTER);

        return wrapper;
    }

    private JPanel createSectionCard(String title, JPanel content) {
        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(CARD_BG);
                g2d.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);

                g2d.setColor(BORDER_COLOR);
                g2d.setStroke(new BasicStroke(1));
                g2d.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);
            }
        };
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setOpaque(false);
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 450));

        // Title
        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        titlePanel.setOpaque(false);
        titlePanel.setBorder(BorderFactory.createEmptyBorder(15, 20, 10, 20));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 16));
        titleLabel.setForeground(Color.WHITE);

        titlePanel.add(titleLabel);
        card.add(titlePanel);

        // Separator
        JSeparator separator = new JSeparator();
        separator.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        separator.setForeground(BORDER_COLOR);
        card.add(separator);

        // Content
        content.setBorder(BorderFactory.createEmptyBorder(10, 20, 15, 20));
        card.add(content);

        return card;
    }

    private JPanel createProfileForm() {
        JPanel form = new JPanel();
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setOpaque(false);

        // Name field
        nameField = new JTextField();
        nameField.setEditable(false);
        form.add(createFormField("Username", nameField));
        form.add(Box.createVerticalStrut(15));

        // Email field
        emailField = new JTextField();
        emailField.setEditable(false);
        form.add(createFormField("Email", emailField));
        form.add(Box.createVerticalStrut(15));

        // Phone field
        phoneField = new JTextField();
        phoneField.setEditable(false);
        form.add(createFormField("Phone Number", phoneField));
        form.add(Box.createVerticalStrut(15));

        // Status field
        statusField = new JTextField();
        statusField.setEditable(false);
        form.add(createFormField("Status", statusField));
        form.add(Box.createVerticalStrut(15));

        // Department field
        deptField = new JTextField();
        deptField.setEditable(false);
        form.add(createFormField("Department", deptField));
        form.add(Box.createVerticalStrut(15));

        // Member Since field
        memberSinceField = new JTextField();
        memberSinceField.setEditable(false);
        form.add(createFormField("Member Since", memberSinceField));

        return form;
    }

    private JPanel createFormField(String label, JTextField inputField) {
        JPanel fieldPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        fieldPanel.setOpaque(false);
        fieldPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));

        // Label
        JLabel labelComponent = new JLabel(label);
        labelComponent.setFont(new Font("SansSerif", Font.PLAIN, 12));
        labelComponent.setForeground(TEXT_COLOR);
        labelComponent.setPreferredSize(new Dimension(150, 25));

        // Style Input field
        inputField.setFont(new Font("SansSerif", Font.PLAIN, 13));
        inputField.setForeground(Color.WHITE);
        inputField.setBackground(new Color(17, 24, 39));
        inputField.setCaretColor(SECONDARY_COLOR);
        inputField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)));
        inputField.setPreferredSize(new Dimension(300, 30));

        fieldPanel.add(labelComponent);
        fieldPanel.add(inputField);

        return fieldPanel;
    }

    private JPanel createActionButtonsPanel() {
        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttonsPanel.setOpaque(false);
        buttonsPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));

        JButton logoutBtn = createStyledButton("Logout", new Color(239, 68, 68));
        JButton cancelBtn = createStyledButton("Cancel", new Color(75, 85, 99));

        cancelBtn.addActionListener(e -> {
            Window window = SwingUtilities.getWindowAncestor(this);
            if (window != null)
                window.dispose();
        });

        logoutBtn.addActionListener(e -> {
            int result = JOptionPane.showConfirmDialog(this, "Are you sure you want to logout?",
                    "Confirm Logout", JOptionPane.YES_NO_OPTION);
            if (result == JOptionPane.YES_OPTION) {
                AuthService authService = new AuthService();
                authService.logoutUser(currentUsername);
                JOptionPane.showMessageDialog(this, "You have been logged out.",
                        "Logout", JOptionPane.INFORMATION_MESSAGE);

                // Close current window
                Window window = SwingUtilities.getWindowAncestor(this);
                if (window != null)
                    window.dispose();

                // Open LoginUI
                SwingUtilities.invokeLater(() -> {
                    try {
                        new LoginUI();
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        JOptionPane.showMessageDialog(null, "Error opening Login: " + ex.getMessage(),
                                "Error", JOptionPane.ERROR_MESSAGE);
                    }
                });
            }
        });

        buttonsPanel.add(logoutBtn);
        buttonsPanel.add(cancelBtn);

        return buttonsPanel;
    }

    private JButton createStyledButton(String text, Color color) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(color);
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 6, 6);

                FontMetrics fm = g2d.getFontMetrics();
                int textX = (getWidth() - fm.stringWidth(getText())) / 2;
                int textY = ((getHeight() - fm.getHeight()) / 2) + fm.getAscent();

                g2d.setColor(getModel().isPressed() ? new Color(0, 0, 0, 50) : new Color(0, 0, 0, 0));
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 6, 6);

                g2d.setColor(Color.WHITE);
                g2d.drawString(getText(), textX, textY);
            }
        };
        btn.setFont(new Font("SansSerif", Font.BOLD, 12));
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setPreferredSize(new Dimension(120, 35));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                btn.setBackground(color.brighter());
            }

            @Override
            public void mouseExited(MouseEvent e) {
                btn.setBackground(color);
            }
        });

        return btn;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Account Profile");
            frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            frame.setSize(900, 700);
            frame.setLocationRelativeTo(null);
            frame.setResizable(false);
            frame.setContentPane(new Account("testuser")); // Pass a default username for testing
            frame.setVisible(true);
        });
    }
}
