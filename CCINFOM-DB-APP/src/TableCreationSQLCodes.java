public enum TableCreationSQLCodes {
    createUsers("""
        CREATE TABLE IF NOT EXISTS users (
            user_id INT PRIMARY KEY AUTO_INCREMENT,
            username VARCHAR(255),
            password VARCHAR(255),
            type VARCHAR(3)
        );
    """),

    createStatusCodes("""
        CREATE TABLE IF NOT EXISTS status_codes (
            status_code VARCHAR(50) PRIMARY KEY,
            description VARCHAR(255)
        );
    """),

    createPosTerminals("""
        CREATE TABLE IF NOT EXISTS pos_terminals (
            terminal_id INT PRIMARY KEY AUTO_INCREMENT,
            location VARCHAR(100) NOT NULL,
            status_code VARCHAR(50) NOT NULL,
            FOREIGN KEY (status_code) REFERENCES status_codes(status_code)
        );
    """),

    createDiscounts("""
        CREATE TABLE IF NOT EXISTS discounts (
            discount_id INT PRIMARY KEY AUTO_INCREMENT,
            discount_name VARCHAR(100) NOT NULL,
            type VARCHAR(50) NOT NULL,
            value DECIMAL(10, 2) NOT NULL,
            status_code VARCHAR(50) NOT NULL,
            FOREIGN KEY (status_code) REFERENCES status_codes(status_code)
        );
    """),

    createPosTransactions("""
        CREATE TABLE IF NOT EXISTS pos_transactions (
            transaction_id INT PRIMARY KEY AUTO_INCREMENT,
            transaction_date DATETIME NOT NULL,
            terminal_id INT,
            total_amount DECIMAL(10, 2) NOT NULL,
            tax_amount DECIMAL(10, 2) NOT NULL,
            net_amount DECIMAL(10, 2) NOT NULL,
            status_code VARCHAR(50) NOT NULL,
            payment_status VARCHAR(50) NOT NULL,
            FOREIGN KEY (terminal_id) REFERENCES pos_terminals(terminal_id),
            FOREIGN KEY (status_code) REFERENCES status_codes(status_code)
        );
    """),

    createPayments("""
        CREATE TABLE IF NOT EXISTS payments (
            payment_id INT PRIMARY KEY AUTO_INCREMENT,
            transaction_id INT NOT NULL,
            payment_method VARCHAR(50) NOT NULL,
            amount DECIMAL(10, 2) NOT NULL,
            payment_date DATETIME NOT NULL,
            FOREIGN KEY (transaction_id) REFERENCES pos_transactions(transaction_id)
        );
    """),

    createPosKitchenOrders("""
        CREATE TABLE IF NOT EXISTS pos_kitchen_orders (
            kitchen_order_id INT PRIMARY KEY AUTO_INCREMENT,
            transaction_id INT NOT NULL,
            status_code VARCHAR(50) NOT NULL,
            created_time DATETIME NOT NULL,
            completed_time DATETIME,
            FOREIGN KEY (transaction_id) REFERENCES pos_transactions(transaction_id),
            FOREIGN KEY (status_code) REFERENCES status_codes(status_code)
        );
    """),

    createItems("""
        CREATE TABLE IF NOT EXISTS items (
            item_id INT PRIMARY KEY AUTO_INCREMENT,
            item_name VARCHAR(255) NOT NULL,
            unit_price DECIMAL(10, 2) NOT NULL,
            status_code VARCHAR(50) NOT NULL,
            FOREIGN KEY (status_code) REFERENCES status_codes(status_code)
        );
    """),

    createPosTransactionLines("""
        CREATE TABLE IF NOT EXISTS pos_transaction_lines (
            line_id INT PRIMARY KEY AUTO_INCREMENT,
            transaction_id INT NOT NULL,
            kitchen_order_id INT,
            item_id INT NOT NULL,
            quantity INT NOT NULL,
            line_total DECIMAL(10, 2) NOT NULL,
            FOREIGN KEY (transaction_id) REFERENCES pos_transactions(transaction_id),
            FOREIGN KEY (kitchen_order_id) REFERENCES pos_kitchen_orders(kitchen_order_id),
            FOREIGN KEY (item_id) REFERENCES items(item_id)
        );
    """),

    createTransactionDiscounts("""
        CREATE TABLE IF NOT EXISTS transaction_discounts (
            transaction_id INT NOT NULL,
            discount_id INT NOT NULL,
            discount_amount DECIMAL(10, 2) NOT NULL,
            PRIMARY KEY (transaction_id, discount_id),
            FOREIGN KEY (transaction_id) REFERENCES pos_transactions(transaction_id),
            FOREIGN KEY (discount_id) REFERENCES discounts(discount_id)
        );
    """);

    private final String sql;

    TableCreationSQLCodes(String sql) {
        this.sql = sql;
    }

    public String getSQL() {
        return sql;
    }
}
