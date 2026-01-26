package ui;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.awt.event.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.ArrayList;

import models.Product;
import models.Sale;
import models.SaleDetail;
import services.SalesService;
import services.ProductService;
import services.CustomerService;
import utils.Validator;

public class SalesUI extends JPanel {
    // Services
    private SalesService salesService;
    private ProductService productService;
    private CustomerService customerService;

    // UI Components
    private JTextField txtCustomerId;
    private JTextField txtSearchProduct;
    private JTable tblProducts;
    private JTable tblCart;
    private DefaultTableModel cartTableModel;
    private JLabel lblTotalAmount;
    private JLabel lblSaleId;
    private JLabel lblSaleDate;
    private JLabel lblCustomerName;

    // Buttons
    private JButton btnAddToCart;
    private JButton btnRemoveItem;
    private JButton btnCheckout;
    private JButton btnGenerateReceipt;
    private JButton btnClearCart;
    private JButton btnViewReceipts;

    // Data
    private List<Product> productList;
    private List<CartItem> cartItems;
    private double currentTotal;
    private int currentSaleId;

    // Colors
    private final Color COLOR_PRIMARY = new Color(41, 128, 185);
    private final Color COLOR_SECONDARY = new Color(52, 152, 219);
    private final Color COLOR_SUCCESS = new Color(46, 204, 113);
    private final Color COLOR_DANGER = new Color(231, 76, 60);
    private final Color COLOR_WARNING = new Color(241, 196, 15);

    public SalesUI() {
        initializeServices();
        initializeUI();
        loadInitialData();
    }

    private void initializeServices() {
        try {
            salesService = new SalesService();
            productService = new ProductService();
            customerService = new CustomerService();
        } catch (Exception e) {
            showErrorMessage("Service Initialization Error",
                    "Failed to initialize services: " + e.getMessage());
        }
    }

    private void initializeUI() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        setBackground(Color.WHITE);

        // Create components
        createComponents();

        // Setup layout
        setupLayout();

