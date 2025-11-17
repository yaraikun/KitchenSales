import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DecimalFormat;

/**
 * Manages the user interface for processing customer payments.
 * <p>
 * This panel displays the total transaction cost. It provides fields for the
 * amount given by the customer and calculates the change. It also supports
 * different payment methods like Cash, Card, and E-Wallet. After a successful
 * payment, it updates the transaction status in the database.
 */
public class PaymentPanel extends BackgroundPanel {

    /**
     * The unique identifier for the current transaction.
     */
    private final int transactionId;

    /**
     * The total cost of items in the current transaction.
     */
    private final double totalCost;

    /**
     * Text field for entering the amount of money from the customer.
     */
    private JTextField amountGivenField;

    /**
     * Displays the formatted total cost of the transaction.
     */
    private JLabel totalCostValue;

    /**
     * Displays the calculated change to be returned to the customer.
     */
    private JLabel changeLabelValue;

    /**
     * Button to finalize the payment and record the transaction.
     */
    private JButton confirmButton;

    /**
     * Button to cancel the payment process and return to the cashier panel.
     */
    private JButton cancelButton;

    /**
     * A dropdown menu to select the method of payment.
     */
    private JComboBox<String> paymentTypeBox;

    /**
     * The payment method currently selected, defaulting to "Cash".
     */
    private String selectedPaymentMethod = "Cash";

    /**
     * Formats numbers into a standard two-decimal currency format.
     */
    private final DecimalFormat currencyFormat = new DecimalFormat("0.00");

    /**
     * Constructs a new PaymentPanel for a specific transaction.
     * <p>
     * This constructor sets up the panel with the transaction details and
     * initializes all user interface components and their event listeners.
     *
     * @param transactionId The unique ID for the transaction.
     * @param totalCost     The total cost for the transaction.
     */
    public PaymentPanel(int transactionId, double totalCost) {
        super("CCINFOM-DB-APP/assets/paymentPanel.png");
        this.transactionId = transactionId;
        this.totalCost = totalCost;
        setLayout(null);

        initComponents();
        addEventListeners();
    }

    /**
     * Initializes and configures all user interface components.
     * This method sets up the labels, text fields, buttons, and the dropdown
     * menu with specific fonts, colors, and positions on the panel.
     */
    private void initComponents() {
        Font labelFont = new Font("Arial", Font.BOLD, 36);
        Color fontColor = Color.WHITE;
        Theme theme = Theme.MONOCHROME;

        // --- LABELS ---
        totalCostValue = new JLabel(currencyFormat.format(totalCost));
        totalCostValue.setBounds(780, 170, 274, 83);
        totalCostValue.setFont(labelFont);
        totalCostValue.setForeground(fontColor);
        totalCostValue.setHorizontalAlignment(SwingConstants.CENTER);
        add(totalCostValue);

        changeLabelValue = new JLabel("0.00");
        changeLabelValue.setBounds(780, 362, 274, 83);
        changeLabelValue.setFont(labelFont);
        changeLabelValue.setForeground(fontColor);
        changeLabelValue.setHorizontalAlignment(SwingConstants.CENTER);
        add(changeLabelValue);

        // --- TEXT FIELD ---
        amountGivenField = new JTextField("0.00");
        amountGivenField.setBounds(780, 266, 274, 83);
        amountGivenField.setFont(labelFont);
        amountGivenField.setForeground(fontColor);
        amountGivenField.setBackground(new Color(80, 80, 80));
        amountGivenField.setCaretColor(Color.WHITE);
        amountGivenField.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        amountGivenField.setHorizontalAlignment(SwingConstants.CENTER);
        add(amountGivenField);

        // --- PAYMENT TYPE DROPDOWN ---
        String[] paymentOptions = {"Cash", "Card", "E-Wallet"};
        paymentTypeBox = new JComboBox<>(paymentOptions);
        paymentTypeBox.setBounds(710, 75, 346, 83);
        paymentTypeBox.setFont(labelFont);
        paymentTypeBox.setForeground(fontColor);
        paymentTypeBox.setBackground(new Color(80, 80, 80));
        paymentTypeBox.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        ((JLabel) paymentTypeBox.getRenderer()).setHorizontalAlignment(
            SwingConstants.CENTER);
        add(paymentTypeBox);

        // --- ACTION BUTTONS ---
        cancelButton = theme.createButton();
        cancelButton.setBounds(142, 469, 416, 104);
        add(cancelButton);

        confirmButton = theme.createButton();
        confirmButton.setBounds(639, 469, 416, 104);
        confirmButton.setEnabled(false);
        add(confirmButton);
    }

