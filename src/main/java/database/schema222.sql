-- Smart Retail System - Database Schema

DROP DATABASE IF EXISTS smart_retail;
CREATE DATABASE smart_retail;
USE smart_retail;

-- ================= USERS TABLE =================
CREATE TABLE IF NOT EXISTS users (
    user_id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(100),
    phone VARCHAR(20),
    status VARCHAR(20) DEFAULT 'Active',
    department VARCHAR(50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    );

-- ================= PRODUCTS TABLE =================
CREATE TABLE IF NOT EXISTS products (
    product_id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    category VARCHAR(50) NOT NULL,
    price DECIMAL(10,2) NOT NULL CHECK (price > 0),
    stock INT NOT NULL DEFAULT 0 CHECK (stock >= 0),
    description TEXT,
    supplier VARCHAR(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    INDEX idx_category (category),
    INDEX idx_name (name)
    );

-- ================= CUSTOMERS TABLE =================
CREATE TABLE IF NOT EXISTS customers (
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
    );

-- ================= SALES TABLE =================
CREATE TABLE IF NOT EXISTS sales (
    sale_id INT AUTO_INCREMENT PRIMARY KEY,
    customer_id INT,
    user_id INT NOT NULL,
    sale_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    total_amount DECIMAL(12,2) NOT NULL,
    discount DECIMAL(10,2) DEFAULT 0,
    final_amount DECIMAL(12,2) NOT NULL,

    payment_method ENUM('Cash','Card','Cheque','Online') DEFAULT 'Cash',
    status ENUM('Completed','Pending','Cancelled') DEFAULT 'Completed',

    notes TEXT,
    cash_received DECIMAL(10,2) DEFAULT NULL,
    change_given DECIMAL(10,2) DEFAULT NULL,

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    FOREIGN KEY (customer_id) REFERENCES customers(customer_id) ON DELETE SET NULL,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE RESTRICT,

    INDEX idx_sale_date (sale_date),
    INDEX idx_customer_id (customer_id)
    );

-- ================= SALE DETAILS =================
CREATE TABLE IF NOT EXISTS sale_details (
    sale_detail_id INT AUTO_INCREMENT PRIMARY KEY,
    sale_id INT NOT NULL,
    product_id INT NOT NULL,

    quantity INT NOT NULL CHECK (quantity > 0),
    unit_price DECIMAL(10,2) NOT NULL,
    total_price DECIMAL(12,2) NOT NULL,
    discount DECIMAL(10,2) DEFAULT 0,

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY (sale_id) REFERENCES sales(sale_id) ON DELETE CASCADE,
    FOREIGN KEY (product_id) REFERENCES products(product_id) ON DELETE RESTRICT,

    INDEX idx_sale_id (sale_id),
    INDEX idx_product_id (product_id)
    );

-- ================= CASH LOGS =================
CREATE TABLE IF NOT EXISTS cash_logs (
    log_id INT AUTO_INCREMENT PRIMARY KEY,

    sale_id INT NOT NULL,
    cash_received DECIMAL(10,2) NOT NULL,
    change_given DECIMAL(10,2) DEFAULT 0.00,
    net_amount DECIMAL(10,2) NOT NULL,

    transaction_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    user_id INT,

    FOREIGN KEY (sale_id) REFERENCES sales(sale_id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE SET NULL
    );

-- ================= AUDIT LOGS =================
CREATE TABLE IF NOT EXISTS audit_logs (
    log_id INT AUTO_INCREMENT PRIMARY KEY,

    user_id INT NOT NULL,
    action VARCHAR(100) NOT NULL,
    table_name VARCHAR(50),
    record_id INT,
    details TEXT,
    ip_address VARCHAR(45),

    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE RESTRICT,

    INDEX idx_user_id (user_id),
    INDEX idx_timestamp (timestamp),
    INDEX idx_action (action)
    );

-- ================= ANALYTICS =================
CREATE TABLE IF NOT EXISTS analytics (
    analytics_id INT AUTO_INCREMENT PRIMARY KEY,

    date DATE NOT NULL UNIQUE,
    total_sales DECIMAL(12,2) DEFAULT 0,
    total_revenue DECIMAL(12,2) DEFAULT 0,
    total_transactions INT DEFAULT 0,
    total_customers INT DEFAULT 0,

    best_product INT,

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    FOREIGN KEY (best_product) REFERENCES products(product_id) ON DELETE SET NULL,

    INDEX idx_date (date)
    );

-- ============================================
-- INSERT USERS
-- ============================================

INSERT INTO users (username, password, email, phone, status, department) VALUES
        ('Demo', 'Demo1234', 'demo@gmail.com', '+230 12345678', 'Active', 'Sales'),
        ('Admin', 'Admin1234', 'admin@smartretail.com', '+230 87654321', 'Active', 'Management'),
        ('Manager', 'Manager1234', 'manager@smartretail.com', '+230 11223344', 'Active', 'Operations'),
        ('Cashier1', 'Cashier1234', 'cashier1@smartretail.com', '+230 22334455', 'Active', 'Sales'),
        ('Cashier2', 'Cashier1234', 'cashier2@smartretail.com', '+230 33445566', 'Active', 'Sales');

-- ============================================
-- INSERT PRODUCTS
-- ============================================

INSERT INTO products (name, category, price, stock, description, supplier) VALUES
       ('Laptop HP', 'Electronics', 45000.00, 15, 'High-performance laptop for business', 'Tech Supplies Ltd'),
       ('Mouse Wireless', 'Electronics', 800.00, 120, 'Ergonomic wireless mouse', 'Tech Supplies Ltd'),
       ('USB-C Cable', 'Electronics', 350.00, 200, 'High-speed USB-C charging cable', 'Tech Supplies Ltd'),
       ('Keyboard RGB', 'Electronics', 3500.00, 45, 'Mechanical RGB gaming keyboard', 'Tech Supplies Ltd'),
       ('Monitor 24inch', 'Electronics', 12000.00, 20, 'Full HD LED Monitor', 'Tech Supplies Ltd'),
       ('Blue Jeans', 'Clothing', 2500.00, 50, 'Premium denim blue jeans', 'Fashion World'),
       ('Cotton T-Shirt', 'Clothing', 800.00, 150, 'Comfortable cotton t-shirt', 'Fashion World'),
       ('Running Shoes', 'Clothing', 3500.00, 40, 'Professional running shoes', 'Fashion World'),
       ('Winter Jacket', 'Clothing', 5500.00, 25, 'Warm winter jacket', 'Fashion World'),
       ('Casual Shirt', 'Clothing', 1500.00, 80, 'Casual wear shirt', 'Fashion World'),
       ('Basmati Rice', 'Food', 120.00, 400, '5kg bag of premium basmati rice', 'Food Distributors'),
       ('Wheat Flour', 'Food', 80.00, 300, '2kg bag of wheat flour', 'Food Distributors'),
       ('Cooking Oil', 'Food', 250.00, 150, '1L bottle of vegetable cooking oil', 'Food Distributors'),
       ('Sugar', 'Food', 95.00, 200, '1kg pack of sugar', 'Food Distributors'),
       ('Salt', 'Food', 40.00, 250, '500g pack of salt', 'Food Distributors'),
       ('Orange Juice', 'Beverages', 150.00, 200, '1L fresh orange juice', 'Beverage Co'),
       ('Cola Drink', 'Beverages', 100.00, 300, '500ml cola bottle', 'Beverage Co'),
       ('Coffee Powder', 'Beverages', 450.00, 100, 'Premium ground coffee powder', 'Beverage Co'),
       ('Tea Bags', 'Beverages', 200.00, 150, 'Box of 50 tea bags', 'Beverage Co'),
       ('Mineral Water', 'Beverages', 50.00, 400, '500ml mineral water bottle', 'Beverage Co'),
       ('Notebook A4', 'Stationery', 150.00, 1000, 'Spiral notebook 100 pages', 'Office Supplies'),
       ('Pen Pack', 'Stationery', 200.00, 600, 'Pack of 10 ballpoint pens', 'Office Supplies'),
       ('Pencil Set', 'Stationery', 250.00, 500, 'Set of 12 colored pencils', 'Office Supplies'),
       ('Ruler 30cm', 'Stationery', 100.00, 400, 'Plastic ruler 30cm', 'Office Supplies'),
       ('Eraser Pack', 'Stationery', 50.00, 300, 'Pack of 5 erasers', 'Office Supplies');

-- ============================================
-- INSERT CUSTOMERS
-- ============================================

INSERT INTO customers (first_name, last_name, email, phone, address, city, postal_code, country, loyalty_points)
VALUES ('Walk-in', 'Customer', 'walkin@store.com', 'N/A', 'Store Address', 'City', '00000', 'Country', 0);

INSERT INTO customers (first_name, last_name, email, phone, address, city, postal_code, country, loyalty_points) VALUES
        ('John', 'Doe', 'john.doe@email.com', '+230 1234567', '123 Main Street', 'Port Louis', '10001', 'Mauritius', 450),
        ('Jane', 'Smith', 'jane.smith@email.com', '+230 2345678', '456 Oak Avenue', 'Beau Bassin', '10002', 'Mauritius', 320),
        ('Mohammed', 'Ahmed', 'mohammed.ahmed@email.com', '+230 3456789', '789 Palm Road', 'Curepipe', '10003', 'Mauritius', 890),
        ('Sarah', 'Wilson', 'sarah.wilson@email.com', '+230 4567890', '321 Rose Lane', 'Vacoas', '10004', 'Mauritius', 150),
        ('Robert', 'Johnson', 'robert.johnson@email.com', '+230 5678901', '654 Lily Street', 'Port Louis', '10005', 'Mauritius', 670),
        ('Maria', 'Garcia', 'maria.garcia@email.com', '+230 6789012', '987 Flower Garden', 'Quatre Bornes', '10006', 'Mauritius', 280),
        ('David', 'Brown', 'david.brown@email.com', '+230 7890123', '159 Sun Valley', 'Moka', '10007', 'Mauritius', 540),
        ('Lisa', 'Anderson', 'lisa.anderson@email.com', '+230 8901234', '753 Moon Street', 'Port Louis', '10008', 'Mauritius', 180),
        ('Ahmed', 'Hassan', 'ahmed.hassan@email.com', '+230 9012345', '456 Star Road', 'Phoenix', '10009', 'Mauritius', 720),
        ('Emily', 'Taylor', 'emily.taylor@email.com', '+230 0123456', '321 Dream Lane', 'Beau Bassin', '10010', 'Mauritius', 390);

-- ============================================
-- INSERT SALES
-- ============================================

INSERT INTO sales (customer_id, user_id, sale_date, total_amount, discount, final_amount, payment_method, status, cash_received, change_given) VALUES
            (2,1,DATE_SUB(NOW(), INTERVAL 30 DAY),15000,0,15000,'Cash','Completed',15000,0),
            (3,1,DATE_SUB(NOW(), INTERVAL 28 DAY),8500,500,8000,'Card','Completed',NULL,NULL),
            (4,2,DATE_SUB(NOW(), INTERVAL 25 DAY),32000,2000,30000,'Card','Completed',NULL,NULL),
            (5,1,DATE_SUB(NOW(), INTERVAL 22 DAY),5200,200,5000,'Cash','Completed',5100,100),
            (6,3,DATE_SUB(NOW(), INTERVAL 20 DAY),9800,0,9800,'Cash','Completed',10000,200),
            (7,1,DATE_SUB(NOW(), INTERVAL 18 DAY),6500,500,6000,'Card','Completed',NULL,NULL),
            (8,2,DATE_SUB(NOW(), INTERVAL 15 DAY),12500,0,12500,'Cash','Completed',13000,500),
            (9,3,DATE_SUB(NOW(), INTERVAL 12 DAY),4800,200,4600,'Card','Completed',NULL,NULL),
            (10,1,DATE_SUB(NOW(), INTERVAL 10 DAY),7200,200,7000,'Cash','Completed',7000,0),
            (11,2,DATE_SUB(NOW(), INTERVAL 8 DAY),18600,1200,17400,'Card','Completed',NULL,NULL),
            (2,1,DATE_SUB(NOW(), INTERVAL 6 DAY),5500,500,5000,'Card','Completed',NULL,NULL),
            (4,3,DATE_SUB(NOW(), INTERVAL 5 DAY),22800,1800,21000,'Cash','Completed',21500,500),
            (3,1,DATE_SUB(NOW(), INTERVAL 4 DAY),3200,0,3200,'Cash','Completed',3200,0),
            (6,2,DATE_SUB(NOW(), INTERVAL 3 DAY),10500,500,10000,'Card','Completed',NULL,NULL),
            (5,1,DATE_SUB(NOW(), INTERVAL 2 DAY),8700,700,8000,'Cash','Completed',8000,0),
            (7,3,DATE_SUB(NOW(), INTERVAL 1 DAY),6200,200,6000,'Card','Completed',NULL,NULL),
            (8,1,CURDATE(),11300,1300,10000,'Cash','Completed',10100,100),
            (10,2,CURDATE(),4500,0,4500,'Card','Completed',NULL,NULL);

-- ============================================
-- INSERT SALE DETAILS
-- ============================================

INSERT INTO sale_details (sale_id, product_id, quantity, unit_price, total_price, discount) VALUES
        (1,1,1,45000,45000,0),
        (1,2,3,800,2400,0),
        (2,6,2,2500,5000,0),
        (2,16,5,150,750,500),
        (3,1,1,45000,45000,2000),
        (3,3,10,350,3500,0),
        (4,5,1,12000,12000,200),
        (4,3,5,350,1750,0),
        (5,11,50,120,6000,0),
        (5,12,30,80,2400,0),
        (6,7,5,800,4000,500),
        (6,16,10,150,1500,0),
        (7,8,3,3500,10500,0),
        (7,20,50,50,2500,0),
        (8,21,10,150,1500,200),
        (8,22,5,200,1000,0);

-- ============================================
-- INSERT CASH LOGS
-- ============================================

INSERT INTO cash_logs (sale_id, cash_received, change_given, net_amount, user_id) VALUES
        (1,15000,0,15000,1),
        (5,10000,200,9800,3),
        (7,13000,500,12500,2),
        (9,7000,0,7000,1),
        (12,21500,500,21000,3),
        (15,8000,0,8000,1),
        (17,10100,100,10000,2);

-- ============================================
-- INSERT AUDIT LOGS
-- ============================================

INSERT INTO audit_logs (user_id, action, table_name, record_id, details, ip_address) VALUES
         (1,'LOGIN','users',1,'User Demo logged in','192.168.1.100'),
         (1,'CREATE','sales',1,'Created new sale with ID 1','192.168.1.100'),
         (2,'LOGIN','users',2,'User Admin logged in','192.168.1.101'),
         (2,'UPDATE','products',1,'Updated product stock','192.168.1.101'),
         (1,'DELETE','sales',2,'Cancelled sale ID 2','192.168.1.100'),
         (3,'LOGIN','users',3,'User Manager logged in','192.168.1.102'),
         (3,'CREATE','customers',10,'Created new customer','192.168.1.102'),
         (1,'VIEW','analytics',NULL,'Viewed analytics dashboard','192.168.1.100');

-- ============================================
-- INSERT ANALYTICS
-- ============================================

INSERT INTO analytics (date, total_sales, total_revenue, total_transactions, total_customers, best_product) VALUES
     (DATE_SUB(CURDATE(), INTERVAL 30 DAY),15000,15000,1,1,1),
     (DATE_SUB(CURDATE(), INTERVAL 28 DAY),8500,8000,1,1,6),
     (DATE_SUB(CURDATE(), INTERVAL 25 DAY),32000,30000,1,1,1),
     (DATE_SUB(CURDATE(), INTERVAL 22 DAY),5200,5000,1,1,5),
     (DATE_SUB(CURDATE(), INTERVAL 20 DAY),9800,9800,1,1,11),
     (CURDATE(),15800,14500,2,2,1);