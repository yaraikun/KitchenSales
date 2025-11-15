import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;

/**
 * Manager Reports screen.
 * Reuses one date filter + table area for multiple reports:
 *  - Sales Summary
 *  - Sales Performance (product performance)
 */
public class ReportsPanel extends BackgroundPanel {

    private JTable reportTable;
    private DefaultTableModel reportTableModel;
    private JScrollPane reportScrollPane;

    private JLabel fromLabel;
    private JLabel toLabel;
    private JTextField fromDateField;
    private JTextField toDateField;
    private JButton runReportButton;

    private final DecimalFormat currencyFormat = new DecimalFormat("#,##0.00");

    // which report is currently selected (controls what the "Run" button does)
    private enum ReportType {
        NONE,
        SALES_SUMMARY,
        SALES_PERFORMANCE
    }

    private ReportType currentReportType = ReportType.NONE;

    // =============================
    // Constructor
    // =============================

    /**
     * Creates the ReportsPanel for a specific manager username.
     */
    public ReportsPanel(String managerUsername) {
        // Use the cashier background image for now
        super("assets/cashierPanel.png");
        // Absolute layout to match the rest of the app
        setLayout(null);

        Theme theme = Theme.MONOCHROME;

        initHeader(theme, managerUsername);
        initReportButtons(theme);
        initSharedDateControls(theme);

        System.out.println("ReportsPanel initialized with report buttons.");
    }


    // ============================================
    //  Header (manager name and logout button).
    // ============================================
    private void initHeader(Theme theme, String managerUsername) {
        JLabel managerLabel = theme.createLabel("Manager: " + managerUsername);
        managerLabel.setBounds(750, 10, 250, 25);
        add(managerLabel);

        JButton logoutButton = theme.createButton();
        logoutButton.setText("Logout");
        logoutButton.setBorderPainted(true);
        logoutButton.setBorder(BorderFactory.createLineBorder(Color.WHITE, 2));
        logoutButton.setBounds(1020, 8, 120, 30);
        logoutButton.addActionListener(_ -> {
            LoginPanel loginPanel = new LoginPanel(theme);
            PanelManager.updateCurrentPanel(loginPanel);
        });
        add(logoutButton);
    }

    // ===========================
    //  report buttons (temporary)
    // ===========================
    private void initReportButtons(Theme theme) {
        JButton salesSummaryButton = theme.createButton();
        JButton salesPerformanceButton = theme.createButton();
        JButton discountUtilizationButton = theme.createButton();
        JButton paymentMethodButton = theme.createButton();
        JButton kitchenEfficiencyButton = theme.createButton();

        salesSummaryButton.setText("Sales Summary");
        salesPerformanceButton.setText("Sales Performance");
        discountUtilizationButton.setText("Discount Utilization");
        paymentMethodButton.setText("Payment Method Breakdown");
        kitchenEfficiencyButton.setText("Kitchen Efficiency");

        JButton[] reportButtons = {
                salesSummaryButton,
                salesPerformanceButton,
                discountUtilizationButton,
                paymentMethodButton,
                kitchenEfficiencyButton
        };

        // styled the transparent buttons with borders (temporarily)
        for (JButton btn : reportButtons) {
            btn.setBorderPainted(true);
            btn.setContentAreaFilled(false);
            btn.setOpaque(false);
            btn.setBorder(BorderFactory.createLineBorder(Color.WHITE, 2));
            btn.setHorizontalAlignment(SwingConstants.CENTER);
        }

        int x = 40;
        int y = 80;
        int width = 260;
        int height = 50;
        int gap = 20;

        salesSummaryButton.setBounds(x, y, width, height);
        salesPerformanceButton.setBounds(x, y + (height + gap), width, height);
        discountUtilizationButton.setBounds(x, y + 2 * (height + gap), width, height);
        paymentMethodButton.setBounds(x, y + 3 * (height + gap), width, height);
        kitchenEfficiencyButton.setBounds(x, y + 4 * (height + gap), width, height);

        add(salesSummaryButton);
        add(salesPerformanceButton);
        add(discountUtilizationButton);
        add(paymentMethodButton);
        add(kitchenEfficiencyButton);


        // Sales Summary button
        salesSummaryButton.addActionListener(_ -> {
            currentReportType = ReportType.SALES_SUMMARY;
            setDateControlsAndTableVisible(true);
            setDefaultDateRange();
            setupSalesSummaryTable();
        });

        // Sales Performance button
        salesPerformanceButton.addActionListener(_ -> {
            currentReportType = ReportType.SALES_PERFORMANCE;
            setDateControlsAndTableVisible(true);
            setDefaultDateRange();
            setupSalesPerformanceTable();
        });


        // Discount Utilization button

        // Payment method button

        // Kitchen efficiency button

    }

