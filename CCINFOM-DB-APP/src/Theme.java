import java.awt.*;
import javax.swing.*;

public enum Theme {
    MONOCHROME(
        new Color(34, 37, 38),          // frame background
        new Color(224, 224, 224),       // label font color
        new Color(224, 224, 224),       // button font color
        new Font("JetBrains Mono", Font.PLAIN, 14), // font style
        new Color(26, 26, 26),          // label background
        new Color(53, 58, 62)        // button background
    );

    private final Color backgroundColor;
    private final Color labelFontColor;
    private final Color buttonFontColor;
    private final Font fontStyle;
    private final Color labelBackground;
    private final Color buttonBackground;

    public Color getBackgroundColor() {
        return backgroundColor;
    }

    public Color getLabelFontColor() {
        return labelFontColor;
    }

    public Color getButtonFontColor() {
        return buttonFontColor;
    }

    public Font getFontStyle() {
        return fontStyle;
    }

    public Color getLabelBackground() {
        return labelBackground;
    }

    public Color getButtonBackground() {
        return buttonBackground;
    }

    Theme(Color background, Color labelFontColor, Color buttonFontColor,
    Font fontStyle, Color labelBackground, Color buttonBackground) {
        this.backgroundColor = background;
        this.labelFontColor = labelFontColor;
        this.buttonFontColor = buttonFontColor;
        this.fontStyle = fontStyle;
        this.labelBackground = labelBackground;
        this.buttonBackground = buttonBackground;
    }

    public JFrame createFrame() {
        JFrame frame = new JFrame("CCINFOM DB Application");
        frame.setSize(1200, 675);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().setBackground(backgroundColor);
        frame.setLayout(new BorderLayout());
        frame.setLocationRelativeTo(null);
        return frame;
    }

    public JButton createButton(String text) {
        JButton button = new JButton(text);
        button.setBackground(buttonBackground);
        button.setForeground(buttonFontColor);
        button.setFont(fontStyle);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createLineBorder(labelBackground, 2));
        return button;
    }

    public JLabel createLabel(String text) {
        JLabel label = new JLabel(text);
        label.setForeground(labelFontColor);
        label.setFont(fontStyle);
        label.setHorizontalAlignment(SwingConstants.CENTER);
        return label;
    }

    public JTextField createTextField() {
        JTextField textField = new JTextField();
        textField.setOpaque(false);
        textField.setBorder(null);
        textField.setForeground(labelFontColor);
        textField.setFont(fontStyle);
        textField.setCaretColor(labelFontColor);
        return textField;
    }

}
