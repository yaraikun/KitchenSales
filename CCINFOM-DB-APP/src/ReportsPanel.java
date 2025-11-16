import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.sql.*;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;


public class ReportsPanel extends BackgroundPanel {

    private JTable reportTable;
    private DefaultTableModel reportTableModel;
    private JScrollPane reportScrollPane;

    private JTextField fromDateField;
    private JTextField toDateField;
    private JButton runReportButton;

    private final DecimalFormat currencyFormat = new DecimalFormat("#,##0.00");

    private enum ReportType {
        NONE,
        SALES_SUMMARY,
        SALES_PERFORMANCE,
        DISCOUNT_UTILIZATION,
        PAYMENT_BREAKDOWN,
        KITCHEN_EFFICIENCY
    }

    private ReportType currentReportType = ReportType.NONE;

    public ReportsPanel(String managerUsername) {
        super("CCINFOM-DB-APP/assets/reportsPanel.png");
        setLayout(null);

        Theme theme = Theme.MONOCHROME;

        initHeader(theme, managerUsername);
        initReportButtons(theme);
        initSharedDateControls(theme);

        System.out.println("ReportsPanel initialized with report buttons.");
    }

    private void initHeader(Theme theme, String managerUsername) {
        JLabel managerLabel = new JLabel( managerUsername);
        managerLabel.setBounds(160, 4, 350, 30);
        managerLabel.setForeground(Color.BLACK);
        managerLabel.setFont(new Font("Arial", Font.BOLD, 20));
        add(managerLabel);



        JButton logoutButton = theme.createButton();
        logoutButton.setBounds(100, 565, 140, 42);
        logoutButton.addActionListener(_ -> {
            LoginPanel loginPanel = new LoginPanel(theme);
            PanelManager.updateCurrentPanel(loginPanel);
        });
        add(logoutButton);
    }

    private void initReportButtons(Theme theme) {
        JButton salesSummaryButton = theme.createButton();
        JButton salesPerformanceButton = theme.createButton();
        JButton discountUtilizationButton = theme.createButton();
        JButton paymentMethodButton = theme.createButton();
        JButton kitchenEfficiencyButton = theme.createButton();

        JButton[] reportButtons = {
                salesSummaryButton,
                salesPerformanceButton,
                discountUtilizationButton,
                paymentMethodButton,
                kitchenEfficiencyButton
        };

        int x = 38;
        int y = 102;
        int width = 250;
        int height = 58;
        int gap = 34;

        salesSummaryButton.setBounds(x, y, width, height);
        salesPerformanceButton.setBounds(x, y + (height + gap), width, height);
        discountUtilizationButton.setBounds(x, y + 2 * (height + gap), width, height);
        paymentMethodButton.setBounds(x, y + 3 * (height + gap), width, height);
        kitchenEfficiencyButton.setBounds(x, y + 4 * (height + gap), width, height);

        for (JButton btn : reportButtons) {
            add(btn);
        }

        salesSummaryButton.addActionListener(_ -> {
            currentReportType = ReportType.SALES_SUMMARY;
            setDateControlsAndTableVisible(true);
            setDefaultDateRange();
            setupSalesSummaryTable();
        });

        salesPerformanceButton.addActionListener(_ -> {
            currentReportType = ReportType.SALES_PERFORMANCE;
            setDateControlsAndTableVisible(true);
            setDefaultDateRange();
            setupSalesPerformanceTable();
        });

        discountUtilizationButton.addActionListener(_ -> {
            currentReportType = ReportType.DISCOUNT_UTILIZATION;
            setDateControlsAndTableVisible(true);
            setDefaultDateRange();
            setupDiscountUtilizationTable();
        });

        paymentMethodButton.addActionListener(_ -> {
            currentReportType = ReportType.PAYMENT_BREAKDOWN;
            setDateControlsAndTableVisible(true);
            setDefaultDateRange();
            setupPaymentBreakdownTable();
        });

        kitchenEfficiencyButton.addActionListener(_ -> {
            currentReportType = ReportType.KITCHEN_EFFICIENCY;
            setDateControlsAndTableVisible(true);
            setDefaultDateRange();
            setupKitchenEfficiencyTable();
        });
    }

