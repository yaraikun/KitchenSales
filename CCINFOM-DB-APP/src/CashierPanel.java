import javax.swing.*;
import java.awt.*;
import java.sql.*;

public class CashierPanel extends BackgroundPanel {
    private void openPaymentPanel(Theme theme) {
        /*
        PaymentPanel paymentPanel = new PaymentPanel(theme);
        PanelManager.updateCurrentPanel(paymentPanel);
        */
    }

    private void sendToKitchen() {
        JOptionPane.showMessageDialog(null,
                "Order sent to kitchen!",
                "Kitchen",
                JOptionPane.INFORMATION_MESSAGE);
    }

    private void voidOrder() {
        int confirm = JOptionPane.showConfirmDialog(
                null,
                "Are you sure you want to void/delete this order?",
                "Confirm Void/Delete",
                JOptionPane.YES_NO_OPTION
        );
        if (confirm == JOptionPane.YES_OPTION) {
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

    public CashierPanel(Theme theme) {
        super("CCINFOM-DB-APP/assets/cashierPanel.png");
        setLayout(null);

        JButton kitchenButton = new JButton();
        JButton discountButton = new JButton();
        JButton voidButton = new JButton();
        JButton payButton = new JButton();
        JButton[] buttons = { kitchenButton, discountButton, voidButton, payButton };

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

        kitchenButton.addActionListener(_ -> sendToKitchen());
        discountButton.addActionListener(_ -> applyDiscount());
        voidButton.addActionListener(_ -> voidOrder());
        payButton.addActionListener(_ -> openPaymentPanel(theme));
    }
}