        // Setup event listeners
        setupEventListeners();
    }

    private void createComponents() {
        // Text fields
        txtCustomerId = createTextField(15);
        txtSearchProduct = createTextField(20);

        // Labels
        lblTotalAmount = createStyledLabel("$0.00", new Font("Arial", Font.BOLD, 24), COLOR_PRIMARY);
        lblSaleId = createStyledLabel("New Sale", new Font("Arial", Font.PLAIN, 12), Color.DARK_GRAY);
        lblSaleDate = createStyledLabel(new SimpleDateFormat("dd-MMM-yyyy").format(new Date()),
                new Font("Arial", Font.PLAIN, 12), Color.DARK_GRAY);
        lblCustomerName = createStyledLabel("", new Font("Arial", Font.PLAIN, 12), Color.DARK_GRAY);

        // Tables
        createProductsTable();
        createCartTable();

        // Buttons
        btnAddToCart = createStyledButton("Add to Cart", COLOR_SUCCESS);
        btnRemoveItem = createStyledButton("Remove", COLOR_DANGER);
        btnCheckout = createStyledButton("Checkout", COLOR_PRIMARY);
        btnGenerateReceipt = createStyledButton("Generate Receipt", COLOR_SECONDARY);
        btnClearCart = createStyledButton("Clear Cart", COLOR_WARNING);
        btnViewReceipts = createStyledButton("View Receipts", new Color(108, 117, 125));

        // Initialize data
        productList = new ArrayList<>();
        cartItems = new ArrayList<>();
        currentTotal = 0.0;
        currentSaleId = 0;

        // Disable receipt button initially
        btnGenerateReceipt.setEnabled(false);
    }

    private JTextField createTextField(int columns) {
        JTextField textField = new JTextField(columns);
        textField.setPreferredSize(new Dimension(textField.getPreferredSize().width, 30));
        return textField;
    }

    private JLabel createStyledLabel(String text, Font font, Color color) {
        JLabel label = new JLabel(text);
        label.setFont(font);
        label.setForeground(color);
        return label;
    }

    private void createProductsTable() {
        String[] columns = {"ID", "Product Name", "Category", "Price", "Stock"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tblProducts = new JTable(model);
        tblProducts.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tblProducts.getTableHeader().setReorderingAllowed(false);
        tblProducts.setRowHeight(25);

        // Set column widths
        TableColumn column;
        int[] widths = {50, 200, 120, 80, 60};
        for (int i = 0; i < widths.length; i++) {
            column = tblProducts.getColumnModel().getColumn(i);
            column.setPreferredWidth(widths[i]);
        }
    }

    private void createCartTable() {
        String[] columns = {"Product", "Qty", "Price", "Subtotal"};
        cartTableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 1; // Only quantity column is editable
            }

            @Override
            public Class<?> getColumnClass(int columnIndex) {
                if (columnIndex == 1) return Integer.class;
                return String.class;
            }
        };

        tblCart = new JTable(cartTableModel);
        tblCart.getTableHeader().setReorderingAllowed(false);
        tblCart.setRowHeight(25);

        // Add listener for quantity changes
        tblCart.getModel().addTableModelListener(e -> {
            if (e.getColumn() == 1) {
                updateCartQuantity(e.getFirstRow());
            }
        });
    }

    private JButton createStyledButton(String text, Color bgColor) {
        JButton button = new JButton(text);
        button.setBackground(bgColor);
        button.setForeground(Color.WHITE);
        button.setFont(new Font("Segoe UI", Font.BOLD, 12));
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(bgColor.darker(), 1),
                BorderFactory.createEmptyBorder(8, 15, 8, 15)
        ));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Hover effect
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(bgColor.darker());
            }

            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(bgColor);
            }
        });

        return button;
    }

    private void setupLayout() {
        // Top Panel - Header
        add(createHeaderPanel(), BorderLayout.NORTH);

        // Center Panel - Main content
        add(createMainPanel(), BorderLayout.CENTER);

        // Bottom Panel - Total and actions
        add(createFooterPanel(), BorderLayout.SOUTH);
    }

    private JPanel createHeaderPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));

        // Title
        JLabel lblTitle = new JLabel("SALES ENTRY SYSTEM", SwingConstants.CENTER);
        lblTitle.setFont(new Font("Arial", Font.BOLD, 24));
        lblTitle.setForeground(COLOR_PRIMARY);
        lblTitle.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));

        // Info panel
        JPanel infoPanel = new JPanel(new GridLayout(2, 3, 10, 5));
        infoPanel.setBackground(Color.WHITE);

        // Row 1
        infoPanel.add(createLabelPanel("Customer ID:", txtCustomerId));
        infoPanel.add(createLabelPanel("Customer:", lblCustomerName));
        infoPanel.add(createLabelPanel("Sale ID:", lblSaleId));

        // Row 2
        infoPanel.add(createLabelPanel("Search Product:", txtSearchProduct));
        infoPanel.add(createLabelPanel("Date:", lblSaleDate));
        infoPanel.add(new JPanel()); // Empty cell

        panel.add(lblTitle, BorderLayout.NORTH);
        panel.add(infoPanel, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createLabelPanel(String labelText, JComponent component) {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        panel.setBackground(Color.WHITE);
        panel.add(new JLabel(labelText));
        panel.add(component);
        return panel;
    }

    private JPanel createMainPanel() {
        JPanel panel = new JPanel(new GridLayout(1, 2, 10, 0));
        panel.setBackground(Color.WHITE);

        // Left panel - Products
        panel.add(createProductsPanel());

        // Right panel - Cart
        panel.add(createCartPanel());

        return panel;
    }

    private JPanel createProductsPanel() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(COLOR_PRIMARY, 2),
                "AVAILABLE PRODUCTS"
        ));
        panel.setBackground(Color.WHITE);

        // Products table
        JScrollPane scrollPane = new JScrollPane(tblProducts);
        scrollPane.setPreferredSize(new Dimension(450, 300));

        // Add button
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        buttonPanel.setBackground(Color.WHITE);
        buttonPanel.add(btnAddToCart);

        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createCartPanel() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(COLOR_SUCCESS, 2),
                "SHOPPING CART"
        ));
        panel.setBackground(Color.WHITE);

        // Cart table
        JScrollPane scrollPane = new JScrollPane(tblCart);
        scrollPane.setPreferredSize(new Dimension(450, 300));

        // Cart buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        buttonPanel.setBackground(Color.WHITE);
        buttonPanel.add(btnRemoveItem);
        buttonPanel.add(btnClearCart);

        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createFooterPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Total display
        JPanel totalPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        totalPanel.setBackground(Color.WHITE);

        JLabel lblTotalText = new JLabel("TOTAL AMOUNT: ");
        lblTotalText.setFont(new Font("Arial", Font.BOLD, 16));

        totalPanel.add(lblTotalText);
        totalPanel.add(lblTotalAmount);

        // Action buttons
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        actionPanel.setBackground(Color.WHITE);
        actionPanel.add(btnCheckout);
        actionPanel.add(btnGenerateReceipt);
        actionPanel.add(btnViewReceipts);

        panel.add(totalPanel, BorderLayout.NORTH);
        panel.add(actionPanel, BorderLayout.SOUTH);

        return panel;
    }

    private void setupEventListeners() {
        // Customer ID field listener
        txtCustomerId.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                validateCustomer();
            }
        });

        // Search field listener
        txtSearchProduct.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                filterProducts();
            }
        });

        // Product table double-click
        tblProducts.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    addToCart();
                }
            }
        });

        // Button actions
        btnAddToCart.addActionListener(e -> addToCart());
        btnRemoveItem.addActionListener(e -> removeFromCart());
        btnClearCart.addActionListener(e -> clearCart());
        btnCheckout.addActionListener(e -> processCheckout());
        btnGenerateReceipt.addActionListener(e -> generateReceipt());
        btnViewReceipts.addActionListener(e -> viewReceipts());
    }

    private void loadInitialData() {
        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                productList = productService.getAllProducts();
                return null;
            }

            @Override
            protected void done() {
                try {
                    get();
                    updateProductsTable();
                } catch (Exception e) {
                    showErrorMessage("Data Load Error",
                            "Failed to load products: " + e.getMessage());
                }
            }
        };
        worker.execute();
    }

    private void updateProductsTable() {
        DefaultTableModel model = (DefaultTableModel) tblProducts.getModel();
        model.setRowCount(0);

        for (Product product : productList) {
            model.addRow(new Object[]{
                    product.getProductId(),
                    product.getProductName(),
                    product.getCategory(),
                    String.format("$%.2f", product.getPrice()),
                    product.getStockQuantity()
            });
        }
    }

    private void filterProducts() {
        String searchText = txtSearchProduct.getText().toLowerCase();
        DefaultTableModel model = (DefaultTableModel) tblProducts.getModel();
        model.setRowCount(0);

        for (Product product : productList) {
            if (searchText.isEmpty() ||
                    product.getProductName().toLowerCase().contains(searchText) ||
                    String.valueOf(product.getProductId()).contains(searchText) ||
                    (product.getCategory() != null && product.getCategory().toLowerCase().contains(searchText))) {

                model.addRow(new Object[]{
                        product.getProductId(),
                        product.getProductName(),
                        product.getCategory(),
                        String.format("$%.2f", product.getPrice()),
                        product.getStockQuantity()
                });
            }
        }
    }

    private void validateCustomer() {
        String customerIdStr = txtCustomerId.getText().trim();
        if (customerIdStr.isEmpty()) {
            lblCustomerName.setText("");
            return;
        }

        if (!Validator.isValidInteger(customerIdStr)) {
            showWarningMessage("Invalid Customer ID", "Please enter a valid numeric Customer ID.");
            txtCustomerId.requestFocus();
            return;
        }

        int customerId = Integer.parseInt(customerIdStr);

        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
            private String customerName = "";

            @Override
            protected Void doInBackground() throws Exception {
                try {
                    models.Customer customer = customerService.getCustomerById(customerId);
                    if (customer != null) {
                        customerName = customer.getCustomerName();
                    }
                } catch (Exception e) {
                    // Customer not found - this is acceptable
                }
                return null;
            }

            @Override
            protected void done() {
                if (customerName.isEmpty()) {
                    lblCustomerName.setText("(New Customer)");
                    lblCustomerName.setForeground(COLOR_WARNING);
                } else {
                    lblCustomerName.setText(customerName);
                    lblCustomerName.setForeground(COLOR_SUCCESS);
                }
            }
        };
        worker.execute();
    }

    private void addToCart() {
        int selectedRow = tblProducts.getSelectedRow();
        if (selectedRow == -1) {
            showWarningMessage("No Selection", "Please select a product first.");
            return;
        }

        try {
            int productId = (Integer) tblProducts.getValueAt(selectedRow, 0);
            String productName = (String) tblProducts.getValueAt(selectedRow, 1);
            double price = Double.parseDouble(
                    ((String) tblProducts.getValueAt(selectedRow, 3)).replace("$", "")
            );
            int stock = (Integer) tblProducts.getValueAt(selectedRow, 4);

            // Check if already in cart
            for (CartItem item : cartItems) {
                if (item.productId == productId) {
                    if (item.quantity < stock) {
                        item.quantity++;
                        updateCartTable();
                        calculateTotal();
                        return;
                    } else {
                        showErrorMessage("Stock Limit",
                                "Only " + stock + " items available in stock.");
                        return;
                    }
                }
            }

            // Add new item to cart
            if (stock > 0) {
                cartItems.add(new CartItem(productId, productName, price, 1));
                updateCartTable();
                calculateTotal();
            } else {
                showErrorMessage("Out of Stock", "This product is currently out of stock.");
            }

        } catch (Exception e) {
            showErrorMessage("Add to Cart Error", "Failed to add product to cart: " + e.getMessage());
        }
    }

    private void updateCartTable() {
        cartTableModel.setRowCount(0);

        for (CartItem item : cartItems) {
            cartTableModel.addRow(new Object[]{
                    item.productName,
                    item.quantity,
                    String.format("$%.2f", item.unitPrice),
                    String.format("$%.2f", item.getSubtotal())
            });
        }
    }

    private void updateCartQuantity(int rowIndex) {
        if (rowIndex >= 0 && rowIndex < cartItems.size()) {
            try {
                int newQuantity = (Integer) tblCart.getValueAt(rowIndex, 1);

                if (newQuantity > 0) {
                    CartItem item = cartItems.get(rowIndex);

                    // Check stock availability
                    int availableStock = getProductStock(item.productId);
                    if (newQuantity <= availableStock) {
                        item.quantity = newQuantity;
                        calculateTotal();
                    } else {
                        showWarningMessage("Stock Limit",
                                "Only " + availableStock + " items available in stock.");
                        tblCart.setValueAt(item.quantity, rowIndex, 1);
                    }
                } else {
                    cartItems.remove(rowIndex);
                    updateCartTable();
                    calculateTotal();
                }
            } catch (Exception e) {
                showErrorMessage("Invalid Input", "Please enter a valid quantity.");
                updateCartTable();
            }
        }
    }

    private void removeFromCart() {
        int selectedRow = tblCart.getSelectedRow();
        if (selectedRow == -1) {
            showWarningMessage("No Selection", "Please select an item to remove.");
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
                "Remove selected item from cart?",
                "Confirm Removal",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            cartItems.remove(selectedRow);
            updateCartTable();
            calculateTotal();
        }
    }

    private void clearCart() {
        if (cartItems.isEmpty()) {
            showInfoMessage("Cart Empty", "The cart is already empty.");
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
                "Clear all items from cart?",
                "Confirm Clear Cart",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            cartItems.clear();
            updateCartTable();
            calculateTotal();
            btnGenerateReceipt.setEnabled(false);
        }
    }

    private void calculateTotal() {
        currentTotal = 0.0;
        for (CartItem item : cartItems) {
            currentTotal += item.getSubtotal();
        }
        lblTotalAmount.setText(String.format("$%.2f", currentTotal));
    }

    private int getProductStock(int productId) {
        for (Product product : productList) {
            if (product.getProductId() == productId) {
                return product.getStockQuantity();
            }
        }
        return 0;
    }

    private void processCheckout() {
        // Validation
        if (cartItems.isEmpty()) {
            showErrorMessage("Empty Cart", "Add products to cart before checkout.");
            return;
        }

        String customerIdStr = txtCustomerId.getText().trim();
        if (customerIdStr.isEmpty()) {
            showErrorMessage("Customer Required", "Please enter Customer ID.");
            txtCustomerId.requestFocus();
            return;
        }

        if (!Validator.isValidInteger(customerIdStr)) {
            showErrorMessage("Invalid Customer ID", "Customer ID must be a number.");
            txtCustomerId.requestFocus();
            return;
        }

        int customerId = Integer.parseInt(customerIdStr);

        // Ask for payment method
        String[] options = {"Cash", "Credit Card", "Debit Card", "Mobile Payment"};
        String paymentMethod = (String) JOptionPane.showInputDialog(this,
                "Select payment method:",
                "Payment Method",
                JOptionPane.QUESTION_MESSAGE,
                null,
                options,
                options[0]);

        if (paymentMethod == null) {
            return; // User cancelled
        }

        // Create sale object
        Sale sale = new Sale(customerId, currentTotal);
        sale.setPaymentMethod(paymentMethod);
        sale.setCreatedBy(System.getProperty("user.name"));

        // Add sale details
        List<SaleDetail> saleDetails = new ArrayList<>();
        for (CartItem cartItem : cartItems) {
            SaleDetail detail = new SaleDetail(
                    cartItem.productId,
                    cartItem.productName,
                    cartItem.unitPrice,
                    cartItem.quantity
            );
            saleDetails.add(detail);
        }
        sale.setSaleDetails(saleDetails);

        // Process checkout in background
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

                    showSuccessMessage("Checkout Successful",
                            "Sale #" + saleId + " has been saved.\n" +
                                    "Total: $" + String.format("%.2f", currentTotal));

                    // Enable receipt generation
                    btnGenerateReceipt.setEnabled(true);

                    // Reset for next sale
                    resetForNewSale();

                } catch (Exception e) {
                    showErrorMessage("Checkout Failed",
                            "Failed to process checkout: " + e.getMessage());
                }
            }
        };
        worker.execute();
    }

    private void generateReceipt() {
        if (currentSaleId == 0) {
            showErrorMessage("No Sale", "No sale has been processed yet.");
            return;
        }

        // Ask for file location
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Save Receipt As");
        fileChooser.setSelectedFile(new File("receipt_" + currentSaleId + ".pdf"));

        int userSelection = fileChooser.showSaveDialog(this);
        if (userSelection != JFileChooser.APPROVE_OPTION) {
            return; // User cancelled
        }

        File file = fileChooser.getSelectedFile();

        // Generate PDF in background
        SwingWorker<Boolean, Void> worker = new SwingWorker<Boolean, Void>() {
            @Override
            protected Boolean doInBackground() throws Exception {
                return salesService.generateReceiptPDF(currentSaleId, file.getAbsolutePath());
            }

            @Override
            protected void done() {
                try {
                    boolean success = get();
                    if (success) {
                        showSuccessMessage("Receipt Generated",
                                "Receipt saved to:\n" + file.getAbsolutePath());

                        // Ask if user wants to open the file
                        int open = JOptionPane.showConfirmDialog(SalesUI.this,
                                "Do you want to open the receipt now?",
                                "Open Receipt",
                                JOptionPane.YES_NO_OPTION);

                        if (open == JOptionPane.YES_OPTION) {
                            if (Desktop.isDesktopSupported()) {
                                Desktop.getDesktop().open(file);
                            }
                        }
                    } else {
                        showErrorMessage("Generation Failed",
                                "Failed to generate receipt.");
                    }
                } catch (Exception e) {
                    showErrorMessage("PDF Error",
                            "Error generating PDF: " + e.getMessage());
                }
            }
        };
        worker.execute();
    }

    private void viewReceipts() {
        // Open receipts directory or show list of recent receipts
        try {
            File receiptsDir = new File("receipts");
            if (!receiptsDir.exists()) {
                receiptsDir.mkdir();
            }

            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().open(receiptsDir);
            }
        } catch (Exception e) {
            showErrorMessage("Directory Error",
                    "Cannot open receipts directory: " + e.getMessage());
        }
    }

    private void resetForNewSale() {
        // Clear cart
        cartItems.clear();
        updateCartTable();
        calculateTotal();

        // Reset customer info
        txtCustomerId.setText("");
        lblCustomerName.setText("");

        // Update sale info
        lblSaleId.setText("New Sale");
        lblSaleDate.setText(new SimpleDateFormat("dd-MMM-yyyy").format(new Date()));

        // Disable receipt button
        btnGenerateReceipt.setEnabled(false);

        // Refresh product list (stock may have changed)
        loadInitialData();
    }

    // Helper class for cart items
    private class CartItem {
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

    // Message dialogs
    private void showErrorMessage(String title, String message) {
        JOptionPane.showMessageDialog(this, message, title, JOptionPane.ERROR_MESSAGE);
    }

    private void showWarningMessage(String title, String message) {
        JOptionPane.showMessageDialog(this, message, title, JOptionPane.WARNING_MESSAGE);
    }

    private void showInfoMessage(String title, String message) {
        JOptionPane.showMessageDialog(this, message, title, JOptionPane.INFORMATION_MESSAGE);
    }

    private void showSuccessMessage(String title, String message) {
        JOptionPane.showMessageDialog(this, message, title, JOptionPane.INFORMATION_MESSAGE);
    }

    // Test method
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Sales Entry System");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(1200, 700);
            frame.setLocationRelativeTo(null);

            SalesUI salesUI = new SalesUI();
            frame.add(salesUI);
            frame.setVisible(true);
        });
    }
}