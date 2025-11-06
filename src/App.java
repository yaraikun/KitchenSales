import javax.swing.JFrame;

public class App {
    public static void main(String[] args) throws Exception {
        Theme defaultTheme = Theme.MONOCHROME;
        JFrame frame = defaultTheme.createFrame();
        LoginPanel loginPanel = new LoginPanel(defaultTheme);
        frame.add(loginPanel);
        frame.setResizable(false);
        frame.setVisible(true);
    }
}
