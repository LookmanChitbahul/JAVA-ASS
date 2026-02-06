package ui;

import models.Customer;
import services.CustomerService;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

public class CustomerUI extends JPanel {
    private JTable table;
    private DefaultTableModel model;
    private JTextField txtFullName, txtContact, txtEmail, txtAddress, txtPoints, txtSearch;
    private CustomerService service;

    public CustomerUI() {
        service = new CustomerService();
        setLayout(new BorderLayout(0, 0));
        setBackground(Color.decode("#111827"));

        add(createGradientHeaderPanel(), BorderLayout.NORTH);
        
        JPanel contentPanel = new JPanel(new BorderLayout(0, 15));
        contentPanel.setBackground(Color.decode("#111827"));
        contentPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        contentPanel.add(createSearchPanel(), BorderLayout.NORTH);
        contentPanel.add(createTablePanel(), BorderLayout.CENTER);
        contentPanel.add(createFormPanel(), BorderLayout.SOUTH);
        
        add(contentPanel, BorderLayout.CENTER);
        
        loadTable();
    }

    // Gradient header with rotating effect
    private JPanel createGradientHeaderPanel() {
        JPanel header = new RotatingGradientHeaderPanel();
        header.setLayout(new BorderLayout());
        header.setPreferredSize(new Dimension(0, 120));
        header.setBorder(BorderFactory.createEmptyBorder(25, 40, 25, 40));

        JLabel title = new JLabel("Customer Management");
        title.setFont(new Font("Segoe UI", Font.BOLD, 32));
        title.setForeground(Color.WHITE);
        
        JLabel subtitle = new JLabel("Manage and organize your customers");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        subtitle.setForeground(new Color(220, 220, 220));

        JPanel textPanel = new JPanel();
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
        textPanel.setOpaque(false);
        textPanel.add(title);
        textPanel.add(Box.createVerticalStrut(5));
        textPanel.add(subtitle);

        header.add(textPanel, BorderLayout.WEST);
        return header;
    }

    private JPanel createSearchPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        panel.setOpaque(false);

        txtSearch = createTextField(20);
        JButton btnSearch = createButton("Search");
        btnSearch.addActionListener(e -> searchCustomer());
        JButton btnRefresh = createButton("Refresh");
        btnRefresh.addActionListener(e -> loadTable());

        panel.add(new JLabel("Search:"));
        panel.add(txtSearch);
        panel.add(btnSearch);
        panel.add(btnRefresh);

