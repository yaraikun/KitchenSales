import java.awt.*;
import java.io.File;
import javax.swing.*;

public class Theme {
    private static final Color labelFontColor = new Color(244, 244, 244);
    public static Font fontStyle;

    public static void loadFont(String fontPath) {
        try {
            Font customFont = Font.createFont(Font.TRUETYPE_FONT, new File(fontPath)).deriveFont(16f);

            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            ge.registerFont(customFont);
            fontStyle = customFont;

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public static JFrame createFrame() {
        JFrame frame = new JFrame("CCINFOM DB Application");
        frame.setSize(1200, 675);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());
        frame.setLocationRelativeTo(null);
        return frame;
    }

    public static JButton createButton() {
        JButton button = new JButton();
        button.setOpaque(false);
        button.setContentAreaFilled(false);
        button.setBorderPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return button;
    }

    public static JLabel createLabel(String text) {
        JLabel label = new JLabel(text);
        label.setForeground(labelFontColor);
        label.setFont(fontStyle);
        label.setHorizontalAlignment(SwingConstants.CENTER);
        return label;
    }

    public static JTextField createTextField() {
        JTextField textField = new JTextField();
        textField.setOpaque(false);
        textField.setBorder(null);
        textField.setForeground(labelFontColor);
        textField.setFont(fontStyle);
        textField.setCaretColor(labelFontColor);
        return textField;
    }

}
