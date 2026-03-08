package ui;

import models.*;
import services.*;
import utils.Validator;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import java.awt.Desktop;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;

/**
 * SalesUI.java
 * Main user interface for processing sales transactions.
 * Supports two types of sales:
 * 1. Quick Cash Sales - For walk-in customers (no customer details required)
 * 2. Regular Sales - For registered customers (requires customer ID and details)
 *
 * Features:
 * - Product browsing and search
 * - Shopping cart management
 * - Cash handling with change calculation
 * - Receipt generation
 * - Daily cash summary
 */
public class SalesUI extends JPanel {
    // Services
    private final SalesService salesService;
    private final ProductService productService;
    private final CustomerService customerService;

    // UI Components - Left Panel (Products)
    private JTextField txtSearchProduct;
    private JTable tblAvailableProducts;
    private DefaultTableModel productsTableModel;
    private JSpinner spnQuantity;

    // UI Components - Right Panel (Cart)
    private JTable tblCart;
    private DefaultTableModel cartTableModel;
    private JLabel lblSubtotal;
    private JLabel lblTax;
    private JLabel lblGrandTotal;
    private JComboBox<String> cmbPaymentMethod;
    private JTextField txtCustomerId;
    private JLabel lblCustomerInfo;

    // Sale Type Selection
    private JRadioButton rbQuickCash;
    private JRadioButton rbRegularSale;
    private ButtonGroup saleTypeGroup;
    private JPanel customerPanel;

    // Cash Handling Components
    private JLabel lblAmountDue;
    private JTextField txtCashReceived;
    private JLabel lblChangeDue;
    private JButton btnCalculateChange;
    private JButton btnViewCashSummary;

    // Buttons
    private JButton btnAddToCart;
    private JButton btnRemoveItem;
    private JButton btnClearCart;
    private JButton btnCheckout;
    private JButton btnNewCustomer;
    private JButton btnGenerateReceipt;
    private JButton btnQuickSale;

    // Data
    private List<Product> availableProducts;
    private List<CartItem> cartItems;
    private double currentSubtotal = 0.0;
    private final double TAX_RATE = 0.10; // 10% tax
    private int currentSaleId = 0;
    private int currentUserId = 1; // This should come from logged-in user

    // Constants
    private static final int WALK_IN_CUSTOMER_ID = 1;

    // Theme Colors
    private final Color DARK_BG = AppTheme.getBgColor();
    private final Color CARD_BG = AppTheme.getCardColor();
    private final Color PRIMARY_COLOR = AppTheme.getPrimaryColor();
    private final Color SUCCESS_COLOR = new Color(34, 197, 94);
    private final Color DANGER_COLOR = new Color(239, 68, 68);
    private final Color WARNING_COLOR = new Color(251, 146, 60);
    private final Color TEXT_PRIMARY = AppTheme.getTextColor();
    private final Color TEXT_SECONDARY = AppTheme.getSubTextColor();
    private final Color BORDER_COLOR = AppTheme.getBorderColor();
    private final Color INFO_COLOR = new Color(56, 189, 248);

    /**
     * Constructor - Initializes the Sales UI
     */
    public SalesUI() {
        // Initialize services
        this.salesService = new SalesService();
        this.productService = new ProductService();
        this.customerService = new CustomerService();

        // Initialize data
        this.availableProducts = new ArrayList<>();
        this.cartItems = new ArrayList<>();

        // Setup UI
        setLayout(new BorderLayout(0, 0));
        setBackground(DARK_BG);

        // Add header
        add(createHeaderPanel(), BorderLayout.NORTH);

        // Create main content panel
        JPanel contentPanel = new JPanel(new BorderLayout(0, 20));
        contentPanel.setBackground(DARK_BG);
        contentPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Top panel - Sale type selector and customer info
        JPanel topPanel = createTopPanel();
        contentPanel.add(topPanel, BorderLayout.NORTH);

        // Center panel - Split pane for products and cart
        JSplitPane centerSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        centerSplitPane.setLeftComponent(createProductsPanel());
        centerSplitPane.setRightComponent(createCartPanel());
        centerSplitPane.setDividerLocation(0.55);
        centerSplitPane.setBackground(DARK_BG);
        centerSplitPane.setBorder(BorderFactory.createEmptyBorder());

        contentPanel.add(centerSplitPane, BorderLayout.CENTER);

        // Bottom panel - Summary and actions
        contentPanel.add(createSummaryPanel(), BorderLayout.SOUTH);

        add(contentPanel, BorderLayout.CENTER);

        // Load initial data
        loadProducts();

        // Set default sale type
        rbQuickCash.setSelected(true);
        toggleSaleType();
    }

