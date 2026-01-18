-- Smart Retail System - Database Schema
-- Create database
CREATE DATABASE IF NOT EXISTS smart_retail_db;
USE smart_retail_db;


Create Table login(
id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
);

-- Products Table
CREATE TABLE IF NOT EXISTS Products (
    product_id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    category VARCHAR(50) NOT NULL,
    price DECIMAL(10, 2) NOT NULL CHECK (price > 0),
    stock INT NOT NULL DEFAULT 0 CHECK (stock >= 0),
    description TEXT,
    supplier VARCHAR(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_category (category),
    INDEX idx_name (name)
) ENGINE=InnoDB;

-- Customers Table
CREATE TABLE IF NOT EXISTS Customers (
    customer_id INT AUTO_INCREMENT PRIMARY KEY,
    first_name VARCHAR(50) NOT NULL,
    last_name VARCHAR(50) NOT NULL,
    email VARCHAR(100),
    phone VARCHAR(20),
    address VARCHAR(200),
    city VARCHAR(50),
    postal_code VARCHAR(10),
    country VARCHAR(50),
    loyalty_points INT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_email (email),
    INDEX idx_phone (phone)
) ENGINE=InnoDB;

-- Sales Table (Transactions)
CREATE TABLE IF NOT EXISTS Sales (
    sale_id INT AUTO_INCREMENT PRIMARY KEY,
    customer_id INT,
    user_id INT NOT NULL,
    sale_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    total_amount DECIMAL(12, 2) NOT NULL,
    discount DECIMAL(10, 2) DEFAULT 0,
    final_amount DECIMAL(12, 2) NOT NULL,
    payment_method ENUM('Cash', 'Card', 'Cheque', 'Online') DEFAULT 'Cash',
    status ENUM('Completed', 'Pending', 'Cancelled') DEFAULT 'Completed',
    notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (customer_id) REFERENCES Customers(customer_id) ON DELETE SET NULL,
    FOREIGN KEY (user_id) REFERENCES Users(user_id) ON DELETE RESTRICT,
    INDEX idx_sale_date (sale_date),
    INDEX idx_customer_id (customer_id)
) ENGINE=InnoDB;

-- Sale Details Table (Individual items in each sale)
CREATE TABLE IF NOT EXISTS Sale_Details (
    sale_detail_id INT AUTO_INCREMENT PRIMARY KEY,
    sale_id INT NOT NULL,
    product_id INT NOT NULL,
    quantity INT NOT NULL CHECK (quantity > 0),
    unit_price DECIMAL(10, 2) NOT NULL,
    total_price DECIMAL(12, 2) NOT NULL,
    discount DECIMAL(10, 2) DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (sale_id) REFERENCES Sales(sale_id) ON DELETE CASCADE,
    FOREIGN KEY (product_id) REFERENCES Products(product_id) ON DELETE RESTRICT,
    INDEX idx_sale_id (sale_id),
    INDEX idx_product_id (product_id)
) ENGINE=InnoDB;

-- Audit Logs Table (Track all user actions)
CREATE TABLE IF NOT EXISTS Audit_Logs (
    log_id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    action VARCHAR(100) NOT NULL,
    table_name VARCHAR(50),
    record_id INT,
    details TEXT,
    ip_address VARCHAR(45),
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES Users(user_id) ON DELETE RESTRICT,
    INDEX idx_user_id (user_id),
    INDEX idx_timestamp (timestamp),
    INDEX idx_action (action)
) ENGINE=InnoDB;

-- Analytics Table (Summary of sales data)
CREATE TABLE IF NOT EXISTS Analytics (
    analytics_id INT AUTO_INCREMENT PRIMARY KEY,
    date DATE NOT NULL UNIQUE,
    total_sales DECIMAL(12, 2) DEFAULT 0,
    total_revenue DECIMAL(12, 2) DEFAULT 0,
    total_transactions INT DEFAULT 0,
    total_customers INT DEFAULT 0,
    best_product INT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (best_product) REFERENCES Products(product_id) ON DELETE SET NULL,
    INDEX idx_date (date)
) ENGINE=InnoDB;


-- Create sample products
INSERT INTO Products (name, category, price, stock, supplier) VALUES
('Laptop HP', 'Electronics', 45000.00, 5, 'Tech Supplies Ltd'),
('Mouse Wireless', 'Electronics', 800.00, 50, 'Tech Supplies Ltd'),
('USB-C Cable', 'Electronics', 350.00, 100, 'Tech Supplies Ltd'),
('Blue Jeans', 'Clothing', 2500.00, 30, 'Fashion World'),
('Cotton T-Shirt', 'Clothing', 800.00, 100, 'Fashion World'),
('Running Shoes', 'Clothing', 3500.00, 25, 'Fashion World'),
('Basmati Rice', 'Food', 120.00, 200, 'Food Distributors'),
('Wheat Flour', 'Food', 80.00, 150, 'Food Distributors'),
('Cooking Oil', 'Food', 250.00, 75, 'Food Distributors'),
('Orange Juice', 'Beverages', 150.00, 100, 'Beverage Co'),
('Cola Drink', 'Beverages', 100.00, 150, 'Beverage Co'),
('Coffee Powder', 'Beverages', 450.00, 50, 'Beverage Co'),
('Notebook A4', 'Stationery', 150.00, 500, 'Office Supplies'),
('Pen Pack', 'Stationery', 200.00, 300, 'Office Supplies'),
('Pencil Set', 'Stationery', 250.00, 250, 'Office Supplies')
ON DUPLICATE KEY UPDATE updated_at = CURRENT_TIMESTAMP;

-- Create sample customers
INSERT INTO Customers (first_name, last_name, email, phone, address, city, country)
VALUES 
('John', 'Doe', 'john.doe@email.com', '+230 1234567', '123 Main Street', 'Port Louis', 'Mauritius'),
('Jane', 'Smith', 'jane.smith@email.com', '+230 2345678', '456 Oak Avenue', 'Beau Bassin', 'Mauritius'),
('Mohammed', 'Ahmed', 'mohammed.ahmed@email.com', '+230 3456789', '789 Palm Road', 'Curepipe', 'Mauritius'),
('Sarah', 'Wilson', 'sarah.wilson@email.com', '+230 4567890', '321 Rose Lane', 'Vacoas', 'Mauritius'),
('Robert', 'Johnson', 'robert.johnson@email.com', '+230 5678901', '654 Lily Street', 'Port Louis', 'Mauritius')
ON DUPLICATE KEY UPDATE updated_at = CURRENT_TIMESTAMP;

-- Display tables summary
SHOW TABLES;
SHOW TABLE STATUS;

-- Display row counts
SELECT 'Users' as Table_Name, COUNT(*) as Row_Count FROM Users
UNION ALL
SELECT 'Products', COUNT(*) FROM Products
UNION ALL
SELECT 'Customers', COUNT(*) FROM Customers
UNION ALL
SELECT 'Sales', COUNT(*) FROM Sales
UNION ALL
SELECT 'Sale_Details', COUNT(*) FROM Sale_Details
UNION ALL
SELECT 'Audit_Logs', COUNT(*) FROM Audit_Logs
UNION ALL
SELECT 'Analytics', COUNT(*) FROM Analytics;