    private void initSharedDateControls(Theme theme) {
        int filterY = 55;
        int filterHeight = 30;

        fromDateField = theme.createTextField();
        fromDateField.setBounds(570, filterY + 2, 90, filterHeight);
        fromDateField.setOpaque(true);
        fromDateField.setBackground(theme.getButtonBackground());
        fromDateField.setBorder(BorderFactory.createLineBorder(theme.getLabelBackground(), 2));
        add(fromDateField);

        toDateField = theme.createTextField();
        toDateField.setBounds(915, filterY + 2, 90, filterHeight);
        toDateField.setOpaque(true);
        toDateField.setBackground(theme.getButtonBackground());
        toDateField.setBorder(BorderFactory.createLineBorder(theme.getLabelBackground(), 2));
        add(toDateField);

        runReportButton = theme.createButton();
        runReportButton.setBounds(1055, filterY, 85, filterHeight);
        add(runReportButton);

        runReportButton.addActionListener(_ -> runCurrentReport());

        setDateControlsAndTableVisible(false);
    }

    private void setDateControlsAndTableVisible(boolean visible) {
        if (fromDateField != null) fromDateField.setVisible(visible);
        if (toDateField != null) toDateField.setVisible(visible);
        if (runReportButton != null) runReportButton.setVisible(visible);

        if (reportScrollPane != null) {
            reportScrollPane.setVisible(visible);
        }
    }

    private void setDefaultDateRange() {
        LocalDate today = LocalDate.now();
        LocalDate firstOfMonth = today.withDayOfMonth(1);

        if (fromDateField.getText().trim().isEmpty()) {
            fromDateField.setText(firstOfMonth.toString());
        }
        if (toDateField.getText().trim().isEmpty()) {
            toDateField.setText(today.toString());
        }
    }

    private void runCurrentReport() {
        switch (currentReportType) {
            case SALES_SUMMARY -> runSalesSummaryQuery();
            case SALES_PERFORMANCE -> runSalesPerformanceQuery();
            case DISCOUNT_UTILIZATION -> runDiscountUtilizationQuery();
            case PAYMENT_BREAKDOWN -> runPaymentBreakdownQuery();
            case KITCHEN_EFFICIENCY -> runKitchenEfficiencyQuery();
            case NONE -> JOptionPane.showMessageDialog(
                    this,
                    "Please select a report first.",
                    "No Report Selected",
                    JOptionPane.WARNING_MESSAGE
            );
        }
    }

    private void setupSalesSummaryTable() {
        String[] columnNames = { "Date", "Total Transactions", "Total Sales" };

        if (reportTableModel == null) {
            reportTableModel = new DefaultTableModel(columnNames, 0);
            reportTable = new JTable(reportTableModel);
            styleReportTable();
            reportScrollPane = new JScrollPane(reportTable);
            reportScrollPane.setBounds(340, 110, 820, 500);
            reportScrollPane.setBorder(null);
            add(reportScrollPane);
        } else {
            reportTableModel.setRowCount(0);
            reportTableModel.setColumnIdentifiers(columnNames);
        }

        reportScrollPane.setVisible(true);
        reportScrollPane.revalidate();
        reportScrollPane.repaint();
        this.revalidate();
        this.repaint();
    }

