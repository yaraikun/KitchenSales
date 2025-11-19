public enum InsertInitialDataSQLCodes {
    insertUsers("""
        INSERT INTO users (username, password, type)
            VALUES
                ('admin', 'admin123!@#', 'ADM'),
                ('manager', 'manager123!@#', 'MAN'),
                ('cashier', 'cashier123!@#', 'CAS');
        """),

    insertStatusCodes("""
        INSERT INTO status_codes (status_code, description)
            VALUES
                ('ACTIVE', 'Order is active in transaction'),
                ('PENDING', 'Order waiting in kitchen'),
                ('COMPLETED', 'Transaction complete.'),
                ('READY', 'Ready to serve kitchen order.'),
                ('A', 'Menu item is available'),
                ('NA', 'Menu item is not available'),
                ('DISC', 'Menu item is discounted');
        """
    ),

    insertTerminal("""
        INSERT INTO pos_terminals (location, status_code)
            VALUES ('Main Cashier', 'A');
        """),

    insertMenuItems("""
            INSERT INTO items (item_name, unit_price, status_code) VALUES
            ('Beef Bulgogi', 199, 'A'),
            ('Pork Samgyeopsal', 179, 'A'),
            ('Spicy Pork Bulgogi', 189, 'A'),
            ('Chicken Teriyaki', 169, 'A'),
            ('Soy Garlic Chicken', 169, 'A'),
            ('Kimchi Jjigae', 149, 'A'),
            ('Doenjang Jjigae', 149, 'A'),
            ('Tteokbokki', 129, 'A'),
            ('Japchae', 139, 'A'),
            ('Bibimbap', 159, 'A'),
            ('Kimchi Fried Rice', 129, 'A'),
            ('Seafood Ramen', 159, 'A'),
            ('Cheese Ramen', 139, 'A'),
            ('Korean Fried Chicken (Half)', 299, 'A'),
            ('Korean Fried Chicken (Whole)', 499, 'A'),
            ('Mandu (5 pcs)', 99, 'A'),
            ('Kimbap', 109, 'A'),
            ('Corn Cheese', 89, 'A'),
            ('Fish Cake Soup', 139, 'A'),
            ('Garlic Rice', 29, 'A'),
            ('Steamed Rice', 25, 'A'),
            ('Lettuce Wrap Set', 39, 'A'),
            ('Potato Pancake', 119, 'A'),
            ('Kimchi Pancake', 129, 'A'),
            ('Egg Roll Omelette', 119, 'A'),
            ('Korean BBQ Platter', 599, 'A'),
            ('Spicy Cold Noodles (Naengmyeon)', 149, 'NA'),
            ('Beef Rib Soup (Galbitang)', 199, 'NA'),
            ('Honey Butter Chicken', 189, 'DISC'),
            ('Cheese Hotdog', 99, 'DISC');
        """);

    private final String sql;

    InsertInitialDataSQLCodes(String sql) {
        this.sql = sql;
    }

    public String getSQL() {
        return sql;
    }
}
