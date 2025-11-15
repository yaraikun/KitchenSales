import javax.swing.*;
import java.awt.*;
import java.sql.*;

public class AdminPanel extends BackgroundPanel {
    private void registerUser(String username, String password, String type) {
        String checkUser = "SELECT COUNT(*) FROM users WHERE username = ?";
        String query = "INSERT INTO users (username, password, type) VALUES (?, ?, ?)";

        try (Connection connection = Database.getConnection();
             Statement stmt = connection.createStatement();
             PreparedStatement checkStmt = connection.prepareStatement(checkUser)) {
                stmt.execute("USE " + App.schemaName);
                checkStmt.setString(1, username);

                try (ResultSet rs = checkStmt.executeQuery()) {
                    rs.next();
                    int count = rs.getInt(1);

                    if (count > 0) {
                        JOptionPane.showMessageDialog(null, "User already exists!");
                        return;
                    }
                }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }


        try (Connection connection = Database.getConnection();
             Statement stmt1 = connection.createStatement();
             PreparedStatement stmt2 = connection.prepareStatement(query)) {
            stmt1.execute("USE " + App.schemaName);
            stmt2.setString(1, username);
            stmt2.setString(2, password);
            stmt2.setString(3, type);
            int rows = stmt2.executeUpdate();

            if (rows > 0) {
                JOptionPane.showMessageDialog(null, "Successfully registered user!");
            } else {
                JOptionPane.showMessageDialog(null, "Registration not successful!");
            }
        } catch (SQLException f) {
            throw new RuntimeException(f);
        }

    }

    private void removeUser(String username, String password, String type) {
        String query = "DELETE FROM users WHERE username = ? AND password = ? AND type = ?";

        try (Connection connection = Database.getConnection();
             Statement stmt1 = connection.createStatement();
             PreparedStatement stmt2 = connection.prepareStatement(query)) {
            stmt1.execute("USE " + App.schemaName);
            stmt2.setString(1, username);
            stmt2.setString(2, password);
            stmt2.setString(3, type);
            int rows = stmt2.executeUpdate();

            if (rows > 0) {
                JOptionPane.showMessageDialog(null, "Successfully deleted user!");
            } else {
                JOptionPane.showMessageDialog(null, "User does not exist!");
            }
        } catch (SQLException f) {
            throw new RuntimeException(f);
        }

    }

    private void backToLogin(Theme theme) {
        LoginPanel loginPanel = new LoginPanel(theme);
        PanelManager.updateCurrentPanel(loginPanel);
    }

    public AdminPanel(Theme theme) {
        super("CCINFOM-DB-APP/assets/adminPanel.png");
        setLayout(null);

        JTextField usernameField = theme.createTextField();
        JTextField passwordField = theme.createTextField();
        usernameField.setBounds(475, 239, 270,46);
        passwordField.setBounds(475, 319, 270,46);
        add(usernameField);
        add(passwordField);
        String[] types = {"ADM", "MAN", "CAS"};
        JComboBox<String> typeDropDownBox = new JComboBox<>(types);
        typeDropDownBox.setBounds(542, 376, 100, 46);
        ((JLabel) typeDropDownBox.getRenderer()).setHorizontalAlignment(SwingConstants.CENTER);
        add(typeDropDownBox);

        JButton registerButton = theme.createButton();
        registerButton.addActionListener(_ -> registerUser(usernameField.getText(), passwordField.getText(), (String) typeDropDownBox.getSelectedItem()));
        registerButton.setBounds(180, 430, 364, 85);
        add(registerButton);

        JButton removeButton = theme.createButton();
        removeButton.addActionListener(_ -> removeUser(usernameField.getText(), passwordField.getText(), (String) typeDropDownBox.getSelectedItem()));
        removeButton.setBounds(640, 430, 364, 85);
        add(removeButton);

        JButton backButton = theme.createButton();
        backButton.addActionListener(_ -> backToLogin(theme));
        backButton.setBounds(360, 538, 462, 70);
        add(backButton);



    }
}