    // =====================================
    // Shared date controls (From/To + Run)
    // =====================================
    private void initSharedDateControls(Theme theme) {
        int filterY = 55;      // y-position for the filters (second row)
        int filterHeight = 25; // height for all filter controls

        fromLabel = theme.createLabel("From (yyyy-MM-dd):");
        fromLabel.setBounds(340, filterY, 180, filterHeight);
        add(fromLabel);

        fromDateField = theme.createTextField();
        fromDateField.setBounds(520, filterY, 120, filterHeight);
        add(fromDateField);

        toLabel = theme.createLabel("To (yyyy-MM-dd):");
        toLabel.setBounds(660, filterY, 180, filterHeight);
        add(toLabel);

        toDateField = theme.createTextField();
        toDateField.setBounds(830, filterY, 120, filterHeight);
        add(toDateField);

        runReportButton = theme.createButton();
        runReportButton.setText("Run");
        runReportButton.setBorderPainted(true);
        runReportButton.setBorder(BorderFactory.createLineBorder(Color.WHITE, 2));
        runReportButton.setBounds(970, filterY, 80, filterHeight);
        add(runReportButton);

        runReportButton.addActionListener(_ -> runCurrentReport());

        setDateControlsAndTableVisible(false);
    }

    //temporarily hides the filter controls and table until a report is chosen by the manager
    private void setDateControlsAndTableVisible(boolean visible) {
        if (fromLabel != null)      fromLabel.setVisible(visible);
        if (fromDateField != null)  fromDateField.setVisible(visible);
        if (toLabel != null)        toLabel.setVisible(visible);
        if (toDateField != null)    toDateField.setVisible(visible);
        if (runReportButton != null) runReportButton.setVisible(visible);

        if (reportScrollPane != null) {
            reportScrollPane.setVisible(visible);
        }
    }

    //sets the default date range "current today"
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

    //runs the current report based on the current report type
    private void runCurrentReport() {
        switch (currentReportType) {
            case SALES_SUMMARY -> runSalesSummaryQuery();
            case SALES_PERFORMANCE -> runSalesPerformanceQuery();
            case NONE -> JOptionPane.showMessageDialog(
                    this,
                    "Please select a report first.",
                    "No Report Selected",
                    JOptionPane.WARNING_MESSAGE
            );
        }
    }

    // =============================
    //        Sales Summary
    // =============================

