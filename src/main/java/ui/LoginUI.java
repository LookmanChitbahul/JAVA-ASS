package ui;

import java.awt.*;
import java.awt.event.*;
import java.sql.SQLException;
import javax.swing.*;
import services.AuthService;

//gradient rotation on root panel(backgd)
class RotatingGradientPanel extends JPanel {
    private double angle = 0;
    private Timer timer;
    private Color color1 = new Color(0, 0, 70); // #000046 (deep navy)
    private Color color2 = new Color(28, 181, 224); // #1cb5e0 (light cyan)

    public RotatingGradientPanel() {
        this.color1 = new Color(0, 0, 70); // #000046
        this.color2 = new Color(28, 181, 224); // #1cb5e0
        initTimer();
    }

    public RotatingGradientPanel(Color color1, Color color2) {
        this.color1 = color1;
        this.color2 = color2;
        initTimer();
    }

    private void initTimer() {
        setOpaque(true);
        timer = new Timer(25, e -> {
            angle += 2;
            if (angle >= 360)
                angle -= 360;
            repaint();
        });
        timer.start();
    }

    @Override
    public void addNotify() {
        super.addNotify();
        if (timer != null && !timer.isRunning())
            timer.start();
    }

    @Override
    public void removeNotify() {
        if (timer != null && timer.isRunning())
            timer.stop();
        super.removeNotify();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        int w = getWidth();
        int h = getHeight();
        double radians = Math.toRadians(angle);

        float cx = w / 2f + (float) (Math.cos(radians) * w / 4f);
        float cy = h / 2f + (float) (Math.sin(radians) * h / 4f);
        float x1 = w / 2f - (float) (Math.cos(radians) * w / 2f);
        float y1 = h / 2f - (float) (Math.sin(radians) * h / 2f);
        float x2 = w / 2f + (float) (Math.cos(radians) * w / 2f);
        float y2 = h / 2f + (float) (Math.sin(radians) * h / 2f);

        GradientPaint gp = new GradientPaint(x1, y1, color1, x2, y2, color2);
        g2.setPaint(gp);
        g2.fillRect(0, 0, w, h);
        g2.dispose();
    }
}

// Glass-like semi-transparent rounded panel
class GlassPanel extends JPanel {
    private Color fill;
    private int arc;

    public GlassPanel(Color fill, int arc) {
        this.fill = fill;
        this.arc = arc;
        setOpaque(false);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        int w = getWidth();
        int h = getHeight();

        // translucent fill ( glass look)
        g2.setColor(fill);
        g2.fillRoundRect(0, 0, w, h, arc, arc);

        // faint border for separation
        g2.setColor(new Color(255, 255, 255, 60));
        g2.drawRoundRect(0, 0, Math.max(0, w - 1), Math.max(0, h - 1), arc, arc);

        g2.dispose();
    }
}

// rounded button with hover and press effects
class RoundedButton extends JButton {
    private Color bg;
    private final int arc;
    private boolean hover = false;
    private boolean pressed = false;
    private final Color hoverBg;
    private final Color pressedBg;

    public RoundedButton(String text, Color bg, int arc) {
        super(text);
        this.bg = bg;
        this.arc = arc;
        // deep-navy hover color (#000046) and slightly darker pressed color
        this.hoverBg = new Color(0, 0, 70);
        this.pressedBg = new Color(0, 0, 50);
        setOpaque(false);
        setContentAreaFilled(false);
        setBorderPainted(false);
        setFocusPainted(false);
        setHorizontalAlignment(SwingConstants.CENTER);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                hover = true;
                repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                hover = false;
                pressed = false;
                repaint();
            }

