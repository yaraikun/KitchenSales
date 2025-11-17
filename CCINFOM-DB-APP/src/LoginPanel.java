import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class LoginPanel extends BackgroundPanel {

    private String checkLogin(String username, String password) {
        // [OLD CONFLICTED CODE - IGNORED BY TEMP BUTTON]
        String query = "SELECT * FROM users WHERE username = ? AND password = ?";

        try (Connection connection = Database.getConnection();
             Statement stmt1 = connection.createStatement();
             PreparedStatement stmt2 = connection.prepareStatement(query)) {
            stmt1.execute("USE " + App.schemaName);
            stmt2.setString(1, username);
            stmt2.setString(2, password);
            try (ResultSet rs = stmt2.executeQuery()) {
                if (rs.next()) {
                    String type = rs.getString("type");
                    System.out.println("User type: " + type);
                    return type;
                } else {
                    JOptionPane.showMessageDialog(null, "User does not exist, wrong password/username!");
                    System.out.println("Invalid login.");
                    return null;
                }
            }
        } catch (SQLException f) {
            throw new RuntimeException(f);
        }
    }

    public LoginPanel(Theme theme) {
        super("CCINFOM-DB-APP/assets/loginPanel.png");
        setLayout(null);
        setBackground(theme.getBackgroundColor());

        JTextField usernameField = theme.createTextField();
        JTextField passwordField = theme.createTextField();
        usernameField.setBounds(805, 245, 265,46);
        passwordField.setBounds(805, 325, 265,46);
        add(usernameField);
        add(passwordField);

        // --- MAIN LOGIN BUTTON (For normal flow) ---
        JButton loginButton = theme.createButton();
        loginButton.addActionListener(e -> {
            String type = checkLogin(usernameField.getText(), passwordField.getText());
            if (type != null) {
                switch (type) {
                    case "CAS":
                        CashierPanel cashierPanel = new CashierPanel();
                        PanelManager.updateCurrentPanel(cashierPanel);
                        break;
                    case "MAN":
                        ReportsPanel reportsPanel = new ReportsPanel(usernameField.getText());
                        PanelManager.updateCurrentPanel(reportsPanel);
                        break;
                    case "ADM":
                        AdminPanel adminPanel = new AdminPanel(theme);
                        PanelManager.updateCurrentPanel(adminPanel);
                        break;
                }
                System.out.println("Successfully logged in!");
            } else {
                System.out.println("Oops");
            }
        });
        loginButton.setBounds(779, 420, 270, 70);
        loginButton.setOpaque(false);
        loginButton.setContentAreaFilled(false);
        loginButton.setBorderPainted(false);
        add(loginButton);

    }
}