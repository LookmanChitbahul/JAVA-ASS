package ui;

import java.awt.*; 
import java.awt.event.*;
import java.util.ArrayList;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import models.Product;
import services.ProductService;

/**
 * ProductUI - User Interface for Product Management
 * Provides a comprehensive GUI for managing products with dark blue theme
 */
public class ProductUI extends JPanel {

    private final ProductService ProductService;
    private JTable productTable;
    private DefaultTableModel tableModel;
    private JTextField searchField;
    private JComboBox<String> categoryFilter;
    private JTextField nameField;
    private JTextField categoryField;
    private JTextField priceField;
    private JSpinner stockSpinner;
    private JButton addButton;
    private JButton refreshButton;
    private JButton clearButton;
    private final String currentUserRole;
    private boolean isEditMode = false;
    private int editingProductId = -1;
    
    // Dark Blue Theme Colors
    private static final Color DARK_BG = new Color(13, 27, 42);              // Deep Navy
    private static final Color DARKER_BG = new Color(8, 15, 25);             // Very Dark Navy
    private static final Color CARD_BG = new Color(25, 42, 65);              // Dark Blue Card
    private static final Color PRIMARY_COLOR = new Color(59, 130, 246);      // Bright Blue
    private static final Color SECONDARY_COLOR = new Color(147, 197, 253);   // Light Blue
    private static final Color ACCENT_COLOR = new Color(96, 165, 250);       // Medium Blue
    private static final Color SUCCESS_COLOR = new Color(34, 197, 94);       // Green
    private static final Color WARNING_COLOR = new Color(251, 146, 60);      // Orange
    private static final Color DANGER_COLOR = new Color(239, 68, 68);        // Red
    private static final Color TEXT_PRIMARY = new Color(241, 245, 249);      // Light Text
    private static final Color TEXT_SECONDARY = new Color(203, 213, 225);    // Secondary Text
    private static final Color BORDER_COLOR = new Color(51, 85, 129);        // Dark Blue Border

    /**
     * Constructor for ProductUI
     * Initializes the UI components and loads product data
     * @param userRole The role of the current user (for access control)
     */
    public ProductUI(String userRole) {
        this.currentUserRole = userRole;
        this.ProductService = new ProductService();
        
        // Initialize the UI components and display them
        initializeComponents();
        setupLayout();
        loadProductData();
    }