    private void runSalesSummaryQuery() {
        Timestamp fromTimestamp;
        Timestamp toTimestamp;

        try {
            LocalDate fromDate = LocalDate.parse(fromDateField.getText().trim());
            LocalDate toDate = LocalDate.parse(toDateField.getText().trim());

            fromTimestamp = Timestamp.valueOf(fromDate.atStartOfDay());
            toTimestamp = Timestamp.valueOf(toDate.atTime(23, 59, 59));
        } catch (DateTimeParseException ex) {
            JOptionPane.showMessageDialog(
                    this,
                    "Please enter valid dates in format yyyy-MM-dd.",
                    "Invalid Date",
                    JOptionPane.ERROR_MESSAGE
            );
            return;
        }

        String sql = """
                SELECT
                    DATE(transaction_date) AS sale_date,
                    COUNT(transaction_id) AS total_transactions,
                    SUM(net_amount) AS total_sales
                FROM pos_transactions
                WHERE status = 'Completed' AND transaction_date BETWEEN ? AND ?
                GROUP BY sale_date
                ORDER BY sale_date;
                """;

        try (Connection connection = Database.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql);
             Statement useStmt = connection.createStatement()) {

            useStmt.execute("USE " + App.schemaName);

            stmt.setTimestamp(1, fromTimestamp);
            stmt.setTimestamp(2, toTimestamp);

            try (ResultSet rs = stmt.executeQuery()) {
                reportTableModel.setRowCount(0);

                while (rs.next()) {
                    Object date = rs.getDate("sale_date");
                    int totalTransactions = rs.getInt("total_transactions");
                    double totalSales = rs.getDouble("total_sales");
                    String totalSalesFormatted = currencyFormat.format(totalSales);

                    reportTableModel.addRow(new Object[]{ date, totalTransactions, totalSalesFormatted });
                }

                if (reportTableModel.getRowCount() == 0) {
                    JOptionPane.showMessageDialog(
                            this,
                            "No completed transactions found for the selected date range.",
                            "No Data",
                            JOptionPane.INFORMATION_MESSAGE
                    );
                }
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(
                    this,
                    "Error running Sales Summary report:\n" + e.getMessage(),
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private void setupSalesPerformanceTable() {
        String[] columnNames = { "Item Name", "Total Quantity Sold", "Total Revenue" };

        if (reportTableModel == null) {
            reportTableModel = new DefaultTableModel(columnNames, 0);
            reportTable = new JTable(reportTableModel);
            styleReportTable();
            reportScrollPane = new JScrollPane(reportTable);
            reportScrollPane.setBounds(340, 110, 820, 500);
            reportScrollPane.setBorder(null);
            add(reportScrollPane);
        } else {
            reportTableModel.setRowCount(0);
            reportTableModel.setColumnIdentifiers(columnNames);
        }

        reportScrollPane.setVisible(true);
        reportScrollPane.revalidate();
        reportScrollPane.repaint();
        this.revalidate();
        this.repaint();
    }

    private void runSalesPerformanceQuery() {
        Timestamp fromTimestamp;
        Timestamp toTimestamp;

        try {
            LocalDate fromDate = LocalDate.parse(fromDateField.getText().trim());
            LocalDate toDate = LocalDate.parse(toDateField.getText().trim());

            fromTimestamp = Timestamp.valueOf(fromDate.atStartOfDay());
            toTimestamp = Timestamp.valueOf(toDate.atTime(23, 59, 59));
        } catch (DateTimeParseException ex) {
            JOptionPane.showMessageDialog(
                    this,
                    "Please enter valid dates in format yyyy-MM-dd.",
                    "Invalid Date",
                    JOptionPane.ERROR_MESSAGE
            );
            return;
        }

        String sql = """
                SELECT
                    ptl.item_name,
                    SUM(ptl.quantity) AS total_quantity_sold,
                    SUM(ptl.line_total) AS total_revenue
                FROM pos_transaction_lines ptl
                JOIN pos_transactions pt ON ptl.transaction_id = pt.transaction_id
                WHERE pt.status = 'Completed' AND pt.transaction_date BETWEEN ? AND ?
                GROUP BY ptl.item_name
                ORDER BY total_revenue DESC;
                """;

        try (Connection connection = Database.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql);
             Statement useStmt = connection.createStatement()) {

            useStmt.execute("USE " + App.schemaName);

            stmt.setTimestamp(1, fromTimestamp);
            stmt.setTimestamp(2, toTimestamp);

            try (ResultSet rs = stmt.executeQuery()) {
                reportTableModel.setRowCount(0);

                while (rs.next()) {
                    String itemName = rs.getString("item_name");
                    int totalQty = rs.getInt("total_quantity_sold");
                    double totalRevenue = rs.getDouble("total_revenue");
                    String totalRevenueFormatted = currencyFormat.format(totalRevenue);

                    reportTableModel.addRow(new Object[]{ itemName, totalQty, totalRevenueFormatted });
                }

                if (reportTableModel.getRowCount() == 0) {
                    JOptionPane.showMessageDialog(
                            this,
                            "No item sales found for the selected date range.",
                            "No Data",
                            JOptionPane.INFORMATION_MESSAGE
                    );
                }
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(
                    this,
                    "Error running Sales Performance report:\n" + e.getMessage(),
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private void setupDiscountUtilizationTable() {
        String[] columnNames = { "Discount Name", "Times Used", "Total Discount Value" };

        if (reportTableModel == null) {
            reportTableModel = new DefaultTableModel(columnNames, 0);
            reportTable = new JTable(reportTableModel);
            styleReportTable();
            reportScrollPane = new JScrollPane(reportTable);
            reportScrollPane.setBounds(340, 110, 820, 500);
            reportScrollPane.setBorder(null);
            add(reportScrollPane);
        } else {
            reportTableModel.setRowCount(0);
            reportTableModel.setColumnIdentifiers(columnNames);
        }

        reportScrollPane.setVisible(true);
        reportScrollPane.revalidate();
        reportScrollPane.repaint();
        this.revalidate();
        this.repaint();
    }

    private void runDiscountUtilizationQuery() {
        Timestamp fromTimestamp;
        Timestamp toTimestamp;

        try {
            LocalDate fromDate = LocalDate.parse(fromDateField.getText().trim());
            LocalDate toDate = LocalDate.parse(toDateField.getText().trim());

            fromTimestamp = Timestamp.valueOf(fromDate.atStartOfDay());
            toTimestamp = Timestamp.valueOf(toDate.atTime(23, 59, 59));
        } catch (DateTimeParseException ex) {
            JOptionPane.showMessageDialog(
                    this,
                    "Please enter valid dates in format yyyy-MM-dd.",
                    "Invalid Date",
                    JOptionPane.ERROR_MESSAGE
            );
            return;
        }

        String sql = """
                SELECT
                    d.discount_name,
                    COUNT(t.transaction_id) AS times_used,
                    SUM(t.discount_amount) AS total_value
                FROM pos_transactions t
                JOIN discounts d ON t.discount_id = d.discount_id
                WHERE t.status = 'Completed' AND t.transaction_date BETWEEN ? AND ?
                GROUP BY d.discount_name
                ORDER BY times_used DESC;
                """;

        try (Connection connection = Database.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql);
             Statement useStmt = connection.createStatement()) {

            useStmt.execute("USE " + App.schemaName);

            stmt.setTimestamp(1, fromTimestamp);
            stmt.setTimestamp(2, toTimestamp);

            try (ResultSet rs = stmt.executeQuery()) {
                reportTableModel.setRowCount(0);

                while (rs.next()) {
                    String discountName = rs.getString("discount_name");
                    int timesUsed = rs.getInt("times_used");
                    double totalValue = rs.getDouble("total_value");
                    String totalValueFormatted = currencyFormat.format(totalValue);

                    reportTableModel.addRow(new Object[]{ discountName, timesUsed, totalValueFormatted });
                }

                if (reportTableModel.getRowCount() == 0) {
                    JOptionPane.showMessageDialog(
                            this,
                            "No discounts used for the selected date range.",
                            "No Data",
                            JOptionPane.INFORMATION_MESSAGE
                    );
                }
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(
                    this,
                    "Error running Discount Utilization report:\n" + e.getMessage(),
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private void setupPaymentBreakdownTable() {
        String[] columnNames = { "Payment Method", "Number of Transactions", "Total Collected" };

        if (reportTableModel == null) {
            reportTableModel = new DefaultTableModel(columnNames, 0);
            reportTable = new JTable(reportTableModel);
            styleReportTable();
            reportScrollPane = new JScrollPane(reportTable);
            reportScrollPane.setBounds(340, 110, 820, 500);
            reportScrollPane.setBorder(null);
            add(reportScrollPane);
        } else {
            reportTableModel.setRowCount(0);
            reportTableModel.setColumnIdentifiers(columnNames);
        }

        reportScrollPane.setVisible(true);
        reportScrollPane.revalidate();
        reportScrollPane.repaint();
        this.revalidate();
        this.repaint();
    }

    private void runPaymentBreakdownQuery() {
        Timestamp fromTimestamp;
        Timestamp toTimestamp;

        try {
            LocalDate fromDate = LocalDate.parse(fromDateField.getText().trim());
            LocalDate toDate = LocalDate.parse(toDateField.getText().trim());

            fromTimestamp = Timestamp.valueOf(fromDate.atStartOfDay());
            toTimestamp = Timestamp.valueOf(toDate.atTime(23, 59, 59));
        } catch (DateTimeParseException ex) {
            JOptionPane.showMessageDialog(
                    this,
                    "Please enter valid dates in format yyyy-MM-dd.",
                    "Invalid Date",
                    JOptionPane.ERROR_MESSAGE
            );
            return;
        }

        String sql = """
                SELECT
                    payment_method,
                    COUNT(payment_id) AS number_of_transactions,
                    SUM(amount) AS total_collected
                FROM payments p
                JOIN pos_transactions t ON p.transaction_id = t.transaction_id
                WHERE t.status = 'Completed' AND p.payment_date BETWEEN ? AND ?
                GROUP BY payment_method
                ORDER BY total_collected DESC;
                """;

        try (Connection connection = Database.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql);
             Statement useStmt = connection.createStatement()) {

            useStmt.execute("USE " + App.schemaName);

            stmt.setTimestamp(1, fromTimestamp);
            stmt.setTimestamp(2, toTimestamp);

            try (ResultSet rs = stmt.executeQuery()) {
                reportTableModel.setRowCount(0);

                while (rs.next()) {
                    String paymentMethod = rs.getString("payment_method");
                    int numberOfTransactions = rs.getInt("number_of_transactions");
                    double totalCollected = rs.getDouble("total_collected");
                    String totalCollectedFormatted = currencyFormat.format(totalCollected);

                    reportTableModel.addRow(new Object[]{ paymentMethod, numberOfTransactions, totalCollectedFormatted });
                }

                if (reportTableModel.getRowCount() == 0) {
                    JOptionPane.showMessageDialog(
                            this,
                            "No payments found for the selected date range.",
                            "No Data",
                            JOptionPane.INFORMATION_MESSAGE
                    );
                }
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(
                    this,
                    "Error running Payment Breakdown report:\n" + e.getMessage(),
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private void setupKitchenEfficiencyTable() {
        String[] columnNames = { "Order Date", "Total Orders", "Avg Prep Time (minutes)" };

        if (reportTableModel == null) {
            reportTableModel = new DefaultTableModel(columnNames, 0);
            reportTable = new JTable(reportTableModel);
            styleReportTable();
            reportScrollPane = new JScrollPane(reportTable);
            reportScrollPane.setBounds(340, 110, 820, 500);
            reportScrollPane.setBorder(null);
            add(reportScrollPane);
        } else {
            reportTableModel.setRowCount(0);
            reportTableModel.setColumnIdentifiers(columnNames);
        }

        reportScrollPane.setVisible(true);
        reportScrollPane.revalidate();
        reportScrollPane.repaint();
        this.revalidate();
        this.repaint();
    }

    private void runKitchenEfficiencyQuery() {
        Timestamp fromTimestamp;
        Timestamp toTimestamp;

        try {
            LocalDate fromDate = LocalDate.parse(fromDateField.getText().trim());
            LocalDate toDate = LocalDate.parse(toDateField.getText().trim());

            fromTimestamp = Timestamp.valueOf(fromDate.atStartOfDay());
            toTimestamp = Timestamp.valueOf(toDate.atTime(23, 59, 59));
        } catch (DateTimeParseException ex) {
            JOptionPane.showMessageDialog(
                    this,
                    "Please enter valid dates in format yyyy-MM-dd.",
                    "Invalid Date",
                    JOptionPane.ERROR_MESSAGE
            );
            return;
        }

        String sql = """
                SELECT
                    DATE(created_time) AS order_date,
                    COUNT(kitchen_order_id) AS total_orders,
                    AVG(TIMESTAMPDIFF(MINUTE, created_time, completed_time)) AS avg_prep_time_minutes
                FROM pos_kitchen_orders
                WHERE status = 'Ready' AND completed_time IS NOT NULL AND created_time BETWEEN ? AND ?
                GROUP BY order_date
                ORDER BY order_date;
                """;

        try (Connection connection = Database.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql);
             Statement useStmt = connection.createStatement()) {

            useStmt.execute("USE " + App.schemaName);

            stmt.setTimestamp(1, fromTimestamp);
            stmt.setTimestamp(2, toTimestamp);

            try (ResultSet rs = stmt.executeQuery()) {
                reportTableModel.setRowCount(0);

                while (rs.next()) {
                    Object orderDate = rs.getDate("order_date");
                    int totalOrders = rs.getInt("total_orders");
                    double avgPrepMinutes = rs.getDouble("avg_prep_time_minutes");

                    reportTableModel.addRow(new Object[]{ orderDate, totalOrders, avgPrepMinutes });
                }

                if (reportTableModel.getRowCount() == 0) {
                    JOptionPane.showMessageDialog(
                            this,
                            "No ready kitchen orders found for the selected date range.",
                            "No Data",
                            JOptionPane.INFORMATION_MESSAGE
                    );
                }
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(
                    this,
                    "Error running Kitchen Efficiency report:\n" + e.getMessage(),
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private void styleReportTable() {
        if (reportTable == null) return;

        Theme theme = Theme.MONOCHROME;

        reportTable.setBackground(theme.getBackgroundColor());
        reportTable.setForeground(theme.getLabelFontColor());
        reportTable.setFont(theme.getFontStyle());
        reportTable.setGridColor(new java.awt.Color(60, 60, 60));
        reportTable.setRowHeight(28);
        reportTable.setShowVerticalLines(false);
        reportTable.setShowHorizontalLines(true);
        reportTable.setSelectionBackground(theme.getButtonBackground());
        reportTable.setSelectionForeground(theme.getButtonFontColor());
        reportTable.setFillsViewportHeight(true);

        JTableHeader header = reportTable.getTableHeader();
        header.setBackground(theme.getButtonBackground());
        header.setForeground(theme.getButtonFontColor());
        header.setFont(theme.getFontStyle());
        header.setReorderingAllowed(false);
        header.setOpaque(true);
    }
}