        return panel;
    }

    private JPanel createTablePanel() {
        JPanel card = new RoundedPanel(20);
        card.setLayout(new BorderLayout());
        card.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        model = new DefaultTableModel(
            new String[]{"ID", "Full Name", "Contact", "Email", "Address", "Loyalty Points", "Created", "Updated"}, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };

        table = new JTable(model);
        table.setBackground(new Color(20, 30, 50));
        table.setForeground(new Color(200, 200, 200));
        table.setGridColor(new Color(50, 60, 90));
        table.setRowHeight(40);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        table.setSelectionBackground(new Color(30, 100, 200, 100));
        table.setSelectionForeground(new Color(230, 230, 230));

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        centerRenderer.setBackground(new Color(20, 30, 50));
        centerRenderer.setForeground(new Color(200, 200, 200));

        for (int i = 0; i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }

        table.getTableHeader().setBackground(new Color(30, 40, 70));
        table.getTableHeader().setForeground(new Color(180, 180, 180));
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        table.getTableHeader().setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(50, 60, 90)));
        table.getTableHeader().setPreferredSize(new Dimension(0, 45));

        table.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                int row = table.getSelectedRow();
                if (row >= 0) {
                    txtFullName.setText(model.getValueAt(row, 1).toString());
                    txtContact.setText(model.getValueAt(row, 2).toString());
                    txtEmail.setText(model.getValueAt(row, 3).toString());
                    txtAddress.setText(model.getValueAt(row, 4).toString());
                    txtPoints.setText(model.getValueAt(row, 5).toString());
                }
            }
        });

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBackground(new Color(20, 30, 50));
        scroll.getViewport().setBackground(new Color(20, 30, 50));
        scroll.setBorder(BorderFactory.createEmptyBorder());

        card.add(scroll, BorderLayout.CENTER);
        return card;
    }

    private JPanel createFormPanel() {
        JPanel card = new RoundedPanel(20);
        card.setLayout(new BorderLayout(10, 10));
        card.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JPanel fieldsPanel = new JPanel(new GridBagLayout());
        fieldsPanel.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 10, 8, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Full Name field
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0;
        fieldsPanel.add(createLabel("Full Name:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        txtFullName = createTextField(20);
        fieldsPanel.add(txtFullName, gbc);

        // Contact field
        gbc.gridx = 2; gbc.weightx = 0;
        fieldsPanel.add(createLabel("Contact:"), gbc);
        gbc.gridx = 3; gbc.weightx = 1.0;
        txtContact = createTextField(20);
        fieldsPanel.add(txtContact, gbc);

        // Email field
        gbc.gridx = 4; gbc.weightx = 0;
        fieldsPanel.add(createLabel("Email:"), gbc);
        gbc.gridx = 5; gbc.weightx = 1.5;
        txtEmail = createTextField(25);
        fieldsPanel.add(txtEmail, gbc);

        // Address field
        gbc.gridx = 6; gbc.weightx = 0;
        fieldsPanel.add(createLabel("Address:"), gbc);
        gbc.gridx = 7; gbc.weightx = 1.0;
        txtAddress = createTextField(20);
        fieldsPanel.add(txtAddress, gbc);

        // Loyalty Points field
        gbc.gridx = 8; gbc.weightx = 0;
        fieldsPanel.add(createLabel("Loyalty Points:"), gbc);
        gbc.gridx = 9; gbc.weightx = 0.5;
        txtPoints = createTextField(10);
        fieldsPanel.add(txtPoints, gbc);

        card.add(fieldsPanel, BorderLayout.CENTER);

        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        buttonsPanel.setOpaque(false);

        JButton btnAdd = createButton("Add Customer");
        btnAdd.addActionListener(e -> addCustomer());
        JButton btnUpdate = createButton("Update");
        btnUpdate.addActionListener(e -> updateCustomer());
        JButton btnDelete = createButton("Delete");
        btnDelete.addActionListener(e -> deleteCustomer());
        JButton btnClear = createButton("Clear");
        btnClear.addActionListener(e -> clearFields());

        buttonsPanel.add(btnAdd);
        buttonsPanel.add(btnUpdate);
        buttonsPanel.add(btnDelete);
        buttonsPanel.add(btnClear);

        card.add(buttonsPanel, BorderLayout.SOUTH);
        return card;
    }

    private JLabel createLabel(String text) {
        JLabel label = new JLabel(text);
        label.setForeground(new Color(150, 150, 150));
        label.setFont(new Font("Segoe UI", Font.BOLD, 13));
        return label;
    }

    private JTextField createTextField(int columns) {
        JTextField tf = new JTextField(columns);
        tf.setBackground(new Color(30, 40, 65));
        tf.setForeground(new Color(200, 200, 200));
        tf.setCaretColor(new Color(200, 200, 200));
        tf.setBorder(BorderFactory.createLineBorder(new Color(50, 60, 90), 1));
        tf.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tf.setMargin(new Insets(8, 10, 8, 10));
        return tf;
    }

    private JButton createButton(String text) {
        JButton btn = new JButton(text);
        btn.setBackground(new Color(59, 130, 246));
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setMargin(new Insets(8, 20, 8, 20));

        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                btn.setBackground(new Color(37, 99, 235));
            }
            public void mouseExited(MouseEvent e) {
                btn.setBackground(new Color(59, 130, 246));
            }
        });

        return btn;
    }

    // Rounded panel helper
    private static class RoundedPanel extends JPanel {
        private int radius;
        public RoundedPanel(int radius) {
            this.radius = radius;
            setOpaque(false);
            setBackground(new Color(30, 45, 75));
        }
        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(getBackground());
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), radius, radius);
            g2.setColor(new Color(50, 60, 90));
            g2.setStroke(new BasicStroke(1));
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, radius, radius);
        }
    }

    // Rotating gradient header
    private static class RotatingGradientHeaderPanel extends JPanel {
        private double angle = 0;
        private final Color color1 = Color.decode("#000428");
        private final Color color2 = Color.decode("#004e92");
        private final Timer timer;

        public RotatingGradientHeaderPanel() {
            setOpaque(false);
            timer = new Timer(60, e -> {
                angle += Math.toRadians(1.8);
                if (angle > Math.PI * 2) angle -= Math.PI * 2;
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
            
            // Subtle border
            g2.setColor(new Color(255, 255, 255, 30));
            g2.setStroke(new BasicStroke(1.2f));
            g2.drawRoundRect(1, 1, Math.max(0, w - 3), Math.max(0, h - 3), arc, arc);
            g2.dispose();
        }
    }

    private void loadTable() {
        model.setRowCount(0);
        List<Customer> list = service.getAllCustomers();
        for (Customer c : list) {
            model.addRow(new Object[]{c.getCustomerId(), c.getFullName(), c.getContact(), c.getEmail(), 
                c.getAddress(), c.getLoyaltyPoints(), c.getCreatedAt(), c.getUpdatedAt()});
        }
    }

    private void addCustomer() {
        try {
            if (txtFullName.getText().trim().isEmpty() || txtContact.getText().trim().isEmpty() || txtEmail.getText().trim().isEmpty()) {
                showError("Please fill all required fields");
                return;
            }
            Customer c = new Customer();
            c.setFullName(txtFullName.getText().trim());
            c.setContact(txtContact.getText().trim());
            c.setEmail(txtEmail.getText().trim());
            c.setAddress(txtAddress.getText().trim());
            c.setLoyaltyPoints(Integer.parseInt(txtPoints.getText().trim().isEmpty() ? "0" : txtPoints.getText().trim()));
            
            if (service.addCustomer(c)) {
                showSuccess("Customer added successfully!");
                loadTable();
                clearFields();
            } else {
                showError("Failed to add customer");
            }
        } catch (NumberFormatException e) {
            showError("Loyalty Points must be a valid number");
        } catch (Exception e) {
            showError("Error: " + e.getMessage());
        }
    }

    private void updateCustomer() {
        int row = table.getSelectedRow();
        if (row < 0) {
            showWarning("Please select a customer to update");
            return;
        }
        try {
            Customer c = new Customer();
            c.setCustomerId(Integer.parseInt(model.getValueAt(row, 0).toString()));
            c.setFullName(txtFullName.getText().trim());
            c.setContact(txtContact.getText().trim());
            c.setEmail(txtEmail.getText().trim());
            c.setAddress(txtAddress.getText().trim());
            c.setLoyaltyPoints(Integer.parseInt(txtPoints.getText().trim().isEmpty() ? "0" : txtPoints.getText().trim()));
            
            if (service.updateCustomer(c)) {
                showSuccess("Customer updated successfully!");
                loadTable();
                clearFields();
            } else {
                showError("Failed to update customer");
            }
        } catch (Exception e) {
            showError("Error: " + e.getMessage());
        }
    }

    private void deleteCustomer() {
        int row = table.getSelectedRow();
        if (row < 0) {
            showWarning("Please select a customer to delete");
            return;
        }
        if (JOptionPane.showConfirmDialog(this, "Delete this customer?", 
            "Confirm", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            int id = Integer.parseInt(model.getValueAt(row, 0).toString());
            if (service.deleteCustomer(id)) {
                showSuccess("Customer deleted successfully!");
                loadTable();
                clearFields();
            } else {
                showError("Failed to delete customer");
            }
        }
    }

    private void searchCustomer() {
        String key = txtSearch.getText().trim();
        model.setRowCount(0);
        List<Customer> results = key.isEmpty() ? service.getAllCustomers() : service.searchCustomers(key);
        for (Customer c : results) {
            model.addRow(new Object[]{c.getCustomerId(), c.getFullName(), c.getContact(), c.getEmail(), 
                c.getAddress(), c.getLoyaltyPoints(), c.getCreatedAt(), c.getUpdatedAt()});
        }
        if (results.isEmpty() && !key.isEmpty()) {
            showInfo("No customers found");
        }
    }

    private void clearFields() {
        txtFullName.setText("");
        txtContact.setText("");
        txtEmail.setText("");
        txtAddress.setText("");
        txtPoints.setText("");
        table.clearSelection();
    }

    private void showSuccess(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Success", JOptionPane.INFORMATION_MESSAGE);
    }

    private void showError(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Error", JOptionPane.ERROR_MESSAGE);
    }

    private void showWarning(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Warning", JOptionPane.WARNING_MESSAGE);
    }

    private void showInfo(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Info", JOptionPane.INFORMATION_MESSAGE);
    }
}