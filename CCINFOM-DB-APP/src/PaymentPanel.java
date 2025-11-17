import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DecimalFormat;

public class PaymentPanel extends BackgroundPanel {

    // Data passed from CashierPanel
    private final int transactionId;
    private final double totalCost;

    // UI Components
    private JTextField amountGivenField;
    private JLabel totalCostValue;
    private JLabel changeLabelValue;
    private JButton confirmButton;
    private JButton cancelButton;
    private JButton cashButton, cardButton, ewalletButton;

    // Logic
    private String selectedPaymentMethod = "Cash";

    // Formatting for currency
    private final DecimalFormat currencyFormat = new DecimalFormat("0.00");

    public PaymentPanel(int transactionId, double totalCost) {
        super("CCINFOM-DB-APP/assets/paymentPanel.png");
        this.transactionId = transactionId;
        this.totalCost = totalCost;
        setLayout(null);

        initComponents();
        addEventListeners();
        updatePaymentButtonStyles();
    }

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

        // --- PAYMENT TYPE BUTTONS ---
        cashButton = theme.createButton();
        cardButton = theme.createButton();
        ewalletButton = theme.createButton();
        cashButton.setBounds(710, 77, 116, 78);
        cardButton.setBounds(826, 77, 116, 78);
        ewalletButton.setBounds(941, 77, 116, 78);
        add(cashButton);
        add(cardButton);
        add(ewalletButton);

        // --- ACTION BUTTONS ---
        cancelButton = theme.createButton();
        cancelButton.setBounds(142, 469, 416, 104);
        add(cancelButton);

        confirmButton = theme.createButton();
        confirmButton.setBounds(639, 469, 416, 104);
        confirmButton.setEnabled(false);
        add(confirmButton);
    }

    private void addEventListeners() {
        amountGivenField.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) {
                calculateChange();
            }

            public void removeUpdate(DocumentEvent e) {
                calculateChange();
            }

            public void changedUpdate(DocumentEvent e) {
                calculateChange();
            }
        });

        cashButton.addActionListener(e -> selectPaymentMethod("Cash"));
        cardButton.addActionListener(e -> selectPaymentMethod("Card"));
        ewalletButton.addActionListener(e -> selectPaymentMethod("E-Wallet"));

        cancelButton.addActionListener(e -> PanelManager.updateCurrentPanel(new CashierPanel()));
        confirmButton.addActionListener(e -> processPayment());
    }

    private void selectPaymentMethod(String method) {
        selectedPaymentMethod = method;
        updatePaymentButtonStyles();

        if (method.equals("Cash")) {
            amountGivenField.setEnabled(true);
            amountGivenField.setText("0.00");
            amountGivenField.requestFocus();
        } else {
            amountGivenField.setEnabled(false);
            amountGivenField.setText(currencyFormat.format(totalCost));
        }
        calculateChange();
    }

    private void updatePaymentButtonStyles() {
        JButton[] buttons = { cashButton, cardButton, ewalletButton };
        for (JButton btn : buttons) {
            btn.setContentAreaFilled(false);
            btn.setBorderPainted(false);

            if (btn.getText().equals(selectedPaymentMethod)) {
                btn.setBorder(BorderFactory.createLineBorder(Color.CYAN, 3));
            }
        }
    }

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

    private void processPayment() {
        double amountGiven;
        try {
            amountGiven = Double.parseDouble(amountGivenField.getText());
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Invalid amount entered.", "Input Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String insertPaymentSQL = "INSERT INTO payments (transaction_id, payment_method, amount, payment_date) VALUES (?, ?, ?, NOW())";
        String updateTransactionSQL = "UPDATE pos_transactions SET status = 'Completed', payment_status = 'Paid' WHERE transaction_id = ?";

        try (Connection connection = Database.getConnection()) {
            connection.setAutoCommit(false);
            try (Statement useStmt = connection.createStatement();
                    PreparedStatement insertStmt = connection.prepareStatement(insertPaymentSQL);
                    PreparedStatement updateStmt = connection.prepareStatement(updateTransactionSQL)) {
                useStmt.execute("USE " + App.schemaName);

                insertStmt.setInt(1, this.transactionId);
                insertStmt.setString(2, selectedPaymentMethod);
                insertStmt.setDouble(3, amountGiven);
                insertStmt.executeUpdate();

                updateStmt.setInt(1, this.transactionId);
                updateStmt.executeUpdate();

                connection.commit();
                JOptionPane.showMessageDialog(this, "Payment successful! Change is " + changeLabelValue.getText(),
                        "Success", JOptionPane.INFORMATION_MESSAGE);
                PanelManager.updateCurrentPanel(new CashierPanel());
            } catch (SQLException e) {
                connection.rollback();
                JOptionPane.showMessageDialog(this, "Database error during payment processing:\n" + e.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Failed to connect to the database:\n" + e.getMessage(),
                    "Connection Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