    /**
     * Registers event listeners for the interactive components.
     * This method connects user actions, like typing or button clicks,
     * to the corresponding logic for handling those events.
     */
    private void addEventListeners() {
        amountGivenField.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { calculateChange(); }
            public void removeUpdate(DocumentEvent e) { calculateChange(); }
            public void changedUpdate(DocumentEvent e) { calculateChange(); }
        });

        paymentTypeBox.addActionListener(e -> {
            String selectedMethod = (String) paymentTypeBox.getSelectedItem();
            selectPaymentMethod(selectedMethod);
        });

        cancelButton.addActionListener(
            e -> PanelManager.updateCurrentPanel(new CashierPanel()));
        confirmButton.addActionListener(e -> processPayment());
    }

    /**
     * Updates the panel based on the selected payment method.
     * If "Cash" is chosen, the amount field is enabled. For other methods,
     * the field is disabled and auto-filled with the total cost.
     *
     * @param method The payment method selected from the dropdown.
     */
    private void selectPaymentMethod(String method) {
        selectedPaymentMethod = method;

        if ("Cash".equals(method)) {
            amountGivenField.setEnabled(true);
            amountGivenField.setText("0.00");
            amountGivenField.requestFocus();
        } else {
            amountGivenField.setEnabled(false);
            amountGivenField.setText(currencyFormat.format(totalCost));
        }
        calculateChange();
    }

    /**
     * Calculates the change due based on the amount given.
     * It reads the value from the amount-given field and subtracts the total
     * cost. The result is displayed. It also validates the input to check
     * for valid numbers and sufficient payment.
     */
    private void calculateChange() {
        try {
            double amountGiven = Double.parseDouble(amountGivenField.getText());
            if (amountGiven >= totalCost) {
                double change = amountGiven - totalCost;
                changeLabelValue.setText(currencyFormat.format(change));
                confirmButton.setEnabled(true);
            } else {
                changeLabelValue.setText("Insufficient");
                confirmButton.setEnabled(false);
            }
        } catch (NumberFormatException e) {
            changeLabelValue.setText("Invalid Input");
            confirmButton.setEnabled(false);
        }
    }

    /**
     * Processes the final payment by updating the database.
     * It inserts a new record into the payments table. It then updates the
     * transaction's status to 'Completed'. All database operations are
     * performed within a single transaction to maintain data integrity.
     */
    private void processPayment() {
        double amountGiven;
        try {
            amountGiven = Double.parseDouble(amountGivenField.getText());
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this,
                "Invalid amount entered.",
                "Input Error",
                JOptionPane.ERROR_MESSAGE);
            return;
        }

        String insertPaymentSQL = "INSERT INTO payments (transaction_id, payment_method, amount, payment_date) VALUES (?, ?, ?, NOW())";
        String updateTransactionSQL = "UPDATE pos_transactions SET status = 'Completed', payment_status = 'Paid' WHERE transaction_id = ?";

        try (Connection connection = Database.getConnection()) {
            connection.setAutoCommit(false);
            try (Statement useStmt = connection.createStatement();
                 PreparedStatement insertStmt =
                     connection.prepareStatement(insertPaymentSQL);
                 PreparedStatement updateStmt =
                     connection.prepareStatement(updateTransactionSQL)) {
                useStmt.execute("USE " + App.schemaName);
                insertStmt.setInt(1, this.transactionId);
                insertStmt.setString(2, selectedPaymentMethod);
                insertStmt.setDouble(3, amountGiven);
                insertStmt.executeUpdate();
                updateStmt.setInt(1, this.transactionId);
                updateStmt.executeUpdate();
                connection.commit();
                JOptionPane.showMessageDialog(this,
                    "Payment successful! Change is " + changeLabelValue.getText(),
                    "Success",
                    JOptionPane.INFORMATION_MESSAGE);
                PanelManager.updateCurrentPanel(new CashierPanel());
            } catch (SQLException e) {
                connection.rollback();
                JOptionPane.showMessageDialog(this,
                    "Database error during payment processing:\n" + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this,
                "Failed to connect to the database:\n" + e.getMessage(),
                "Connection Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }

    // =========================================================================
    // ===== MAIN METHOD FOR FINAL VISUAL CHECK ================================
    // =========================================================================
    // public static void main(String[] args) {
    //     JFrame testFrame = new JFrame("Payment Panel Final Visual Test");
    //     testFrame.setSize(1200, 675);
    //     testFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    //     testFrame.setLocationRelativeTo(null);
    //     testFrame.setResizable(false);
    //     testFrame.setLayout(new BorderLayout());
    //
    //     PaymentPanel testPanel = new PaymentPanel(999, 1250.50);
    //
    //     testFrame.add(testPanel, BorderLayout.CENTER);
    //     testFrame.setVisible(true);
    // }
}
