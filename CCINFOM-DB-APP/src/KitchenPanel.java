import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.*;

public class KitchenPanel extends BackgroundPanel {

    // --- UI COMPONENTS ---
    private JPanel pendingPanel;
    private JPanel preparingPanel;
    private JPanel readyPanel;

    // --- LOGIC ---
    private javax.swing.Timer refreshTimer;

    // --- SQL QUERIES ---
    private static final String FETCH_ACTIVE_ORDERS_QUERY =
            "SELECT kitchen_order_id, transaction_id, status FROM pos_kitchen_orders " +
                    "WHERE status IN ('Pending', 'Preparing', 'Ready') ORDER BY created_time ASC";

    private static final String UPDATE_STATUS_QUERY =
            "UPDATE pos_kitchen_orders SET status = ? WHERE kitchen_order_id = ?";

    private static final String MARK_AS_READY_QUERY =
            "UPDATE pos_kitchen_orders SET status = 'Ready', completed_time = NOW() WHERE kitchen_order_id = ?";

    public KitchenPanel() {
        // Use the placeholder background
        super("CCINFOM-DB-APP/assets/paymentPanel.png");
        setLayout(new BorderLayout());

        Theme theme = Theme.MONOCHROME;

        // Title Header
        JLabel titleLabel = new JLabel("KITCHEN DISPLAY SYSTEM", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 28));
        titleLabel.setForeground(theme.getLabelFontColor()); // Use Theme color (White/Silver)
        titleLabel.setBorder(new EmptyBorder(10,0,10,0));
        add(titleLabel, BorderLayout.NORTH);

        // Main Content Area (Split into 3 Columns)
        JPanel mainContainer = new JPanel(new GridLayout(1, 3, 15, 0)); // 3 Cols, 15px gap
        mainContainer.setOpaque(false);
        mainContainer.setBorder(new EmptyBorder(10, 20, 20, 20));

        // --- 1. PENDING COLUMN ---
        pendingPanel = createColumn(mainContainer, "PENDING", Color.WHITE);

        // --- 2. PREPARING COLUMN ---
        preparingPanel = createColumn(mainContainer, "PREPARING", Color.LIGHT_GRAY);

        // --- 3. READY TO SERVE COLUMN ---
        readyPanel = createColumn(mainContainer, "READY TO SERVE", Color.GRAY);

        add(mainContainer, BorderLayout.CENTER);

