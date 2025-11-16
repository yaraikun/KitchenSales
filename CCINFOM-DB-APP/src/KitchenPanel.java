import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.sql.*;

public class KitchenPanel extends BackgroundPanel {

    // UI COMPONENTS
    private JTable ordersTable;
    private DefaultTableModel ordersTableModel;
    private JScrollPane ordersScrollPane;
    private JButton advanceStatusButton;

    // OGIC
    private javax.swing.Timer refreshTimer;

    // SQL QUERIES
    private static final String FETCH_ACTIVE_ORDERS_QUERY =
            "SELECT kitchen_order_id, transaction_id, status, created_time " +
                    "FROM pos_kitchen_orders " +
                    "WHERE status = 'Pending' OR status = 'Preparing' " +
                    "ORDER BY created_time ASC";

    private static final String UPDATE_TO_PREPARING_QUERY =
            "UPDATE pos_kitchen_orders SET status = 'Preparing' WHERE kitchen_order_id = ? AND status = 'Pending'";

    private static final String UPDATE_TO_READY_QUERY =
            "UPDATE pos_kitchen_orders SET status = 'Ready', completed_time = NOW() WHERE kitchen_order_id = ? AND status = 'Preparing'";


    public KitchenPanel() {
        // 1. Call super() and setLayout(null)
        // Using paymentPanel.png as a placeholder
        super("CCINFOM-DB-APP/assets/paymentPanel.png");
        setLayout(null);

        // 2. Create the Theme object
        Theme theme = Theme.MONOCHROME;

        // 3. Call init helper methods
        initTable(theme);
        initControls(theme);
        initTableSelectionListener();
        initRefreshTimer(); // Start the 10-second auto-refresh

        System.out.println("KitchenPanel initialized.");
    }

    private void initTable(Theme theme) {
        String[] columnNames = {"Kitchen ID", "Order ID", "Status", "Time Placed"};

        ordersTableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        ordersTable = new JTable(ordersTableModel);
        ordersTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        ordersScrollPane = new JScrollPane(ordersTable);
        // Your designer can change these bounds
        ordersScrollPane.setBounds(100, 100, 1000, 400);
        add(ordersScrollPane);

        styleOrdersTable(theme);
    }

    private void initControls(Theme theme) {
        advanceStatusButton = theme.createButton();
        // Your designer can change these bounds
        advanceStatusButton.setBounds(100, 520, 1000, 50);

        advanceStatusButton.setText("Select an Order");
        advanceStatusButton.setForeground(theme.getButtonFontColor());
        advanceStatusButton.setFont(theme.getFontStyle());
        advanceStatusButton.setBackground(theme.getButtonBackground());
        advanceStatusButton.setOpaque(true);

        advanceStatusButton.setEnabled(false); // Disabled until a row is selected

        advanceStatusButton.addActionListener(e -> advanceSelectedOrderStatus());

        add(advanceStatusButton);
    }

    private void initTableSelectionListener() {
        ordersTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && ordersTable.getSelectedRow() != -1) {
                // A row is selected
                String currentStatus = (String) ordersTableModel.getValueAt(ordersTable.getSelectedRow(), 2);
                String orderId = ordersTableModel.getValueAt(ordersTable.getSelectedRow(), 0).toString();

                if ("Pending".equals(currentStatus)) {
                    advanceStatusButton.setText("Start Preparing (Order #" + orderId + ")");
                    advanceStatusButton.setEnabled(true);
                } else if ("Preparing".equals(currentStatus)) {
                    advanceStatusButton.setText("Mark as Ready (Order #" + orderId + ")");
                    advanceStatusButton.setEnabled(true);
                }
            } else if (ordersTable.getSelectedRow() == -1) {
                advanceStatusButton.setText("Select an Order");
                advanceStatusButton.setEnabled(false);
            }
        });
    }

    private void initRefreshTimer() {
        refreshTimer = new javax.swing.Timer(10000, e -> fetchActiveOrders());
        refreshTimer.setInitialDelay(0); // Fire immediately on load
        refreshTimer.start();
        System.out.println("KitchenPanel auto-refresh timer started.");
    }

    public void stopRefreshTimer() {
        if (refreshTimer != null) {
            refreshTimer.stop();
            System.out.println("KitchenPanel auto-refresh timer stopped.");
        }
    }

    private void fetchActiveOrders() {
        int selectedRow = ordersTable.getSelectedRow();

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(FETCH_ACTIVE_ORDERS_QUERY)) {

            stmt.execute("USE " + App.schemaName);

            ResultSet rs = stmt.executeQuery();
            ordersTableModel.setRowCount(0); // Clear table

            while (rs.next()) {
                ordersTableModel.addRow(new Object[]{
                        rs.getInt("kitchen_order_id"),
                        rs.getInt("transaction_id"),
                        rs.getString("status"),
                        rs.getTimestamp("created_time")
                });
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(
                    this,
                    "Error fetching kitchen orders:\n" + e.getMessage(),
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }


        if (selectedRow != -1 && selectedRow < ordersTableModel.getRowCount()) {
            ordersTable.setRowSelectionInterval(selectedRow, selectedRow);
        }
    }

    private void advanceSelectedOrderStatus() {
        int selectedRow = ordersTable.getSelectedRow();
        if (selectedRow == -1) return; // No row selected

        // Get data from the selected row
        int kitchenOrderId = (int) ordersTableModel.getValueAt(selectedRow, 0);
        String currentStatus = (String) ordersTableModel.getValueAt(selectedRow, 2);

        String queryToRun;
        if ("Pending".equals(currentStatus)) {
            queryToRun = UPDATE_TO_PREPARING_QUERY;
        } else if ("Preparing".equals(currentStatus)) {
            queryToRun = UPDATE_TO_READY_QUERY;
        } else {
            return; // Should not happen
        }

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(queryToRun)) {

            // The "antic"
            stmt.execute("USE " + App.schemaName);
            stmt.setInt(1, kitchenOrderId);

            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                fetchActiveOrders(); // Success, refresh the table immediately
            }
        } catch (SQLException e) {
            // The "antic"
            JOptionPane.showMessageDialog(
                    this,
                    "Error updating order status:\n" + e.getMessage(),
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private void styleOrdersTable(Theme theme) {
        if (ordersTable == null) return;

        ordersTable.setBackground(theme.getBackgroundColor());
        ordersTable.setForeground(theme.getLabelFontColor());
        ordersTable.setFont(theme.getFontStyle());
        ordersTable.setGridColor(new java.awt.Color(60, 60, 60));
        ordersTable.setRowHeight(28);
        ordersTable.setShowVerticalLines(false);
        ordersTable.setShowHorizontalLines(true);
        ordersTable.setSelectionBackground(theme.getButtonBackground());
        ordersTable.setSelectionForeground(theme.getButtonFontColor());
        ordersTable.setFillsViewportHeight(true);

        JTableHeader header = ordersTable.getTableHeader();
        header.setBackground(theme.getButtonBackground());
        header.setForeground(theme.getButtonFontColor());
        header.setFont(theme.getFontStyle());
        header.setReorderingAllowed(false);
        header.setOpaque(true);
    }
}