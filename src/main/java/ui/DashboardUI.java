package ui;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;

public class DashboardUI {
    private final JFrame frame;
    private final JPanel mainContent;
    private final CardLayout cardLayout;

    private final String username;

    public DashboardUI(String username) {
        this.username = username;
        frame = new JFrame("Smart Retails & Analytics");
        frame.setTitle("Main");
        frame.setSize(1200, 800);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);
        frame.setLayout(new BorderLayout());
        cardLayout = new CardLayout();
        mainContent = new JPanel(cardLayout);
        // right side background (dark) #111827
        mainContent.setBackground(Color.decode("#111827"));
        mainContent.setOpaque(true);

        mainContent.add(new Account(username), "Account");
        mainContent.add(createPlaceholder("Dashboard"), "Dashboard");
        mainContent.add(createPlaceholder("Analytics"), "Analytics");
        mainContent.add(new SalesUI(), "sales");
        mainContent.add(createPlaceholder("Setting"), "Setting");
        mainContent.add(new CustomerUI(), "Customer");
        mainContent.add(new ProductUI(), "Product");

        frame.add(sideBar(), BorderLayout.WEST);
        frame.add(mainContent, BorderLayout.CENTER);

        frame.setVisible(true);
    }

    public DashboardUI() {
        this("Guest");
    }

    private JPanel sideBar() {
        RotatingGradientPanel sidebar = new RotatingGradientPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setPreferredSize(new Dimension(220, frame.getHeight()));
        sidebar.setOpaque(false);

        String[] menuItems = {
                "Dashboard", "Customer", "Product",
                "Analytics", "sales", "Setting", "Account" };

        for (String item : menuItems) {
            RoundedButton button = new RoundedButton(item);
            button.setAlignmentX(Component.CENTER_ALIGNMENT);
            button.setMaximumSize(new Dimension(200, 40));
            // keep transparent background; use white text
            button.setForeground(Color.WHITE);
            button.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            button.setFocusPainted(false);
            button.setMargin(new Insets(8, 16, 8, 16));

            button.addActionListener(e -> cardLayout.show(mainContent, item));

            sidebar.add(Box.createVerticalStrut(15));
            sidebar.add(button);
        }
        sidebar.add(Box.createVerticalGlue());
        return sidebar;
    }

    private JPanel createPlaceholder(String name) {
        JPanel panel = new JPanel();
        panel.setBackground(Color.decode("#111827"));
        panel.setLayout(new BorderLayout());
        JLabel label = new JLabel(name + " Page (Coming Soon)", SwingConstants.CENTER);
        label.setFont(new Font("Segoe UI", Font.BOLD, 18));
        label.setForeground(new Color(230, 230, 230));
        panel.add(label, BorderLayout.CENTER);
        return panel;
    }

    // Rounded button with hover bottom-border
    private static class RoundedButton extends JButton {
        private final Border defaultBorder = BorderFactory.createEmptyBorder(8, 16, 8, 16);
        private boolean hovered = false;

        public RoundedButton(String text) {
            super(text);
            setContentAreaFilled(false);
            setOpaque(false);
            setBorder(defaultBorder);
            setHorizontalAlignment(SwingConstants.CENTER);
            setFocusable(false);

            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    hovered = true;
                    repaint();
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    hovered = false;
                    repaint();
                }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            // Keep button transparent. On hover draw only a bottom blue bar (no
            // layout/inset changes).
            if (hovered) {
                g2.setColor(new Color(255, 255, 255, 180)); // pale white
                int thickness = 3;
                int padX = 10; // horizontal padding from sides
                int y = getHeight() - thickness - 6; // slight offset from bottom for visual spacing
                g2.fillRect(padX, y, getWidth() - padX * 2, thickness);
            }
            g2.dispose();
            super.paintComponent(g);
        }
    }

    // Sidebar with rotating gradient and rounded corners
    private class RotatingGradientPanel extends JPanel {
        private double angle = 0;
        // gradient from #000428 → #004e92
        private final Color color1 = Color.decode("#000428");
        private final Color color2 = Color.decode("#004e92");
        private final Timer timer;

        public RotatingGradientPanel() {
            setOpaque(false);
            timer = new Timer(60, e -> {
                angle += Math.toRadians(1.8);
                if (angle > Math.PI * 2)
                    angle -= Math.PI * 2;
                repaint();
            });
            timer.start();
        }

        @Override
        protected void paintComponent(Graphics g) {
            int w = getWidth(), h = getHeight();
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            float cx = w / 2f, cy = h / 2f;
            float dx = (float) (Math.cos(angle) * w / 2f);
            float dy = (float) (Math.sin(angle) * h / 2f);
            GradientPaint gp = new GradientPaint(cx - dx, cy - dy, color1, cx + dx, cy + dy, color2, true);
            g2.setPaint(gp);
            int arc = 18;
            g2.fillRoundRect(0, 0, w, h, arc, arc);
            // subtle border
            g2.setColor(new Color(255, 255, 255, 30));
            g2.setStroke(new BasicStroke(1.2f));
            g2.drawRoundRect(1, 1, Math.max(0, w - 3), Math.max(0, h - 3), arc, arc);
            g2.dispose();
            super.paintComponent(g);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(DashboardUI::new);
    }

}
