public enum InsertInitialDataSQLCodes {
    createUsers("""
        INSERT INTO users (username, password, type)
            VALUES
                ('admin', 'admin123!@#', 'ADM'),
                ('Shawn Lutz', 'manager123!@#', 'MAN'),
                ('Juan Cruz', 'cashier123!@#', 'CAS');
        """);

    private final String sql;

    InsertInitialDataSQLCodes(String sql) {
        this.sql = sql;
    }

    public String getSQL() {
        return sql;
    }
}