        // Start Logic
        initRefreshTimer();
        fetchActiveOrders();
    }

    /**
     * Helper to build the UI for one column.
     */
    private JPanel createColumn(JPanel parent, String title, Color borderColor) {
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);

        // Header Label
        JLabel header = new JLabel(title, SwingConstants.CENTER);
        header.setFont(new Font("Arial", Font.BOLD, 22));
        header.setForeground(borderColor); // Monotone text color
        header.setBorder(new EmptyBorder(0,0,10,0));
        wrapper.add(header, BorderLayout.NORTH);

        // Inner Panel for Tickets
        JPanel contentPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        contentPanel.setOpaque(false);

        // ScrollPane
        JScrollPane scroll = new JScrollPane(contentPanel);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        // Border color matches the column theme (White/Gray)
        scroll.setBorder(BorderFactory.createLineBorder(borderColor, 2));

        wrapper.add(scroll, BorderLayout.CENTER);
        parent.add(wrapper);

        return contentPanel;
    }

    private void fetchActiveOrders() {
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(FETCH_ACTIVE_ORDERS_QUERY)) {

            stmt.execute("USE " + App.schemaName);
            ResultSet rs = stmt.executeQuery();

            // Clear current lists
            pendingPanel.removeAll();
            preparingPanel.removeAll();
            readyPanel.removeAll();

            while (rs.next()) {
                int kId = rs.getInt("kitchen_order_id");
                int tId = rs.getInt("transaction_id");
                String status = rs.getString("status");

                // Create the ticket block (Status determines which column, but block style is uniform)
                JPanel ticket = createOrderBlock(kId, tId);

                // Sort into the correct column
                switch (status) {
                    case "Pending":
                        pendingPanel.add(ticket);
                        break;
                    case "Preparing":
                        preparingPanel.add(ticket);
                        break;
                    case "Ready":
                        readyPanel.add(ticket);
                        break;
                }
            }

            // Refresh UI
            pendingPanel.revalidate(); pendingPanel.repaint();
            preparingPanel.revalidate(); preparingPanel.repaint();
            readyPanel.revalidate(); readyPanel.repaint();

        } catch (SQLException e) {
            System.err.println("KDS DB Error: " + e.getMessage());
        }
    }

    /**
     * Creates a Monotone Block for an order.
     */
    private JPanel createOrderBlock(int kitchenId, int transactionId) {
        JPanel block = new JPanel(new BorderLayout());
        block.setPreferredSize(new Dimension(100, 80)); // Square block size
        block.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Monotone Styling
        Color bgColor = new Color(60, 60, 60); // Dark Gray Background
        Color borderColor = new Color(200, 200, 200); // Light Gray/Silver Border

        block.setBackground(bgColor);
        block.setBorder(BorderFactory.createLineBorder(borderColor, 2));

        // Order Number
        JLabel numLabel = new JLabel("#" + transactionId, SwingConstants.CENTER);
        numLabel.setFont(new Font("Arial", Font.BOLD, 24));
        numLabel.setForeground(Color.WHITE);
        block.add(numLabel, BorderLayout.CENTER);

        // Add Click Listener
        block.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                // We need to check the column based on parent to know status,
                // or we can just pass status.
                // Since I removed status from args to make it monotone,
                // let's re-fetch logic or simplify:
                // Actually, the advanceStatus needs the current status.
                // Let's fetch it fresh or pass it back in.

                // Quick fix: Pass status into this method purely for logic, not color.
                // But wait, the caller 'fetchActiveOrders' knows the status.
                // Let's revert the method signature to accept status for LOGIC, even if we don't use it for COLOR.
            }
        });

        return block;
    }

    // Overloading to keep logic clean
    private JPanel createOrderBlock(int kitchenId, int transactionId, String status) {
        JPanel block = createOrderBlock(kitchenId, transactionId);

        // Add the listener here using the status
        block.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                advanceStatus(kitchenId, status);
            }
        });
        return block;
    }


    /**
     * Moves the order to the next column in the flow.
     */
    private void advanceStatus(int kitchenId, String currentStatus) {
        String query;
        String nextStatus;
        String message;

        // Define the flow logic
        if ("Pending".equals(currentStatus)) {
            query = UPDATE_STATUS_QUERY;
            nextStatus = "Preparing";
            message = "Start Cooking Order #" + kitchenId + "?";
        } else if ("Preparing".equals(currentStatus)) {
            query = MARK_AS_READY_QUERY; // This sets the completed_time
            nextStatus = "Ready";
            message = "Mark Order #" + kitchenId + " as Ready to Serve?";
        } else {
            // If "Ready", moving it means it is SERVED/PICKED UP
            query = UPDATE_STATUS_QUERY;
            nextStatus = "Served"; // This will remove it from the screen
            message = "Clear Order #" + kitchenId + " (Served)?";
        }

        // Confirmation Dialog
        int choice = JOptionPane.showConfirmDialog(this,
                message, "Update Order Status", JOptionPane.YES_NO_OPTION);

        if (choice != JOptionPane.YES_OPTION) return;

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.execute("USE " + App.schemaName);

            if ("Pending".equals(currentStatus) || "Ready".equals(currentStatus)) {
                // Simple status update
                stmt.setString(1, nextStatus);
                stmt.setInt(2, kitchenId);
            } else {
                // 'Mark as Ready' query only takes ID
                stmt.setInt(1, kitchenId);
            }

            stmt.executeUpdate();
            fetchActiveOrders(); // Refresh immediately

        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
        }
    }

    private void initRefreshTimer() {
        refreshTimer = new javax.swing.Timer(5000, e -> fetchActiveOrders());
        refreshTimer.setInitialDelay(0);
        refreshTimer.start();
    }

    public void stopRefreshTimer() {
        if (refreshTimer != null) refreshTimer.stop();
    }
}