            @Override
            public void mousePressed(MouseEvent e) {
                pressed = true;
                repaint();
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                pressed = false;
                repaint();
            }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        int w = getWidth();
        int h = getHeight();
        Color fill = pressed ? pressedBg : (hover ? hoverBg : bg);
        g2.setColor(fill);
        g2.fillRoundRect(0, 0, w, h, arc, arc);
        // slightly transparent border for softer appearance
        Color darker = fill.darker();
        Color borderColor = new Color(darker.getRed(), darker.getGreen(), darker.getBlue(), 200);
        g2.setColor(borderColor);
        g2.setStroke(new BasicStroke(1f));
        g2.drawRoundRect(0, 0, w - 1, h - 1, arc, arc);
        g2.dispose();
        super.paintComponent(g);
    }
}

public class LoginUI {

    private final JFrame frame;

    public LoginUI() {
        // login Frame
        frame = new JFrame("Smart retail & Analytics");
        frame.setSize(1200, 800);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);
        frame.setLayout(new GridLayout(1, 2)); // 1 row 2 column frame (no divider)

        // left panel = welcome message + rotating gradient background

        // single rotating gradient background across the whole frame (single panel)
        RotatingGradientPanel rootPanel = new RotatingGradientPanel();
        rootPanel.setLayout(new GridLayout(1, 2));

        // left content container (transparent) so gradient shows through
        JPanel leftContainer = new JPanel(new GridBagLayout());
        leftContainer.setOpaque(false);

        JPanel leftContent = new JPanel();
        leftContent.setOpaque(false); // transparent over gradient
        leftContent.setLayout(new BoxLayout(leftContent, BoxLayout.Y_AXIS));

        // Text on left panel
        JLabel welcomeLabel = new JLabel(
                "<html><div style='text-align:center;'><h1 style='color:white;font-size:23px;'>Welcome to Smart Retail & Analytics</h1>"
                        + "<p style='color:white;font-size:12px;'>A university project showcasing modern retail solutions.<br>"
                        + "Integrating data-driven insights, customer engagement,<br>"
                        + "and efficient operations for a smarter future.</p></div></html>");

        welcomeLabel.setAlignmentX(Component.CENTER_ALIGNMENT); // content center

        leftContent.add(welcomeLabel);
        leftContent.add(Box.createRigidArea(new Dimension(0, 20)));
        leftContainer.add(leftContent);
        rootPanel.add(leftContainer); // added left container to the left screen

        // right container consisting Login Box
        JPanel rightContainer = new JPanel(new GridBagLayout());
        rightContainer.setOpaque(false);

        // glass-styled form box that sits centered over the gradient
        GlassPanel formBox = new GlassPanel(new Color(255, 255, 255, 170), 18);
        formBox.setLayout(new BoxLayout(formBox, BoxLayout.Y_AXIS));
        formBox.setBorder(BorderFactory.createEmptyBorder(18, 24, 18, 24));
        formBox.setPreferredSize(new Dimension(340, 380));
        formBox.setMinimumSize(new Dimension(320, 360));
        formBox.setAlignmentX(Component.CENTER_ALIGNMENT);

        // login Box header
        JLabel signInLabel = new JLabel("Sign In");
        signInLabel.setForeground(Color.BLACK);
        signInLabel.setFont(signInLabel.getFont().deriveFont(Font.BOLD, 20f));
        signInLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        signInLabel.setHorizontalAlignment(SwingConstants.CENTER);

        // Username label + underline field
        JLabel usernameLabel = new JLabel("Username");
        usernameLabel.setForeground(Color.DARK_GRAY);
        usernameLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        usernameLabel.setFont(usernameLabel.getFont().deriveFont(12f));

