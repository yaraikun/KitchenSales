import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.sql.*;
import java.text.SimpleDateFormat;

public class KitchenPanel extends BackgroundPanel {
    private JTable orderListTable;
    private DefaultTableModel orderListModel;
    private final JTextArea orderDetailsArea;
    private final JLabel selectedOrderLabel;
    private final JButton btnPreparing;
    private final JButton btnReady;
    private final JButton btnServed;
    private javax.swing.Timer refreshTimer;
    private int selectedKitchenId = -1;
    private int selectedTransactionId = -1;

    private static final String FETCH_ORDERS_QUERY =
            "SELECT kitchen_order_id, transaction_id, status, created_time " +
                    "FROM pos_kitchen_orders " +
                    "WHERE status IN ('Pending', 'Preparing', 'Ready') " +
                    "ORDER BY FIELD(status, 'Ready', 'Preparing', 'Pending'), created_time ASC";

    private static final String FETCH_ITEMS_QUERY =
            "SELECT item_name, quantity FROM pos_transaction_lines WHERE transaction_id = ?";

    private static final String UPDATE_STATUS_QUERY =
            "UPDATE pos_kitchen_orders SET status = ? WHERE kitchen_order_id = ?";

    private static final String MARK_READY_QUERY =
            "UPDATE pos_kitchen_orders SET status = 'Ready', completed_time = NOW() WHERE kitchen_order_id = ?";


