import java.awt.Cursor;
import javax.swing.JButton;
import javax.swing.JTextField;

public class LoginPanel extends BackgroundPanel {
    public LoginPanel(Theme theme) {
        super("assets/loginPanel.png");
        setLayout(null);
        setBackground(theme.getBackgroundColor());

        JTextField usernameField = theme.createTextField();
        JTextField passwordField = theme.createTextField();
        usernameField.setBounds(805, 245, 265,46);
        passwordField.setBounds(805, 325, 265,46);
        add(usernameField);
        add(passwordField);

        JButton loginButton = new JButton();
        loginButton.setBounds(779, 420, 270, 70);   
        loginButton.setOpaque(false);
        loginButton.setContentAreaFilled(false);
        loginButton.setBorderPainted(false);
        loginButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        add(loginButton);
    }
}