    /**
     * Initializes all UI components including table, buttons, and form fields
     * Sets up event listeners for user interactions
     */
    private void initializeComponents() {
        // Search field with dark theme
        searchField = new JTextField(20);
        searchField.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        searchField.setBorder(new RoundedBorder(8, PRIMARY_COLOR));
        searchField.setBackground(CARD_BG);
        searchField.setForeground(TEXT_PRIMARY);
        searchField.setCaretColor(PRIMARY_COLOR);
        
        // Category filter
        categoryFilter = new JComboBox<>(new String[]{
            "All Categories", "Electronics", "Clothing", "Food", 
            "Beverages", "Stationery", "Other"
        });
        categoryFilter.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        categoryFilter.setBackground(CARD_BG);
        categoryFilter.setForeground(TEXT_PRIMARY);
        
        // Search listeners
        searchField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                filterProducts();
            }
        });
        
        categoryFilter.addActionListener(e -> filterProducts());
        
        // Table setup
        String[] columnNames = {"ID", "Product Name", "Category", "Price (Rs)", "Stock", "Actions"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 5;
            }
        };
        
        productTable = new JTable(tableModel);
        productTable.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        productTable.setRowHeight(45);
        productTable.setShowGrid(true);
        productTable.setGridColor(BORDER_COLOR);
        productTable.setBackground(CARD_BG);
        productTable.setForeground(TEXT_PRIMARY);
        productTable.setSelectionBackground(ACCENT_COLOR);
        productTable.setSelectionForeground(TEXT_PRIMARY);
        
        // Header styling
        JTableHeader header = productTable.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 14));
        header.setBackground(PRIMARY_COLOR);
        header.setForeground(Color.WHITE);
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, BORDER_COLOR));
        header.setPreferredSize(new Dimension(0, 50));
        
        // Column widths
        productTable.getColumnModel().getColumn(0).setPreferredWidth(50);
        productTable.getColumnModel().getColumn(1).setPreferredWidth(180);
        productTable.getColumnModel().getColumn(2).setPreferredWidth(110);
        productTable.getColumnModel().getColumn(3).setPreferredWidth(100);
        productTable.getColumnModel().getColumn(4).setPreferredWidth(80);
        productTable.getColumnModel().getColumn(5).setPreferredWidth(180);
        
        productTable.getColumnModel().getColumn(5).setCellRenderer(new ButtonRenderer());
        productTable.getColumnModel().getColumn(5).setCellEditor(new ButtonEditor(new JCheckBox()));
        
        // Form fields
        nameField = new JTextField(20);
        categoryField = new JTextField(20);
        priceField = new JTextField(20);
        
        styleInputField(nameField);
        styleInputField(categoryField);
        styleInputField(priceField);
        
        SpinnerNumberModel spinnerModel = new SpinnerNumberModel(0, 0, 10000, 1);
        stockSpinner = new JSpinner(spinnerModel);
        styleInputField(stockSpinner);
        
        // Buttons
        addButton = new JButton("Add Product");
        refreshButton = new JButton("Refresh");
        clearButton = new JButton("Clear Form");
        
        styleButton(addButton, SUCCESS_COLOR);
        styleButton(refreshButton, PRIMARY_COLOR);
        styleButton(clearButton, BORDER_COLOR);
    }

    /**
     * Styles input fields with rounded borders and consistent appearance
     * @param component The component to style
     */
    private void styleInputField(JComponent component) {
        component.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        if (component instanceof JTextField) {
            JTextField field = (JTextField) component;
            field.setBorder(new RoundedBorder(8, BORDER_COLOR));
            field.setBackground(DARKER_BG);
            field.setForeground(TEXT_PRIMARY);
            field.setCaretColor(PRIMARY_COLOR);
        } else if (component instanceof JSpinner) {
            component.setBorder(new RoundedBorder(8, BORDER_COLOR));
            component.setBackground(DARKER_BG);
            component.setForeground(TEXT_PRIMARY);
        }
    }

    /**
     * Sets up the main layout of the UI using a modern card-based design
     */
    private void setupLayout() {
        setBackground(DARK_BG);
        setLayout(new BorderLayout(0, 0));
        
        JPanel headerPanel = createHeaderPanel();
        JPanel contentPanel = new JPanel(new BorderLayout(15, 15));
        contentPanel.setBackground(DARK_BG);
        contentPanel.setBorder(new EmptyBorder(15, 15, 15, 15));
        
        JPanel searchPanel = createSearchPanel();
        JPanel tableCard = createTableCard();
        JPanel formCard = createFormPanel();
        
        contentPanel.add(searchPanel, BorderLayout.NORTH);
        
        JPanel centerPanel = new JPanel(new BorderLayout(15, 0));
        centerPanel.setBackground(DARK_BG);
        centerPanel.add(tableCard, BorderLayout.CENTER);
        centerPanel.add(formCard, BorderLayout.EAST);
        
        contentPanel.add(centerPanel, BorderLayout.CENTER);
        
        add(headerPanel, BorderLayout.NORTH);
        add(contentPanel, BorderLayout.CENTER);
    }

    /**
     * Creates the header panel with title and gradient background
     * @return JPanel configured header
     */
    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                GradientPaint gradient = new GradientPaint(0, 0, PRIMARY_COLOR, getWidth(), 0, ACCENT_COLOR);
                g2d.setPaint(gradient);
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        
        headerPanel.setPreferredSize(new Dimension(0, 80));
        headerPanel.setLayout(new BorderLayout());
        headerPanel.setBorder(new EmptyBorder(20, 30, 20, 30));
        
        JLabel titleLabel = new JLabel("📦 Product Management System");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        titleLabel.setForeground(Color.WHITE);
        
        headerPanel.add(titleLabel, BorderLayout.WEST);
        
        return headerPanel;
    }

    /**
     * Creates the search and filter panel as a card
     * @return JPanel with search components
     */
    private JPanel createSearchPanel() {
        JPanel searchCard = new RoundedPanel(15, DARK_BG);
        searchCard.setLayout(new FlowLayout(FlowLayout.LEFT, 20, 15));
        searchCard.setBorder(new EmptyBorder(15, 20, 15, 20));
        searchCard.setBackground(CARD_BG);
        
        JLabel searchLabel = new JLabel("🔍 Search:");
        searchLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        searchLabel.setForeground(TEXT_PRIMARY);
        
        JLabel filterLabel = new JLabel("📂 Category:");
        filterLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        filterLabel.setForeground(TEXT_PRIMARY);
        
        searchCard.add(searchLabel);
        searchCard.add(searchField);
        searchCard.add(filterLabel);
        searchCard.add(categoryFilter);
        searchCard.add(refreshButton);
        
        return searchCard;
    }

    /**
     * Creates the table panel as a card
     * @return JPanel with table
     */
    private JPanel createTableCard() {
        JPanel tableCard = new RoundedPanel(15, DARK_BG);
        tableCard.setLayout(new BorderLayout());
        tableCard.setBackground(CARD_BG);
        tableCard.setBorder(new CompoundBorder(
            new LineBorder(BORDER_COLOR, 2, true),
            new EmptyBorder(15, 15, 15, 15)
        ));
        
        JScrollPane tableScrollPane = new JScrollPane(productTable);
        tableScrollPane.setBorder(BorderFactory.createEmptyBorder());
        tableScrollPane.getViewport().setBackground(CARD_BG);
        tableScrollPane.setBackground(CARD_BG);
        
        // Customize scrollbar
        tableScrollPane.getVerticalScrollBar().setUI(new CustomScrollBarUI());
        tableScrollPane.getHorizontalScrollBar().setUI(new CustomScrollBarUI());
        
        tableCard.add(tableScrollPane, BorderLayout.CENTER);
        
        return tableCard;
    }

    /**
     * Creates the right form panel as a card
     * @return JPanel configured with form components
     */
    private JPanel createFormPanel() {
        JPanel formCard = new RoundedPanel(15, DARK_BG);
        formCard.setLayout(new BoxLayout(formCard, BoxLayout.Y_AXIS));
        formCard.setBackground(CARD_BG);
        formCard.setBorder(new CompoundBorder(
            new LineBorder(BORDER_COLOR, 2, true),
            new EmptyBorder(25, 25, 25, 25)
        ));
        formCard.setPreferredSize(new Dimension(380, 0));
        
        JLabel formTitle = new JLabel("📝 Product Details");
        formTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        formTitle.setForeground(PRIMARY_COLOR);
        formTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        formCard.add(formTitle);
        formCard.add(Box.createVerticalStrut(25));
        
        formCard.add(createFormField("Product Name:", nameField));
        formCard.add(Box.createVerticalStrut(12));
        formCard.add(createFormField("Category:", categoryField));
        formCard.add(Box.createVerticalStrut(12));
        formCard.add(createFormField("Price (Rs):", priceField));
        formCard.add(Box.createVerticalStrut(12));
        formCard.add(createFormField("Stock Quantity:", stockSpinner));
        formCard.add(Box.createVerticalStrut(30));
        
        JSeparator separator = new JSeparator();
        separator.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        separator.setBackground(BORDER_COLOR);
        separator.setForeground(BORDER_COLOR);
        formCard.add(separator);
        formCard.add(Box.createVerticalStrut(20));
        
        JPanel buttonPanel = new JPanel(new GridLayout(2, 1, 0, 10));
        buttonPanel.setBackground(CARD_BG);
        buttonPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));
        
        addButton.addActionListener(e -> handleAddOrUpdateProduct());
        refreshButton.addActionListener(e -> loadProductData());
        clearButton.addActionListener(e -> clearForm());
        
        buttonPanel.add(addButton);
        buttonPanel.add(clearButton);
        
        formCard.add(buttonPanel);
        formCard.add(Box.createVerticalGlue());
        
        return formCard;
    }

    /**
     * Helper method to create a form field with label and component
     * @param labelText The text for the field label
     * @param component The input component
     * @return JPanel containing the label and component
     */
    private JPanel createFormField(String labelText, JComponent component) {
        JPanel fieldPanel = new JPanel();
        fieldPanel.setLayout(new BoxLayout(fieldPanel, BoxLayout.Y_AXIS));
        fieldPanel.setBackground(CARD_BG);
        fieldPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));
        
        JLabel label = new JLabel(labelText);
        label.setFont(new Font("Segoe UI", Font.BOLD, 12));
        label.setForeground(TEXT_SECONDARY);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        component.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));
        component.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        fieldPanel.add(label);
        fieldPanel.add(Box.createVerticalStrut(5));
        fieldPanel.add(component);
        
        return fieldPanel;
    }

    /**
     * Styles a button with vibrant colors and modern appearance
     * @param button The button to style
     * @param color The background color for the button
     */
    private void styleButton(JButton button, Color color) {
        button.setBackground(color);
        button.setForeground(Color.WHITE);
        button.setFont(new Font("Segoe UI", Font.BOLD, 13));
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setContentAreaFilled(true);
        button.setOpaque(true);
        button.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(adjustBrightness(color, 1.15f));
            }
            
            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(color);
            }
            
            @Override
            public void mousePressed(MouseEvent e) {
                button.setBackground(adjustBrightness(color, 0.85f));
            }
            
            @Override
            public void mouseReleased(MouseEvent e) {
                button.setBackground(adjustBrightness(color, 1.15f));
            }
        });
    }

    /**
     * Adjusts brightness of a color
     * @param color The original color
     * @param factor Brightness factor (>1 = lighter, <1 = darker)
     * @return Adjusted color
     */
    private Color adjustBrightness(Color color, float factor) {
        return new Color(
            Math.min(255, (int)(color.getRed() * factor)),
            Math.min(255, (int)(color.getGreen() * factor)),
            Math.min(255, (int)(color.getBlue() * factor))
        );
    }

    /**
     * Loads all products from the database and populates the table
     */
    private void loadProductData() {
        tableModel.setRowCount(0);
        
        try {
            ArrayList<Product> products = ProductService.getAllProducts();
            
            for (Product product : products) {
                Object[] rowData = {
                    product.getProductId(),
                    product.getName(),
                    product.getCategory(),
                    String.format("Rs %.2f", product.getPrice()),
                    product.getStock(),
                    "Actions"
                };
                tableModel.addRow(rowData);
            }
            
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                "Error loading products: " + ex.getMessage(),
                "Database Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Filters products based on search term and selected category
     */
    private void filterProducts() {
        String searchText = searchField.getText().toLowerCase();
        String selectedCategory = (String) categoryFilter.getSelectedItem();

        tableModel.setRowCount(0);
        
        try {
            ArrayList<Product> products = ProductService.getAllProducts();
            
            for (Product product : products) {
                boolean matchesSearch = searchText.isEmpty() || 
                    product.getName().toLowerCase().contains(searchText) ||
                    product.getCategory().toLowerCase().contains(searchText);
                
                boolean matchesCategory = selectedCategory.equals("All Categories") ||
                    product.getCategory().equals(selectedCategory);
                
                if (matchesSearch && matchesCategory) {
                    Object[] rowData = {
                        product.getProductId(),
                        product.getName(),
                        product.getCategory(),
                        String.format("Rs %.2f", product.getPrice()),
                        product.getStock(),
                        "Actions"
                    };
                    tableModel.addRow(rowData);
                }
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                "Error filtering products: " + ex.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Handles adding a new product or updating an existing product
     */
    private void handleAddOrUpdateProduct() {
        if (!validateInputs()) {
            return;
        }
        
        try {
            String name = nameField.getText().trim();
            String category = categoryField.getText().trim();
            double price = Double.parseDouble(priceField.getText().trim());
            int stock = (Integer) stockSpinner.getValue();
            
            if (isEditMode) {
                Product product = new Product(editingProductId, name, category, price, stock);
                boolean success = ProductService.updateProduct(product);
                
                if (success) {
                    JOptionPane.showMessageDialog(this,
                        "Product updated successfully!",
                        "Success",
                        JOptionPane.INFORMATION_MESSAGE);
                    clearForm();
                    loadProductData();
                } else {
                    JOptionPane.showMessageDialog(this,
                        "Failed to update product.",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                }
                
            } else {
                Product product = new Product(name, category, price, stock);
                boolean success = ProductService.addProduct(product);
                
                if (success) {
                    JOptionPane.showMessageDialog(this,
                        "Product added successfully!",
                        "Success",
                        JOptionPane.INFORMATION_MESSAGE);
                    clearForm();
                    loadProductData();
                } else {
                    JOptionPane.showMessageDialog(this,
                        "Failed to add product.",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                }
            }
            
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this,
                "Invalid price format. Please enter a valid number.",
                "Input Error",
                JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                "Error: " + ex.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Loads product details into the form for editing
     * @param productId The ID of the product to edit
     */
    private void editProduct(int productId) {
        try {
            Product product = ProductService.getProductById(productId);
            
            if (product != null) {
                nameField.setText(product.getName());
                categoryField.setText(product.getCategory());
                priceField.setText(String.valueOf(product.getPrice()));
                stockSpinner.setValue(product.getStock());
                
                isEditMode = true;
                editingProductId = productId;
                addButton.setText("Update Product");
                styleButton(addButton, WARNING_COLOR);
                
            } else {
                JOptionPane.showMessageDialog(this,
                    "Product not found!",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            }
            
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                "Error loading product: " + ex.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Deletes a product after user confirmation
     * @param productId The ID of the product to delete
     */
    private void deleteProduct(int productId) {
        int confirm = JOptionPane.showConfirmDialog(this,
            "Are you sure you want to delete this product?\nThis action cannot be undone.",
            "Confirm Delete",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE);
        
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                boolean success = ProductService.deleteProduct(productId);
                
                if (success) {
                    JOptionPane.showMessageDialog(this,
                        "Product deleted successfully!",
                        "Success",
                        JOptionPane.INFORMATION_MESSAGE);
                    
                    loadProductData();
                } else {
                    JOptionPane.showMessageDialog(this,
                        "Failed to delete product.",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                }
                
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this,
                    "Error deleting product: " + ex.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /**
     * Clears all form fields and resets to add mode
     */
    private void clearForm() {
        nameField.setText("");
        categoryField.setText("");
        priceField.setText("");
        stockSpinner.setValue(0);
        
        isEditMode = false;
        editingProductId = -1;
        addButton.setText("Add Product");
        styleButton(addButton, SUCCESS_COLOR);
    }

    /**
     * Validates all form inputs
     * @return true if all inputs are valid
     */
    private boolean validateInputs() {
        if (nameField.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Product name cannot be empty!",
                "Validation Error",
                JOptionPane.WARNING_MESSAGE);
            nameField.requestFocus();
            return false;
        }
        
        if (categoryField.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Category cannot be empty!",
                "Validation Error",
                JOptionPane.WARNING_MESSAGE);
            categoryField.requestFocus();
            return false;
        }
        
        if (priceField.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Price cannot be empty!",
                "Validation Error",
                JOptionPane.WARNING_MESSAGE);
            priceField.requestFocus();
            return false;
        }
        
        try {
            double price = Double.parseDouble(priceField.getText().trim());
            if (price <= 0) {
                JOptionPane.showMessageDialog(this,
                    "Price must be greater than 0!",
                    "Validation Error",
                    JOptionPane.WARNING_MESSAGE);
                priceField.requestFocus();
                return false;
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this,
                "Please enter a valid price!",
                "Validation Error",
                JOptionPane.WARNING_MESSAGE);
            priceField.requestFocus();
            return false;
        }
        
        int stock = (Integer) stockSpinner.getValue();
        if (stock < 0) {
            JOptionPane.showMessageDialog(this,
                "Stock cannot be negative!",
                "Validation Error",
                JOptionPane.WARNING_MESSAGE);
            stockSpinner.requestFocus();
            return false;
        }
        
        return true;
    }

    /**
     * ButtonRenderer - Custom table cell renderer for Edit/Delete buttons
     */
    class ButtonRenderer extends JPanel implements TableCellRenderer {
        private final JButton editButton;
        private final JButton deleteButton;
        
        public ButtonRenderer() {
            setLayout(new FlowLayout(FlowLayout.CENTER, 8, 5));
            setBackground(CARD_BG);
            
            editButton = new JButton("✏️ Edit");
            editButton.setBackground(PRIMARY_COLOR);
            editButton.setForeground(Color.WHITE);
            editButton.setFocusPainted(false);
            editButton.setFont(new Font("Segoe UI", Font.BOLD, 11));
            editButton.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
            
            deleteButton = new JButton("🗑️ Delete");
            deleteButton.setBackground(DANGER_COLOR);
            deleteButton.setForeground(Color.WHITE);
            deleteButton.setFocusPainted(false);
            deleteButton.setFont(new Font("Segoe UI", Font.BOLD, 11));
            deleteButton.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
            
            add(editButton);
            add(deleteButton);
        }
        
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            setBackground(isSelected ? ACCENT_COLOR : CARD_BG);
            return this;
        }
    }

    /**
     * ButtonEditor - Custom table cell editor for button actions
     */
    class ButtonEditor extends DefaultCellEditor {
        private final JPanel panel;
        private final JButton editButton;
        private final JButton deleteButton;
        private int currentRow;
        
        public ButtonEditor(JCheckBox checkBox) {
            super(checkBox);
            
            panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 5));
            panel.setBackground(CARD_BG);
            
            editButton = new JButton("✏️ Edit");
            editButton.setBackground(PRIMARY_COLOR);
            editButton.setForeground(Color.WHITE);
            editButton.setFocusPainted(false);
            editButton.setFont(new Font("Segoe UI", Font.BOLD, 11));
            editButton.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
            editButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
            
            deleteButton = new JButton("🗑️ Delete");
            deleteButton.setBackground(DANGER_COLOR);
            deleteButton.setForeground(Color.WHITE);
            deleteButton.setFocusPainted(false);
            deleteButton.setFont(new Font("Segoe UI", Font.BOLD, 11));
            deleteButton.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
            deleteButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
            
            editButton.addActionListener(e -> {
                fireEditingStopped();
                int productId = (Integer) tableModel.getValueAt(currentRow, 0);
                editProduct(productId);
            });
            
            deleteButton.addActionListener(e -> {
                fireEditingStopped();
                int productId = (Integer) tableModel.getValueAt(currentRow, 0);
                deleteProduct(productId);
            });
            
            panel.add(editButton);
            panel.add(deleteButton);
        }
        
        @Override
        public Component getTableCellEditorComponent(JTable table, Object value,
                boolean isSelected, int row, int column) {
            currentRow = row;
            panel.setBackground(ACCENT_COLOR);
            return panel;
        }
        
        @Override
        public Object getCellEditorValue() {
            return "Actions";
        }
    }

    /**
     * RoundedPanel - Custom JPanel with rounded corners
     */
    static class RoundedPanel extends JPanel {
        private int cornerRadius;
        
        public RoundedPanel(int radius, Color bg) {
            super();
            cornerRadius = radius;
            setOpaque(false);
        }
        
        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            
            g2d.setColor(getBackground());
            g2d.fillRoundRect(0, 0, getWidth(), getHeight(), cornerRadius, cornerRadius);
            
            super.paintComponent(g);
        }
    }

    /**
     * RoundedBorder - Custom border with rounded corners
     */
    static class RoundedBorder extends AbstractBorder {
        private int radius;
        private Color color;
        
        public RoundedBorder(int radius, Color color) {
            this.radius = radius;
            this.color = color;
        }
        
        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setColor(color);
            g2d.setStroke(new BasicStroke(2));
            g2d.drawRoundRect(x, y, width - 1, height - 1, radius, radius);
        }
        
        @Override
        public Insets getBorderInsets(Component c) {
            return new Insets(8, 8, 8, 8);
        }
    }

    /**
     * Custom scrollbar UI for consistent theming
     */
    static class CustomScrollBarUI extends javax.swing.plaf.basic.BasicScrollBarUI {
        @Override
        protected void configureScrollBarColors() {
            this.thumbColor = PRIMARY_COLOR;
            this.trackColor = CARD_BG;
            this.thumbDarkShadowColor = BORDER_COLOR;
        }
    }

    /**
     * Main method - Entry point for the ProductUI application
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Product Management System");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(1400, 800);
            frame.setLocationRelativeTo(null);
            frame.setResizable(true);
            frame.setBackground(DARK_BG);
            
            ProductUI productUI = new ProductUI("Admin");
            frame.add(productUI);
            
            frame.setVisible(true);
        });
    }
}
