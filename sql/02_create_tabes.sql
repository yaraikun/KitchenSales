-- File: 02_create_tables.sql
-- Description: Creates all tables required for the POS application.
-- Instructions: Run this script after 01_create_database.sql.

-- =================================================================
-- Table: pos_terminals
-- =================================================================
CREATE TABLE IF NOT EXISTS pos_terminals (
    terminal_id INT PRIMARY KEY AUTO_INCREMENT,
    location VARCHAR(100) NOT NULL,
    status VARCHAR(50) NOT NULL
);

-- =================================================================
-- Table: discounts
-- =================================================================
CREATE TABLE IF NOT EXISTS discounts (
    discount_id INT PRIMARY KEY AUTO_INCREMENT,
    discount_name VARCHAR(100) NOT NULL,
    type VARCHAR(50) NOT NULL,
    value DECIMAL(10, 2) NOT NULL,
    status VARCHAR(50) NOT NULL
);

-- =================================================================
-- Table: pos_transactions
-- =================================================================
CREATE TABLE IF NOT EXISTS pos_transactions (
    transaction_id INT PRIMARY KEY AUTO_INCREMENT,
    transaction_date DATETIME NOT NULL,
    terminal_id INT,
    total_amount DECIMAL(10, 2) NOT NULL,
    discount_amount DECIMAL(10, 2) DEFAULT 0.00,
    tax_amount DECIMAL(10, 2) NOT NULL,
    net_amount DECIMAL(10, 2) NOT NULL,
    status VARCHAR(50) NOT NULL,
    payment_status VARCHAR(50) NOT NULL,
    FOREIGN KEY (terminal_id) REFERENCES pos_terminals(terminal_id)
);

-- =================================================================
-- Table: payments
-- =================================================================
CREATE TABLE IF NOT EXISTS payments (
    payment_id INT PRIMARY KEY AUTO_INCREMENT,
    transaction_id INT NOT NULL,
    payment_method VARCHAR(50) NOT NULL,
    amount DECIMAL(10, 2) NOT NULL,
    payment_date DATETIME NOT NULL,
    FOREIGN KEY (transaction_id) REFERENCES pos_transactions(transaction_id)
);

-- =================================================================
-- Table: pos_kitchen_orders
-- =================================================================
CREATE TABLE IF NOT EXISTS pos_kitchen_orders (
    kitchen_order_id INT PRIMARY KEY AUTO_INCREMENT,
    transaction_id INT NOT NULL,
    status VARCHAR(50) NOT NULL,
    created_time DATETIME NOT NULL,
    completed_time DATETIME,
    FOREIGN KEY (transaction_id) REFERENCES pos_transactions(transaction_id)
);


-- =================================================================
-- Table: pos_transaction_lines
-- =================================================================
CREATE TABLE IF NOT EXISTS pos_transaction_lines (
    line_id INT PRIMARY KEY AUTO_INCREMENT,
    transaction_id INT NOT NULL,
    kitchen_order_id INT,
    item_name VARCHAR(255) NOT NULL,
    quantity INT NOT NULL,
    unit_price DECIMAL(10, 2) NOT NULL,
    line_total DECIMAL(10, 2) NOT NULL,
    FOREIGN KEY (transaction_id) REFERENCES pos_transactions(transaction_id),
    FOREIGN KEY (kitchen_order_id) REFERENCES pos_kitchen_orders(kitchen_order_id)
);

-- --- End of Script ---
