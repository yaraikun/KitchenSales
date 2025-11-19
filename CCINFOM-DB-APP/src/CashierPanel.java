import javax.swing.*;
import java.awt.*;
import java.sql.*;
import java.util.ArrayList;

public class CashierPanel extends BackgroundPanel {
    ArrayList<ItemRecord> currentItems = new ArrayList<>();

    private void openPaymentPanel() {
        double lastTransactionTotal = 0;
        int lastOrderTransactionID = 0;

        try (Connection connection = Database.getConnection();
             Statement stmt = connection.createStatement()) {
            stmt.execute("USE " + App.schemaName);
            String sql = """
                SELECT total_amount
                FROM pos_transactions
                ORDER BY transaction_id DESC
                LIMIT 1;
            """;

            ResultSet rs = stmt.executeQuery(sql);

            if (rs.next()) {
                lastTransactionTotal = rs.getDouble("total_amount");
            }

            sql = """
                SELECT transaction_id
                FROM pos_transactions
                ORDER BY transaction_id DESC
                LIMIT 1;
            """;

            rs = stmt.executeQuery(sql);

            if (rs.next()) {
                lastOrderTransactionID = rs.getInt("transaction_id");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        PaymentPanel paymentPanel = new PaymentPanel(lastOrderTransactionID, lastTransactionTotal);
        PanelManager.updateCurrentPanel(paymentPanel);
    }

    private void sendToKitchen(CashierCurrentItemsPanel itemPanel) {
        if (currentItems.isEmpty()) {
            JOptionPane.showMessageDialog(null, "No items to send.");
            return;
        }

        try (Connection connection = Database.getConnection();
            Statement stmt1 = connection.createStatement()) {
            connection.setAutoCommit(false);
            stmt1.execute("USE " + App.schemaName);
            double totalAmount = currentItems.stream()
                    .mapToDouble(ItemRecord::getPrice)
                    .sum();
            double tax = totalAmount * 0.12;
            double net = totalAmount - tax;

            String transSQL = """
            INSERT INTO pos_transactions (
                transaction_date, terminal_id,
                total_amount, tax_amount, net_amount,
                status_code, payment_status
            ) VALUES (NOW(), 1, ?, ?, ?, 'ACTIVE', 'UNPAID');
        """;

            PreparedStatement transStmt = connection.prepareStatement(transSQL, Statement.RETURN_GENERATED_KEYS);
            transStmt.setDouble(1, totalAmount);
            transStmt.setDouble(2, tax);
            transStmt.setDouble(3, net);
            transStmt.executeUpdate();

            ResultSet transKeys = transStmt.getGeneratedKeys();
            transKeys.next();
            int transactionId = transKeys.getInt(1);

            String kitchenSQL = """
            INSERT INTO pos_kitchen_orders (
                transaction_id, status_code, created_time, completed_time
            ) VALUES (?, 'PENDING', NOW(), NULL);
        """;

            PreparedStatement kitchenStmt = connection.prepareStatement(kitchenSQL, Statement.RETURN_GENERATED_KEYS);
            kitchenStmt.setInt(1, transactionId);
            kitchenStmt.executeUpdate();

            ResultSet kitchenKeys = kitchenStmt.getGeneratedKeys();
            kitchenKeys.next();
            int kitchenOrderId = kitchenKeys.getInt(1);

            String lineSQL = """
            INSERT INTO pos_transaction_lines (
                transaction_id, kitchen_order_id, item_id, quantity, line_total
            ) VALUES (?, ?, ?, ?, ?);
        """;

            PreparedStatement lineStmt = connection.prepareStatement(lineSQL);

            for (ItemRecord item : currentItems) {
                lineStmt.setInt(1, transactionId);
                lineStmt.setInt(2, kitchenOrderId);
                lineStmt.setInt(3, item.getId());
                lineStmt.setInt(4, 1);
                lineStmt.setDouble(5, item.getPrice());
                lineStmt.addBatch();
            }

            lineStmt.executeBatch();
            connection.commit();
            JOptionPane.showMessageDialog(null,
                    "Order sent to kitchen!\nKitchen Order ID: " + kitchenOrderId);

            currentItems.clear();
            itemPanel.refresh(itemPanel.itemContainer, currentItems);
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error sending to kitchen.");
        }
    }

    private void voidOrder(CashierCurrentItemsPanel itemPanel) {
        int confirm = JOptionPane.showConfirmDialog(
                null,
                "Are you sure you want to void/delete this order?",
                "Confirm Void/Delete",
                JOptionPane.YES_NO_OPTION
        );
        if (confirm == JOptionPane.YES_OPTION) {
            currentItems.clear();
            itemPanel.refresh(itemPanel.itemContainer, currentItems);
            JOptionPane.showMessageDialog(null,
                    "Order has been voided.");
        }
    }

    private void applyDiscount() {
        String[] discountOptions = {
                "PWD",
                "Senior Citizen",
                "Employee Discount",
                "Promo Discount",
                "Manager Override",
                "Staff Meal",
                "Holiday Promo",
                "No Discount"
        };

        String selected = (String) JOptionPane.showInputDialog(
                null,
                "Select Discount to Apply:",
                "Apply Discount",
                JOptionPane.QUESTION_MESSAGE,
                null,
                discountOptions,
                discountOptions[0]
        );

        if (selected != null) {
            JOptionPane.showMessageDialog(null,
                    "Applied discount: " + selected);
        }
    }

    public CashierPanel() {
        super("CCINFOM-DB-APP/assets/cashierPanel.png");
        setLayout(null);

        CashierCurrentItemsPanel itemPanel = new CashierCurrentItemsPanel(currentItems);
        itemPanel.setBounds(0, 0, 1200, 70);
        add(itemPanel);

        CashierMenuPanel menuPanel = new CashierMenuPanel(currentItems, itemPanel);
        menuPanel.setBounds(0, 110, 1200, 330);
        add(menuPanel);

        JButton kitchenButton = new JButton();
        JButton discountButton = new JButton();
        JButton voidButton = new JButton();
        JButton payButton = new JButton();
        JButton[] buttons = {kitchenButton, discountButton, voidButton, payButton};

        kitchenButton.setBounds(135, 458, 134, 127);
        discountButton.setBounds(401, 458, 134, 127);
        voidButton.setBounds(665, 458, 134, 127);
        payButton.setBounds(906, 458, 134, 127);

        for (JButton btn : buttons) {
            btn.setOpaque(false);
            btn.setContentAreaFilled(false);
            btn.setBorderPainted(false);
            btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
            add(btn);
        }

        kitchenButton.addActionListener(_ -> sendToKitchen(itemPanel));
        discountButton.addActionListener(_ -> applyDiscount());
        voidButton.addActionListener(_ -> voidOrder(itemPanel));
        payButton.addActionListener(_ -> openPaymentPanel());
    }
}