        JTextField usernameField = new JTextField();
        usernameField.setMaximumSize(new Dimension(320, 30));
        usernameField.setPreferredSize(new Dimension(320, 30));
        usernameField.setAlignmentX(Component.CENTER_ALIGNMENT);
        usernameField.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, Color.BLACK));
        usernameField.setOpaque(false);
        usernameField.setForeground(Color.DARK_GRAY);

        // Password label + underline field
        JLabel passwordLabel = new JLabel("Password");
        passwordLabel.setForeground(Color.DARK_GRAY);
        passwordLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        passwordLabel.setFont(passwordLabel.getFont().deriveFont(12f));

        JPasswordField passwordField = new JPasswordField();
        passwordField.setMaximumSize(new Dimension(320, 30));
        passwordField.setPreferredSize(new Dimension(320, 30));
        passwordField.setAlignmentX(Component.CENTER_ALIGNMENT);
        passwordField.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, Color.BLACK));
        passwordField.setOpaque(false);
        passwordField.setForeground(Color.DARK_GRAY);

        // rounded login button
        RoundedButton signInBtn = new RoundedButton("Login", new Color(33, 150, 243), 10);
        signInBtn.setForeground(Color.WHITE);
        signInBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        signInBtn.setFocusable(false);
        signInBtn.setBorder(BorderFactory.createEmptyBorder(8, 20, 8, 20));
        signInBtn.setPreferredSize(new Dimension(140, 36));

        // Authenticating user from database........
        // 1.Must add new User manually in database....(to be noted : not create or
        // register user available)
        // 2. use login to verify user from database before proceeding to main dashboard
        /*
         * 3. if present in database(accepted) --> welocome panel --> proceed to
         * database
         * else if not present.( Display invalid credentials)...
         */

        signInBtn.addActionListener(e -> {
            String username = usernameField.getText(); // obtaining user input for username
            String password = new String(passwordField.getPassword()); // ....for password

            AuthService authService = new AuthService();

            // Validate inputs
            if (!authService.validateInputs(username, password)) {
                JOptionPane.showMessageDialog(frame, "Please enter username and password", "Error",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            try {
                // Authenticate user
                if (authService.authenticateUser(username, password)) {
                    JOptionPane.showMessageDialog(frame, "Login Successful! Welcome " + username, "Success",
                            JOptionPane.INFORMATION_MESSAGE);

                    // Dispose login frame first
                    frame.dispose();

                    // Then create and show dashboard
                    try {
                        SwingUtilities.invokeLater(() -> {
                            try {
                                new DashboardUI(username);
                            } catch (Exception ex) {
                                ex.printStackTrace();
                                JOptionPane.showMessageDialog(null, "Error loading Dashboard: " + ex.getMessage(),
                                        "Error", JOptionPane.ERROR_MESSAGE);
                            }
                        });
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        JOptionPane.showMessageDialog(frame, "Error launching Dashboard: " + ex.getMessage(), "Error",
                                JOptionPane.ERROR_MESSAGE);
                    }
                } else {
                    JOptionPane.showMessageDialog(frame, "Username or Password doesnot exist..please contact IT dept",
                            "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(frame, "Database Error: " + ex.getMessage(), "Error",
                        JOptionPane.ERROR_MESSAGE);
            } catch (IllegalArgumentException ex) {
                JOptionPane.showMessageDialog(frame, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        // compose form: header stays at the top, the fields and button vertically
        // centered
        formBox.add(signInLabel);
        formBox.add(Box.createRigidArea(new Dimension(0, 10)));
        formBox.add(Box.createVerticalGlue());

        JPanel centerPanel = new JPanel();
        centerPanel.setOpaque(false);
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        centerPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        centerPanel.add(usernameLabel);
        centerPanel.add(Box.createRigidArea(new Dimension(0, 6)));
        centerPanel.add(usernameField);
        centerPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        centerPanel.add(passwordLabel);
        centerPanel.add(Box.createRigidArea(new Dimension(0, 6)));
        centerPanel.add(passwordField);
        centerPanel.add(Box.createRigidArea(new Dimension(0, 18)));
        centerPanel.add(signInBtn);

        formBox.add(centerPanel);
        formBox.add(Box.createVerticalGlue());
        formBox.add(Box.createRigidArea(new Dimension(0, 10)));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.fill = GridBagConstraints.NONE;
        rightContainer.add(formBox, gbc);

        rootPanel.add(rightContainer); // added right container to the right root screen

        frame.setContentPane(rootPanel);
        frame.setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new LoginUI());
    }
}