    /**
     * Creates the header panel with title and gradient background
     */
    private JPanel createHeaderPanel() {
        RotatingGradientHeaderPanel header = new RotatingGradientHeaderPanel();
        header.setLayout(new BorderLayout());
        header.setPreferredSize(new Dimension(0, 120));
        header.setBorder(BorderFactory.createEmptyBorder(25, 40, 25, 40));

        JLabel title = new JLabel("Sales Management");
        title.setFont(new Font("Segoe UI", Font.BOLD, 32));
        title.setForeground(Color.WHITE);

        JLabel subtitle = new JLabel("Process customer sales and generate receipts");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        subtitle.setForeground(new Color(220, 220, 220));

        // Cash summary button
        btnViewCashSummary = createButton("Daily Cash Summary", INFO_COLOR);
        btnViewCashSummary.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnViewCashSummary.addActionListener(e -> showCashSummary());

        JPanel textPanel = new JPanel();
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
        textPanel.setOpaque(false);
        textPanel.add(title);
        textPanel.add(Box.createVerticalStrut(5));
        textPanel.add(subtitle);

        // Sale info on right
        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setOpaque(false);

        JLabel lblSaleNo = new JLabel("Sale #: NEW");
        lblSaleNo.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblSaleNo.setForeground(Color.WHITE);

        JLabel lblDate = new JLabel(new SimpleDateFormat("dd MMM yyyy HH:mm").format(new Date()));
        lblDate.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblDate.setForeground(new Color(200, 200, 200));

        infoPanel.add(btnViewCashSummary);
        infoPanel.add(Box.createVerticalStrut(10));
        infoPanel.add(lblSaleNo);
        infoPanel.add(Box.createVerticalStrut(5));
        infoPanel.add(lblDate);

        header.add(textPanel, BorderLayout.WEST);
        header.add(infoPanel, BorderLayout.EAST);

        return header;
    }