    public KitchenPanel() {
        super("CCINFOM-DB-APP/assets/paymentPanel.png");
        setLayout(new BorderLayout());

        // --- HEADER PANEL (Updated for Back Button) ---
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        headerPanel.setBorder(new EmptyBorder(15, 20, 15, 20));

        // 1. Back Button (Top Left)
        JButton backButton = new JButton("<< BACK TO CASHIER");
        backButton.setFont(new Font("Arial", Font.BOLD, 12));
        backButton.setForeground(Color.WHITE);
        backButton.setBackground(new Color(50, 50, 50));
        backButton.setFocusPainted(false);
        backButton.setBorder(new LineBorder(Color.GRAY, 1));
        backButton.setPreferredSize(new Dimension(180, 30));
        backButton.addActionListener(e -> {
            PanelManager.updateCurrentPanel(new CashierPanel());
        });

        // 2. Title Label (Center)
        JLabel titleLabel = new JLabel("KITCHEN ORDER MANAGEMENT", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setForeground(Color.WHITE);

        // 3. Spacer (Right) to balance the layout
        JLabel spacer = new JLabel();
        spacer.setPreferredSize(new Dimension(180, 30));

        headerPanel.add(backButton, BorderLayout.WEST);
        headerPanel.add(titleLabel, BorderLayout.CENTER);
        headerPanel.add(spacer, BorderLayout.EAST);

        add(headerPanel, BorderLayout.NORTH);

        // --- MAIN SPLIT PANE ---
        JPanel mainContainer = new JPanel(new GridLayout(1, 2, 20, 0));
        mainContainer.setOpaque(false);
        mainContainer.setBorder(new EmptyBorder(10, 20, 20, 20));

        // 1. LEFT SIDE: Order List
        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.setOpaque(false);

        JLabel listHeader = Theme.createLabel("INCOMING ORDERS");
        listHeader.setHorizontalAlignment(SwingConstants.CENTER);
        leftPanel.add(listHeader, BorderLayout.NORTH);

        initOrderTable();
        JScrollPane tableScroll = new JScrollPane(orderListTable);
        tableScroll.getViewport().setBackground(new Color(30, 30, 30));
        leftPanel.add(tableScroll, BorderLayout.CENTER);

        // 2. RIGHT SIDE: Details & Controls
        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.setOpaque(false);
        rightPanel.setBorder(new LineBorder(Color.GRAY, 1));

        // Right Header
        selectedOrderLabel = Theme.createLabel("SELECT AN ORDER");
        selectedOrderLabel.setFont(new Font("Arial", Font.BOLD, 20));
        selectedOrderLabel.setHorizontalAlignment(SwingConstants.CENTER);
        selectedOrderLabel.setBorder(new EmptyBorder(10,0,10,0));
        rightPanel.add(selectedOrderLabel, BorderLayout.NORTH);

        // Item Details Area
        orderDetailsArea = new JTextArea();
        orderDetailsArea.setEditable(false);
        orderDetailsArea.setFont(new Font("Monospaced", Font.PLAIN, 16));
        orderDetailsArea.setBackground(new Color(40, 40, 40));
        orderDetailsArea.setForeground(Color.WHITE);
        orderDetailsArea.setMargin(new Insets(10,10,10,10));
        JScrollPane detailScroll = new JScrollPane(orderDetailsArea);
        rightPanel.add(detailScroll, BorderLayout.CENTER);

        // Control Buttons Panel
        JPanel buttonPanel = new JPanel(new GridLayout(1, 3, 10, 0));
        buttonPanel.setOpaque(false);
        buttonPanel.setBorder(new EmptyBorder(10, 0, 0, 0));

        btnPreparing = createActionButton("START PREPARING", Color.ORANGE);
        btnReady = createActionButton("MARK READY", Color.YELLOW);
        btnServed = createActionButton("ORDER SERVED", Color.GREEN);

        // Initially disable buttons
        toggleButtons(null);

        buttonPanel.add(btnPreparing);
        buttonPanel.add(btnReady);
        buttonPanel.add(btnServed);

        rightPanel.add(buttonPanel, BorderLayout.SOUTH);

        // Add sides to container
        mainContainer.add(leftPanel);
        mainContainer.add(rightPanel);
        add(mainContainer, BorderLayout.CENTER);

        // --- LISTENERS ---
        // Table Selection
        orderListTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                loadSelectedOrderDetails();
            }
        });

        // Button Actions
        btnPreparing.addActionListener(e -> updateOrderStatus("Preparing"));
        btnReady.addActionListener(e -> updateOrderStatus("Ready"));
        btnServed.addActionListener(e -> updateOrderStatus("Served"));

        // Start Refresh Timer
        initRefreshTimer();
        fetchOrders();
    }

    private void initOrderTable() {
        String[] cols = {"Kitchen ID", "Order #", "Time", "Status"};
        orderListModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };

        orderListTable = new JTable(orderListModel);
        orderListTable.setRowHeight(35);
        orderListTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Styling
        orderListTable.setBackground(new Color(40, 40, 40));
        orderListTable.setForeground(Color.WHITE);
        orderListTable.setGridColor(Color.GRAY);
        orderListTable.setFont(new Font("Arial", Font.PLAIN, 14));
        orderListTable.setSelectionBackground(new Color(70, 70, 70));
        orderListTable.setSelectionForeground(Color.WHITE);

        JTableHeader header = orderListTable.getTableHeader();
        header.setBackground(Color.BLACK);
        header.setForeground(Color.WHITE);
        header.setFont(new Font("Arial", Font.BOLD, 14));
    }

    private JButton createActionButton(String text, Color highlight) {
        JButton btn = new JButton("<html><center>" + text + "</center></html>");
        btn.setFont(new Font("Arial", Font.BOLD, 12));
        btn.setBackground(new Color(60, 60, 60));
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setPreferredSize(new Dimension(0, 50));

        // Simple visual tweak: Add border matching the action color
        btn.setBorder(new LineBorder(highlight, 2));
        return btn;
    }

    // --- DATA FETCHING ---

    private void fetchOrders() {
        // Save selection to restore it after refresh
        int savedRow = orderListTable.getSelectedRow();
        int savedId = -1;
        if (savedRow != -1) {
            savedId = (int) orderListModel.getValueAt(savedRow, 0);
        }

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(FETCH_ORDERS_QUERY)) {

            stmt.execute("USE " + App.schemaName);
            ResultSet rs = stmt.executeQuery();

            orderListModel.setRowCount(0);
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");

            while (rs.next()) {
                int kId = rs.getInt("kitchen_order_id");
                int tId = rs.getInt("transaction_id");
                String status = rs.getString("status");
                Timestamp time = rs.getTimestamp("created_time");

                orderListModel.addRow(new Object[]{kId, tId, sdf.format(time), status});
            }

            // Restore selection if it still exists
            if (savedId != -1) {
                for (int i = 0; i < orderListModel.getRowCount(); i++) {
                    if ((int) orderListModel.getValueAt(i, 0) == savedId) {
                        orderListTable.setRowSelectionInterval(i, i);
                        break;
                    }
                }
            }

        } catch (SQLException e) {
            System.err.println("KDS List Error: " + e.getMessage());
        }
    }

    private void loadSelectedOrderDetails() {
        int row = orderListTable.getSelectedRow();
        if (row == -1) {
            selectedOrderLabel.setText("SELECT AN ORDER");
            orderDetailsArea.setText("");
            toggleButtons(null);
            return;
        }

        selectedKitchenId = (int) orderListModel.getValueAt(row, 0);
        selectedTransactionId = (int) orderListModel.getValueAt(row, 1);
        String status = (String) orderListModel.getValueAt(row, 3);

        selectedOrderLabel.setText("ORDER #" + selectedTransactionId + " (" + status + ")");
        toggleButtons(status);

        // Fetch Items for this order
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(FETCH_ITEMS_QUERY)) {

            stmt.execute("USE " + App.schemaName);
            stmt.setInt(1, selectedTransactionId);
            ResultSet rs = stmt.executeQuery();

            StringBuilder sb = new StringBuilder();
            sb.append("--------------------------------\n");
            sb.append(" QTY   ITEM\n");
            sb.append("--------------------------------\n");

            while (rs.next()) {
                String name = rs.getString("item_name");
                int qty = rs.getInt("quantity");
                sb.append(String.format(" %-5d %s\n", qty, name));
            }
            sb.append("--------------------------------\n");

            orderDetailsArea.setText(sb.toString());

        } catch (SQLException e) {
            orderDetailsArea.setText("Error loading items: " + e.getMessage());
        }
    }

    private void updateOrderStatus(String newStatus) {
        if (selectedKitchenId == -1) return;

        String query = "Served".equals(newStatus) ? UPDATE_STATUS_QUERY :
                ("Ready".equals(newStatus) ? MARK_READY_QUERY : UPDATE_STATUS_QUERY);

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.execute("USE " + App.schemaName);

            if ("Ready".equals(newStatus)) {
                stmt.setInt(1, selectedKitchenId);
            } else {
                stmt.setString(1, newStatus);
                stmt.setInt(2, selectedKitchenId);
            }

            stmt.executeUpdate();
            fetchOrders(); // Refresh list

            // If Served, clear selection
            if ("Served".equals(newStatus)) {
                orderListTable.clearSelection();
                selectedOrderLabel.setText("SELECT AN ORDER");
                orderDetailsArea.setText("");
            } else {
                // Else just reload details to update label
                loadSelectedOrderDetails();
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Update Error: " + e.getMessage());
        }
    }

    private void toggleButtons(String status) {
        btnPreparing.setEnabled(false);
        btnReady.setEnabled(false);
        btnServed.setEnabled(false);

        // Visual dimming
        btnPreparing.setBackground(new Color(40, 40, 40));
        btnReady.setBackground(new Color(40, 40, 40));
        btnServed.setBackground(new Color(40, 40, 40));

        if (status == null) return;

        switch (status) {
            case "Pending":
                btnPreparing.setEnabled(true);
                btnPreparing.setBackground(new Color(80, 60, 30)); // Dim Orange
                break;
            case "Preparing":
                btnReady.setEnabled(true);
                btnReady.setBackground(new Color(80, 80, 30)); // Dim Yellow
                break;
            case "Ready":
                btnServed.setEnabled(true);
                btnServed.setBackground(new Color(30, 80, 30)); // Dim Green
                break;
        }
    }

    private void initRefreshTimer() {
        refreshTimer = new javax.swing.Timer(5000, e -> fetchOrders());
        refreshTimer.setInitialDelay(0);
        refreshTimer.start();
    }

    public void stopRefreshTimer() {
        if (refreshTimer != null) refreshTimer.stop();
    }
}