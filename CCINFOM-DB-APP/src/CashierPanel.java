import javax.swing.*;
import java.awt.*;
import java.sql.*;

public class CashierPanel extends BackgroundPanel {
    private boolean checkLogin(String username, String password) {
        String query = "SELECT * FROM users WHERE username = ? AND password = ?";

        try (Connection connection = Database.getConnection();
             Statement stmt1 = connection.createStatement();
             PreparedStatement stmt2 = connection.prepareStatement(query)) {
            stmt1.execute("USE " + App.schemaName);

            return true;
        } catch (SQLException f) {
            throw new RuntimeException(f);
        }

    }

    public CashierPanel() {
        super("assets/cashierPanel.png");
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


//        loginButton.addActionListener(e -> {
//
//        });

    }
}