    /**
     * Creates the top panel with sale type selection and customer info
     */
    private JPanel createTopPanel() {
        JPanel panel = new RoundedPanel(15);
        panel.setLayout(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 10, 5, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Sale Type Selection
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        gbc.weightx = 0;
        panel.add(createLabel("Sale Type:"), gbc);

        gbc.gridx = 1;
        rbQuickCash = new JRadioButton("Quick Cash Sale (Walk-in)");
        rbQuickCash.setForeground(TEXT_PRIMARY);
        rbQuickCash.setBackground(CARD_BG);
        rbQuickCash.addActionListener(e -> toggleSaleType());
        panel.add(rbQuickCash, gbc);

        gbc.gridx = 2;
        rbRegularSale = new JRadioButton("Regular Sale (Registered Customer)");
        rbRegularSale.setForeground(TEXT_PRIMARY);
        rbRegularSale.setBackground(CARD_BG);
        rbRegularSale.addActionListener(e -> toggleSaleType());
        panel.add(rbRegularSale, gbc);

        saleTypeGroup = new ButtonGroup();
        saleTypeGroup.add(rbQuickCash);
        saleTypeGroup.add(rbRegularSale);

        // Customer Panel (shown only for regular sales)
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 3;
        customerPanel = createCustomerPanel();
        panel.add(customerPanel, gbc);

        // Search Products (always visible)
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 1;
        gbc.weightx = 0;
        panel.add(createLabel("Search Products:"), gbc);

        gbc.gridx = 1;
        gbc.gridwidth = 2;
        gbc.weightx = 1.0;
        txtSearchProduct = createTextField(30);
        txtSearchProduct.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                filterProducts();
            }
        });
        panel.add(txtSearchProduct, gbc);

        return panel;
    }

    /**
     * Creates the customer information panel (for regular sales)
     */
    private JPanel createCustomerPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setOpaque(false);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 10, 5, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0;
        panel.add(createLabel("Customer ID:"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.3;
        txtCustomerId = createTextField(10);
        txtCustomerId.addActionListener(e -> validateCustomer());
        panel.add(txtCustomerId, gbc);

        gbc.gridx = 2;
        gbc.weightx = 0;
        panel.add(createLabel("Customer:"), gbc);

        gbc.gridx = 3;
        gbc.weightx = 0.5;
        lblCustomerInfo = new JLabel("Enter customer ID");
        lblCustomerInfo.setForeground(TEXT_SECONDARY);
        lblCustomerInfo.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        panel.add(lblCustomerInfo, gbc);

        gbc.gridx = 4;
        gbc.weightx = 0;
        btnNewCustomer = createButton("+ New Customer", PRIMARY_COLOR);
        btnNewCustomer.addActionListener(e -> showNewCustomerDialog());
        panel.add(btnNewCustomer, gbc);

        return panel;
    }

    /**
     * Creates the products panel with available items
     */
    private JPanel createProductsPanel() {
        JPanel card = new RoundedPanel(15);
        card.setLayout(new BorderLayout(0, 10));
        card.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // Table header
        JPanel tableHeader = new JPanel(new BorderLayout());
        tableHeader.setOpaque(false);

        JLabel title = new JLabel("Available Products");
        title.setFont(new Font("Segoe UI", Font.BOLD, 16));
        title.setForeground(TEXT_PRIMARY);
        tableHeader.add(title, BorderLayout.WEST);

        // Quantity selector
        JPanel quantityPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
        quantityPanel.setOpaque(false);
        quantityPanel.add(new JLabel("Qty:"));

        spnQuantity = new JSpinner(new SpinnerNumberModel(1, 1, 100, 1));
        styleSpinner(spnQuantity);
        quantityPanel.add(spnQuantity);

        tableHeader.add(quantityPanel, BorderLayout.EAST);
        card.add(tableHeader, BorderLayout.NORTH);

        // Products table
        String[] columns = { "ID", "Product", "Category", "Price", "Stock" };
        productsTableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tblAvailableProducts = new JTable(productsTableModel);
        styleTable(tblAvailableProducts);

        // Center align numeric columns
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        centerRenderer.setBackground(CARD_BG);
        centerRenderer.setForeground(TEXT_PRIMARY);

        tblAvailableProducts.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);
        tblAvailableProducts.getColumnModel().getColumn(3).setCellRenderer(centerRenderer);
        tblAvailableProducts.getColumnModel().getColumn(4).setCellRenderer(centerRenderer);

        // Table header styling
        JTableHeader header = tblAvailableProducts.getTableHeader();
        header.setBackground(AppTheme.getCardColor());
        header.setForeground(AppTheme.getTextColor());
        header.setFont(new Font("Segoe UI", Font.BOLD, 13));

        JScrollPane scrollPane = new JScrollPane(tblAvailableProducts);
        scrollPane.setBorder(BorderFactory.createLineBorder(BORDER_COLOR, 1));
        scrollPane.getViewport().setBackground(CARD_BG);
        card.add(scrollPane, BorderLayout.CENTER);

        // Add to cart button
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttonPanel.setOpaque(false);

        btnAddToCart = createButton("Add to Cart", SUCCESS_COLOR);
        btnAddToCart.addActionListener(e -> addToCart());
        buttonPanel.add(btnAddToCart);

        card.add(buttonPanel, BorderLayout.SOUTH);

        return card;
    }

    /**
     * Creates the shopping cart panel
     */
    private JPanel createCartPanel() {
        JPanel card = new RoundedPanel(15);
        card.setLayout(new BorderLayout(0, 10));
        card.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // Title
        JLabel title = new JLabel("Shopping Cart");
        title.setFont(new Font("Segoe UI", Font.BOLD, 16));
        title.setForeground(TEXT_PRIMARY);
        card.add(title, BorderLayout.NORTH);

        // Cart table
        String[] columns = { "Product", "Qty", "Price", "Subtotal" };
        cartTableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 1; // Only quantity is editable
            }

            @Override
            public Class<?> getColumnClass(int columnIndex) {
                if (columnIndex == 1)
                    return Integer.class;
                return String.class;
            }
        };

        tblCart = new JTable(cartTableModel);
        styleTable(tblCart);

        // Quantity editor
        tblCart.getColumnModel().getColumn(1).setCellEditor(new DefaultCellEditor(new JTextField()) {
            @Override
            public boolean stopCellEditing() {
                try {
                    int row = tblCart.getSelectedRow();
                    int newQty = Integer.parseInt((String) getCellEditorValue());
                    CartItem item = cartItems.get(row);

                    // Check stock
                    if (newQty > getProductStock(item.productId)) {
                        JOptionPane.showMessageDialog(SalesUI.this,
                                "Insufficient stock!", "Error", JOptionPane.ERROR_MESSAGE);
                        return false;
                    }

                    item.quantity = newQty;
                    updateCartTable();
                    calculateTotals();
                    return true;
                } catch (NumberFormatException e) {
                    return false;
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(tblCart);
        scrollPane.setBorder(BorderFactory.createLineBorder(BORDER_COLOR, 1));
        scrollPane.getViewport().setBackground(CARD_BG);
        card.add(scrollPane, BorderLayout.CENTER);

        // Cart actions
        JPanel cartActions = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        cartActions.setOpaque(false);

        btnRemoveItem = createButton("Remove Selected", DANGER_COLOR);
        btnRemoveItem.addActionListener(e -> removeFromCart());

        btnClearCart = createButton("Clear Cart", WARNING_COLOR);
        btnClearCart.addActionListener(e -> clearCart());

        cartActions.add(btnRemoveItem);
        cartActions.add(btnClearCart);
        card.add(cartActions, BorderLayout.SOUTH);

        return card;
    }

    /**
     * Creates the summary panel with totals and checkout
     */
    private JPanel createSummaryPanel() {
        JPanel panel = new RoundedPanel(15);
        panel.setLayout(new BorderLayout(20, 0));
        panel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

        // Payment method and cash handling section
        JPanel paymentPanel = new JPanel(new GridBagLayout());
        paymentPanel.setOpaque(false);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Payment Method
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0;
        paymentPanel.add(createLabel("Payment Method:"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.3;
        cmbPaymentMethod = new JComboBox<>(new String[] { "Cash", "Card", "Cheque", "Online" });
        styleComboBox(cmbPaymentMethod);
        cmbPaymentMethod.addActionListener(e -> toggleCashFields());
        paymentPanel.add(cmbPaymentMethod, gbc);

        // Cash Received (only visible for Cash payment)
        gbc.gridx = 2;
        gbc.weightx = 0;
        lblAmountDue = new JLabel("Amount Due:");
        lblAmountDue.setForeground(TEXT_SECONDARY);
        lblAmountDue.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblAmountDue.setVisible(false);
        paymentPanel.add(lblAmountDue, gbc);

        gbc.gridx = 3;
        gbc.weightx = 0.2;
        txtCashReceived = createTextField(10);
        txtCashReceived.setVisible(false);
        txtCashReceived.addActionListener(e -> calculateChange());
        paymentPanel.add(txtCashReceived, gbc);

        // Calculate Change Button
        gbc.gridx = 4;
        gbc.weightx = 0;
        btnCalculateChange = createButton("Calculate Change", INFO_COLOR);
        btnCalculateChange.setVisible(false);
        btnCalculateChange.addActionListener(e -> calculateChange());
        paymentPanel.add(btnCalculateChange, gbc);

        // Change Due Label
        gbc.gridx = 5;
        gbc.weightx = 0.2;
        lblChangeDue = new JLabel("Change: $0.00");
        lblChangeDue.setForeground(SUCCESS_COLOR);
        lblChangeDue.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblChangeDue.setVisible(false);
        paymentPanel.add(lblChangeDue, gbc);

        panel.add(paymentPanel, BorderLayout.WEST);

        // Totals
        JPanel totalsPanel = new JPanel(new GridLayout(1, 6, 20, 0));
        totalsPanel.setOpaque(false);

        totalsPanel.add(createTotalLabel("Subtotal:", lblSubtotal = new JLabel("$0.00")));
        totalsPanel.add(createTotalLabel("Tax (10%):", lblTax = new JLabel("$0.00")));
        totalsPanel.add(createTotalLabel("Grand Total:", lblGrandTotal = new JLabel("$0.00")));

        panel.add(totalsPanel, BorderLayout.CENTER);

        // Action buttons
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actionPanel.setOpaque(false);

        btnGenerateReceipt = createButton("Generate Receipt", PRIMARY_COLOR);
        btnGenerateReceipt.setEnabled(false);
        btnGenerateReceipt.addActionListener(e -> generateReceipt());

        btnCheckout = createButton("Process Checkout", SUCCESS_COLOR);
        btnCheckout.addActionListener(e -> processCheckout());

        actionPanel.add(btnGenerateReceipt);
        actionPanel.add(btnCheckout);

        panel.add(actionPanel, BorderLayout.EAST);

        return panel;
    }

    /**
     * Toggles between Quick Cash and Regular sale modes
     */
    private void toggleSaleType() {
        boolean isRegular = rbRegularSale.isSelected();
        customerPanel.setVisible(isRegular);

        // If quick cash, set payment method to Cash and disable other methods
        if (rbQuickCash.isSelected()) {
            cmbPaymentMethod.setSelectedItem("Cash");
            cmbPaymentMethod.setEnabled(false);
            toggleCashFields(); // Show cash fields
        } else {
            cmbPaymentMethod.setEnabled(true);
        }

        // Clear customer fields if switching to quick cash
        if (!isRegular) {
            txtCustomerId.setText("");
            lblCustomerInfo.setText("Quick Cash Sale - No customer details needed");
            lblCustomerInfo.setForeground(SUCCESS_COLOR);
        }

        revalidate();
        repaint();
    }

    /**
     * Toggles cash fields visibility based on payment method
     */
    private void toggleCashFields() {
        boolean isCash = "Cash".equals(cmbPaymentMethod.getSelectedItem());
        lblAmountDue.setVisible(isCash);
        txtCashReceived.setVisible(isCash);
        btnCalculateChange.setVisible(isCash);
        lblChangeDue.setVisible(isCash);

        if (isCash) {
            updateAmountDueLabel();
        }
    }

    /**
     * Updates the amount due label
     */
    private void updateAmountDueLabel() {
        double grandTotal = calculateGrandTotal();
        lblAmountDue.setText(String.format("Amount Due: $%.2f", grandTotal));
    }

    /**
     * Calculates the grand total including tax
     */
    private double calculateGrandTotal() {
        double tax = currentSubtotal * TAX_RATE;
        return currentSubtotal + tax;
    }

    /**
     * Calculates change based on cash received
     */
    private void calculateChange() {
        try {
            double grandTotal = calculateGrandTotal();
            String cashStr = txtCashReceived.getText().trim();

            if (cashStr.isEmpty()) {
                lblChangeDue.setText("Change: $0.00");
                return;
            }

            double cashReceived = Double.parseDouble(cashStr);

            if (cashReceived < grandTotal) {
                lblChangeDue.setForeground(DANGER_COLOR);
                lblChangeDue.setText(String.format("Insufficient: $%.2f", grandTotal - cashReceived));
                return;
            }

            double change = cashReceived - grandTotal;
            lblChangeDue.setForeground(SUCCESS_COLOR);
            lblChangeDue.setText(String.format("Change: $%.2f", change));

        } catch (NumberFormatException e) {
            lblChangeDue.setForeground(DANGER_COLOR);
            lblChangeDue.setText("Invalid amount");
        }
    }

    // ==================== HELPER METHODS ====================

    private JLabel createLabel(String text) {
        JLabel label = new JLabel(text);
        label.setForeground(TEXT_SECONDARY);
        label.setFont(new Font("Segoe UI", Font.BOLD, 13));
        return label;
    }

    private JLabel createTotalLabel(String text, JLabel valueLabel) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setOpaque(false);

        JLabel label = new JLabel(text);
        label.setForeground(TEXT_SECONDARY);
        label.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        valueLabel.setForeground(TEXT_PRIMARY);
        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));

        panel.add(label);
        panel.add(valueLabel);

        return new JLabel() {
            @Override
            public Component getComponent(int n) {
                return panel;
            }
        };
    }

    private JTextField createTextField(int columns) {
        JTextField tf = new JTextField(columns);
        tf.setBackground(new Color(30, 41, 65));
        tf.setForeground(TEXT_PRIMARY);
        tf.setCaretColor(TEXT_PRIMARY);
        tf.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1),
                BorderFactory.createEmptyBorder(8, 10, 8, 10)));
        tf.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        return tf;
    }

    private JButton createButton(String text, Color color) {
        JButton btn = new JButton(text);
        btn.setBackground(color);
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setMargin(new Insets(8, 20, 8, 20));

        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                btn.setBackground(darkenColor(color, 0.9f));
            }

            public void mouseExited(MouseEvent e) {
                btn.setBackground(color);
            }
        });

        return btn;
    }

    private void styleSpinner(JSpinner spinner) {
        spinner.setBackground(AppTheme.getCardColor());
        spinner.setForeground(AppTheme.getTextColor());
        spinner.setBorder(BorderFactory.createLineBorder(AppTheme.getBorderColor(), 1));
        JFormattedTextField tf = ((JSpinner.DefaultEditor) spinner.getEditor()).getTextField();
        tf.setBackground(AppTheme.getCardColor());
        tf.setForeground(AppTheme.getTextColor());
        tf.setCaretColor(AppTheme.getTextColor());
    }

    private void styleComboBox(JComboBox<?> comboBox) {
        comboBox.setBackground(AppTheme.getCardColor());
        comboBox.setForeground(AppTheme.getTextColor());
        comboBox.setBorder(BorderFactory.createLineBorder(AppTheme.getBorderColor(), 1));
        comboBox.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value,
                                                          int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                setBackground(isSelected ? PRIMARY_COLOR : AppTheme.getCardColor());
                setForeground(isSelected ? Color.WHITE : AppTheme.getTextColor());
                return this;
            }
        });
    }

    private void styleTable(JTable table) {
        table.setBackground(CARD_BG);
        table.setForeground(TEXT_PRIMARY);
        table.setGridColor(BORDER_COLOR);
        table.setRowHeight(35);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        table.setSelectionBackground(new Color(59, 130, 246, 50));
        table.setSelectionForeground(TEXT_PRIMARY);
        table.setShowGrid(true);
        table.setIntercellSpacing(new Dimension(0, 0));
    }

    private Color darkenColor(Color color, float factor) {
        return new Color(
                Math.max((int) (color.getRed() * factor), 0),
                Math.max((int) (color.getGreen() * factor), 0),
                Math.max((int) (color.getBlue() * factor), 0));
    }

    // ==================== DATA OPERATIONS ====================

    /**
     * Loads all products from database
     */
    private void loadProducts() {
        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                availableProducts = productService.getAllProducts();
                return null;
            }

            @Override
            protected void done() {
                try {
                    get();
                    updateProductsTable();
                } catch (Exception e) {
                    showError("Failed to load products: " + e.getMessage());
                }
            }
        };
        worker.execute();
    }

    /**
     * Updates the products table display
     */
    private void updateProductsTable() {
        productsTableModel.setRowCount(0);
        for (Product product : availableProducts) {
            productsTableModel.addRow(new Object[] {
                    product.getProductId(),
                    product.getName(),
                    product.getCategory(),
                    String.format("$%.2f", product.getPrice()),
                    product.getStock()
            });
        }
    }

    /**
     * Filters products based on search text
     */
    private void filterProducts() {
        String searchText = txtSearchProduct.getText().toLowerCase();
        productsTableModel.setRowCount(0);

        for (Product product : availableProducts) {
            if (searchText.isEmpty() ||
                    product.getName().toLowerCase().contains(searchText) ||
                    product.getCategory().toLowerCase().contains(searchText)) {
                productsTableModel.addRow(new Object[] {
                        product.getProductId(),
                        product.getName(),
                        product.getCategory(),
                        String.format("$%.2f", product.getPrice()),
                        product.getStock()
                });
            }
        }
    }

    /**
     * Adds selected product to cart
     */
    private void addToCart() {
        int selectedRow = tblAvailableProducts.getSelectedRow();
        if (selectedRow == -1) {
            showWarning("Please select a product first");
            return;
        }

        int productId = (int) productsTableModel.getValueAt(selectedRow, 0);
        String productName = (String) productsTableModel.getValueAt(selectedRow, 1);
        double price = Double.parseDouble(((String) productsTableModel.getValueAt(selectedRow, 3)).replace("$", ""));
        int stock = (int) productsTableModel.getValueAt(selectedRow, 4);
        int quantity = (int) spnQuantity.getValue();

        if (quantity <= 0) {
            showError("Quantity must be greater than 0");
            return;
        }

        if (quantity > stock) {
            showError("Insufficient stock! Only " + stock + " available");
            return;
        }

        // Check if already in cart
        for (CartItem item : cartItems) {
            if (item.productId == productId) {
                if (item.quantity + quantity <= stock) {
                    item.quantity += quantity;
                    updateCartTable();
                    calculateTotals();
                    return;
                } else {
                    showError("Cannot add more than available stock");
                    return;
                }
            }
        }

        // Add new item to cart
        cartItems.add(new CartItem(productId, productName, price, quantity));
        updateCartTable();
        calculateTotals();
    }

    /**
     * Updates the cart table display
     */
    private void updateCartTable() {
        cartTableModel.setRowCount(0);
        for (CartItem item : cartItems) {
            cartTableModel.addRow(new Object[] {
                    item.productName,
                    item.quantity,
                    String.format("$%.2f", item.unitPrice),
                    String.format("$%.2f", item.getSubtotal())
            });
        }
    }

    /**
     * Removes selected item from cart
     */
    private void removeFromCart() {
        int selectedRow = tblCart.getSelectedRow();
        if (selectedRow == -1) {
            showWarning("Please select an item to remove");
            return;
        }

        cartItems.remove(selectedRow);
        updateCartTable();
        calculateTotals();
    }

    /**
     * Clears all items from cart
     */
    private void clearCart() {
        if (cartItems.isEmpty()) {
            showInfo("Cart is already empty");
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
                "Clear all items from cart?",
                "Confirm Clear",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            cartItems.clear();
            updateCartTable();
            calculateTotals();
        }
    }

    /**
     * Calculates subtotal, tax, and grand total
     */
    private void calculateTotals() {
        currentSubtotal = 0.0;
        for (CartItem item : cartItems) {
            currentSubtotal += item.getSubtotal();
        }

        double tax = currentSubtotal * TAX_RATE;
        double grandTotal = currentSubtotal + tax;

        lblSubtotal.setText(String.format("$%.2f", currentSubtotal));
        lblTax.setText(String.format("$%.2f", tax));
        lblGrandTotal.setText(String.format("$%.2f", grandTotal));

        // Update amount due label if cash payment is selected
        if ("Cash".equals(cmbPaymentMethod.getSelectedItem())) {
            updateAmountDueLabel();
            calculateChange();
        }
    }

    /**
     * Gets product stock level
     */
    private int getProductStock(int productId) {
        for (Product product : availableProducts) {
            if (product.getProductId() == productId) {
                return product.getStock();
            }
        }
        return 0;
    }

    /**
     * Validates customer ID
     */
    private void validateCustomer() {
        String customerIdStr = txtCustomerId.getText().trim();
        if (customerIdStr.isEmpty()) {
            lblCustomerInfo.setText("Enter customer ID");
            lblCustomerInfo.setForeground(TEXT_SECONDARY);
            return;
        }

        if (!Validator.isValidInteger(customerIdStr)) {
            lblCustomerInfo.setText("Invalid ID format");
            lblCustomerInfo.setForeground(DANGER_COLOR);
            return;
        }

        int customerId = Integer.parseInt(customerIdStr);

        SwingWorker<Customer, Void> worker = new SwingWorker<Customer, Void>() {
            @Override
            protected Customer doInBackground() throws Exception {
                return customerService.getCustomerById(customerId);
            }

            @Override
            protected void done() {
                try {
                    Customer customer = get();
                    if (customer != null) {
                        lblCustomerInfo.setText(customer.getFullName() + " | " + customer.getContact());
                        lblCustomerInfo.setForeground(SUCCESS_COLOR);
                    } else {
                        lblCustomerInfo.setText("Customer not found");
                        lblCustomerInfo.setForeground(DANGER_COLOR);
                    }
                } catch (Exception e) {
                    lblCustomerInfo.setText("Error loading customer");
                    lblCustomerInfo.setForeground(DANGER_COLOR);
                }
            }
        };
        worker.execute();
    }

    /**
     * Shows dialog to create new customer
     */
    private void showNewCustomerDialog() {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "New Customer", true);
        dialog.setLayout(new BorderLayout(10, 10));
        dialog.setSize(400, 300);
        dialog.setLocationRelativeTo(this);

        JPanel formPanel = new JPanel(new GridLayout(4, 2, 10, 10));
        formPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        formPanel.setBackground(CARD_BG);

        JTextField txtFirstName = new JTextField();
        JTextField txtLastName = new JTextField();
        JTextField txtContact = new JTextField();
        JTextField txtEmail = new JTextField();

        formPanel.add(createDialogLabel("First Name:"));
        formPanel.add(txtFirstName);
        formPanel.add(createDialogLabel("Last Name:"));
        formPanel.add(txtLastName);
        formPanel.add(createDialogLabel("Contact:"));
        formPanel.add(txtContact);
        formPanel.add(createDialogLabel("Email:"));
        formPanel.add(txtEmail);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        buttonPanel.setBackground(CARD_BG);
        JButton btnSave = createButton("Save", SUCCESS_COLOR);
        JButton btnCancel = createButton("Cancel", DANGER_COLOR);

        btnSave.addActionListener(e -> {
            // Create customer object
            Customer customer = new Customer();
            customer.setFullName(txtFirstName.getText().trim() + " " + txtLastName.getText().trim());
            customer.setContact(txtContact.getText().trim());
            customer.setEmail(txtEmail.getText().trim());
            customer.setAddress(""); // Optional
            customer.setLoyaltyPoints(0);

            if (customerService.addCustomer(customer)) {
                showSuccess("Customer added successfully!");
                dialog.dispose();
            } else {
                showError("Failed to add customer");
            }
        });

        btnCancel.addActionListener(e -> dialog.dispose());

        buttonPanel.add(btnSave);
        buttonPanel.add(btnCancel);

        dialog.add(formPanel, BorderLayout.CENTER);
        dialog.add(buttonPanel, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    private JLabel createDialogLabel(String text) {
        JLabel label = new JLabel(text);
        label.setForeground(TEXT_PRIMARY);
        label.setFont(new Font("Segoe UI", Font.BOLD, 12));
        return label;
    }

    /**
     * Processes checkout based on sale type
     */
    private void processCheckout() {
        // Validate cart
        if (cartItems.isEmpty()) {
            showError("Add items to cart before checkout");
            return;
        }

        int customerId;

        // Handle based on sale type
        if (rbQuickCash.isSelected()) {
            // Quick Cash Sale - Use walk-in customer ID
            customerId = WALK_IN_CUSTOMER_ID;
        } else {
            // Regular Sale - Validate customer ID
            String customerIdStr = txtCustomerId.getText().trim();
            if (customerIdStr.isEmpty()) {
                showError("Please enter Customer ID for regular sale");
                txtCustomerId.requestFocus();
                return;
            }

            if (!Validator.isValidInteger(customerIdStr)) {
                showError("Invalid Customer ID");
                return;
            }

            customerId = Integer.parseInt(customerIdStr);

            // Verify customer exists
            Customer customer = customerService.getCustomerById(customerId);
            if (customer == null) {
                showError("Customer not found. Please enter a valid Customer ID or use Quick Cash Sale.");
                return;
            }
        }

        String paymentMethod = (String) cmbPaymentMethod.getSelectedItem();

        // Cash validation
        Double cashReceived = null;
        Double changeGiven = null;

        if ("Cash".equals(paymentMethod)) {
            String cashStr = txtCashReceived.getText().trim();
            if (cashStr.isEmpty()) {
                showError("Please enter cash received amount");
                txtCashReceived.requestFocus();
                return;
            }

            try {
                cashReceived = Double.parseDouble(cashStr);
                double grandTotal = calculateGrandTotal();

                if (cashReceived < grandTotal) {
                    showError("Cash received is less than total amount");
                    txtCashReceived.requestFocus();
                    return;
                }

                changeGiven = cashReceived - grandTotal;

            } catch (NumberFormatException e) {
                showError("Invalid cash amount");
                txtCashReceived.requestFocus();
                return;
            }
        }

        // Create Sale object based on sale type
        Sale sale;
        if (rbQuickCash.isSelected()) {
            sale = Sale.createQuickCashSale(currentUserId, currentSubtotal, cashReceived);
            sale.setNotes("Quick cash sale - Walk-in customer");
        } else {
            sale = Sale.createRegularSale(customerId, currentUserId, currentSubtotal, paymentMethod);
            sale.setCashReceived(cashReceived);
            sale.setChangeGiven(changeGiven);
        }

        // Create SaleDetails
        List<SaleDetail> saleDetails = new ArrayList<>();
        for (CartItem cartItem : cartItems) {
            SaleDetail detail = new SaleDetail(
                    cartItem.productId,
                    cartItem.productName,
                    cartItem.unitPrice,
                    cartItem.quantity);
            detail.setTotalPrice(cartItem.getSubtotal());
            detail.setDiscount(0.0);
            saleDetails.add(detail);
        }
        sale.setSaleDetails(saleDetails);

        // Process checkout
        final String finalPaymentMethod = paymentMethod;
        final Double finalCashReceived = cashReceived;
        final Double finalChangeGiven = changeGiven;
        final boolean isQuickSale = rbQuickCash.isSelected();

        SwingWorker<Integer, Void> worker = new SwingWorker<Integer, Void>() {
            @Override
            protected Integer doInBackground() throws Exception {
                return salesService.createSale(sale);
            }

            @Override
            protected void done() {
                try {
                    int saleId = get();
                    currentSaleId = saleId;

                    SwingUtilities.invokeLater(() -> {
                        String message;
                        if (isQuickSale) {
                            message = String.format(
                                    "Quick Cash Sale successful!\nSale #%d\nTotal: $%.2f\nCash: $%.2f\nChange: $%.2f",
                                    saleId, calculateGrandTotal(), finalCashReceived, finalChangeGiven);
                        } else {
                            message = String.format(
                                    "Regular Sale successful!\nSale #%d\nTotal: $%.2f\nCustomer ID: %d",
                                    saleId, calculateGrandTotal(), customerId);
                        }
                        showSuccess(message);

                        // Enable receipt generation
                        btnGenerateReceipt.setEnabled(true);
                        btnCheckout.setEnabled(false);

                        // Clear cart for next sale
                        cartItems.clear();
                        updateCartTable();
                        calculateTotals();

                        // Clear cash fields
                        txtCashReceived.setText("");
                        lblChangeDue.setText("Change: $0.00");
                    });

                } catch (Exception e) {
                    SwingUtilities.invokeLater(() -> {
                        showError("Checkout failed: " + e.getMessage());
                        e.printStackTrace();
                    });
                }
            }
        };
        worker.execute();
    }

    /**
     * Generates PDF receipt for the last sale
     */
    private void generateReceipt() {
        if (currentSaleId == 0) {
            showError("No sale has been processed yet");
            return;
        }

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Save Receipt");
        fileChooser.setSelectedFile(new File("receipt_" + currentSaleId + ".pdf"));

        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();

            // Ensure filename ends with .pdf
            if (!file.getName().toLowerCase().endsWith(".pdf")) {
                file = new File(file.getParentFile(), file.getName() + ".pdf");
            }

            final File targetFile = file;

            SwingWorker<Boolean, Void> worker = new SwingWorker<Boolean, Void>() {
                @Override
                protected Boolean doInBackground() throws Exception {
                    return salesService.generateReceiptPDF(currentSaleId, targetFile.getAbsolutePath());
                }

                @Override
                protected void done() {
                    try {
                        boolean success = get();
                        if (success) {
                            showSuccess("Receipt generated successfully!");

                            // Ask to open file
                            int option = JOptionPane.showConfirmDialog(SalesUI.this,
                                    "Do you want to open the receipt?",
                                    "Receipt Generated",
                                    JOptionPane.YES_NO_OPTION);

                            if (option == JOptionPane.YES_OPTION && Desktop.isDesktopSupported()) {
                                try {
                                    Desktop.getDesktop().open(targetFile);
                                } catch (IOException ex) {
                                    showError("Cannot open file: " + ex.getMessage());
                                }
                            }
                        } else {
                            showError("Failed to generate receipt");
                        }
                    } catch (Exception e) {
                        showError("Error generating receipt: " + e.getMessage());
                    }
                }
            };
            worker.execute();
        }
    }

    /**
     * Shows daily cash summary
     */
    private void showCashSummary() {
        SwingWorker<List<Object[]>, Void> worker = new SwingWorker<List<Object[]>, Void>() {
            @Override
            protected List<Object[]> doInBackground() throws Exception {
                return salesService.getTodayCashSummary();
            }

            @Override
            protected void done() {
                try {
                    List<Object[]> summary = get();
                    if (!summary.isEmpty()) {
                        Object[] data = summary.get(0);

                        String message = String.format(
                                "DAILY CASH SUMMARY\n" +
                                        "─────────────────────────\n" +
                                        "Transactions: %d\n" +
                                        "Cash Received: $%.2f\n" +
                                        "Change Given: $%.2f\n" +
                                        "Net Cash: $%.2f\n" +
                                        "─────────────────────────\n" +
                                        "Date: %s",
                                (Integer) data[0],
                                (Double) data[1],
                                (Double) data[2],
                                (Double) data[3],
                                new SimpleDateFormat("dd-MMM-yyyy").format(new Date()));

                        JOptionPane.showMessageDialog(SalesUI.this,
                                message,
                                "Daily Cash Summary",
                                JOptionPane.INFORMATION_MESSAGE);
                    } else {
                        showInfo("No cash transactions today");
                    }
                } catch (Exception e) {
                    showError("Error fetching cash summary: " + e.getMessage());
                }
            }
        };
        worker.execute();
    }

    // ==================== INNER CLASSES ====================

    /**
     * CartItem - Represents an item in the shopping cart
     */
    private static class CartItem {
        int productId;
        String productName;
        double unitPrice;
        int quantity;

        CartItem(int productId, String productName, double unitPrice, int quantity) {
            this.productId = productId;
            this.productName = productName;
            this.unitPrice = unitPrice;
            this.quantity = quantity;
        }

        double getSubtotal() {
            return unitPrice * quantity;
        }
    }

    /**
     * RoundedPanel - Custom JPanel with rounded corners
     */
    private static class RoundedPanel extends JPanel {
        private final int radius;

        public RoundedPanel(int radius) {
            this.radius = radius;
            setOpaque(false);
            setBackground(AppTheme.getCardColor());
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(getBackground());
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), radius, radius);
            g2.setColor(AppTheme.getBorderColor());
            g2.setStroke(new BasicStroke(1));
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, radius, radius);
        }
    }

    /**
     * RotatingGradientHeaderPanel - Header with animated gradient
     */
    private static class RotatingGradientHeaderPanel extends JPanel {
        private double angle = 0;
        private final Color color1 = AppTheme.getGradient1();
        private final Color color2 = AppTheme.getGradient2();
        private final javax.swing.Timer timer; // Fully qualified to avoid ambiguity

        public RotatingGradientHeaderPanel() {
            setOpaque(false);
            timer = new javax.swing.Timer(60, e -> { // Fully qualified here too
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

            g2.setColor(new Color(255, 255, 255, 30));
            g2.setStroke(new BasicStroke(1.2f));
            g2.drawRoundRect(1, 1, Math.max(0, w - 3), Math.max(0, h - 3), arc, arc);
            g2.dispose();
        }
    }

    // ==================== MESSAGE HELPERS ====================

    private void showSuccess(String message) {
        JOptionPane.showMessageDialog(this, message, "Success", JOptionPane.INFORMATION_MESSAGE);
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    private void showWarning(String message) {
        JOptionPane.showMessageDialog(this, message, "Warning", JOptionPane.WARNING_MESSAGE);
    }

    private void showInfo(String message) {
        JOptionPane.showMessageDialog(this, message, "Info", JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * Main method for testing
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Sales Management");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(1400, 800);
            frame.setLocationRelativeTo(null);
            frame.setContentPane(new SalesUI());
            frame.setVisible(true);
        });
    }
}