    private void setupSalesSummaryTable() {
        String[] columnNames = { "Date", "Total Transactions", "Total Sales" };

        if (reportTableModel == null) {
            reportTableModel = new DefaultTableModel(columnNames, 0);
            reportTable = new JTable(reportTableModel);
            reportScrollPane = new JScrollPane(reportTable);
            reportScrollPane.setBounds(340, 95, 820, 505);
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

    // runs a query from 04_report_queries.sql
    private void runSalesSummaryQuery() {
        // Parse and validate dates
        java.sql.Timestamp fromTimestamp;
        java.sql.Timestamp toTimestamp;

        try {
            LocalDate fromDate = LocalDate.parse(fromDateField.getText().trim());
            LocalDate toDate   = LocalDate.parse(toDateField.getText().trim());

            fromTimestamp = Timestamp.valueOf(fromDate.atStartOfDay());
            toTimestamp   = Timestamp.valueOf(toDate.atTime(23, 59, 59));
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
             PreparedStatement stmt = connection.prepareStatement(sql)) {

            try (Statement useStmt = connection.createStatement()) {
                useStmt.execute("USE " + App.schemaName);
            }

            // Bind params and execute
            stmt.setTimestamp(1, fromTimestamp);
            stmt.setTimestamp(2, toTimestamp);

            try (ResultSet rs = stmt.executeQuery()) {
                reportTableModel.setRowCount(0);

                while (rs.next()) {
                    Object date = rs.getDate("sale_date");
                    int totalTransactions = rs.getInt("total_transactions");
                    double totalSales = rs.getDouble("total_sales");
                    String totalSalesFormatted = currencyFormat.format(totalSales);

                    reportTableModel.addRow(
                            new Object[]{ date, totalTransactions, totalSalesFormatted }
                    );
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
            e.printStackTrace();
            JOptionPane.showMessageDialog(
                    this,
                    "Error running Sales Summary report:\n" + e.getMessage(),
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    // =============================
    //       Sales Performance
    // =============================

    private void setupSalesPerformanceTable() {
        String[] columnNames = { "Item Name", "Total Quantity Sold", "Total Revenue" };

        if (reportTableModel == null) {
            reportTableModel = new DefaultTableModel(columnNames, 0);
            reportTable = new JTable(reportTableModel);
            reportScrollPane = new JScrollPane(reportTable);
            reportScrollPane.setBounds(340, 95, 820, 505);
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

    // runs a query from 04_report_queries.sql
    private void runSalesPerformanceQuery() {
        java.sql.Timestamp fromTimestamp;
        java.sql.Timestamp toTimestamp;

        try {
            LocalDate fromDate = LocalDate.parse(fromDateField.getText().trim());
            LocalDate toDate   = LocalDate.parse(toDateField.getText().trim());

            fromTimestamp = Timestamp.valueOf(fromDate.atStartOfDay());
            toTimestamp   = Timestamp.valueOf(toDate.atTime(23, 59, 59));
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
             PreparedStatement stmt = connection.prepareStatement(sql)) {

            try (Statement useStmt = connection.createStatement()) {
                useStmt.execute("USE " + App.schemaName);
            }

            stmt.setTimestamp(1, fromTimestamp);
            stmt.setTimestamp(2, toTimestamp);

            try (ResultSet rs = stmt.executeQuery()) {
                reportTableModel.setRowCount(0);

                while (rs.next()) {
                    String itemName = rs.getString("item_name");
                    int totalQty = rs.getInt("total_quantity_sold");
                    double totalRevenue = rs.getDouble("total_revenue");
                    String totalRevenueFormatted = currencyFormat.format(totalRevenue);

                    reportTableModel.addRow(
                            new Object[]{ itemName, totalQty, totalRevenueFormatted }
                    );
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
            e.printStackTrace();
            JOptionPane.showMessageDialog(
                    this,
                    "Error running Sales Performance report:\n" + e.getMessage(),
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    // =============================
    // Misc
    // =============================
    private boolean checkLogin(String username, String password, boolean unusedFlag) {
        String query = "SELECT * FROM users WHERE username = ? AND password = ?";

        try (Connection connection = Database.getConnection();
             Statement stmt1 = connection.createStatement();
             PreparedStatement stmt2 = connection.prepareStatement(query)) {

            stmt1.execute("USE " + App.schemaName);
            // Real implementation would verify the ResultSet;
            // for now we just return true if no exception occurs.
            return true;
        } catch (SQLException f) {
            throw new RuntimeException(f);
        }
    }
}